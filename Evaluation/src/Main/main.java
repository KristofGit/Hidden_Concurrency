package Main;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import BusinessObjects.OverlappingExecution;
import BusinessObjects.Unit;
import BusinessObjects.UnitExecution;
import Collections.RelatedExecutions;
import ConcurrencyAssessment.RiskAssessment;
import Evaluation.Evaluate;
import Evaluation.RandomPriorization;
import Evaluation.RandomTestGroup;
import Evaluation.RandomTestGroupGeneration;
import EvaluationNew.EvaluateNew;
import Faults.ArtificalFault;
import Faults.FaultGeneration;
import Filtering.SlidingWindow;
import Helper.Tuple;
import Parser.Activity;
import Parser.BProcess;
import Parser.DataExtraction;
import Parser.Execution;
import Prioritize.TestGroupProrization;
import Tests.TestCase;
import Tests.TestGroup;
import Tests.TestSelection;
import UnitGrouping.Groupings;
import UnitGrouping.UnitGroup;

public class main {

	public static void main(String[] args) {
		
		Tuple<List<Activity>,List<BProcess>> xesData = DataExtraction.analyzeXES();
		
		List<Activity> definedActivities = xesData.x;
		List<BProcess> definedProcesses = xesData.y;

	    Calendar cal = Calendar.getInstance();
	    cal.set(2010, 0, 1);
	    Date start = cal.getTime();
	    cal.set(Calendar.YEAR, 2015);
	    Date end = cal.getTime();
	    		
		//SlidingWindow.applyWindow(definedActivities, start, end);
		
		RelatedExecutions relatedExecutions = new RelatedExecutions();
		
		// used for test case construction
		Map<Activity, Unit> activityUnitMapping = new ConcurrentHashMap<>();
		
		for(Activity eachActivity : definedActivities)
		{
			String activityName = eachActivity.getName();
			Unit newUnit = relatedExecutions.addUnit("XES", activityName);

			activityUnitMapping.put(eachActivity, newUnit);
			
			for(Execution eachExection : eachActivity.getExecutions())
			{
				relatedExecutions.addExecution(
						newUnit, 
						eachExection.getStart(),
						eachExection.getEnd());
			}
		}
						
		Map<Unit, List<OverlappingExecution>> unitOverlapExecutionInformation = relatedExecutions.claculateOverlappings();
		Map<Unit, List<UnitExecution>> executions = relatedExecutions.getExecutions();
		
		RiskAssessment riskAssessment = new RiskAssessment();
		Map<Unit, Map<Unit, Double>> overlapRisk = riskAssessment.assesOverlappingRisk(unitOverlapExecutionInformation, executions);
	
	
		Groupings groupings = new Groupings();
		List<UnitGroup> concurrencyTestingGroups = groupings.createUnitGroups(overlapRisk);
		
		TestSelection testSelection = new TestSelection();
		List<TestCase> availableTestCases = testSelection.constructTestCases(activityUnitMapping, definedProcesses);
		List<TestGroup> construcedTestGroups = testSelection.constructTestGroups(availableTestCases, concurrencyTestingGroups);
		
		TestGroupProrization priorizationProposed = TestGroupProrization.of(construcedTestGroups);
 		//TestGroup nextTestGroupToExecute = priorization.nextTestGroupToExecute();
 		
		FaultGeneration faults = new FaultGeneration();
		List<ArtificalFault> artificialFaults = faults.generateAtificialFaults(overlapRisk);
		
		// for comparison with random approach
		RandomTestGroupGeneration testGroupGenRandom = new RandomTestGroupGeneration();
		//List<RandomTestGroup> randomTestGroups = testGroupGenRandom.generateRandomTestGroup(availableTestCases);
		//RandomPriorization priorizationRandomWithRandom = RandomPriorization.of(randomTestGroups);
		RandomPriorization priorizationRandomWithProposed = RandomPriorization.of(construcedTestGroups);

 		
 		
		Evaluate evaluation = new Evaluate();
		EvaluateNew evaluatioNew = new EvaluateNew();
 		
		//evaluatioNew.evaluateTestCaseGroupSelection(availableTestCases, artificialFaults);

		evaluation.determineAPFDForProposeApproach(priorizationProposed, artificialFaults);
		//evaluation.determineAPFDForRandomApproach(priorizationRandomWithProposed, artificialFaults);
	}

}

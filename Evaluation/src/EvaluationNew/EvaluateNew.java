package EvaluationNew;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.CipherOutputStream;

import BusinessObjects.Unit;
import Evaluation.RandomPriorization;
import Evaluation.RandomTestGroup;
import Evaluation.RandomTestGroupGeneration;
import Faults.ArtificalFault;
import Helper.MyFileWriter;
import Main.MainConfiguration;
import Tests.TestCase;
import Tests.TestGroup;

public class EvaluateNew {

	/* Compare the propose grouping/selection based approach with the classic load testing approach
	 * So compare random test case grouping with the presented approach
	 * Therefore determine how many test case groups must be generated with the RANDOM to determine test case groups that 
	 * cover the artificial faults 
	 */
	public void evaluateTestCaseGroupSelection(List<TestCase> availableTests, List<ArtificalFault>originalFaults)
	{
		System.out.println("Fault Count:"+originalFaults.size());
		
		MyFileWriter writer = new MyFileWriter(MainConfiguration.pathEvaluationSelectionRANDOMTestGroupGeneration);
		
		for(int i=0;i<MainConfiguration.amountOfRepeatingsOfEvaluationRANDOMTestGroupGeneration;i++)
		{
			List<ArtificalFault> faults = new ArrayList<>(originalFaults);
			
			RandomTestGroupGeneration generation = new RandomTestGroupGeneration();
							
			Set<RandomTestGroup> generatedTestCases  = new HashSet<>();
			
			do
			{
				 List<RandomTestGroup> generatedGroups = generation.generateRandomTestGroup(availableTests, 1);
			
				 int oldCount = generatedTestCases.size();
				 
				 generatedTestCases.addAll(generatedGroups);
				 
				 int newCount = generatedTestCases.size();
				 
				 if(oldCount != newCount)
				 {
					 filterFaultList(generatedGroups, faults);
				 }
				 
			}
			while(!faults.isEmpty());
			
			System.out.println(i + ") Needed test groups random:"+generatedTestCases.size());
			double dayExecutionTime = determineExecutionTimeInDays(generatedTestCases);
			System.out.println("Execution time:"+new BigDecimal(dayExecutionTime).toPlainString());
			System.out.println("TestGroupSize:"+generatedTestCases.iterator().next().getConcurrentlyTestedUnits().size());


			writer.writeNewLine(Integer.toString(generatedTestCases.size())+ ";"+new BigDecimal(dayExecutionTime).toPlainString());
		}
	}
	
	private double determineExecutionTimeInDays(Set<RandomTestGroup> randomTests)
	{
		double result = 0;
		
		for(RandomTestGroup eachGroup : randomTests)
		{
			result+=eachGroup.getTotalExecutionTimesInDays();
		}
		
		return result;
	}
	
	private void filterFaultList(List<RandomTestGroup> randomTests, List<ArtificalFault> faults)
	{		
		List<ArtificalFault> coveredFaults = new ArrayList<>();
		
		for(ArtificalFault eachFault : faults)
		{
			Unit funitOne = eachFault.getFaultUnitOne();
			Unit funitTwo = eachFault.getFaultUnitTwo();
						
			for(RandomTestGroup eachTestGroup : randomTests)
			{
				List<Unit>testedUnits = eachTestGroup.getConcurrentlyTestedUnits();
				
				if(testedUnits.contains(funitOne))
				{
					if(testedUnits.contains(funitTwo))
					{
						coveredFaults.add(eachFault);
						break;
					}
				}
			}
		}
		
		faults.removeAll(coveredFaults);		
	}
}

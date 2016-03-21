package Tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import BusinessObjects.Unit;
import Helper.Tuple;
import Parser.Activity;
import Parser.BProcess;
import UnitGrouping.UnitGroup;

public class TestSelection {

	/* Each process is converted into a test case
	 * The processes are extracted from the xes traces, each trace is an process instance
	 * Hence the same process can be contained multiple times in the list BUT the list here is already filtered for duplicates
	 * Hence, if the same process is in there then it has a different execution order and therefore also has to get its own test
	 * So simply each process list entry = 1 test (later tests can also be multiplied if necessary)
	 */
	
	public List<TestCase> constructTestCases(
			Map<Activity, Unit> activityUnitMapping,
			List<BProcess> processExecutions)
	{
		List<TestCase> constructedTest = new ArrayList<>();
		
		for(BProcess eachProcess : processExecutions)
		{
			List<Unit> coveredUnitsForNewTest = new ArrayList<>();
			long totalExecutionTimeOfTest = 0;
			Set<String> backgroundSystemIdentifier = new HashSet<>(); //use a set because then we automatically get a distinct list
			
			for(Activity eachActivity : eachProcess.getActivities())
			{
				Unit unitForActivity = activityUnitMapping.get(eachActivity);
				coveredUnitsForNewTest.add(unitForActivity);
				
				totalExecutionTimeOfTest+=eachActivity.getAverageExecutionTime();
				backgroundSystemIdentifier.add(eachActivity.getBackendSystem());
			}			
			
			if(!coveredUnitsForNewTest.isEmpty())
			{
				constructedTest.add(TestCase.of(
						coveredUnitsForNewTest, 
						totalExecutionTimeOfTest,
						backgroundSystemIdentifier));
			}
		}
		
		return constructedTest;
	}
	
	//availableTests tests cases we know of
	//concurrencyTestingGroups groups of units that have a high concurrent execution risk 
	//test cases will be selected that test the units contains in a unit group
	//one test case for one unit (probably the same test must be contained multiple times!)
	//why? Because each test will become its own instance, wait until it arrives at the correct spot (righ before executing the concurrent risk unit)
	//and then continue -> so one test one unit! (i.e. same test multiple times but allways different unit!)
	public List<TestGroup> constructTestGroups(
			List<TestCase> availableTests,
			List<UnitGroup> concurrencyTestingGroups)
	{
		List<TestGroup> testGroups = new ArrayList<>();
		
		for(UnitGroup eachConcurGroup : concurrencyTestingGroups)
		{
			List<Unit> unitsCoveredByCurGroup = eachConcurGroup.getAllSubGroupElements();
			
			List<Tuple<TestCase, Unit>> testsForCurGroup = new ArrayList<>();
			
			//each unit must be covered by a test case
			for(Unit eachUnitInGroup : unitsCoveredByCurGroup)
			{
				//check which test case covers the unit, if I found one proceed with next unit
				for(TestCase eachAvailableTest  : availableTests)
				{
					if(eachAvailableTest.coversUnit(eachUnitInGroup))
					{
						testsForCurGroup.add(Tuple.of(eachAvailableTest, eachUnitInGroup));
						break;
					}
				}	
			}			
			
			testGroups.add(TestGroup.of(testsForCurGroup, eachConcurGroup.getMaxHiddenConcurrencyRiskOfUnits()));
		}
		
		return testGroups;
	}
	
}

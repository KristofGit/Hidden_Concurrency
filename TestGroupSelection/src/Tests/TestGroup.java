package Tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import BusinessObjects.Unit;
import Helper.SimpleObjectEquality;
import Helper.Tuple;

public class TestGroup extends SimpleObjectEquality {

	// holds the test cases and the unit which is the reason why the test case it included (because it covers this unit)
	private final List<Tuple<TestCase, Unit>> testUnitMatching = new ArrayList<>();
	// holds the sum of all hidden concurrency  
	private final double summedUpHiddenConcurencyRisk;
	
	/*private TestGroup(Tuple<TestCase, Unit> testUnitMatch)
	{
		testUnitMatching.add(testUnitMatch);
	}*/
	
	private TestGroup(
			List<Tuple<TestCase, Unit>> testUnitMatch,
			double summedUpHiddenConcurrentyRiskCoveredByThisTestGroup)
	{
		testUnitMatching.addAll(testUnitMatch);
		summedUpHiddenConcurencyRisk = summedUpHiddenConcurrentyRiskCoveredByThisTestGroup;
	}
	
	public void addAdditonalTestUnitMatch(Tuple<TestCase, Unit> testUnitMatch)
	{
		this.testUnitMatching.add(testUnitMatch);
	}
	
	/*public static TestGroup of(Tuple<TestCase, Unit> testUnitMatch)
	{
		return new TestGroup(testUnitMatch);
	}*/
	
	public static TestGroup of(
			List<Tuple<TestCase, Unit>> testUnitMatch,
			double summedUpHiddenConcurrentyRiskCoveredByThisTestGroup)
	{
		return new TestGroup(testUnitMatch, summedUpHiddenConcurrentyRiskCoveredByThisTestGroup);
	}
	
	public int getTestcaseCount()
	{
		return testUnitMatching.size();
	}
	
	// all units executed somewhere at a test case (not necessarily concurrently)
	public List<Unit> getCoveredUnits()
	{
		Set<Unit> allCoveredUnits = new HashSet<>();
		
		for(Tuple<TestCase, Unit> eachTestUnit : testUnitMatching)
		{
			allCoveredUnits.addAll(eachTestUnit.x.getUnitsCoveredByTest());
		}
		
		return Collections.unmodifiableList(new ArrayList<>(allCoveredUnits));
	}
	
	//returns the units that are executed concurrently to determine concurrency problems
	public List<Unit> getSpecificallyTestedConcurrencyUnits()
	{
		Set<Unit> allSpecificallyTestedUnits = new HashSet<>();
		
		for(Tuple<TestCase, Unit> eachTestUnit : testUnitMatching)
		{
			allSpecificallyTestedUnits.add(eachTestUnit.y);
		}
		
		return Collections.unmodifiableList(new ArrayList<>(allSpecificallyTestedUnits));
	}
	
	public int distinctCoveredBackendSystemsCount()
	{
		Set<String> allBackendSystems = new HashSet<>();
		
		for(Tuple<TestCase, Unit> eachTestUnit : testUnitMatching)
		{
			allBackendSystems.addAll(eachTestUnit.x.getDistinctCoveredBackendSystems());
		}
		
		return allBackendSystems.size();
	}

	//compares this test groups with other test groups and measures howw novel the tested combinations of this test group are
	//so how many of the combinations tested in this test group were not tested by the test groups present in this list
	public double getAdditionalHiddenConcurrencyCoverage(List<TestGroup> otherTestGroups)
	{
		double result = 0;
		
		List<Unit> unitsTestedConcurrently = getSpecificallyTestedConcurrencyUnits();
		
		for(TestGroup eachOtherTestGroup : otherTestGroups)
		{
			List<Unit> concurrTestUnits = eachOtherTestGroup.getSpecificallyTestedConcurrencyUnits();
			
		}	
		
		return result;
	}
	
	public int distinctTestCasesCount()
	{
		Set<TestCase> allTestCasesDistinct = new HashSet<>();
		
		for(Tuple<TestCase, Unit> eachTestUnit : testUnitMatching)
		{
			allTestCasesDistinct.add(eachTestUnit.x);
		}
		
		return allTestCasesDistinct.size();
	}
	
	public int amountOfUnitsCoveredMultipleTimes()
	{
		/*
		 * Take each unit and see if its already checked
		 * if not add it to the already checked list
		 * If it is then check if is in the already double checked set, if it is not then its double checked (at least double, probably more often)
		 */
		
		Set<Unit> unitsAlreadyChecked = new HashSet<>();
		Set<Unit> unitsAlreadyDoubleChecked = new HashSet<>();

		for(Tuple<TestCase, Unit> eachTestUnit : testUnitMatching)
		{
			for(Unit eachCoveredUnit : eachTestUnit.x.getUnitsCoveredByTest())
			{
				if(unitsAlreadyChecked.contains(eachCoveredUnit))
				{
					if(!unitsAlreadyDoubleChecked.contains(eachCoveredUnit))
					{
						unitsAlreadyDoubleChecked.add(eachCoveredUnit);
					}					
				}
				else
				{
					unitsAlreadyChecked.add(eachCoveredUnit);
				}
			}			
		}
		
		return unitsAlreadyDoubleChecked.size();			
	}
	
	public long getSequentialTestGroupExecutionTime()
	{
		long totalExecutionTime = 0;
		
		for(Tuple<TestCase, Unit> eachTestCase : testUnitMatching)
		{
			totalExecutionTime += eachTestCase.x.getTotalTestCaseExecutionTime();			
		}
		
		return totalExecutionTime;
	}

	// assumes a concurrent execution where the total test group execution time is the execution time of the test
	// case that takes the maximum execution time
	public long getConcurrentestGroupExecutionTime()
	{
		long totalExecutionTime = 0;
		
		for(Tuple<TestCase, Unit> eachTestCase : testUnitMatching)
		{
			totalExecutionTime = Math.max(totalExecutionTime, eachTestCase.x.getTotalTestCaseExecutionTime());			
		}
		
		return totalExecutionTime;
	}
	
	// summed up risk of all covered/tested concurrently executed units that a they expirience a concurrent execution
	public double getCoveredHiddenConcurencyRisk() {
		return summedUpHiddenConcurencyRisk;
	}
}

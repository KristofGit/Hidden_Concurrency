package Evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import BusinessObjects.Unit;
import Helper.RandomHelper;
import Helper.SimpleObjectEquality;
import Helper.Tuple;
import Tests.TestCase;

public class RandomTestGroup extends SimpleObjectEquality {

	/*The random test groups are generate just randomly from random combinations of test cases that could
	 * be executed concurrently. So we only need a list of TestCases!
	 */
	
	private final List<TestCase> testUnitMatching = new ArrayList<>();
	private final List<Unit> coveredConcurrentlyTestedUnits = new ArrayList<>();

	private RandomTestGroup(List<TestCase> testUnitMatching)
	{
		this.testUnitMatching.addAll(testUnitMatching);
		fillCoveredUnits();
	}
	
	//the test cases allways focus on the concurrent execution of specific units 
	//because only those concurrent executions can determine concurrency errors
	//hence here we will randomly choose which unit of each test case are executed concurrently
	private void fillCoveredUnits()
	{
		Random random = RandomHelper.getRandom();
		
		for(TestCase eachTestCase : testUnitMatching)
		{
			List<Unit> coveredUnits = eachTestCase.getUnitsCoveredByTest();
						
			int nextIndex = random.nextInt(coveredUnits.size());
			
			Unit unit = coveredUnits.get(nextIndex);
			
			coveredConcurrentlyTestedUnits.add(unit);
		}
	}
	
	public double getTotalExecutionTimesInDays()
	{
		double result = 0;
		
		for(TestCase eachTestCase : testUnitMatching)
		{
			result+=eachTestCase.getTotalTestCaseEexcutionTimeDays();
		}
		
		return result;
	}
	
	public List<Unit> getConcurrentlyTestedUnits()
	{
		return Collections.unmodifiableList(coveredConcurrentlyTestedUnits);
	}
	
	public static RandomTestGroup of (List<TestCase> testUnitMatching)
	{
		return new RandomTestGroup(testUnitMatching);
	}
}

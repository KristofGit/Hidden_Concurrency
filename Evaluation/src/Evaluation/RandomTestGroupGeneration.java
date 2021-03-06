package Evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Helper.RandomHelper;
import Main.MainConfiguration;
import Tests.TestCase;
import Tests.TestGroup;

public class RandomTestGroupGeneration {

	/* Create fully random test groups
	 * So for each unit create a test group that adds the same amount of levels/units which the ones generated by the none random approach
	 * For each unit choose the amount of elements to add to its test group fully random
	 */
	
	public List<RandomTestGroup> generateRandomTestGroup(List<TestCase> availableTests)
	{
		int amountOfTestGroupsToGenerate = (int) (availableTests.size() * MainConfiguration.amountMultiplierForRandomTestGroups);

		return generateRandomTestGroup(availableTests, amountOfTestGroupsToGenerate);
	}
	public List<RandomTestGroup> generateRandomTestGroup(List<TestCase> availableTests, int amountOfTestGroupsToGenerate)
	{
		List<RandomTestGroup> testGroups = new ArrayList<>();

		//http://stackoverflow.com/questions/515214/total-number-of-nodes-in-a-tree-data-structure
		//(N^(L+1)-1) / (N-1)  N = subnodes count and L = levels
		long N = MainConfiguration.maximumGroupDistribution;
		long L = MainConfiguration.maximumGroupLevels -1;
		long amountOfTestsPerTestGroup = (long) ((Math.pow(N, L+1.0)-1.0)/(N-1.0));

		
		for(int i=0;i<amountOfTestGroupsToGenerate;i++)
		{
			List<TestCase> testsForGroup = new ArrayList<>();
			while(testsForGroup.size()<amountOfTestsPerTestGroup)
			{
				int randomIndex = RandomHelper.getRandom().nextInt(availableTests.size());

				TestCase testCase = availableTests.get(randomIndex);
								
				if(!testGroups.contains(testCase))
				{
					testsForGroup.add(testCase);
				}			
			}
						
			testGroups.add(RandomTestGroup.of(testsForGroup));
		}
		
		return testGroups;
	}
}

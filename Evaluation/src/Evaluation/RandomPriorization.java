package Evaluation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Helper.RandomHelper;
import Tests.TestGroup;

public class RandomPriorization<T> {

	
	private List<T> availableTestGroups = new ArrayList<>();
	private List<T> alreadyUsedTestGroups = new ArrayList<>();

	private RandomPriorization(List<T> randomTestGroups)
	{
		this.availableTestGroups.addAll(randomTestGroups);
	}
	
	public static <T> RandomPriorization of(List<T> randomTestGroups)
	{
		return  new RandomPriorization(randomTestGroups);
	}
	
	// will return null if all tests groups were already returned
	public T nextTestGroupToExecute()
	{
		T result = null;
		
		if(!availableTestGroups.isEmpty())
		{
			int nextElementToReturnIndex = RandomHelper.getRandom().nextInt(availableTestGroups.size());
			
			result = availableTestGroups.get(nextElementToReturnIndex);
			
			availableTestGroups.remove(nextElementToReturnIndex);
			
			alreadyUsedTestGroups.add(result);
		}
				
		return result;
	}

	public boolean hasTestsLeft() {
		return !availableTestGroups.isEmpty();
	}

	public int notYetReturnedTestCasesCount() {
		return availableTestGroups.size();
	}
	
	public int alreadyReturnedTestCasesCount() {
		return alreadyUsedTestGroups.size();
	}
	
	public void resetPrioritization()
	{
		this.availableTestGroups.addAll(this.alreadyUsedTestGroups);
		alreadyUsedTestGroups.clear();
	}
}

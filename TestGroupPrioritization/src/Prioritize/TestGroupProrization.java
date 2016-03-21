package Prioritize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import BusinessObjects.Unit;
import Helper.Tuple;
import Tests.TestGroup;

public class TestGroupProrization {

	//Normalization Approach:
	//http://stats.stackexchange.com/questions/70801/how-to-normalize-data-to-0-1-range
	
	private final static int AmountOfPrimaryFactors = 3;
	private final static int AmountOfSecondaryFactors = 4;
	
	//holds initally all test cases and is reduced whenever a new test case is selected
	private final List<TestGroup> availableTestGroups = new ArrayList<>();
	
	//hold the already executed test cases in their reversed selection order
	//mainly used for statistics
	private final List<TestGroup> alreadyExecutedTestGroups = new ArrayList<>();
	
	private TestGroupProrization(List<TestGroup> availableTestGroups)
	{
		this.availableTestGroups.addAll(availableTestGroups);
	}
	
	public static TestGroupProrization of(List<TestGroup> availableTestGroups)
	{
		return new TestGroupProrization(availableTestGroups);
	}
	
	public boolean hasTestsLeft()
	{
		return !availableTestGroups.isEmpty();
	}
	
	public int notYetReturnedTestCasesCount()
	{
		return availableTestGroups.size();
	}

	public int alreadyReturnedTestCasesCount()
	{
		return alreadyExecutedTestGroups.size();
	}
	
	public synchronized TestGroup nextTestGroupToExecute()
	{
		List<Tuple<TestGroup, Double>> rankedTestGroups = rateTestGroups();
		
		Tuple<TestGroup, Double> highestRankedFoundSoFar = null;
		for(Tuple<TestGroup, Double> eachRangedGroup : rankedTestGroups)
		{
			if(highestRankedFoundSoFar == null)
			{
				highestRankedFoundSoFar = eachRangedGroup;
			}
			else if(highestRankedFoundSoFar.y<eachRangedGroup.y)
			{
				highestRankedFoundSoFar = eachRangedGroup;
			}
		}
		
		TestGroup result = null;
		if(highestRankedFoundSoFar != null)
		{
			availableTestGroups.remove(highestRankedFoundSoFar.x);
			alreadyExecutedTestGroups.add(highestRankedFoundSoFar.x);
			
			result = highestRankedFoundSoFar.x;
		}
		
		return result;
	}
	
	//ranking must be based on higher = better
	private List<Tuple<TestGroup, Double>> rateTestGroups()
	{
		List<Tuple<TestGroup, Double>> rankedTestGroups = new ArrayList<>();
		
		//Primär
		List<Double> hiddenConcurrencyRisk = hiddenConcurrencyRisk();
		List<Double> rankExecutionTime = getTestGroupExecutionTime();
		List<Double> rankTestDiversity = getTestDiversity();
		
		//Sekundär
		List<Double> rankAmountOfTestCases = amountOfTestCases();
		List<Double> rankCoveredMultipleTime = amountOfActivitiesCoveredMultipleTimes();
		List<Double> rankCoveredBackendSystems = amountOfCoveredDifferentBackendSystems();
		List<Double> rankAdditionalCoverageGained = additionalCoverageGained();
		
		double weightPrimary = 1.0/AmountOfPrimaryFactors;
		double weightSecundary = 1.0/AmountOfSecondaryFactors;
		
		for(int i=0;i<availableTestGroups.size();i++)
		{
			TestGroup currentTestGroup = availableTestGroups.get(i);
			
			double rankinghiddenConcurrencyRisk = hiddenConcurrencyRisk.get(i);
			double rankingExecutionTime = rankExecutionTime.get(i);
			double rankingTestDiversity = rankTestDiversity.get(i);
			double rankingCoveredBackendSystems = rankCoveredBackendSystems.get(i);
			double rankingAdditionalCoverageGained = rankAdditionalCoverageGained.get(i);

			double finalPrimary = (weightPrimary*rankingExecutionTime)+
					(weightPrimary*rankingTestDiversity)+
					(weightPrimary*rankinghiddenConcurrencyRisk);
		
			double rankingAmountOfTestCases = rankAmountOfTestCases.get(i);
			double rankingCoveredMultipleTime = rankCoveredMultipleTime.get(i);

			double finalSekondary= (weightSecundary*rankingAmountOfTestCases)+
					(weightSecundary*rankingCoveredBackendSystems)+
					(weightSecundary*rankingAdditionalCoverageGained)+
					(weightSecundary*rankingCoveredMultipleTime)/2;
			
			// used to generate the APFD solely based on the "coverage gained" factor
			/*finalPrimary = 0.0;
			finalSekondary = 0.0;
			finalPrimary = rankingAdditionalCoverageGained;
			*/
			
			double finalRanking = finalPrimary+finalSekondary;
			
			finalRanking = rankingAdditionalCoverageGained;
			
			rankedTestGroups.add(new Tuple<TestGroup, Double>(currentTestGroup, finalRanking));
		}
		
		return rankedTestGroups;
	}
	
	/*
	 * Each method below determined one specific factor
	 * Sometimes a lower value is better then a higher one and sometimes
	 * its allways the other way around
	 * Nevertheless its allwasy normalized to an value 0 to 1 and so that a higher value indicates a better
	 * result then a lower value
	 */
	
	//higher is better
	private List<Double>  hiddenConcurrencyRisk()
	{
		return generateRankings(new RankableValue() {
			
			@Override
			public double getValue(TestGroup group) {
				return group.getCoveredHiddenConcurencyRisk();
			}
		}, false);
	}
	
	//lower is better, nevertheless 1 is the best value
	private List<Double>  getTestGroupExecutionTime()
	{
		/* 1) determine the test group that takes the longest time to executed
		 * 2) determine the test group that takes the least time to executed
		 * 3) Use those values to dermine the raking (longest = 1, least = 0, everyhting between is between 0 and 1)
		 */
		
		return generateRankings(new RankableValue() {
			
			@Override
			public double getValue(TestGroup group) {
				return group.getSequentialTestGroupExecutionTime();
			}
		}, true);
	}
	
	//higher is better
	//check for the test groups if the concurrency combinations that they are testing were probably already
	//tested by the already returned test groups
	private List<Double>  getTotalDiversity()
	{
		/* Min/max check for unique test case count for each testgroup
		 * Then normalize the independent values
		 */
		
		return generateRankings(new RankableValue() {
			
			@Override
			public double getValue(TestGroup group) {
				return group.distinctTestCasesCount();
			}
		}, false);
	}
	
	//0-1 should indicate if e.g. each contained test case is only contained once (1) or each unit is tested by the 
	//same test case (0)
	private List<Double>  getTestDiversity()
	{
		/* Min/max check for unique test case count for each testgroup
		 * Then normalize the independent values
		 */
		
		return generateRankings(new RankableValue() {
			
			@Override
			public double getValue(TestGroup group) {
				return group.distinctTestCasesCount();
			}
		}, false);
	}
	
	// higher is better
	private List<Double> amountOfCoveredDifferentBackendSystems()
	{
		return generateRankings(new RankableValue() {
			
			@Override
			public double getValue(TestGroup group) {
				return group.distinctCoveredBackendSystemsCount();
			}
		}, false);
	}
	
	// higher is better
	/* We use a quite basic approach here more oriented on classic coverage techniques then 
	 * the one proposed in the genetic selection paper.
	 * Hence, simply count how many new, not yet covered by selected testgroups, units are covered by each not 
	 * yet selected group.
	 */ 
	private List<Double> additionalCoverageGained()
	{
		final Set<Unit> alreadyCoveredUnits = new HashSet<>();
		
		for(TestGroup eachAlreadyExecutedTestGroup : alreadyExecutedTestGroups)
		{
			alreadyCoveredUnits.addAll(eachAlreadyExecutedTestGroup.getCoveredUnits());
		}
		
		return generateRankings(new RankableValue() {
			
			@Override
			public double getValue(TestGroup group) {
				long notYetCoveredUnits = 0;
				
				List<Unit> currentCoveredUnits = group.getCoveredUnits();
									
				for(Unit eachUnitOfTestGroup : currentCoveredUnits)
				{
					if(!alreadyCoveredUnits.contains(eachUnitOfTestGroup))
					{
						notYetCoveredUnits++;
					}
				}
				
				return notYetCoveredUnits;
			}
		}, false);
	}
	
	// lower is better
	// normalerweise immer gleich groß wenn die Gruppen gleich groß sind (1 Unit in Gruppe = 1 Test Case)
	// schwanken aber die Gruppengrößen ändert sich auch diese zahl
	//kann passieren durch die minRiskPerGroupExtension Konfigurationswert
	private List<Double> amountOfTestCases()
	{
		return generateRankings(new RankableValue() {
			
			@Override
			public double getValue(TestGroup group) {
				return group.getTestcaseCount();
			}
		}, true);
	}
	
	// higher is better
	private List<Double> amountOfActivitiesCoveredMultipleTimes()
	{
		return generateRankings(new RankableValue() {
			
			@Override
			public double getValue(TestGroup group) {
				return group.amountOfUnitsCoveredMultipleTimes();
			}
		}, false);
	}
	
	private List<Double> generateRankings(RankableValue accessHelper, boolean reversed)
	{
		double minTotalValue = Double.MAX_VALUE, maxTotalValue = Double.MIN_VALUE;
		
		for(TestGroup eachTestGroup :availableTestGroups)
		{
			double currentValue = accessHelper.getValue(eachTestGroup);
			
			if(minTotalValue>currentValue)
			{
				minTotalValue = currentValue;
			}
			
			if(maxTotalValue<currentValue)
			{
				maxTotalValue = currentValue;
			}
		}
		
		List<Double> ranks = new ArrayList<>();
		
		for(TestGroup eachTestGroup :availableTestGroups)
		{
			double currentValue = accessHelper.getValue(eachTestGroup);
			
			double rank = normalize(currentValue, maxTotalValue, minTotalValue, reversed);
			
			ranks.add(rank);
		}
			
		return ranks;
	}
	
	private double normalize(double value, double max, double min, boolean reverse)
	{
		double finalValue = ((value - min)/(max - (double)min));
		
		if(reverse)
		{
			finalValue = 1.0 - finalValue;
		}
		
		return finalValue;		
	}
	
	public List<TestGroup> getAlreadyExecutedTestGroups() {
		return alreadyExecutedTestGroups;
	}

	public List<TestGroup> getAvailableTestGroups() {
		return availableTestGroups;
	}

	private static abstract class RankableValue
	{
		public abstract double getValue(TestGroup group);
	}
}

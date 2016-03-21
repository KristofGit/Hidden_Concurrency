package Faults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import BusinessObjects.Unit;
import Helper.Tuple;
import Main.MainConfiguration;

public class FaultGeneration {

	//overlapRisk holds for each unit A a list of all other units and the likelyhood that this unit is executed concurrently with A
	//the units with the highest average concurrency risk are chosen to get assigned aritificial faults
	public List<ArtificalFault> generateAtificialFaults(Map<Unit, Map<Unit, Double>> overlapRisk)
	{		
		/* A fault allways is connected to concurrent executions
		 * A concurrent execution must AT LEAST contain two units (or two times the same unit)
		 * else we would not get a concurrent execution.
		 * Hence, create the faults based on unit execution combinations that likely result in concurrent executions
		 * 
		 * Therefore: 
		 * 1)Extract for each unit the other unit that has the highest concurrent execution risk factor
		 * 2)Order all unit combinations based on this risk factor
		 * 3)Take the ones with the highest risk factor
		 */

		
		//Adding up all probabilities the correct way is too complex, so simply order based on a simpler approach
		//Sum all propabilities of potential concurrent executions and the divide throught the amount of units that 
		//create these summed up potential concurrent executions
		
		//a unit and its summed up probability used to select the most risk ones
		/*List<Tuple<Unit, Double>> totalUnitRiskConcurrProbability = new ArrayList<>();
		
		for(Entry<Unit, Map<Unit, Double>> eachOverlappingRisk : overlapRisk.entrySet())
		{
			double relativeRisk = 0;
			
			if(!eachOverlappingRisk.getValue().isEmpty())
			{
				double summedRisk = 0;
				for(double value : eachOverlappingRisk.getValue().values())
				{
					summedRisk+=value;
				}
				
				relativeRisk = summedRisk/eachOverlappingRisk.getValue().size();
			}
			
			totalUnitRiskConcurrProbability.add(Tuple.of(eachOverlappingRisk.getKey(), relativeRisk));
		}
		
		Collections.sort(totalUnitRiskConcurrProbability, new Comparator<Tuple<Unit, Double>>() {
	        @Override
	        public int compare(Tuple<Unit, Double>  one, Tuple<Unit, Double>  two)
	        {
	            return  one.y.compareTo(two.y);
	        }
	    });
		
		int amountOfFaultsToGenerate = (int) (overlapRisk.size()*MainConfiguration.artificalFaultyUnits);*/
		
		List<Tuple<Tuple<Unit, Unit>, Double>> maxUnitRiskConcurrProbability = new ArrayList<>();
		
		
		for(Entry<Unit, Map<Unit, Double>> eachOverlappingRisk : overlapRisk.entrySet())
		{
			Unit currentUnit = eachOverlappingRisk.getKey();
			
			if(!eachOverlappingRisk.getValue().isEmpty())
			{
				List<Entry<Unit,Double>> listOfConcurrentExecutions = new ArrayList<>(eachOverlappingRisk.getValue().entrySet());

				Entry<Unit,Double> entryWithMaxConcurrency = Collections.max(listOfConcurrentExecutions, new Comparator<Entry<Unit,Double>>() {
			        @Override
			        public int compare(Entry<Unit,Double>  one, Entry<Unit,Double>  two)
			        {
			            return  one.getValue().compareTo(two.getValue());
			        }});
				
				// unit with max concurrency risk
				Unit secondUnit = entryWithMaxConcurrency.getKey();
				Double concurrencyRiskOfCombination = entryWithMaxConcurrency.getValue();
				
				maxUnitRiskConcurrProbability.add(Tuple.of(Tuple.of(currentUnit, secondUnit), concurrencyRiskOfCombination));
			}
		}
				
		Collections.sort(maxUnitRiskConcurrProbability, new Comparator<Tuple<Tuple<Unit, Unit>, Double>>() {
	        @Override
	        public int compare(Tuple<Tuple<Unit, Unit>, Double>  one, Tuple<Tuple<Unit, Unit>, Double>  two)
	        {
	            return  one.y.compareTo(two.y);
	        }
	    });
		
		int amountOfFaultsToGenerate = Math.max((int) (overlapRisk.size()*MainConfiguration.artificalFaultyUnits), MainConfiguration.minArtificalFaultCount);

		if(amountOfFaultsToGenerate > MainConfiguration.maxArtificalFaultCount)
		{
			amountOfFaultsToGenerate = MainConfiguration.maxArtificalFaultCount;
		}
		
		List<ArtificalFault> result = new ArrayList<>();
		
		for(int i=0;i<maxUnitRiskConcurrProbability.size() && i<amountOfFaultsToGenerate;i++)
		{
			Tuple<Unit, Unit> unitCombinationWithHighRisk = maxUnitRiskConcurrProbability.get(i).x;
			result.add(ArtificalFault.of(unitCombinationWithHighRisk.x, unitCombinationWithHighRisk.y));
		}		
		
		return result;
	}
	
	
}

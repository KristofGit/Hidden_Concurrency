package ConcurrencyAssessment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import BusinessObjects.OverlappingExecution;
import BusinessObjects.Unit;
import BusinessObjects.UnitExecution;
import Enumerations.Overlapping;
import Helper.Parallel;
import Main.MainConfiguration;

public class RiskAssessment {
	
	/*Ermitteln von jeder Unit zu jeder anderen Unit das 
	 *Risiko dass es zu einer parallelen Ausführung kommt 
	 *
	 * Verhältnis: Wie oft die beiden verglichenen ausgeführt werden 
	 * und wie oft davon es Overlappings gab und wie Stark diese waren (Complete,Start/End,...)
	 * Dauer: Wirklich notwendig? Wenn Aufgrund langer Laufzeiten Überlappungen enstehen
	 * werden diese dadurch ja nicht weniger Wahrscheinlich/Schlimm
	 * 
	 * Jeweils getrennt für die Richtung A -> B und dann B -> A erfassen! (z.B. A sehr wenig ausgeführt
	 * B aber sehr oft und überlappen immer) Dann hat A einen hohe Wahrscheinlichkeit gleichzeitig mit B
	 * ausgeführt zu werden. B aber nicht unbedingt.
	 */
	
	//overlapinfo = Eine Unit und welche Executions sich mit den Executions der Unit überlappen (kann sich auch mit sich selbst überlappen)
	//execution info, eine Unit und welche Executions diese hat
	public Map<Unit, Map<Unit, Double>> assesOverlappingRisk(Map<Unit, List<OverlappingExecution>> overlapInfo,
			Map<Unit, List<UnitExecution>> executionInfo)
	{		
		ConcurrentHashMap<Unit, Map<Unit, Double>> hiddenConcurrencyRiskFactors = new ConcurrentHashMap<>();
		
		for(final Entry<Unit,List<UnitExecution>> eachUnit : executionInfo.entrySet())
		{
			Unit unit = eachUnit.getKey();
			List<UnitExecution> executions = eachUnit.getValue();
			
			//wie oft wurde die Unit ausgeführt
			final int executionCount = executions.size();
			
			//gives me the executions that overlap with the specific unit of this loop
			List<OverlappingExecution> overlap =  overlapInfo.get(unit);
			
			// holds all overlappings of the the sepcific unit of this loop iteration
			// separated by the unit which it is overlapping to (can include itself)
			// So I know that my current unit overlaps with this units in this list and how often and how intense (overlappingmode)
			// allowing me to determine the overlapping risk from unit A with each other unit
			// mainly this filteres out NoOverlappingOccurrences
			ConcurrentHashMap<Unit, List<OverlappingExecution>> specificOverlapping = new ConcurrentHashMap<>();
			
			//filtering of no overlapping modes
			//separating each overlapping based on the unit that produced it
			for(OverlappingExecution eachOverlapping : overlap)
			{		
				if(eachOverlapping.getMode() != Overlapping.NoOverlapping)
				{
					Unit overlappingUnit = eachOverlapping.getUnit();	

					List<OverlappingExecution> overlapingsList = specificOverlapping.get(overlappingUnit);
					
					if(overlapingsList == null)
					{
						overlapingsList = new ArrayList<>();
						specificOverlapping.put(overlappingUnit, overlapingsList);
					}
					
					overlapingsList.add(eachOverlapping);
				}
			}
			
			//calculate overlapping factor for each unit which the current list iteration unit has overlappings with
			//the factor depends currently only on the total amount of executions of the iteration unit
			//Checks, for example, if executions of this unit always overlap with executions of a different unit
			
			//the concurrentExecution risk factor is normalized to 1
			/*It uses the type of the concurrency
			 *The amount of concurrent Executions
			 *The relative amount of concurrent executions (all Executions to Concurrent Executions)
			 *Always for to Units A and B, to determine how likely A is executed and at the same time a execution of B occurs
			 */
			
			ConcurrentHashMap<Unit, Double> hiddenConcurrencyRiskFactor = new ConcurrentHashMap<>();

			// key and the unit in the overlapping execution must be equal
			// because this list holds the overlappings separated by their origin unit that produced the overlapping
			for(final Entry<Unit,List<OverlappingExecution>> eachOverlappingUnit : specificOverlapping.entrySet())
			{
				Unit otherUnitImOverlappingWith = eachOverlappingUnit.getKey();
				
				long totalExecutions = executionCount;
				long concurrentExecutions = eachOverlappingUnit.getValue().size();
				
				long amountOfNearlyConcurrentExecutions = countExecutionTypes(eachOverlappingUnit.getValue(), Overlapping.AlmostOverlapping);
				long amountOfCompletelyConcurrentExecutions = countExecutionTypes(eachOverlappingUnit.getValue(), Overlapping.CompleteOverlapping);
				long amountOfPartlyConcurrentExecutions = countExecutionTypes(eachOverlappingUnit.getValue(), Overlapping.StartOrEndOverlapping);

				double concurrentciesFoundFactor = concurrentExecutions/(double)totalExecutions;
				double completeConcurrFactor = amountOfCompletelyConcurrentExecutions*MainConfiguration.valueCompleteOverlap;
				double nearlyConcurrentFactor = amountOfNearlyConcurrentExecutions*MainConfiguration.valueNearlyOverlap;
				double partlyConcurrentFactor = amountOfPartlyConcurrentExecutions*MainConfiguration.valuePartlyOverlap;
				
				double sumConcurrFactor = completeConcurrFactor+nearlyConcurrentFactor+partlyConcurrentFactor;
				
				double realtiveConcurrFactor = sumConcurrFactor/concurrentExecutions;
				double finalConcurrFactor = realtiveConcurrFactor*concurrentciesFoundFactor;
			
				hiddenConcurrencyRiskFactor.put(otherUnitImOverlappingWith, finalConcurrFactor);
			}
			
			hiddenConcurrencyRiskFactors.put(eachUnit.getKey(), hiddenConcurrencyRiskFactor);				
		}
		
		//search for the unit pair that has the highest concurrency risk to use this information
		//to construct a rough relative concurrency risk rating 
		
		double maximumConcurrencyRisk = Double.MIN_VALUE;
		for(final Entry<Unit, Map<Unit, Double>> eachUnit : hiddenConcurrencyRiskFactors.entrySet())
		{
			if(!eachUnit.getValue().isEmpty())
			{
				double currentIterationMax = Collections.max(eachUnit.getValue().values());
				
				if(currentIterationMax > maximumConcurrencyRisk)
				{
					maximumConcurrencyRisk = currentIterationMax;
				}	
			}
		}
		
		double averageSum = 0;
		for(final Entry<Unit, Map<Unit, Double>> eachUnit : hiddenConcurrencyRiskFactors.entrySet())
		{
			double currentTotal = 0;
			for(Double value : eachUnit.getValue().values())
			{
				currentTotal+=value;
			}
			
			if(!eachUnit.getValue().isEmpty())
			{
				averageSum += currentTotal/eachUnit.getValue().size();
			}
		}
		
		System.out.println("Maximum identified concurrency risk is:"+maximumConcurrencyRisk);
		System.out.println("Average identified concurrency risk is:"+averageSum/hiddenConcurrencyRiskFactors.size());

		return hiddenConcurrencyRiskFactors;
	}

	private long countExecutionTypes(List<OverlappingExecution> overlappings, Overlapping countFor)
	{
		long result = 0;
		
		for(OverlappingExecution eachOverLapping : overlappings)
		{
			if(eachOverLapping.getMode()==countFor)
			{
				result++;
			}
		}
		
		return result;
	}
}

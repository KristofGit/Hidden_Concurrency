package Collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import BusinessObjects.OverlappingExecution;
import BusinessObjects.Unit;
import BusinessObjects.UnitExecution;
import Enumerations.Overlapping;
import Helper.ListHelper;
import Main.MainConfiguration;

public class RelatedExecutions {
	
	/* A unit has executions, and these executions can overlap with other executions */
	
	//welche executions hat die Unit
	private final ConcurrentHashMap<Unit, List<UnitExecution>> executions = new ConcurrentHashMap<>();
	//welche anderen executions überlappen sich mit einer Execution
	private final ConcurrentHashMap<UnitExecution, List<OverlappingExecution>> overlappings= new ConcurrentHashMap<>();
	
	public Map<Unit, List<UnitExecution>> getExecutions()
	{
		return Collections.unmodifiableMap(executions);
	}
	
	//for each unit it contains the list of executions that overlapp with executions of the unit
	public Map<Unit, List<OverlappingExecution>> claculateOverlappings()
	{
		fillOverlappingExecutions();
		printStatistics();
		
		ConcurrentHashMap<Unit, List<OverlappingExecution>> calcedOverlap= new ConcurrentHashMap<>();
		
		for(final Entry<Unit,List<UnitExecution>> eachUnit : executions.entrySet())
		{
			List<OverlappingExecution> unitOverlap = new ArrayList<>();
					
			for(UnitExecution eachUnitExecution : eachUnit.getValue())
			{
				List<OverlappingExecution> identifiedOverlap = overlappings.get(eachUnitExecution);
				
				unitOverlap.addAll(identifiedOverlap);
			}
			
			calcedOverlap.put(eachUnit.getKey(), unitOverlap);
		}
		
		return calcedOverlap;
	}
	
	public Unit addUnit(String unitMainName, String unitSubName)
	{
		Unit result = null;
		
		if(unitMainName != null)
		{
			String name = unitMainName;
			
			if(unitSubName != null)
			{
				name += " - " + unitSubName;
			}
			
			result = Unit.of(name);
			
			executions.put(result, Collections.synchronizedList(new ArrayList<UnitExecution>()));
		}
		
		return result;
	}
	
	public void addExecution(Unit originUnit, Date start, Date end)
	{
		if(originUnit != null)
		{
			if(start != null && end != null)
			{
				List<UnitExecution> executionList = executions.get(originUnit);
		
				executionList.add(UnitExecution.of(start, end));
			}
		}
	}
	
	private void fillOverlappingExecutions()
	{
		long ignoredOwnOverlappings = 0;
		long ignoredNoOverlappings = 0;
		long extractedOverlappings = 0;

		overlappings.clear();
		
		for(final Entry<Unit,List<UnitExecution>> eachUnit : executions.entrySet())
		{
			Unit originalUnit = eachUnit.getKey();
			
			for(final UnitExecution eachExecution :  eachUnit.getValue())
			{
				//again iterate through each unit (including the one were the execution comes from) and
				//search for overlappings except the one execution that is equal to the one we are searching
				//overlappings for
				//=> An activity execution can overlap executions of its own activity
				
				List<OverlappingExecution> overlappingExecutionsFound = new ArrayList<>();
				
				for(final Entry<Unit,List<UnitExecution>> eachUnitSecondIteration : executions.entrySet())
				{
					Unit originalUnitSecondIteration = eachUnitSecondIteration.getKey();
					
					for(final UnitExecution eachExecutionSecond :  eachUnitSecondIteration.getValue())
					{
						// ensures that a execution cant overlap itself because we again iterate through
						// all executions, which also includes the one that we search overlappings for
						if(eachExecution.equals(eachExecutionSecond))
						{
							continue;
						}
						
						Overlapping determinedOverlapping = determineOverlappingType(eachExecution, eachExecutionSecond);
						
						// is null if we compare the same elementes 
						if(determinedOverlapping != null)
						{ 
							if(!MainConfiguration.earlyFilterOutNoOverlappingsState ||
									(determinedOverlapping != Overlapping.NoOverlapping))
							{							
								if(MainConfiguration.ignoreOwnOverlappings)
								{
									if(!originalUnitSecondIteration.equals(originalUnit))
									{
										overlappingExecutionsFound.add(
												OverlappingExecution.of(
														determinedOverlapping,
														originalUnitSecondIteration,
														eachExecutionSecond));
										
										extractedOverlappings++;
									}
									else
									{
										ignoredOwnOverlappings++;
									}
								}
								else
								{
										overlappingExecutionsFound.add(
										OverlappingExecution.of(
												determinedOverlapping,
												originalUnitSecondIteration,
												eachExecutionSecond));		
										
										extractedOverlappings++;
								}
							}	
							else
							{
								ignoredNoOverlappings++;
							}
						}						
					}
				}
				
				overlappings.put(eachExecution, overlappingExecutionsFound);				
			}
		}

		System.out.println("Ignored Own Overlappings:"+ignoredOwnOverlappings);
		System.out.println("Ignored No Overlappings:"+ignoredNoOverlappings);
		System.out.println("Detected Overlappings:"+extractedOverlappings);

	}
	
	// return null if no overlapping can be identified because both unit executions are equal
	private Overlapping determineOverlappingType(UnitExecution originalExecution, UnitExecution otherExecution)
	{
		Overlapping result = null;
		
		if(originalExecution != null && otherExecution != null)
		{
			if(!originalExecution.equals(otherExecution))
			{
				Date startOriginal = originalExecution.getStartDate();
				Date endOriginal = originalExecution.getEndDate();
				
				Date startOther = otherExecution.getStartDate();
				Date endOther = otherExecution.getEndDate();
				
				result = Overlapping.checkOverlap(startOriginal, endOriginal, startOther, endOther);
			}
		}
		
		return result;
	}
	
	public void printStatistics()
	{
		int amountOfUnits = executions.size();
		int minAmountOfExecitions = Integer.MAX_VALUE, maxAmountOfExecutions = Integer.MIN_VALUE;
		long totalAmountOfExecutions = 0; double averageAmountOfExecutions;

		for(List<UnitExecution> eachExection : executions.values())
		{
			if(eachExection.size()<minAmountOfExecitions)
			{
				minAmountOfExecitions = eachExection.size();
			}
		
			if(eachExection.size()>maxAmountOfExecutions)
			{
				maxAmountOfExecutions = eachExection.size();
			}
			
			totalAmountOfExecutions += eachExection.size();
		}
		
		averageAmountOfExecutions = ((double)totalAmountOfExecutions)/amountOfUnits;
		
		long amountOfExecutionDetails = 0, amountOfContainedExecutions=0, amountOfNearlyOverlappingExecutions=0, amountOfStartEndOverlappings=0, amountNoOverlapping=0;
		double averageContainedExecutions, averageNearlyOverlappingExecutions, averageStartEndOverlapping, averageNoOverlapping;
		
		for(List<OverlappingExecution> eachOverlappings : overlappings.values())
		{
			for(OverlappingExecution eachOverlapping : eachOverlappings)
			{
				switch(eachOverlapping.getMode())
				{
				case AlmostOverlapping:
					amountOfNearlyOverlappingExecutions++;
					break;
				case CompleteOverlapping:
					amountOfContainedExecutions++;
					break;
				case NoOverlapping:
					amountNoOverlapping++;
					break;
				case StartOrEndOverlapping:
					amountOfStartEndOverlappings++;
					break;
				}
				
			}
			
			amountOfExecutionDetails+=eachOverlappings.size();
		}
		
		averageContainedExecutions=amountOfContainedExecutions/(double)totalAmountOfExecutions;
		averageNearlyOverlappingExecutions=amountOfNearlyOverlappingExecutions/(double)totalAmountOfExecutions;
		averageStartEndOverlapping=amountOfStartEndOverlappings/(double)totalAmountOfExecutions;
		averageNoOverlapping=amountNoOverlapping/(double)totalAmountOfExecutions;

		System.out.println("Amount of units:" + amountOfUnits);
		System.out.println("Min amount of Executions:" + minAmountOfExecitions);
		System.out.println("Max amount of Executions:" + maxAmountOfExecutions);
		System.out.println("Total amount of Executions:" + totalAmountOfExecutions);
		System.out.println("Average amount of Executions:" + averageAmountOfExecutions);

		System.out.println("Amount of execution details:" + amountOfExecutionDetails);
		System.out.println("Amount of contained executions:" + amountOfContainedExecutions);
		System.out.println("Amount of nearly overlapping executions:" + amountOfNearlyOverlappingExecutions);
		System.out.println("Amount of start/end overlapping executions:" + amountOfStartEndOverlappings);
		System.out.println("Amount of no overlapping:" + amountNoOverlapping);
		System.out.println("Average contained executions:" + averageContainedExecutions);
		System.out.println("Average nearly overlapping executions:" + averageNearlyOverlappingExecutions);
		System.out.println("Average start/end overlapping:" + averageStartEndOverlapping);
		System.out.println("Average no overlapping:" + averageNoOverlapping);
	}	
}


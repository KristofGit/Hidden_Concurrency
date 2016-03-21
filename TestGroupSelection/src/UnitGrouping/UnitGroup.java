package UnitGrouping;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import BusinessObjects.Unit;

public class UnitGroup {

	 //a unit which is part of the group
	private final Unit unit;
	//a list of units that are connected to this unit because they occur frequently in a concurrent manner
	private final List<Entry<UnitGroup, Double>> hiddenConcurrencyElements = new ArrayList<>();
	
	private UnitGroup(Unit unit)
	{
		this.unit = unit;
	}
		
	public void addHiddenConcurrency(UnitGroup nextGroupElement, double concurrencyRisk)
	{
		this.hiddenConcurrencyElements.add(
				new AbstractMap.SimpleEntry<UnitGroup, Double>(
						nextGroupElement, concurrencyRisk));
	}
	
	// ocmbines all hidden concurrent elements covered by this group into a list (including the ones 
	// that are covered by sub groups on a different lower level)
	public List<Unit> getAllSubGroupElements()
	{
		List<Unit> result = new ArrayList<>();
		result.add(unit);
		
		for(Entry<UnitGroup, Double> eachConcurrentPartner : hiddenConcurrencyElements)
		{
			result.addAll(eachConcurrentPartner.getKey().getAllSubGroupElements());			
		}		
		
		return result;
	}
	
	public static UnitGroup of(Unit unit)
	{
		return new UnitGroup(unit);
	}

	public double getMaxHiddenConcurrencyRiskOfUnits()
	{
		double result = 0;
		
		//sum up my own level
		for(Entry<UnitGroup, Double> eachConcurrentPartner : hiddenConcurrencyElements)
		{
			result=Math.max(result, eachConcurrentPartner.getValue());
		}
		
		// get the hidden concurrency risk from sublevels
		for(Entry<UnitGroup, Double> eachConcurrentPartner : hiddenConcurrencyElements)
		{
			result=Math.max(result, eachConcurrentPartner.getKey().getMaxHiddenConcurrencyRiskOfUnits());
		}
				
		return result;
	}
		
	public double getSummedUpHiddenConcurrencyRiskOfUnits()
	{
		double result = 0;
		
		//sum up my own level
		for(Entry<UnitGroup, Double> eachConcurrentPartner : hiddenConcurrencyElements)
		{
			result+=eachConcurrentPartner.getValue();
		}
		
		// get the hidden concurrency risk from sublevels
		for(Entry<UnitGroup, Double> eachConcurrentPartner : hiddenConcurrencyElements)
		{
			result+=eachConcurrentPartner.getKey().getSummedUpHiddenConcurrencyRiskOfUnits();
		}
		
		return result;
	}
	
	public Unit getUnit() {
		return unit;
	}	
}

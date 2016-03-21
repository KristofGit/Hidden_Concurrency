package UnitGrouping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


import BusinessObjects.Unit;
import Main.MainConfiguration;

public class Groupings {

	/* Groups are created individually for each unit. 
	 * Therefore from each group we start up to fill a specific amount of levels.
	 * Each level is built by checking for the current unit which X most risky for concurrent execution 
	 * other units exist. Thoese X units are then connected.
	 * By repeaseing this steps with the just connected units "levels" are created that extended the group
	 */
	public List<UnitGroup> createUnitGroups(Map<Unit, Map<Unit, Double>> executionOverlapData)
	{
		List<UnitGroup> groups = new ArrayList<>();
		
		for(Entry<Unit, Map<Unit, Double>> eachOveralp : executionOverlapData.entrySet())
		{
			Unit groupStart = eachOveralp.getKey();
						
			//eachOveralp.getValue().entrySet()
			
			UnitGroup initalGroup = UnitGroup.of(groupStart);
			extendGroup(1, initalGroup, executionOverlapData);
			
			groups.add(initalGroup);
		}
		
		return groups;
	}
	
	private void extendGroup(
			long currentLevel,
			UnitGroup groupToExtend,
			Map<Unit, Map<Unit, Double>> executionOverlapData)
	{
		if(currentLevel<MainConfiguration.maximumGroupLevels)
		{
			Unit unitOfCurrentGroup = groupToExtend.getUnit();
			
			Map<Unit, Double> overlapingKnownForCurrentGoupUnit = executionOverlapData.get(unitOfCurrentGroup);
			
			List<Unit> unitsToExtendGroup = getMaxX(MainConfiguration.maximumGroupDistribution, overlapingKnownForCurrentGoupUnit);

			for(Unit eachUnitForExtension : unitsToExtendGroup)
			{
				UnitGroup groupForExtension = UnitGroup.of(eachUnitForExtension);
				
				double overlappingCurrencyRisk = overlapingKnownForCurrentGoupUnit.get(eachUnitForExtension);
				
				if(overlappingCurrencyRisk >= MainConfiguration.minRiskPerGroupExtension)
				{
					groupToExtend.addHiddenConcurrency(groupForExtension, overlappingCurrencyRisk);
					
					extendGroup(currentLevel+1, groupForExtension, executionOverlapData);
				}
			}
		}
	}
	
	/* TODO: Umbauen auf Sortieren der Liste? Dann schauen an die jeweilige Position? Und wenn mehrere gleiche gibt mit
	 * gleichen concurrency risk dann zufälig von denen eines auswählen? Diese zufälligkeit wäre vl. ganz nett
	 * 
	 */
	private List<Unit> getMaxX(long getBestX, Map<Unit, Double> values)
	{
		List<Unit> result = new ArrayList<>();
		
		double lastBestValueOverakenToResult = Double.MAX_VALUE;
		
		do
		{
			double bestValueFoundSoFar = Double.MIN_VALUE;
			Unit bestUnitFoundSoFar = null;
		
			// we have added all that is possible
			// so stop!
			if(result.size()==values.size())
			{
				break;
			}
			
			for(Entry<Unit, Double> eachUnit : values.entrySet())
			{
				// using equal signs to allow units with equal concurrency risk to be taken
				// as long as the unit itself is not yet present in the result set (we found that many units have a equal concurrency risk)
				if(bestValueFoundSoFar <= eachUnit.getValue() 
						&& lastBestValueOverakenToResult >= eachUnit.getValue()
						&& !result.contains(eachUnit.getKey()))
				{
					bestValueFoundSoFar = eachUnit.getValue();
					bestUnitFoundSoFar = eachUnit.getKey();
				}
			}			
			
			if(bestUnitFoundSoFar == null)
			{
				break;
			}
			
			result.add(bestUnitFoundSoFar);
			
			lastBestValueOverakenToResult = bestValueFoundSoFar;
		}
		while(result.size()<getBestX);
		
		return result;
	}
}

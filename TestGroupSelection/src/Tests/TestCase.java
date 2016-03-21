package Tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import BusinessObjects.Unit;

public class TestCase{

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unitsCoveredByTest == null) ? 0 : unitsCoveredByTest.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestCase other = (TestCase) obj;
		if (unitsCoveredByTest == null) {
			if (other.unitsCoveredByTest != null)
				return false;
		} else if (!unitsCoveredByTest.equals(other.unitsCoveredByTest))
			return false;
		return true;
	}

	private final List<Unit> unitsCoveredByTest = new ArrayList<>();
	private final Set<Unit> unitsCoveredHashSet = new HashSet<>(); //for quick checking if test covers a specific unit
	private final long totalTestCaseExecutionTime;
	private final Set<String> coveredBackendSystems = new HashSet<>();
	
	private TestCase(
			List<Unit> coveredUnits,
			long totalExecutionTime,
			Set<String> coveredBackendSystems)
	{
		unitsCoveredByTest.addAll(coveredUnits);
		unitsCoveredHashSet.addAll(coveredUnits);
		this.totalTestCaseExecutionTime = totalExecutionTime;
		this.coveredBackendSystems.addAll(coveredBackendSystems);
	}
	
	public Set<String> getDistinctCoveredBackendSystems()
	{
		return Collections.unmodifiableSet(coveredBackendSystems);
	}
	
	public boolean coversUnit(Unit unit)
	{
		return unitsCoveredHashSet.contains(unit);
	}
	
	public static TestCase of(
			List<Unit> coveredUnits,
			long totalExecutionTime,
			Set<String> coveredBackendSystems)
	{
		return new TestCase(
				coveredUnits,
				totalExecutionTime, 
				coveredBackendSystems);
	}

	public long getTotalTestCaseExecutionTime() {
		return totalTestCaseExecutionTime;
	}
	
	public double getTotalTestCaseEexcutionTimeDays()
	{
		return totalTestCaseExecutionTime/1000/60/24;
	}
	
	public int getCoveredBackendSystemsCount()
	{
		return coveredBackendSystems.size();
	}
	
	public List<Unit> getUnitsCoveredByTest()
	{
		return Collections.unmodifiableList(unitsCoveredByTest);
	}
	
	
	
}

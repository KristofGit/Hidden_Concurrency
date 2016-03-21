package BusinessObjects;

import Enumerations.Overlapping;

/*
 * Combined with an different unit execution this overlapping exeuciton class holds
 * one execution that is executed concurrently to a other execution
 */
public class OverlappingExecution {

	private final Overlapping mode;
	private final Unit unit; 
	private final UnitExecution execution; //this execution belongs to the unit variable
	
	private OverlappingExecution(Overlapping mode, Unit unit, UnitExecution execution)
	{
		this.mode = mode;
		this.unit = unit;
		this.execution = execution;
	}
	
	//mode = type of overlapping
	//unit that is producing the overlapping execution
	//execution of the unit that is production the overlapping execution
	public static OverlappingExecution of(Overlapping mode, Unit unit, UnitExecution execution)
	{
		return new OverlappingExecution(mode, unit, execution);
	}

	public Overlapping getMode() {
		return mode;
	}

	public Unit getUnit() {
		return unit;
	}

}

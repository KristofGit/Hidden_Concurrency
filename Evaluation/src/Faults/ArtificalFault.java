package Faults;

import BusinessObjects.Unit;
import Helper.SimpleObjectEquality;

public class ArtificalFault extends SimpleObjectEquality{

	/* An artifical fault always is related to an concurrent execution
	 * So an execution were TWO units are executed concurrently and were this concurrent execution leads 
	 * to some kind of issue.
	 */
	
	private final Unit faultUnitOne;
	private final Unit faultUnitTwo;

	private ArtificalFault(Unit unitOne, Unit unitTwo)
	{
		this.faultUnitOne = unitOne;
		this.faultUnitTwo = unitTwo;

	}
	
	public static ArtificalFault of(Unit unitOne, Unit unitTwo)
	{ 
		return new ArtificalFault(unitOne, unitTwo);
	}

	public Unit getFaultUnitOne() {
		return faultUnitOne;
	}

	public Unit getFaultUnitTwo() {
		return faultUnitTwo;
	}
}

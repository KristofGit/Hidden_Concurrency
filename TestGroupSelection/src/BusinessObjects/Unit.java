package BusinessObjects;

import java.util.Hashtable;

import Helper.IDGenerator;
import Helper.SimpleObjectEquality;

public class Unit extends SimpleObjectEquality{

	private final String unitIdentifier;
	
	private Unit(String identifier)
	{
		this.unitIdentifier = identifier;
	}
	
	public static Unit of(String identifier)
	{
		return new Unit(identifier);
	}

	public String getUnitIdentifier() {
		return unitIdentifier;
	}
}

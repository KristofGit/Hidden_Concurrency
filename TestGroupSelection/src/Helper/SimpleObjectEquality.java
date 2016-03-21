package Helper;

import BusinessObjects.Unit;

public abstract class SimpleObjectEquality {

	private final int objectID = IDGenerator.next();

	public boolean equals(Object obj) {
		
		if(this == obj)
		{
			return true;
		}
		
		if(obj instanceof SimpleObjectEquality)
		{
			return ((SimpleObjectEquality)obj).objectID == this.objectID;
		}
		
	  return false;
	}
	
	public int hashCode() {
	    return objectID;
	}
	
	public int getObjectID() {
		return objectID;
	}
}

package Enumerations;

import java.util.Date;

import Main.MainConfiguration;

public enum Overlapping {

	NoOverlapping,
	AlmostOverlapping,
	StartOrEndOverlapping,
	CompleteOverlapping;
	
	public static Overlapping checkOverlap(Date oneFirst, Date oneSecond, Date twoFirst, Date twoSecond)
	{
		Overlapping result = Overlapping.NoOverlapping;
		
		if(checkCompleteOverlap(oneFirst, oneSecond, twoFirst, twoSecond))
		{
			result = Overlapping.CompleteOverlapping;	
		}
		else if(checkStartOrEndOverlapping(oneFirst, oneSecond, twoFirst, twoSecond))
		{
			result = Overlapping.StartOrEndOverlapping;
		}
		else if(checkAlmostOverlapping(oneFirst, oneSecond, twoFirst, twoSecond))
		{
			result = Overlapping.AlmostOverlapping;
		}
		
		return result;
	}
	
	public static boolean checkCompleteOverlap(Date oneFirst, Date oneSecond, Date twoFirst, Date twoSecond)
	{
		boolean result = false;
		
		// Check for 
		//	|   |  (first larger then second)
		//    ||
		if(oneFirst.before(twoFirst) || oneFirst.equals(twoFirst))
		{
			if(oneSecond.after(twoSecond)||oneSecond.equals(twoSecond))
			{
				result = true;
			}
		}
		/* One check should be enought because the other one is then caching it
		// Check for 
		//	||  
		// |  | (the second larger then the first)  
				
		else if(twoFirst.before(oneFirst)||twoFirst.equals(oneFirst))
		{
			if(twoSecond.after(oneSecond) || twoSecond.equals(oneSecond))
			{
				result = true;
			}
		}*/		
		
		return result;
	}
	
	public static boolean checkStartOrEndOverlapping(Date oneFirst, Date oneSecond, Date twoFirst, Date twoSecond)
	{
		boolean result = false;

		/*
		//check for end overlapping
		//  |    |
		//|    |
		// OR
		//   |    |
		//|       |
		// NOT
		// |  |
		// |  | // because this is a complete overlap
		if(oneFirst.before(twoSecond)||oneFirst.equals(twoSecond))
		{
			if(oneSecond.after(twoSecond))
			{
				result = true;
			}
		}		
		//check for start overlapping
		// |    |
		//   |    |
		// OR
		// |    |
		// |       |
		// NOT
		// |  |
		// |  | // because this is a complete overlap
		else if(oneFirst.after(twoFirst))
		{
			if(oneSecond.before(twoSecond) || oneSecond.equals(twoSecond))
			{
				result = true;
			}
		}
		*/
		
		if(oneFirst.before(twoFirst)||oneFirst.equals(twoFirst))
		{
			if(oneSecond.before(twoSecond))
			{
				if(oneSecond.after(twoFirst))
				{
					result = true;
				}
			}
		}
		else if(oneFirst.after(twoFirst))
		{
			if(oneSecond.after(twoSecond))
			{
				if(oneFirst.before(twoFirst)||oneFirst.equals(twoFirst))
				{
					result = true;
				}
			}
		}
		return result;
	}
	
	public static boolean checkAlmostOverlapping(Date oneFirst, Date oneSecond, Date twoFirst, Date twoSecond)
	{
		long oneExecutionTime = oneSecond.getTime()-oneFirst.getTime();
		long twoExecutionTIme = twoSecond.getTime()-twoFirst.getTime();
		
		long oneExpandSpan = (long) (oneExecutionTime*MainConfiguration.almostOverlappingIndicator);
		long twoExpandSpan = (long) (twoExecutionTIme*MainConfiguration.almostOverlappingIndicator);
		
		
		// to check for almost overlapping we expand all dates based on the same overlapping configuration
		oneFirst = new Date(oneFirst.getTime()-oneExpandSpan);
		oneSecond = new Date(oneSecond.getTime()+oneExpandSpan);
		twoFirst = new Date(twoFirst.getTime()-twoExpandSpan);
		twoSecond = new Date(twoSecond.getTime()+twoExpandSpan);

		//then see if we get a start or end overlapping
		//complete overlapping should not be reqired to check because this should already be true without the expansion
		return checkCompleteOverlap(oneFirst, oneSecond, twoFirst, twoSecond) ||
				checkStartOrEndOverlapping(oneFirst, oneSecond, twoFirst, twoSecond);
	}
	
}

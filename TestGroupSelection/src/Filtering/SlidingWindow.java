package Filtering;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import Parser.Activity;
import Parser.Execution;

public class SlidingWindow {

	public static void applyWindow(List<Activity> activities, Date start, Date end)
	{
		for(Activity eachActivity : activities)
		{
			eachActivity.applySlidingWindow(start, end);
		}
		
		Iterator<Activity> iter = activities.iterator();
		
		while(iter.hasNext())
		{
			Activity activity = iter.next();
			
			if(!activity.hasExecutions())
			{
				iter.remove();
			}
		}
	}
	
}

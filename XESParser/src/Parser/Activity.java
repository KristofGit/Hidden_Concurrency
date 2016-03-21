package Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Activity {

	private final String name;
	private final List<Execution> executions = new ArrayList<>();
	private final String backendSystem;
	
	private Activity(String name, String backendSystem)
	{
		this.name = name;
		this.backendSystem = backendSystem;
	}
	
	public static Activity of(String name, String backendSystem)
	{
		return new Activity(name, backendSystem);
	}
	
	public void addExection(Execution newExecution)
	{
		if(newExecution != null)
		{
			executions.add(newExecution);
		}
	}
	
	public int hashCode()
	{ 
	   return name.hashCode();
	}
	
	public boolean hasExecutions()
	{
		return !executions.isEmpty();
	}
	
	public long getAverageExecutionTime()
	{
		long averageExecutionTime = 0;
		
		if(!executions.isEmpty())
		{
			long totalExecuitionTime = 0;

			for(Execution eachExecution : executions)
			{
				totalExecuitionTime += eachExecution.executionTime();
			}
			
			averageExecutionTime = totalExecuitionTime/executions.size();
		}
		
		return averageExecutionTime;
	}
	
	//removes executions that do not occure between these two dates
	public void applySlidingWindow(Date start, Date end)
	{
		Iterator<Execution> iter = executions.iterator();
		
		while(iter.hasNext())
		{
			Execution execution = iter.next();
			
			boolean remove = true;
			
			// ganz eingeschlossen vom windows zeitraum
			if((start.before(execution.getStart())||start.equals(execution.getStart())) &&
					(end.after(execution.getEnd())||end.equals(execution.getEnd())))
			{
				remove = false;
			}
			// überragt den bereich (execution schließt Window Zeitraum ein)
			else if(start.after(execution.getStart()) && end.before(execution.getEnd()))
			{
				remove = false;
			}
			//ragt hinein und ended vor dem Ende
			else if(end.after(execution.getEnd()) || end.equals(execution.getEnd()))
			{
				if(start.before(execution.getEnd()))
				{
					remove = false;
				}
			}
			// beginnt im Bereich und geht darüber hinaus
			else if(start.before(execution.getStart())||start.equals(execution.getStart()))
			{
				if(end.after(execution.getStart()))
				{
					remove = false;
				}
			}
			
			if(remove)
			{
				iter.remove();
			}
		}
	}
	
	public boolean equals(Object obj) {
		
		if(obj instanceof Activity)
		{
			return ((Activity)obj).name.equals(name);
		}
		
		return false;
	}

	public String getName() {
		return name;
	}

	public List<Execution> getExecutions() {
		return Collections.unmodifiableList(executions);
	}

	public String getBackendSystem() {
		return backendSystem;
	}
}

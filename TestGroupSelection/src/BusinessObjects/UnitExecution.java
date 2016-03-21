package BusinessObjects;

import java.util.Date;

import Helper.IDGenerator;
import Helper.SimpleObjectEquality;

public class UnitExecution extends SimpleObjectEquality{

	private final Date startDate, endDate;
	private final long timespan; //execution time span in ms
	
	private UnitExecution(Date start, Date end)
	{
		startDate = new Date(start.getTime());
		endDate = new Date(end.getTime()); 
		
		timespan = end.getTime() - start.getTime();
	}
		
	public static UnitExecution of(Date start, Date end)
	{
		return new UnitExecution(start, end);
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public long getTimespan() {
		return timespan;
	}
}

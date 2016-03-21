package Parser;

import java.util.Date;

public class Execution {

	private final Date start, end;
	
	private Execution(Date start, Date end)
	{
		if(start.before(end))
		{
			this.start = start;
			this.end = end;
		}
		else
		{
			this.end = start;
			this.start = end;
		}
	}
		
	public static Execution of(Date start, Date end)
	{
		return new Execution(start, end);
	}

	public long executionTime()
	{
		return end.getTime() - start.getTime();
	}
	
	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}
}

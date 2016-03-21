package Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BProcess {

	/* Processes are constructed directly from the traces assuming a 1 trace = 1 process relation
	 * Later on the identified processes will be filtered to remove completely equal duplicate processes/traces
	 * So if the same process has a different trace because of parallel executions this would not be a problem
	 * because we would need a specific test to cover this execution anyway -> so its actically good
	 */
	
	private final List<Activity> activities = new ArrayList<Activity>();
	
	private BProcess(Activity initalAct)
	{
		this.activities.add(initalAct);
	}
	
	public static BProcess of(Activity initalActivity)
	{
		return new BProcess(initalActivity);
	}
	
	public void addActivity(Activity activity)
	{
		activities.add(activity);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for( Activity s : activities )
		{
		    result = result * prime + s.hashCode();
		}
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
		BProcess other = (BProcess) obj;
		if (activities == null) {
			if (other.activities != null)
				return false;
		} else if (!activities.equals(other.activities))
			return false;
		return true;
	}

	public List<Activity> getActivities() {
		return Collections.unmodifiableList(activities);
	}
}

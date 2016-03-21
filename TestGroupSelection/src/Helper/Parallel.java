package Helper;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Parallel {

	static final int cpuCount = Runtime.getRuntime().availableProcessors();

	private static class ParallelHelper <T>
	{
		private final Iterator<T> iterator;
		
		public ParallelHelper(Iterator<T> iterator)
		{
			this.iterator = iterator;
		}

		public synchronized T getNextElementToProcess()
		{
			T result = null;
			
			if(iterator.hasNext())
			{
				result = iterator.next();
			}
			
			return result;
		}		
	}
	
	public static abstract class Each <T>
	{
		void execute(ParallelHelper<T> helper)
	    {
	    	for(;;)
	    	{
	    		T element = helper.getNextElementToProcess();
		    	
	    		// null if no more can be processed
		    	if(element == null)
		    	{
		    		break;
		    	}
		    	
		    	executeImpl(element);
	    	}
	    }
	    
	    public abstract void executeImpl(T i);    
	}
	
	public static <T> void ForEach(Iterable<T> list, final Each<T> each)
	{
	    ExecutorService executor = Executors.newFixedThreadPool(cpuCount);
	    
	    final ParallelHelper<T> helper = new ParallelHelper<T>(list.iterator());
	
	    for(int i=0;i<cpuCount;i++)
	    {
	    	executor.execute(new Runnable() {
				
				@Override
				public void run() {
					each.execute(helper);
				}
			});
	    }
	   	    
	    executor.shutdown();
	    try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

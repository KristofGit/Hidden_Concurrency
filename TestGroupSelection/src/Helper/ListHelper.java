package Helper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ListHelper {
	
	public static interface MultiListFunc<E> {
		
		public Class<E> getEType();
		
	    void execute(E e);
	}
	
	public static <E> void MultiListHandler(Iterable<Object> multiList, MultiListFunc<E> func)
	{
		if(multiList == null || func == null)
		{
			return;
		}
		
		String className = func.getEType().getName();
		
		Iterator<Object> iterator = multiList.iterator();
		
		while(iterator.hasNext())
		{
			Object nextElement = iterator.next();
			
			if(nextElement != null)
			{
				if(nextElement instanceof Iterable<?>)
				{
					MultiListHandler((Iterable)nextElement, func);
				} 			
				else if(nextElement.getClass().getName().equals(className))
				{
					func.execute((E)nextElement);
				}	
			}
		}
	}	
}

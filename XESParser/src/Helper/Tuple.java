package Helper;

public class Tuple<X, Y> { 
	  public final X x; 
	  public final Y y; 
	  public Tuple(X x, Y y) { 
	    this.x = x; 
	    this.y = y; 
	  } 
	  
	  public static <X,Y> Tuple<X,Y> of(X x, Y y)
	  {
		  return new Tuple<X,Y>(x, y);
	  }
	} 

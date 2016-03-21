package Helper;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {

	private static final AtomicInteger sequence = new AtomicInteger();

	public static int next() {
	    return sequence.incrementAndGet();
	}
}

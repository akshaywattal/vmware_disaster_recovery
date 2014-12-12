/**
 * Benchmark.java
 *
 * (C) Copyright IBM Corp. 2005, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author: Chung-hao Tan, IBM ,chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */
package org.sblim.wbem.util;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Benchmark {
	private static class ThreadLocalTimer extends ThreadLocal {
		public Object initialValue() {
			return new Benchmark();
		}
		public Benchmark getLocal() {
			return (Benchmark)super.get();
		}
	}
	
	public static final Runtime rt = Runtime.getRuntime();
	public long cumulativeTime = 0;
	public long elapse = 0;
	public long currentTime = 0;
	
	public long transportCumulativeTime = 0;
	public long transportElapse = 0;
	public long transportCurrentTime = 0;
	public long memory = 0;
	private static ThreadLocalTimer local = new ThreadLocalTimer();
	
	/**
	 * @deprecated
	 */
	public static void resetTime() {
		resetTimer();
	}
	
	public static void resetTimer() {
		Benchmark timer = local.getLocal();
		
		timer.cumulativeTime = 0;
		timer.elapse = 0;
		timer.transportElapse = 0;
		timer.transportCurrentTime = 0;
		timer.memory = 0;
	}
	
	public static void startTimer() {
		Benchmark timer = local.getLocal();
		timer.currentTime = System.currentTimeMillis();
	}
	
	public static void stopTimer() {
		Benchmark timer = local.getLocal();
		timer.elapse = System.currentTimeMillis()-timer.currentTime;
		timer.cumulativeTime += timer.elapse;		
	}
	
	public static long getElapse() {	
		Benchmark timer = local.getLocal();
		return timer.elapse;
	}
	
	public static long getCumulativeTime() {
		Benchmark timer = local.getLocal();
		return timer.cumulativeTime;
	}
	
	public static void startTransportTimer() {
		Benchmark timer = local.getLocal();
		timer.transportCurrentTime =  System.currentTimeMillis(); 
	}

	public static void stopTransportTimer() {
		Benchmark timer = local.getLocal();
		timer.transportElapse = System.currentTimeMillis()-timer.transportCurrentTime;
		timer.transportCumulativeTime += timer.transportElapse;		
	}
	
	public static long getTransportElapse() {	
		Benchmark timer = local.getLocal();
		return timer.transportElapse;
	}
	
	public static long getTransportCumulativeTime() {
		Benchmark timer = local.getLocal();
		return timer.transportCumulativeTime;
	}
	
	public static void startMemoryMeter() {
		Benchmark timer = local.getLocal();
		timer.memory = rt.freeMemory();
	}

	public static void stopMemoryMeter() {
		Benchmark timer = local.getLocal();
		timer.memory = rt.freeMemory() - timer.memory;
	}
	
	public static long getMemoryConsumption() {
		Benchmark timer = local.getLocal();
		return timer.memory;
	}
	
	public static long showMemory() {
//		long used = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
//		System.out.println("TotalUsed:"+(used)/((float)1024*1024)+"Mb, FreeMemory:"+Runtime.getRuntime().freeMemory()/((float)1024*1024)+"Mb, TotalMemory:"+Runtime.getRuntime().totalMemory()/((float)1024*1024)+"Mb, MaxMemory:"+Runtime.getRuntime().maxMemory()/((float)1024*1024)+"Mb");
//		return used;
		return 0;
	}
	
	public static void gc() {
		return;
//		int nn = 0;
//		long used = 0;
//		do {
//			nn++;
//			long prev;
//			System.out.print("GC+: "); prev = Benchmark.showMemory();
//			Runtime.getRuntime().gc();
//			try {
//				Thread.sleep(500);
//			}
//			catch (Exception e) {
//			}
//			System.out.print("GC-: "); used = Benchmark.showMemory();
//			if (used == prev) break;
//		} while (nn < 3);
	}
}

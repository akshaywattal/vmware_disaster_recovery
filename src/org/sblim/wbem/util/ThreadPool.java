/**
 * ThreadPool.java
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
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1649779    2007-02-04  lupusalex    Indication listener threads freeze
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */
package org.sblim.wbem.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class ThreadPool implements a pool that manages threads and executes
 * submitted tasks using this threads.
 * 
 */
public class ThreadPool {

	/**
	 * Class Worker implements a worker thread used by the thread pool
	 * 
	 */
	private static class Worker extends Thread {

		private boolean iAlive = true;

		private boolean iStarted;

		private ThreadPool iPool;

		private Runnable iTask;

		private long iIdleTimeout;

		private boolean iTimedOut = false;

		/**
		 * Ctor.
		 * 
		 * @param pool
		 *            The owning pool
		 * @param name
		 *            The name of the thread
		 */
		public Worker(ThreadPool pool, String name) {
			super(pool.getGroup(), name);
			iPool = pool;
			setDaemon(true);
		}

		public synchronized void start() {
			iStarted = true;
			iAlive = true;
			super.start();
		}

		/**
		 * Kills the thread
		 */
		public synchronized void kill() {
			iAlive = false;
			iStarted = false;
			notify();
		}

		/**
		 * Assigns a new task to the thread
		 * 
		 * @param task
		 *            The task
		 * @return <code>true</code> if the task could be successfully
		 *         assigned.
		 */
		public synchronized boolean assignTask(Runnable task) {
			if (iAlive) {
				iTask = task;
				if (!iStarted) {
					start();
				} else {
					notify();
				}
				return true;
			}
			return false;
		}

		/**
		 * Returns the idle timeout
		 * 
		 * @return The idle timeout
		 */
		public long getIdleTimeout() {
			return iIdleTimeout;
		}

		/**
		 * Set the idle timeout
		 * 
		 * @param pIdleTimeout
		 *            The new value
		 */
		public void setIdleTimeout(long pIdleTimeout) {
			iIdleTimeout = pIdleTimeout;
		}

		/**
		 * Waits for a new task execute
		 * 
		 * @return The task or <code>null</code>
		 */
		private synchronized Runnable waitForTask() {
			if (iTask != null) {
				Runnable tsk = iTask;
				iTask = null;
				return tsk;
			}
			iAlive = iPool.taskCompleted(this, iTimedOut);
			if (iAlive && (iTask == null)) {
				try {
					long idleTimeOut = getIdleTimeout();
					if (idleTimeOut > 0) {
						wait(idleTimeOut);
						iTimedOut = (iTask == null);
					} else {
						wait();
					}
				} catch (InterruptedException e) {
					// this is expected
				}
			}
			return null;
		}

		public void run() {
			while (iAlive) {
				Runnable tsk = waitForTask();
				if (tsk != null) {
					try {
						tsk.run();
					} catch (Throwable t) {
						Logger logger = SessionProperties.getGlobalProperties().getLogger();
						if (logger.isLoggable(Level.SEVERE)) {
							logger.log(Level.SEVERE,
									"Exception while executing task from thread pool", t);
						}
					}
				}
			}
			iStarted = false;
			iPool.removeThread(this);
		}
	}

	private ThreadGroup iGroup;

	private List iIdleThreads = new LinkedList();

	private List iThreadPool = new LinkedList();

	private List iQueue = new LinkedList();

	private long iIdleTimeout = 30000;

	private int iMaxPoolSize;

	private int iMinPoolSize;

	private int iCntr = 0;

	private boolean iShutdown = false;

	private String iWorkerName;

	/**
	 * Ctor
	 * 
	 * @param pMinPoolSize
	 *            The minimal pool size. The pool will always keep at least this
	 *            number of worker threads alive even in no load situations.
	 * @param pMaxPoolSize
	 *            The maximal pool size. The pool will create up to that number
	 *            of worker threads on heavy load.
	 * @param pGroup
	 *            Then thread group to put the worker threads in
	 * @param pWorkerName
	 *            The name to use for worker threads
	 */
	public ThreadPool(int pMinPoolSize, int pMaxPoolSize, ThreadGroup pGroup, String pWorkerName) {
		iGroup = pGroup != null ? pGroup : new ThreadGroup("TreadPool Group");
		iMinPoolSize = pMinPoolSize;
		iMaxPoolSize = pMaxPoolSize;
		iWorkerName = pWorkerName != null ? pWorkerName : "Worker ";

		for (int i = 0; i < pMinPoolSize; i++) {
			Worker worker = new Worker(this, iWorkerName + getID());
			iThreadPool.add(worker);
			worker.start();
		}
	}

	/**
	 * Returns the idle timeout
	 * 
	 * @return The timeout
	 */
	public synchronized long getIdleTimeOutMs() {
		return (iThreadPool.size() <= iMinPoolSize) ? -1 : iIdleTimeout;
	}

	public boolean execute(Runnable task) {
		return execute(task, true);
	}

	/**
	 * Submits a task for execution
	 * 
	 * @param task
	 *            The task
	 * @param enqueue
	 *            if <code>true</code> the task will be enqueued if no worker
	 *            is free, otherwise the task is executed immediately or not at
	 *            all.
	 * @return <code>true</code> if the task was executed or enqueued,
	 *         <code>false</code> otherwise.
	 */
	public synchronized boolean execute(Runnable task, boolean enqueue) {
		if (iShutdown) return false;
		int totalIdle = iIdleThreads.size();
		if (totalIdle > 0) {
			// takes the last one that came back, so that the older ones have a
			// chance to timeout
			Worker worker = (Worker) iIdleThreads.remove(totalIdle - 1);
			return worker.assignTask(task);
		} else if ((iMaxPoolSize == -1) || (iThreadPool.size() <= iMaxPoolSize)) {
			Worker worker = createWorker();
			return worker.assignTask(task);
		} else if (enqueue) {
			iQueue.add(task);
			return true;
		}
		return false;
	}

	/**
	 * Creates a new worker thread
	 * 
	 * @return The worker thread
	 */
	private Worker createWorker() {
		Worker worker = new Worker(this, iWorkerName + getID());
		iThreadPool.add(worker);
		return worker;
	}

	/**
	 * Gets the associated thread group
	 * 
	 * @return The thread group
	 */
	protected ThreadGroup getGroup() {
		return iGroup;
	}

	/**
	 * Used by the worker to report task completion and ask for a new task
	 * 
	 * @param worker
	 *            The worker
	 * @param timedOut
	 *            <code>true</code> if the worker has been idle and reached
	 *            timeout
	 * @return <code>true</code> if the worker shall stay alive,
	 *         <code>false</code> if it shall shut down itself
	 */
	public synchronized boolean taskCompleted(Worker worker, boolean timedOut) {
		if (iShutdown) return false;

		if (iQueue.size() > 0) {
			iIdleThreads.remove(worker);
			worker.assignTask((Runnable) iQueue.remove(0));
			return true;
		} else if (timedOut && (iThreadPool.size() > iMinPoolSize)) {
			iIdleThreads.remove(worker);
			iThreadPool.remove(worker);
			return false;
		} else if ((iMaxPoolSize == -1) || (iThreadPool.size() <= iMaxPoolSize)) {
			if (!iIdleThreads.contains(worker)) iIdleThreads.add(worker);
			worker.setIdleTimeout(getIdleTimeOutMs());
			return true;
		}
		return false;
	}

	/**
	 * Removes a worker from the pool. 
	 * @param worker The worker
	 */
	protected synchronized void removeThread(Worker worker) {
		if (worker != null && iThreadPool != null) iThreadPool.remove(worker);
	}

	/**
	 *  Shuts down the thread pool and all workers
	 */
	public synchronized void shutdown() {
		if (!iShutdown) {
			iShutdown = true;
			Iterator iter;
			if (iIdleThreads != null) {
				iter = iIdleThreads.iterator();
				while (iter.hasNext()) {
					Worker worker = (Worker) iter.next();
					worker.kill();
				}
				iIdleThreads = null;
			}

			if (iThreadPool != null) {
				iter = iThreadPool.iterator();
				while (iter.hasNext()) {
					Worker worker = (Worker) iter.next();
					worker.kill();
				}
				iThreadPool = null;
			}
		}
	}

	/**
	 * Generates an &quot;unique&quot; id for a worker thread
	 * @return The id
	 */
	private String getID() {
		if (++iCntr >= 10000) iCntr = 1;
		return String.valueOf(iCntr);
	}

}

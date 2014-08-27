/**
 * 
 */
package eu.parmerasa.sik.tas2otawa.xml.tas;

import java.util.LinkedList;

/**
 * Common interface for all parallelism skeletons.
 * 
 * @author Rolf Kiefhaber
 */
public interface TasParallelism extends TasObjectWithId {

	/**
	 * Returns a list of all threads that are used by this skeleton
	 * 
	 * @return the threads used by this skeleton
	 */
	public LinkedList<TasThreadRef> getThreads();

	/**
	 * Returns the id of the skeleton that executed this skeleton. This typically happens if skeletons are nested. If
	 * the caller was the main thread, BEGIN is returned.
	 * <p>
	 * Example: A Pipeline Parallelism skeleton s1 executes a Data Parallelism skeleton s2 to parallelize one of its
	 * pipeline steps and therefore shorten its run time to match its other pipeline steps. s1 would return BEGIN, while
	 * s2 would return the id of s1.
	 * </p>
	 * 
	 * @return The id of the skeleton that executed this pattern or BEGIN, if the skeleton is executed by the main
	 *         thread.
	 */
	public String getCalledBy();

	/**
	 * Returns the id of the skeleton that was sequentially executed before this pattern. If this skeleton is the first
	 * to be executed, BEGIN is returned.
	 * <p>
	 * Example: The main thread executes two Task Parallelism skeletons (s1, s2) after each other. s1 would return
	 * BEGIN, while s2 would return the id of s1.
	 * </p>
	 * 
	 * @return
	 */
	public String getExecutedAfter();

	/**
	 * @return finalize_id if existing
	 */
	public String getFinalizeId();

	/**
	 * @return init_id if set
	 */
	public String getInitId();

	/**
	 * @return Description of skeleton
	 */
	public String getDescription();

	/** Checks if the skeleton uses a given thread id */
	public boolean hasThread(String worker_id);

	/** Returns thread by its ID */
	public TasThreadRef getThread(String worker_id);

	/** Returns main thread */
	public TasThreadRef getMainThread();

	public abstract String getExecuteWorkAvailableBarrierId();

	public abstract String getExecuteWorkDoneBarrierId();
}

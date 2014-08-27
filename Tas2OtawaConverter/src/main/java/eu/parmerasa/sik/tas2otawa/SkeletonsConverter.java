package eu.parmerasa.sik.tas2otawa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import eu.parmerasa.sik.tas2otawa.xml.CSection;
import eu.parmerasa.sik.tas2otawa.xml.otawa.OtawaProgram;
import eu.parmerasa.sik.tas2otawa.xml.otawa.OtawaSync;
import eu.parmerasa.sik.tas2otawa.xml.otawa.OtawaThread;
import eu.parmerasa.sik.tas2otawa.xml.otawa.OtawaThreadInSync;
import eu.parmerasa.sik.tas2otawa.xml.otawa.OtawaWait;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasAbstractSkeleton;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasDataParallelism;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasObjectWithId;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasParallelism;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasPipelineParallelism;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasProgram;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasTaskParallelism;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasThread;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasThreadRef;

/**
 * Converter to convert TAS patterns to OTAWA specific XML
 * 
 * @author Rolf Kiefhaber
 * 
 */
public class SkeletonsConverter {

	private static final String WORKER_THREAD_FUNCTION = "tas_thread";
	private static final String THREAD_DELIMETER = ",";

	private OtawaSync tasInitMainSync;
	private OtawaSync tasInitWorkerSync;

	private OtawaSync syncThreadAvailable;
	private OtawaSync syncThreadDone;

	private OtawaProgram otawaProgram;

	private Map<String, TasParallelism> parallelismMap;

	/**
	 * Constructor for SkeletonsConvertor, sets start values
	 */
	public SkeletonsConverter() {
		reset();
	}

	/**
	 * Set start values in this class
	 */
	private void reset() {
		otawaProgram = null;

		tasInitMainSync = null;
		tasInitWorkerSync = null;
		syncThreadAvailable = null;
		syncThreadDone = null;

		parallelismMap = null;
	}

	/**
	 * Converts the given TAS XML object representation to a OTAWA XML representation
	 * 
	 * @param tasProgram
	 *            the base object to transform
	 * @return the transformed OTAWA XML object that is ready to be serialized
	 */
	public OtawaProgram transformParallelism(TasProgram tasProgram) throws MisformedXmlException {
		// Set start values
		reset();

		otawaProgram = new OtawaProgram();
		parallelismMap = generatePatternMap(tasProgram);

		// Check if the XML file ist consistent
		checkConsistency(tasProgram);

		// To ease debugging print the use of threads by the skeleton instances
		printThreadUsage();

		// Sync tag for worker threads waiting for work to be available
		syncThreadAvailable = OtawaSync.getOrCreate(otawaProgram, "tas_thread_work_available_barrier");
		syncThreadAvailable.setInloopId("loop_runnable");
		syncThreadAvailable.setComment("Worker threads waiting for work executing a skeleton");
		otawaProgram.addSync(syncThreadAvailable);

		// Sync tag for worker threads waiting for work to be done
		syncThreadDone = OtawaSync.getOrCreate(otawaProgram, "tas_thread_work_done_barrier");
		syncThreadDone.setComment("Worker threads waiting for completion of work");
		otawaProgram.addSync(syncThreadDone);

		// Get ID of first skeleton in the program
		String initId = this.getFirstTasParallelism().getInitId();

		// If ID found: create syncs
		tasInitMainSync = (initId == null) ? OtawaSync.BEGIN : OtawaSync.getOrCreate(otawaProgram, initId
				+ ".tas_abstract_init_worker_init_barrier");
		tasInitMainSync.setComment("tas_abstract_init - Initialization for all skeletons with "
				+ "all worker threads only once after program startup");

		tasInitWorkerSync = (initId == null) ? OtawaSync.BEGIN : OtawaSync.getOrCreate(otawaProgram,
				"tas_thread_worker_init_barrier");

		convertThreads(tasProgram);

		if (initId != null) {
			createTasInit(tasProgram);
			otawaProgram.addSync(tasInitMainSync);
			otawaProgram.addSync(tasInitWorkerSync);
		}

		convertTaskParallelisms(tasProgram);
		convertDataParallelisms(tasProgram);
		convertPipelineParallelisms(tasProgram);

		convertCSections(tasProgram);

		return otawaProgram;
	}

	/** Tests the structure of the XML input */
	private void checkConsistency(TasProgram tasProgram) throws MisformedXmlException {
		// Check if all skeletons are accessible
		checkConsistencyAccessible();

		// Get first skeleton, this checks if there is exactly one first skeleton
		TasParallelism first = getFirstTasParallelism();

		// Check for each skeleton: if neither executed_after nor called_by,
		// then the main thread must have a method != tas_thread
		for (TasThread thread : tasProgram.getThreads()) {
			if (thread.getId().equals(first.getMainThread().getId())) {
				if (thread.getRoutine().equals("tas_thread")) {
					throw new MisformedXmlException("Main thread " + first.getMainThread().getId()
							+ " of first skeleton " + first + " cannot have routine tas_thread");
				}
			}
		}

		for (TasParallelism item : parallelismMap.values()) {
			// Check if all other threads have tas_thread if they are used by any pattern

			for (TasThreadRef thread : item.getThreads()) {
				String routine = tasProgram.getThreadById(thread.getId()).getRoutine();

				if (routine == null) {
					throw new MisformedXmlException(
							"A routine must be specified for every thread, this includes thread " + thread.getId());
				}

				if (item.getId().equals(first.getId())) {
					if (thread.getMain().equals("1")) {
						if (routine.equals("tas_thread")) {
							throw new MisformedXmlException("Main thread " + thread.getId()
									+ " of first skeleton " + item + " cannot have routine tas_thread");
						}
					} else {
						if (!routine.equals("tas_thread")) {
							throw new MisformedXmlException("Worker thread " + thread.getId()
									+ " of first skeleton " + item + " must have routine tas_thread");
						}
					}
				} else {
					if (!thread.getMain().equals("1") && !routine.equals("tas_thread")) {
						throw new MisformedXmlException("Worker thread " + thread.getId()
								+ " of skeleton " + item + " must have routine tas_thread");
					}
				}
			}

			// Check for each skeleton: if it either has executed_after or
			// called_by
			if (!item.getExecutedAfter().equals("BEGIN") && !item.getCalledBy().equals("BEGIN"))
				throw new MisformedXmlException("Skeleton " + item
						+ " has both executed_after and called_by set - this is not allowed.");

			// Check for each skeleton: if one thread is main
			int main_found = 0;
			for (TasThreadRef thread : item.getThreads()) {
				if (thread.getMain().equals("1"))
					main_found++;
			}
			if (main_found != 1) {
				throw new MisformedXmlException("Skeleton " + item
						+ " does not have a main thread or several of them (" + main_found + ")");
			}

			// Check for each skeleton: if the threads are existent
			for (TasThreadRef thread : item.getThreads()) {
				if (!tasProgram.hasThread(thread)) {
					throw new MisformedXmlException("Skeleton " + item + " has thread " + thread.getId()
							+ " which is not existing.");
				}
			}

			// Check for each skeleton: if executed_after != BEGIN, then main
			// thread must be the same for skeleton and executed_after-skeleton
			if (!item.getExecutedAfter().equals("BEGIN")) {
				String other = parallelismMap.get(item.getExecutedAfter()).getMainThread().getId();
				if (!item.getMainThread().getId().equals(other)) {
					throw new MisformedXmlException("Main thread " + item.getMainThread().getId() + " of " + item
							+ " is not the same as " + other + " of skeleton " + item.getExecutedAfter());
				}
			}

			// Check for each skeleton: if called_by != BEGIN, then main thread
			// must be included in calling skeleton and no other threads may be
			// in both skeletons
			if (!item.getCalledBy().equals("BEGIN")) {
				if (!parallelismMap.get(item.getCalledBy()).hasThread(item.getMainThread().getId())) {
					throw new MisformedXmlException("Main thread " + item.getMainThread().getId() + " of " + item
							+ " is not present in skeleton " + item.getCalledBy());
				}

				int intersection = 0;
				TasParallelism other = parallelismMap.get(item.getCalledBy());
				for (TasThreadRef thread : item.getThreads()) {
					if (other.hasThread(thread.getId())) {
						intersection++;
					}
				}

				if (intersection == 0) {
					throw new MisformedXmlException("Thread intersection of skeleton " + item
							+ " with calling skeleton " + other + " is empty and must be 1 to have main thread.");
				}

				if (intersection > 1) {
					throw new MisformedXmlException("Thread intersection of skeleton " + item
							+ " with calling skeleton " + other + " is " + intersection
							+ " and must be 1 to have main thread only.");
				}
			}

			// Skeleton should have either both init and finalize or none
			if ((item.getInitId() == null && item.getFinalizeId() != null) ||
					(item.getInitId() != null && item.getFinalizeId() == null)) {
				throw new MisformedXmlException(
						"Either both init and finalize or none should be defined, check skeleton " + item + ".");
			}

			// Skeleton without init/finalize: workers used by these skeletons may not be used by any other skeleton
			if (item.getInitId() == null && item.getFinalizeId() == null) {
				for (TasThreadRef thread : item.getThreads()) {
					String foobar = thread.getMain();
					if (thread.getMain().equals("0")) {
						// Thread may not be in use by other skeleton
						for (TasParallelism skel : parallelismMap.values()) {
							if (!skel.getId().equals(item.getId()) && // Not checking A == A
									skel.getThread(thread.getId()) != null && // Both skeletons have same thread
									!skel.getThread(thread.getId()).getMain().equals("1") // Not main thread of other
																							// skeleton
							) {
								throw new MisformedXmlException(
										"Skeleton " + item + " uses worker thread " + thread.getId() + " as well as "
												+ skel + ", but " + item + " does not release it");
							}
						}
					}
				}
			}
		}
	}

	/** Check if all skeletons are accessible */
	private void checkConsistencyAccessible() throws MisformedXmlException {
		LinkedList<TasParallelism> to_visit = new LinkedList<TasParallelism>();
		HashSet<TasParallelism> visited = new HashSet<TasParallelism>();
		HashSet<TasParallelism> not_visited = new HashSet<TasParallelism>();

		not_visited.addAll(parallelismMap.values());

		to_visit.add(getFirstTasParallelism());

		while (!to_visit.isEmpty()) {
			TasParallelism item = to_visit.pop();

			if (visited.contains(item))
				continue;

			not_visited.remove(item);
			visited.add(item);

			to_visit.addAll(getCalledTasParallelisms(item));

			if (getNextTasParallelism(item) != null)
				to_visit.add(getNextTasParallelism(item));
		}

		if (!not_visited.isEmpty()) {
			throw new MisformedXmlException(
					"There are skeleton instances which are not accessible by following the call tree: "
							+ not_visited.toString());
		}
	}

	private TreeMap<TasParallelism, String> ptuDictionary = new TreeMap<TasParallelism, String>();
	private char ptuNextLetter = 'A';

	/**
	 * Prints the usage of threads by the different skeletons and also their order of execution
	 */
	private void printThreadUsage() {
		System.out.println("STARTING WITH printThreadUsage");

		// Print thread usage
		HashSet<TasParallelism> active = new HashSet<TasParallelism>();
		printThreadUsage(getFirstTasParallelism(), active);

		// Print dictionary
		for (TasParallelism key : ptuDictionary.keySet()) {
			System.out.println(ptuDictionary.get(key) + " " + key);
		}

		System.out.println("FINISHED WITH printThreadUsage");
	}

	private void printThreadUsage(TasParallelism item, HashSet<TasParallelism> active) {
		// Add item to active instances
		active.add(item);

		// Get key from dictionary or add it
		String letter = ptuDictionary.get(item);
		if (letter == null) {
			letter = "" + ptuNextLetter;
			ptuNextLetter++;
			ptuDictionary.put(item, letter);
		}

		// Print thread usage line
		printThreadUsageLine(active, "START[" + letter + "]");

		// Handling called instances

		HashSet<TasParallelism> new_active = (HashSet<TasParallelism>) active.clone();

		for (TasParallelism child : getCalledTasParallelisms(item)) {
			// System.out.println(item + " calls some skeleton instances...");
			new_active.add(child);
		}

		for (TasParallelism child : getCalledTasParallelisms(item)) {
			printThreadUsage(child, new_active);
		}

		printThreadUsageLine(active, "  END[" + letter + "]");

		// Remove item from active instances
		active.remove(item);

		// Calling following instances

		// System.out.println("Going to next instance of " + item);

		TasParallelism next = getNextTasParallelism(item);
		if (next != null) {
			printThreadUsage(next, active);
		}

		// System.out.println("Finished with " + item);
	}

	/** Print in a single line which thread is occupied by a skeleton instance */
	private void printThreadUsageLine(HashSet<TasParallelism> active, String note) {
		String[] threads = new String[256];
		int threads_max_index = 0;
		int threads_min_index = Integer.MAX_VALUE;
		for (int i = 0; i < threads.length; i++)
			threads[i] = "";

		for (TasParallelism foo : active) {
			String my_letter = ptuDictionary.get(foo);
			for (TasThreadRef th : foo.getThreads()) {
				// System.out.println(foo + " uses Thread " + th.getId() +
				// " with main state " + th.getMain());
				int thread = Integer.parseInt(th.getId());

				if (th.getMain() != null && th.getMain().equals("1"))
					threads[thread] = threads[thread] + "<" + my_letter + ">";
				else
					threads[thread] = threads[thread] + "" + my_letter + "";

				threads_max_index = Math.max(threads_max_index, thread);
				threads_min_index = Math.min(threads_min_index, thread);
			}
		}

		// Get length of longest thread description
		int longest = parallelismMap.size() + 5;
		/*
		 * for (int i = threads_min_index; i < threads_max_index + 1; i++) { longest = Math.max(longest,
		 * threads[i].length()); }
		 */

		// Print thread info
		System.out.print(note + " ");
		for (int i = threads_min_index; i < threads_max_index + 1; i++) {
			boolean empty = !threads[i].equals("");
			while (threads[i].length() < longest)
				threads[i] = " " + threads[i];

			if (empty) {
				System.out.print(threads[i] + "[" + i + "]" + " ");
			} else {
				System.out.print(threads[i] + "   " + " ");
			}
		}
		System.out.println("");
	}

	private void createTasInit(TasProgram tasProgram) {
		String[] threadIds = createComposedThreadIds(tasProgram.getThreads(), THREAD_DELIMETER);
		String mainThreadId = threadIds[0];
		String nonMainThreadIds = threadIds[1];

		OtawaThreadInSync first_main = tasInitMainSync.getThreadOrCreate(mainThreadId);

		OtawaWait first_main_wait = new OtawaWait(nonMainThreadIds, tasInitWorkerSync, "BEGIN", "BEGIN");
		first_main.addWait(first_main_wait);

		OtawaThreadInSync second_main = tasInitWorkerSync.getThreadOrCreate(nonMainThreadIds);

		OtawaWait second_main_wait = new OtawaWait(mainThreadId, tasInitMainSync, "BEGIN", "BEGIN");
		second_main.addWait(second_main_wait);
	}

	/**
	 * Calculate first TAS skeleton of the program, i.e., the skeleton without getCalledBy() or getExecutedAfter()
	 * 
	 * @param parallelismMap
	 *            Map with all skeleton instances
	 * @return First skeleton in program
	 */
	private TasParallelism getFirstTasParallelism() {
		ArrayList<TasParallelism> results = new ArrayList<TasParallelism>();

		for (TasParallelism item : parallelismMap.values()) {
			if (item.getExecutedAfter().equals("BEGIN") && item.getCalledBy().equals("BEGIN"))
				results.add(item);
		}

		if (results.size() == 1)
			return results.get(0);
		else {
			throw new MisformedXmlException("Null or more than one Skeletons are identified as first skeleton: "
					+ results.toString());
		}
	}

	/**
	 * Calculates list of all skeletons instances which are called by a given skeleton instance
	 */
	private ArrayList<TasParallelism> getCalledTasParallelisms(TasParallelism root) {
		ArrayList<TasParallelism> results = new ArrayList<TasParallelism>();

		for (TasParallelism item : parallelismMap.values()) {
			if (item.getCalledBy().equals(root.getId()))
				results.add(item);
		}

		/*
		 * if (results.size() == 1) return results.get(0); else { throw new MisformedXmlException
		 * ("Null or more than one Skeletons are identified as first skeleton: " + results.toString()); }
		 */

		return results;
	}

	/**
	 * Calculates list of all skeletons instances which are called by a given skeleton instance
	 */
	private TasParallelism getNextTasParallelism(TasParallelism root) {
		ArrayList<TasParallelism> results = new ArrayList<TasParallelism>();

		for (TasParallelism item : parallelismMap.values()) {
			if (item.getExecutedAfter().equals(root.getId()))
				results.add(item);
		}

		if (results.size() == 1)
			return results.get(0);
		if (results.size() == 0)
			return null;
		else {
			throw new MisformedXmlException("More than one Skeletons are identified as next skeleton: "
					+ results.toString());
		}
	}

	/**
	 * Converts all task parallelism tags to corresponding OTAWA XML code
	 * 
	 * @param tasProgram
	 *            the TAS XML as deserialized object
	 * @param otawaProgram
	 *            the resulting OTAWA XML as deserialized object
	 */
	private void convertTaskParallelisms(TasProgram tasProgram) {

		for (TasTaskParallelism parallelism : tasProgram.getTaskParallelisms()) {
			String parallelismId = parallelism.getId();
			String description = parallelism.getDescription();

			String[] threadIds = createComposedThreadRefIds(parallelism.getThreads(), THREAD_DELIMETER);
			String mainThreadId = threadIds[0];
			String nonMainThreadIds = threadIds[1];
			// Only generate something if there are more than 1 thread
			if (nonMainThreadIds != null) {
				// Add tags for initialization
				if (parallelism.getInitId() != null) {
					createSkeletonInit(parallelism);
				}

				// Calculate last common sync point of all threads...
				// To do this, you need to find (a) the init point if it is
				// generated or (b) another skeleton called before this one
				// where all here active threads were involved, too.

				ArrayList<TasParallelism> completed = this.getCompletedSkeletons(parallelism);
				TasParallelism last_skel_with_all_threads = null;
				for (TasParallelism item : completed) {
					boolean all_in = true;
					for (TasThreadRef thread : parallelism.getThreads()) {
						if (all_in && !item.hasThread(thread.getId()))
							all_in = false;
					}

					if (all_in) {
						last_skel_with_all_threads = item;
					}
				}

				String last_sync_workers = null;
				String last_sync_main = null;
				if (last_skel_with_all_threads != null && last_skel_with_all_threads.getFinalizeId() != null) {
					last_sync_main = last_skel_with_all_threads.getFinalizeId() + "."
							+ "tas_abstract_finalize_work_done_barrier";
					last_sync_workers = "tas_thread_work_done_barrier";
				} else if (last_skel_with_all_threads != null && last_skel_with_all_threads.getFinalizeId() == null) {
					last_sync_main = last_skel_with_all_threads.getExecuteWorkDoneBarrierId();
					last_sync_workers = "tas_thread_work_done_barrier";
				} else {
					last_sync_main = tasInitMainSync.getId();
					last_sync_workers = tasInitWorkerSync.getId();
				}

				// Syncs
				OtawaSync syncExecuteAvailable = OtawaSync.getOrCreate(otawaProgram,
						parallelism.getExecuteWorkAvailableBarrierId());
				syncExecuteAvailable.setInloopId("tas_taskparallel_execute_loop");
				syncExecuteAvailable.setComment("Main thread executing task parallelism \"" + description + "\" (id = "
						+ parallelismId + ")");
				OtawaSync syncExecuteDone = OtawaSync.getOrCreate(otawaProgram,
						parallelism.getExecuteWorkDoneBarrierId());

				// sync: tas_taskparallel_execute_work_available_barrier
				OtawaThreadInSync threadInExecuteAvailable = syncExecuteAvailable.getThreadOrCreate(mainThreadId);

				OtawaWait waitInExecuteAvailable = new OtawaWait(nonMainThreadIds);
				threadInExecuteAvailable.addWait(waitInExecuteAvailable);

				// TODO: Check this!
				waitInExecuteAvailable.setSync(syncThreadAvailable);
				waitInExecuteAvailable.setLastMe(OtawaSync.getOrCreate(otawaProgram, last_sync_main)); // OtawaSync.BEGIN
				waitInExecuteAvailable.setLastEm(OtawaSync.getOrCreate(otawaProgram, last_sync_workers)); // OtawaSync.BEGIN
				waitInExecuteAvailable.setLastMeInLoop(syncExecuteDone);
				waitInExecuteAvailable.setLastEmInLoop(syncThreadDone);

				// sync: tas_taskparallel_execute_work_done_barrier
				OtawaThreadInSync threadInExecuteDone = syncExecuteDone.getThreadOrCreate(mainThreadId);

				OtawaWait waitInExecuteDone = new OtawaWait(nonMainThreadIds);
				threadInExecuteDone.addWait(waitInExecuteDone);

				waitInExecuteDone.setSync(syncThreadDone);
				waitInExecuteDone.setLastMe(syncExecuteAvailable);
				waitInExecuteDone.setLastEm(syncThreadAvailable);

				// sync: tas_thread_work_available_barrier
				OtawaThreadInSync threadInWorkAvailable = syncThreadAvailable.getThreadOrCreate(nonMainThreadIds);
				OtawaWait waitInWorkAvailable = new OtawaWait(mainThreadId);
				waitInWorkAvailable.setComment("wait for task parallelism skeleton \"" + description + "\" (id = "
						+ parallelismId + ") [T " + nonMainThreadIds + "]");
				threadInWorkAvailable.addWait(waitInWorkAvailable);

				// TODO: Check this!
				waitInWorkAvailable.setSync(syncExecuteAvailable);
				waitInWorkAvailable.setLastMe(OtawaSync.getOrCreate(otawaProgram, last_sync_workers)); // OtawaSync.BEGIN
				waitInWorkAvailable.setLastEm(OtawaSync.getOrCreate(otawaProgram, last_sync_main)); // OtawaSync.BEGIN
				waitInWorkAvailable.setLastMeInLoop(syncThreadDone);
				waitInWorkAvailable.setLastEmInLoop(syncExecuteDone);

				// sync: tas_thread_work_done_barrier
				OtawaThreadInSync threadInWorkDone = syncThreadDone.getThreadOrCreate(nonMainThreadIds);
				OtawaWait waitInWorkDone = new OtawaWait(mainThreadId);
				waitInWorkDone.setComment("wait for task parallelism skeleton \"" + description + "\" (id = "
						+ parallelismId + ") [T " + nonMainThreadIds + "]");

				threadInWorkDone.addWait(waitInWorkDone);

				waitInWorkDone.setSync(syncExecuteDone);
				waitInWorkDone.setLastMe(syncThreadAvailable);
				waitInWorkDone.setLastEm(syncExecuteAvailable);

				otawaProgram.addSync(syncExecuteAvailable);
				otawaProgram.addSync(syncExecuteDone);

				// Finalize skeleton if necessary
				if (parallelism.getFinalizeId() != null) {
					createSkeletonFinalize(parallelism);
				}
			}
		}
	}

	private void createSkeletonFinalize(TasAbstractSkeleton parallelism) {

		String description = parallelism.getDescription();
		String parallelismId = parallelism.getFinalizeId();

		String[] threadIds = createComposedThreadRefIds(parallelism.getThreads(), THREAD_DELIMETER);
		String mainThreadId = threadIds[0];
		String nonMainThreadIds = threadIds[1];

		// Syncs
		OtawaSync syncExecuteAvailable = OtawaSync.getOrCreate(otawaProgram, parallelismId
				+ ".tas_abstract_finalize_work_available_barrier");
		syncExecuteAvailable.setInloopId("tas_taskparallel_execute_loop");

		syncExecuteAvailable.setComment("tas_abstract_finalize for skeleton " + "\"" + description + "\" (id = "
				+ parallelismId + ")" + " - barrier for main and worker threads - I");

		OtawaSync syncExecuteDone = OtawaSync.getOrCreate(otawaProgram, parallelismId
				+ ".tas_abstract_finalize_work_done_barrier");
		OtawaSync syncThreadDone_execute = OtawaSync.getOrCreate(otawaProgram, parallelism.getId()
				+ ".tas_abstract_finalize_work_done_barrier");

		// sync: tas_taskparallel_execute_work_available_barrier
		OtawaThreadInSync threadInExecuteAvailable = syncExecuteAvailable.getThreadOrCreate(mainThreadId);

		OtawaWait waitInExecuteAvailable = new OtawaWait(nonMainThreadIds);
		threadInExecuteAvailable.addWait(waitInExecuteAvailable);

		// TODO: Check this!
		waitInExecuteAvailable.setSync(syncThreadAvailable);
		waitInExecuteAvailable.setLastMe(syncThreadDone_execute);
		waitInExecuteAvailable.setLastEm(syncThreadDone);
		// waitInExecuteAvailable.setLastMeInLoop(syncExecuteDone);
		// waitInExecuteAvailable.setLastEmInLoop(syncThreadDone);

		// sync: tas_taskparallel_execute_work_done_barrier
		OtawaThreadInSync threadInExecuteDone = syncExecuteDone.getThreadOrCreate(mainThreadId);

		OtawaWait waitInExecuteDone = new OtawaWait(nonMainThreadIds);
		threadInExecuteDone.addWait(waitInExecuteDone);

		waitInExecuteDone.setSync(syncThreadDone);
		waitInExecuteDone.setLastMe(syncExecuteAvailable);
		waitInExecuteDone.setLastEm(syncThreadAvailable);

		// sync: tas_thread_work_available_barrier
		OtawaThreadInSync threadInWorkAvailable = syncThreadAvailable.getThreadOrCreate(nonMainThreadIds);
		OtawaWait waitInWorkAvailable = new OtawaWait(mainThreadId);
		waitInWorkAvailable.setComment("tas_abstract_finalize for skeleton " + "\"" + description + "\" (id = "
				+ parallelismId + ")" + " - barrier for main and worker threads - II");
		threadInWorkAvailable.addWait(waitInWorkAvailable);

		waitInWorkAvailable.setSync(syncExecuteAvailable);
		waitInWorkAvailable.setLastMe(syncThreadDone);
		waitInWorkAvailable.setLastEm(OtawaSync.getOrCreate(otawaProgram, parallelism.getExecuteWorkDoneBarrierId())); // ERROR!
		// waitInWorkAvailable.setLastMeInLoop(syncThreadDone);
		// waitInWorkAvailable.setLastEmInLoop(syncExecuteDone);

		// sync: tas_thread_work_done_barrier
		OtawaThreadInSync threadInWorkDone = syncThreadDone.getThreadOrCreate(nonMainThreadIds);
		OtawaWait waitInWorkDone = new OtawaWait(mainThreadId);
		waitInWorkDone.setComment("tas_abstract_finalize for skeleton " + "\"" + description + "\" (id = "
				+ parallelismId + ")" + " - barrier for main and worker threads - III");
		threadInWorkDone.addWait(waitInWorkDone);

		waitInWorkDone.setSync(syncExecuteDone);
		waitInWorkDone.setLastMe(syncThreadAvailable);
		waitInWorkDone.setLastEm(syncExecuteAvailable);

		otawaProgram.addSync(syncExecuteAvailable);
		otawaProgram.addSync(syncExecuteDone);

	}

	/** Init for any skeleton instance */
	private void createSkeletonInit(TasParallelism parallelism) {

		OtawaSync main_sync_1st = OtawaSync.getOrCreate(otawaProgram, parallelism.getInitId()
				+ ".tas_abstract_init_pattern_assigned_barrier_idle");
		OtawaSync worker_sync_1st = OtawaSync.getOrCreate(otawaProgram, "tas_thread_pattern_assigned_barrier_idle");

		OtawaSync main_sync_2nd = OtawaSync.getOrCreate(otawaProgram, parallelism.getInitId()
				+ ".tas_abstract_init_pattern_assigned_barrier");
		OtawaSync worker_sync_2nd = OtawaSync.getOrCreate(otawaProgram, "tas_thread_pattern_assigned_barrier");

		if (true) {
			// Create sync tags for main and worker threads

			String[] threadIds = createComposedThreadRefIds(parallelism.getThreads(), THREAD_DELIMETER);

			String mainThreadId = threadIds[0];
			String[] nonMainThreadIds = threadIds[1].split(THREAD_DELIMETER);

			// Calculate last common sync point of all threads...
			// To do this, you need to find (a) the init point if it is
			// generated or (b) another skeleton called before this one where
			// all here active threads were involved, too.

			// add wait tags for main thread waiting for the workers
			OtawaThreadInSync init_main_thread = main_sync_1st.getThreadOrCreate(mainThreadId);

			// For each worker add the necessary tags...
			for (String worker_id : nonMainThreadIds) {
				String last_sync_main = null;
				String last_sync_workers = null;

				if (!parallelism.getExecutedAfter().equals("BEGIN")) {
					// Skeleton is executed after another skeleton.
					// TasParallelism executed_before =
					// parallelismMap.get(parallelism.getExecutedAfter());

					// Now it needs to be checked for the worker if its thread
					// was already in use. If yes, the last sync point is
					// tas_thread_work_done_barrier skeleton, else
					// tas_thread_worker_init_barrier is the last sync point.

					ArrayList<TasParallelism> completed_skeletons = this.getCompletedSkeletons(parallelism);
					for (TasParallelism completed : completed_skeletons) {
						if (completed.hasThread(worker_id) && completed.hasThread(mainThreadId)) {
							last_sync_workers = "tas_thread_work_done_barrier";
							last_sync_main = completed.getFinalizeId() + "."
									+ "tas_abstract_finalize_work_done_barrier";
						}
					}

					if (last_sync_workers == null) {
						// Meet at the initial setup barrier for all workers
						last_sync_workers = "tas_thread_worker_init_barrier";
						last_sync_main = getFirstTasParallelism().getInitId() + "."
								+ "tas_abstract_init_worker_init_barrier";
					}
				} else if (!parallelism.getCalledBy().equals("BEGIN")) {
					// Skeleton is executed by another skeleton

					// Now it needs to be checked for the worker if its thread
					// was already in use. If yes, the last sync point is
					// tas_thread_work_done_barrier skeleton, else
					// tas_thread_worker_init_barrier is the last sync point.

					ArrayList<TasParallelism> completed_skeletons = this.getCompletedSkeletons(parallelism);
					for (TasParallelism completed : completed_skeletons) {
						if (completed.hasThread(worker_id) && completed.hasThread(mainThreadId)) {
							last_sync_workers = "tas_thread_work_done_barrier";
							last_sync_main = completed.getFinalizeId() + "."
									+ "tas_abstract_finalize_work_done_barrier";
						}
					}

					if (last_sync_workers == null) {
						// Meet at the initial setup barrier for all workers
						last_sync_workers = "tas_thread_worker_init_barrier";
						last_sync_main = getFirstTasParallelism().getInitId() + "."
								+ "tas_abstract_init_worker_init_barrier";
					}
				} else {
					// Skeleton is executed as first skeleton
					last_sync_main = parallelism.getInitId() + "." + "tas_abstract_init_worker_init_barrier";
					last_sync_workers = "tas_thread_worker_init_barrier";
				}

				OtawaWait my_wait_1 = new OtawaWait(
						worker_id, worker_sync_1st,
						last_sync_main,
						last_sync_workers
						);
				init_main_thread.addWait(my_wait_1);

				// add wait tags for the worker thread waiting for the main
				// thread
				OtawaThreadInSync init_worker_thread = worker_sync_1st.getThreadOrCreate(worker_id);

				OtawaWait my_wait_2 = new OtawaWait(
						mainThreadId, main_sync_1st,
						last_sync_workers,
						last_sync_main
						);
				init_worker_thread.addWait(my_wait_2);
			}

			// Add new sync tags to OTAWA program tag
			main_sync_1st.setComment("tas_abstract_init for skeleton " + parallelism.getId()
					+ " - bilateral barrier for each worker - I");
			otawaProgram.addSync(main_sync_1st);
			otawaProgram.addSync(worker_sync_1st);
		}

		if (true) {
			// Create sync tags for main and worker threads

			String[] threadIds = createComposedThreadRefIds(parallelism.getThreads(), THREAD_DELIMETER);

			String mainThreadId = threadIds[0];
			String[] nonMainThreadIds = threadIds[1].split(THREAD_DELIMETER);

			// add wait tags for main thread waiting for the workers
			OtawaThreadInSync init_main_thread = main_sync_2nd.getThreadOrCreate(mainThreadId);

			for (String worker_id : nonMainThreadIds) {
				OtawaWait my_wait = new OtawaWait(worker_id, worker_sync_2nd, main_sync_1st.getId(),
						worker_sync_1st.getId());
				init_main_thread.addWait(my_wait);
			}

			// add wait tags for the worker thread waiting for the main thread
			for (String worker_id : nonMainThreadIds) {
				OtawaThreadInSync init_worker_thread = worker_sync_2nd.getThreadOrCreate(worker_id);

				OtawaWait my_wait = new OtawaWait(mainThreadId, main_sync_2nd, worker_sync_1st.getId(),
						main_sync_1st.getId());
				init_worker_thread.addWait(my_wait);
			}

			// Add new sync tags to OTAWA program tag
			main_sync_2nd.setComment("tas_abstract_init for skeleton " + parallelism.getId()
					+ " - bilateral barrier for each worker - II");
			otawaProgram.addSync(main_sync_2nd);
			otawaProgram.addSync(worker_sync_2nd);
		}

	}

	/**
	 * Calculates the set of all skeletons, which have been completed before execution of a given skeleton instance.
	 * 
	 * In the call tree of the skeletons, this should be (a) all skeletons on the path to the root node and (b) the
	 * skeleton instances they execute. In other words: All skeleton instances with lower depth in the tree then the
	 * given skeleton.
	 * 
	 * Sorting is important: the first element is the closest to the reference point.
	 */
	ArrayList<TasParallelism> getCompletedSkeletons(TasParallelism parallelism) {
		ArrayList<TasParallelism> result = new ArrayList<TasParallelism>();

		TasParallelism pointer = parallelism;

		while (pointer != null) {
			TasParallelism next_pointer = null;

			// If this skeleton is executed by another one: Continue with
			// inspecting the called skeleton instance.
			if (!pointer.getCalledBy().equals("BEGIN")) {
				next_pointer = parallelismMap.get(pointer.getCalledBy());
			}

			// If this skeleton is executed after another one: add the other
			// one, it has been completed! Also add everything which is called
			// by the other one (whole branch).
			String prev = pointer.getExecutedAfter();
			if (!prev.equals("BEGIN")) {
				result.add(parallelismMap.get(prev));

				for (TasParallelism item : getSubordinatedSkeletons(parallelismMap.get(prev)))
					result.add(item);

				// Set pointer to the element, which was executed before the
				// current one
				next_pointer = parallelismMap.get(prev);
			} else {
				// There is no element, which was executed before
			}

			// Set pointer to next_pointer. If it null, then we are done!
			pointer = next_pointer;
		}

		return result;
	}

	/**
	 * Calculate set of all Skeleton instances, which are called by a given Skeleton in any way. In other words: all
	 * Skeletons, which are called by or executed after one of the called skeletons.
	 */
	private HashSet<TasParallelism> getSubordinatedSkeletons(TasParallelism parallelism) {
		HashSet<TasParallelism> result = new HashSet<TasParallelism>();

		// Queue for items to be processed
		LinkedList<TasParallelism> to_visit = new LinkedList<TasParallelism>();

		// Start: Add skeletons called by the given skeleton to the queue
		for (TasParallelism item : this.getCalledTasParallelisms(parallelism)) {
			to_visit.add(item);
		}

		// Step: While the queue is not empty...
		while (!to_visit.isEmpty()) {
			// Get element from queue
			TasParallelism item = to_visit.pop();

			// Add element to result set
			result.add(item);

			// Add all called skeleton instances to queue
			for (TasParallelism foo : this.getCalledTasParallelisms(item)) {
				to_visit.add(foo);
			}

			// Add succeeding skeleton instance to queue
			if (getNextTasParallelism(item) != null) {
				to_visit.add(getNextTasParallelism(item));
			}
		}

		return result;
	}

	/**
	 * Converts all data parallelism tags to corresponding OTAWA XML code
	 * 
	 * @param tasProgram
	 *            the TAS XML as deserialized object
	 * @param otawaProgram
	 *            the resulting OTAWA XML as deserialized object
	 */
	private void convertDataParallelisms(TasProgram tasProgram) {

		for (TasAbstractSkeleton parallelism : tasProgram.getDataParallelisms()) {

			String parallelismId = parallelism.getId();
			String description = parallelism.getDescription();

			String[] threadIds = createComposedThreadRefIds(parallelism.getThreads(), THREAD_DELIMETER);
			String mainThreadId = threadIds[0];
			String nonMainThreadIds = threadIds[1];
			// Only generate something if there are more than 1 thread
			if (nonMainThreadIds != null) {
				// Add tags for initialization
				if (parallelism.getInitId() != null) {
					createSkeletonInit(parallelism);
				}

				// Calculate last common sync point of all threads...
				// To do this, you need to find (a) the init point if it is
				// generated or (b) another skeleton called before this one
				// where all here active threads were involved, too.

				ArrayList<TasParallelism> completed = this.getCompletedSkeletons(parallelism);
				TasParallelism last_skel_with_all_threads = null;
				for (TasParallelism item : completed) {
					boolean all_in = true;
					for (TasThreadRef thread : parallelism.getThreads()) {
						if (all_in && !item.hasThread(thread.getId()))
							all_in = false;
					}

					if (all_in) {
						last_skel_with_all_threads = item;
					}
				}

				String last_sync_workers = null;
				String last_sync_main = null;
				if (last_skel_with_all_threads != null && last_skel_with_all_threads.getFinalizeId() != null) {
					last_sync_main = last_skel_with_all_threads.getFinalizeId() + "."
							+ "tas_abstract_finalize_work_done_barrier";
					last_sync_workers = "tas_thread_work_done_barrier";
				} else if (last_skel_with_all_threads != null && last_skel_with_all_threads.getFinalizeId() == null) {
					last_sync_main = last_skel_with_all_threads.getExecuteWorkDoneBarrierId();
					last_sync_workers = "tas_thread_work_done_barrier";
				} else {
					last_sync_main = tasInitMainSync.getId();
					last_sync_workers = tasInitWorkerSync.getId();
				}

				// Syncs
				OtawaSync syncExecuteAvailable = OtawaSync.getOrCreate(otawaProgram,
						parallelism.getExecuteWorkAvailableBarrierId());
				syncExecuteAvailable.setInloopId("tas_dataparallel_execute_loop");
				syncExecuteAvailable.setComment("Main thread executing data parallelism \"" + description + "\" (id = "
						+ parallelismId + ")");
				OtawaSync syncExecuteDone = OtawaSync.getOrCreate(otawaProgram,
						parallelism.getExecuteWorkDoneBarrierId());

				// sync: tas_taskparallel_execute_work_available_barrier
				OtawaThreadInSync threadInExecuteAvailable = syncExecuteAvailable.getThreadOrCreate(mainThreadId);

				OtawaWait waitInExecuteAvailable = new OtawaWait(nonMainThreadIds);
				threadInExecuteAvailable.addWait(waitInExecuteAvailable);

				// TODO: Check this!
				waitInExecuteAvailable.setSync(syncThreadAvailable);
				waitInExecuteAvailable.setLastMe(OtawaSync.getOrCreate(otawaProgram, last_sync_main)); // OtawaSync.BEGIN
				waitInExecuteAvailable.setLastEm(OtawaSync.getOrCreate(otawaProgram, last_sync_workers)); // OtawaSync.BEGIN
				waitInExecuteAvailable.setLastMeInLoop(syncExecuteDone);
				waitInExecuteAvailable.setLastEmInLoop(syncThreadDone);

				// sync: tas_taskparallel_execute_work_done_barrier
				OtawaThreadInSync threadInExecuteDone = syncExecuteDone.getThreadOrCreate(mainThreadId);

				OtawaWait waitInExecuteDone = new OtawaWait(nonMainThreadIds);
				threadInExecuteDone.addWait(waitInExecuteDone);

				waitInExecuteDone.setSync(syncThreadDone);
				waitInExecuteDone.setLastMe(syncExecuteAvailable);
				waitInExecuteDone.setLastEm(syncThreadAvailable);

				// sync: tas_thread_work_available_barrier
				OtawaThreadInSync threadInWorkAvailable = syncThreadAvailable.getThreadOrCreate(nonMainThreadIds);
				OtawaWait waitInWorkAvailable = new OtawaWait(mainThreadId);
				waitInWorkAvailable.setComment("wait for data parallelism skeleton \"" + description + "\" (id = "
						+ parallelismId + ") [T " + nonMainThreadIds + "]");
				threadInWorkAvailable.addWait(waitInWorkAvailable);

				// TODO: Check this!
				waitInWorkAvailable.setSync(syncExecuteAvailable);
				waitInWorkAvailable.setLastMe(OtawaSync.getOrCreate(otawaProgram, last_sync_workers)); // OtawaSync.BEGIN
				waitInWorkAvailable.setLastEm(OtawaSync.getOrCreate(otawaProgram, last_sync_main)); // OtawaSync.BEGIN
				waitInWorkAvailable.setLastMeInLoop(syncThreadDone);
				waitInWorkAvailable.setLastEmInLoop(syncExecuteDone);

				// sync: tas_thread_work_done_barrier
				OtawaThreadInSync threadInWorkDone = syncThreadDone.getThreadOrCreate(nonMainThreadIds);
				OtawaWait waitInWorkDone = new OtawaWait(mainThreadId);
				waitInWorkDone.setComment("wait for data parallelism skeleton \"" + description + "\" (id = "
						+ parallelismId + ") [T " + nonMainThreadIds + "]");

				threadInWorkDone.addWait(waitInWorkDone);

				waitInWorkDone.setSync(syncExecuteDone);
				waitInWorkDone.setLastMe(syncThreadAvailable);
				waitInWorkDone.setLastEm(syncExecuteAvailable);

				otawaProgram.addSync(syncExecuteAvailable);
				otawaProgram.addSync(syncExecuteDone);

				// Finalize skeleton if necessary
				if (parallelism.getFinalizeId() != null) {
					createSkeletonFinalize(parallelism);
				}
			}
		}
	}

	/**
	 * Converts all piepline parallelism tags to corresponding OTAWA XML code
	 * 
	 * @param tasProgram
	 *            the TAS XML as deserialized object
	 * @param otawaProgram
	 *            the resulting OTAWA XML as deserialized object
	 */
	private void convertPipelineParallelisms(TasProgram tasProgram) {
		for (TasPipelineParallelism parallelism : tasProgram.getPipelineParallelisms()) {
			if (1 == 1)
				throw new UnsupportedOperationException("Pipeline must be adapted.");

			String parallelismId = parallelism.getId();
			String description = parallelism.getDescription();

			String[] threadIds = createComposedThreadRefIds(parallelism.getThreads(), THREAD_DELIMETER);
			String mainThreadId = threadIds[0];
			String nonMainThreadIds = threadIds[1];
			// Only generate something if there are more than 1 thread
			if (nonMainThreadIds != null) {
				// Add tags for initialization
				if (parallelism.getInitId() != null) {
					createSkeletonInit(parallelism);
				}

				// Calculate last common sync point of all threads...
				// To do this, you need to find (a) the init point if it is
				// generated or (b) another skeleton called before this one
				// where
				// all here active threads were involved, too.
				// TODO: Program this!
				String last_sync_main = tasInitMainSync.getId();
				String last_sync_workers = tasInitWorkerSync.getId();

				// Syncs
				OtawaSync syncExecuteAvailable = OtawaSync.getOrCreate(otawaProgram,
						parallelism.getExecuteWorkAvailableBarrierId());
				syncExecuteAvailable.setInloopId("tas_taskparallel_execute_loop");
				syncExecuteAvailable.setComment("Main thread executing pipeline parallelism \"" + description
						+ "\" (id = " + parallelismId + ")");
				OtawaSync syncExecuteDone = OtawaSync.getOrCreate(otawaProgram,
						parallelism.getExecuteWorkDoneBarrierId());

				// sync: tas_taskparallel_execute_work_available_barrier
				OtawaThreadInSync threadInExecuteAvailable = syncExecuteAvailable.getThreadOrCreate(mainThreadId);

				OtawaWait waitInExecuteAvailable = new OtawaWait(nonMainThreadIds);
				threadInExecuteAvailable.addWait(waitInExecuteAvailable);

				waitInExecuteAvailable.setSync(syncThreadAvailable);
				waitInExecuteAvailable.setLastMe(OtawaSync.getOrCreate(otawaProgram, last_sync_main)); // OtawaSync.BEGIN
				waitInExecuteAvailable.setLastEm(OtawaSync.getOrCreate(otawaProgram, last_sync_workers)); // OtawaSync.BEGIN
				waitInExecuteAvailable.setLastMeInLoop(syncExecuteDone);
				waitInExecuteAvailable.setLastEmInLoop(syncThreadDone);

				// sync: tas_taskparallel_execute_work_done_barrier
				OtawaThreadInSync threadInExecuteDone = syncExecuteDone.getThreadOrCreate(mainThreadId);

				OtawaWait waitInExecuteDone = new OtawaWait(nonMainThreadIds);
				threadInExecuteDone.addWait(waitInExecuteDone);

				waitInExecuteDone.setSync(syncThreadDone);
				waitInExecuteDone.setLastMe(syncExecuteAvailable);
				waitInExecuteDone.setLastEm(syncThreadAvailable);

				// sync: tas_thread_work_available_barrier
				OtawaThreadInSync threadInWorkAvailable = syncThreadAvailable.getThreadOrCreate(nonMainThreadIds);
				OtawaWait waitInWorkAvailable = new OtawaWait(mainThreadId);
				waitInWorkAvailable.setComment("for pipeline parallelism skeleton \"" + description + "\" (id = "
						+ parallelismId + ")");
				threadInWorkAvailable.addWait(waitInWorkAvailable);

				waitInWorkAvailable.setSync(syncExecuteAvailable);
				waitInWorkAvailable.setLastMe(OtawaSync.getOrCreate(otawaProgram, last_sync_workers)); // OtawaSync.BEGIN
				waitInWorkAvailable.setLastEm(OtawaSync.getOrCreate(otawaProgram, last_sync_main)); // OtawaSync.BEGIN
				waitInWorkAvailable.setLastMeInLoop(syncThreadDone);
				waitInWorkAvailable.setLastEmInLoop(syncExecuteDone);

				// sync: tas_thread_work_done_barrier
				OtawaThreadInSync threadInWorkDone = syncThreadDone.getThreadOrCreate(nonMainThreadIds);
				OtawaWait waitInWorkDone = new OtawaWait(mainThreadId);
				waitInWorkDone.setComment("for pipeline parallelism skeleton \"" + description + "\" (id = "
						+ parallelismId + ")");
				threadInWorkDone.addWait(waitInWorkDone);

				waitInWorkDone.setSync(syncExecuteDone);
				waitInWorkDone.setLastMe(syncThreadAvailable);
				waitInWorkDone.setLastEm(syncExecuteAvailable);

				otawaProgram.addSync(syncExecuteAvailable);
				otawaProgram.addSync(syncExecuteDone);

				// Finalize skeleton if necessary
				if (parallelism.getFinalizeId() != null) {
					createSkeletonFinalize(parallelism);
				}
			}
		}
	}

	/**
	 * Converts all thread tags to corresponding OTAWA XML code
	 * 
	 * @param tasProgram
	 *            the TAS XML as deserialized object
	 * @param otawaProgram
	 *            the resulting OTAWA XML as deserialized object
	 */
	private void convertThreads(TasProgram tasProgram) {
		for (TasThread tasThread : tasProgram.getThreads()) {
			OtawaThread otawaThread = new OtawaThread(tasThread.getId());
			otawaThread.setRoutineId(tasThread.getRoutine());
			otawaThread.setMapping(tasThread.getCluster(), tasThread.getCore());
			otawaProgram.addThread(otawaThread);
		}
	}

	/**
	 * Moves all CSections from the TAS XML to the OTAWA XML. Also creates additional csections as needed.
	 * 
	 * @param tasProgram
	 *            the TAS XML as deserialized object
	 * @param otawaProgram
	 *            the resulting OTAWA XML as deserialized object
	 */
	private void convertCSections(TasProgram tasProgram) {
		// create additional csection if at least one skeleton exists
		if (tasProgram.getDataParallelisms().size() > 0 || tasProgram.getTaskParallelisms().size() > 0
				|| tasProgram.getPipelineParallelisms().size() > 0) {
			CSection cs = new CSection(
					"tas_worker_get_worker_available_lock tas_get_workers_available_worker_available_lock tas_worker_release_worker_available_lock tas_thread_worker_available_lock");
			TasObjectWithId[] ids = new TasObjectWithId[0];
			ids = tasProgram.getThreads().toArray(ids);
			cs.setThreadId(createComposedIds(ids, THREAD_DELIMETER));
			cs.setComment("additional csection for skeletons");
			otawaProgram.addCSection(cs);
		}
		for (CSection cs : tasProgram.getcSections()) {
			cs.setComment(cs.getDescription());
			cs.setDescription(null);
			otawaProgram.addCSection(cs);
		}
	}

	/**
	 * Calculates the id of the main thread and a comma separated list of the ids of worker threads.
	 * 
	 * @param threads
	 *            The threads of a parallelism pattern
	 * @return An array with the id, with index 0: id of the main thread and index 1: comma separated ids of the worker
	 *         threads
	 */
	private static String[] createComposedThreadRefIds(List<TasThreadRef> threads, String delimeter) {
		String[] result = new String[2];

		String mainThreadId = null;
		String nonMainThreadIds = null;

		TasThreadRef[] threadIds = new TasThreadRef[threads.size() - 1];
		int index = 0;

		for (TasThreadRef tasThreadRef : threads) {
			if (equalsNullSafe(tasThreadRef.getMain(), "1")) {
				mainThreadId = tasThreadRef.getId();
			} else {
				threadIds[index++] = tasThreadRef;
			}
		}
		if (threadIds.length > 0) {
			Arrays.sort(threadIds);
			nonMainThreadIds = createComposedIds(threadIds, delimeter);
		}

		result[0] = mainThreadId;
		result[1] = nonMainThreadIds;
		return result;
	}

	private static String[] createComposedThreadIds(List<TasThread> threads, String delimeter) {
		String[] result = new String[2];

		String mainThreadId = null;
		String nonMainThreadIds = null;

		TasThread[] threadIds = new TasThread[threads.size() - 1];
		int index = 0;

		for (TasThread tasThread : threads) {
			if (!equalsNullSafe(tasThread.getRoutine(), WORKER_THREAD_FUNCTION)) {
				mainThreadId = tasThread.getId();
			} else {
				threadIds[index++] = tasThread;
			}
		}
		if (threadIds.length > 0) {
			Arrays.sort(threadIds);
			nonMainThreadIds = createComposedIds(threadIds, delimeter);
		}

		result[0] = mainThreadId;
		result[1] = nonMainThreadIds;
		return result;
	}

	/**
	 * Creates a comma separated list of all ids of the given id object array. These are typically TasThread or
	 * TasThreadRef objects
	 * 
	 * @param ids
	 *            An array of id objects
	 * @param delimeter
	 *            delimiter to use between each id
	 * @return the created comma separated string
	 */
	private static String createComposedIds(TasObjectWithId[] ids, String delimiter) {
		StringBuilder result = new StringBuilder();

		for (TasObjectWithId thread : ids) {
			result.append(thread.getId());
			result.append(delimiter);
		}
		result.delete(result.length() - 1, result.length());

		return result.toString();
	}

	/**
	 * Generates and returns a map that matches all parallelisms to their ids.
	 * 
	 * @param tasProgram
	 *            The representation of the TAS XML file.
	 * @return A map with that has all parallelisms as values with their ids as keys.
	 */
	private static Map<String, TasParallelism> generatePatternMap(TasProgram tasProgram) {
		Map<String, TasParallelism> result = new HashMap<String, TasParallelism>();

		for (TasTaskParallelism parallelism : tasProgram.getTaskParallelisms()) {
			result.put(parallelism.getId(), parallelism);
		}
		for (TasDataParallelism parallelism : tasProgram.getDataParallelisms()) {
			result.put(parallelism.getId(), parallelism);
		}
		for (TasPipelineParallelism parallelism : tasProgram.getPipelineParallelisms()) {
			result.put(parallelism.getId(), parallelism);
		}

		return result;
	}

	/**
	 * Compares s1 and s2 on equality in a null safe manner. s1 and s2 are equal if both s1 and s2 are null or both are
	 * not null and s1.equals(s2) returns true.
	 * 
	 * @param s1
	 *            The first string to compare
	 * @param s2
	 *            the second string to compare to
	 * @return true, of both are equal, false otherwise
	 */
	public static boolean equalsNullSafe(String s1, String s2) {
		boolean result;
		if (s1 == null && s2 == null) {
			result = true;
		} else if (s1 != null && s2 == null) {
			result = false;
		} else if (s1 == null && s2 != null) {
			result = false;
		} else {
			result = s1.equals(s2);
		}
		return result;
	}
}

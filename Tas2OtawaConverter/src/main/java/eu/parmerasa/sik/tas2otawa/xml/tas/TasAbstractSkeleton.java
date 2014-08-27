package eu.parmerasa.sik.tas2otawa.xml.tas;

import java.util.LinkedList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import eu.parmerasa.sik.tas2otawa.xml.Commentable;

public abstract class TasAbstractSkeleton extends Commentable implements TasParallelism {

	@Attribute(name = "id")
	private final String id;
	@Attribute(name = "finalize_id", required = false)
	private String finalizeId;
	@Attribute(name = "init_id", required = false)
	private String initId;
	@Attribute(name = "description")
	private String description;
	@Attribute(name = "main_as_worker")
	private String mainAsWorker;
	@Attribute(name = "executed_after", empty = "BEGIN", required = false)
	private String executedAfter;
	@Attribute(name = "called_by", empty = "BEGIN", required = false)
	private String calledBy;
	@ElementList(name = "threads")
	private LinkedList<TasThreadRef> threads = new LinkedList<TasThreadRef>();

	public TasAbstractSkeleton(@Attribute(name = "id") String id) {
		this.id = id;
	}

	/**
	 * @return the mainAsWorker
	 */
	public String getMainAsWorker() {
		return mainAsWorker;
	}

	/**
	 * @param mainAsWorker
	 *            the mainAsWorker to set
	 */
	public void setMainAsWorker(String mainAsWorker) {
		this.mainAsWorker = mainAsWorker;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFinalizeId() {
		return finalizeId;
	}

	/**
	 * @param finalizeId
	 */
	public void setFinalizeId(String finalizeId) {
		this.finalizeId = finalizeId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getInitId() {
		return initId;
	}

	/**
	 * @param initId
	 *            ID for initialization of skeleton
	 */
	public void setInitId(String initId) {
		this.initId = initId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public void addThread(TasThread thread) {
		TasThreadRef threadRef = new TasThreadRef(thread.getId());
		threads.add(threadRef);
	}

	public void addThread(TasThread thread, String main) {
		TasThreadRef threadRef = new TasThreadRef(thread.getId());
		threadRef.setMain(main);
		threads.add(threadRef);
	}

	/**
	 * @return the threads
	 */
	public LinkedList<TasThreadRef> getThreads() {
		return threads;
	}

	/**
	 * @param threads
	 *            the threads to set
	 */
	public void setThreads(LinkedList<TasThreadRef> threads) {
		this.threads = threads;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCalledBy() {
		return calledBy;
	}

	/**
	 * Sets calledBy, see {@link TasParallelism#getCalledBy()}
	 * 
	 * @param calledBy
	 *            the calledBy to set
	 */
	public void setCalledBy(String calledBy) {
		this.calledBy = calledBy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExecutedAfter() {
		return executedAfter;
	}

	/**
	 * Sets executedAfter, see {@link TasParallelism#getExecutedAfter()}
	 * 
	 * @param executedAfter
	 *            the executedAfter to set
	 */
	public void setExecutedAfter(String executedAfter) {
		this.executedAfter = executedAfter;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":" + this.getId();
	}

	@Override
	public int compareTo(TasObjectWithId o) {
		return this.getId().compareTo(o.getId());
	}

	public abstract String getExecuteWorkAvailableBarrierId();

	public abstract String getExecuteWorkDoneBarrierId();

	/**
	 * {@inheritDoc}
	 */
	public boolean hasThread(String worker_id) {
		for (TasThreadRef thread : getThreads()) {
			if (thread.getId().equals(worker_id))
				return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public TasThreadRef getThread(String worker_id) {
		for (TasThreadRef thread : getThreads()) {
			if (thread.getId().equals(worker_id))
				return thread;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TasThreadRef getMainThread() {
		for (TasThreadRef thread : getThreads()) {
			if (thread.getMain().equals("1"))
				return thread;
		}
		return null;
	}

}
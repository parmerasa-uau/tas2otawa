package eu.parmerasa.sik.tas2otawa.xml.tas;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Represents a tas_dataparallelism tag in the TAS XML
 * 
 * @author Rolf Kiefhaber
 */
@Root(name = "tas_dataparallelism")
public class TasDataParallelism extends TasAbstractSkeleton {

	@Attribute(name = "nr_args")
	private int numberOfArguments;
	@Element(name = "task")
	private TasTask task;

	/**
	 * @param id
	 */
	public TasDataParallelism(@Attribute(name = "id") String id) {
		super(id);
	}

	/**
	 * @return the numberOfArguments
	 */
	public int getNumberOfArguments() {
		return numberOfArguments;
	}

	/**
	 * @param numberOfArguments
	 *            the numberOfArguments to set
	 */
	public void setNumberOfArguments(int numberOfArguments) {
		this.numberOfArguments = numberOfArguments;
	}

	/**
	 * @param task
	 *            the task to set
	 */
	public void setTask(TasTask task) {
		this.task = task;
	}

	/**
	 * @return the task
	 */
	public TasTask getTask() {
		return task;
	}

	@Override
	public String getExecuteWorkAvailableBarrierId() {
		return this.getId() + ".tas_dataparallel_execute_work_available_barrier";
	}

	@Override
	public String getExecuteWorkDoneBarrierId() {
		return this.getId() + ".tas_dataparallel_execute_work_done_barrier";
	}

	// parallelismId + ".tas_dataparallel_execute_work_available_barrier"
}

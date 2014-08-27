package eu.parmerasa.sik.tas2otawa.xml.tas;

import java.util.LinkedList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "tas_taskparallelism")
public class TasTaskParallelism extends TasAbstractSkeleton {

	@ElementList(name = "tasks")
	private LinkedList<TasTask> tasks = new LinkedList<TasTask>();

	/**
	 * @param id
	 */
	public TasTaskParallelism(@Attribute(name = "id") String id) {
		super(id);
	}

	/**
	 * @return the task
	 */
	public List<TasTask> getTask() {
		return tasks;
	}

	public void addTask(TasTask task) {
		tasks.add(task);
	}

	/**
	 * @param task
	 *            the task to set
	 */
	public void setTask(LinkedList<TasTask> tasks) {
		this.tasks = tasks;
	}

	@Override
	public String getExecuteWorkAvailableBarrierId() {
		return this.getId() + ".tas_taskparallel_execute_work_available_barrier";
	}

	@Override
	public String getExecuteWorkDoneBarrierId() {
		return this.getId() + ".tas_taskparallel_execute_work_done_barrier";
	}
}

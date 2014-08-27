package eu.parmerasa.sik.tas2otawa.xml.tas;

import java.util.LinkedList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Represents a tas_pipelineparallelism tag in the TAS XML
 * 
 * @author Rolf Kiefhaber
 */
@Root(name = "tas_pipelineparallelism")
public class TasPipelineParallelism extends TasAbstractSkeleton {

	@Attribute(name = "iterations")
	private int iterations;

	@ElementList(name = "tasks")
	private LinkedList<TasTask> tasks = new LinkedList<TasTask>();

	/**
	 * @param id
	 */
	public TasPipelineParallelism(@Attribute(name = "id") String id) {
		super(id);
	}

	/**
	 * @return the iterations
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * @param iterations
	 *            the iterations to set
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	/**
	 * @return the tasks
	 */
	public LinkedList<TasTask> getTasks() {
		return tasks;
	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	public void setTasks(LinkedList<TasTask> tasks) {
		this.tasks = tasks;
	}

	public void addTask(TasTask task) {
		tasks.add(task);
	}

	@Override
	public String getExecuteWorkAvailableBarrierId() {
		return this.getId() + ".tas_pipeline_tp_execute.tas_taskparallel_execute_work_available_barrier";
	}

	@Override
	public String getExecuteWorkDoneBarrierId() {
		return this.getId() + ".tas_pipeline_tp_execute.tas_taskparallel_execute_work_done_barrier";
	}
}

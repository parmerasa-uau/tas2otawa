package eu.parmerasa.sik.tas2otawa.xml.tas;

import java.util.LinkedList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.NamespaceList;
import org.simpleframework.xml.Root;

import eu.parmerasa.sik.tas2otawa.xml.CSection;
import eu.parmerasa.sik.tas2otawa.xml.Commentable;

@Root(name = "program")
@NamespaceList({
		@Namespace(reference = "http://www.w3schools.com"),
		@Namespace(reference = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi") })
public class TasProgram extends Commentable {

	@Attribute(name = "schemaLocation")
	@Namespace(reference = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
	private static final String XSI_SCHEMA = "http://www.w3schools.com tas.xsd";

	@ElementList(name = "threads")
	private LinkedList<TasThread> threads = new LinkedList<TasThread>();

	@ElementList(name = "tas_taskparallelisms", required = false)
	private LinkedList<TasTaskParallelism> taskParallelisms = new LinkedList<TasTaskParallelism>();

	@ElementList(name = "tas_dataparallelisms", required = false)
	private LinkedList<TasDataParallelism> dataParallelisms = new LinkedList<TasDataParallelism>();

	@ElementList(name = "tas_pipelineparallelisms", required = false)
	private LinkedList<TasPipelineParallelism> pipelineParallelisms = new LinkedList<TasPipelineParallelism>();

	@ElementList(name = "csections", required = false)
	private LinkedList<CSection> cSections = new LinkedList<CSection>();

	public void addThread(TasThread thread) {
		threads.add(thread);
	}

	public void addTaskParallelism(TasTaskParallelism taskParallelism) {
		taskParallelisms.add(taskParallelism);
	}

	public void addDataParallelism(TasDataParallelism dataParallelism) {
		dataParallelisms.add(dataParallelism);
	}

	/**
	 * @return the threads
	 */
	public LinkedList<TasThread> getThreads() {
		return threads;
	}

	/**
	 * @param threads
	 *            the threads to set
	 */
	public void setThreads(LinkedList<TasThread> threads) {
		this.threads = threads;
	}

	/**
	 * @return the prarallelisms
	 */
	public LinkedList<TasTaskParallelism> getTaskParallelisms() {
		return taskParallelisms;
	}

	/**
	 * @param prarallelisms
	 *            the prarallelisms to set
	 */
	public void setTaskParallelisms(LinkedList<TasTaskParallelism> prarallelisms) {
		this.taskParallelisms = prarallelisms;
	}

	/**
	 * @return the dataParallelisms
	 */
	public LinkedList<TasDataParallelism> getDataParallelisms() {
		return dataParallelisms;
	}

	/**
	 * @param dataParallelisms
	 *            the dataParallelisms to set
	 */
	public void setDataParallelisms(
			LinkedList<TasDataParallelism> dataParallelisms) {
		this.dataParallelisms = dataParallelisms;
	}

	/**
	 * @return the pipelineParallelisms
	 */
	public LinkedList<TasPipelineParallelism> getPipelineParallelisms() {
		return pipelineParallelisms;
	}

	/**
	 * @param pipelineParallelisms
	 *            the pipelineParallelisms to set
	 */
	public void setPipelineParallelisms(
			LinkedList<TasPipelineParallelism> pipelineParallelisms) {
		this.pipelineParallelisms = pipelineParallelisms;
	}

	/**
	 * @return the cSections
	 */
	public LinkedList<CSection> getcSections() {
		return cSections;
	}

	/**
	 * @param cSections
	 *            the cSections to set
	 */
	public void setcSections(LinkedList<CSection> cSections) {
		this.cSections = cSections;
	}

	/**
	 * 
	 * @param cSection
	 */
	public void addCSection(CSection cSection) {
		cSections.add(cSection);
	}

	/** Checks if the program has a thread with a given ID */
	public boolean hasThread(TasThreadRef thread) {
		for (TasThread th : this.getThreads()) {
			if (th.getId().equals(thread.getId()))
				return true;
		}
		return false;
	}

	/** Finds a thread by its ID */
	public TasThread getThreadById(String id) {
		for (TasThread th : this.getThreads()) {
			if (th.getId().equals(id))
				return th;
		}
		return null;
	}
}

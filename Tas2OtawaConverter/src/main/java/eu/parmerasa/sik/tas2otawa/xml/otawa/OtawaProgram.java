package eu.parmerasa.sik.tas2otawa.xml.otawa;

import java.util.LinkedList;
import java.util.List;

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
public class OtawaProgram extends Commentable {

	@Attribute(name = "schemaLocation")
	@Namespace(reference = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
	private static final String XSI_SCHEMA = "http://www.w3schools.com annotations.xsd";

	@ElementList(inline = true)
	private List<OtawaThread> threads = new LinkedList<OtawaThread>();

	@ElementList(inline = true)
	private List<OtawaSync> syncs = new LinkedList<OtawaSync>();

	@ElementList(inline = true)
	private List<CSection> cSections = new LinkedList<CSection>();

	/**
	 * 
	 * @param thread
	 */
	public void addThread(OtawaThread thread) {
		threads.add(thread);
	}

	/**
	 * @return the threads
	 */
	public List<OtawaThread> getThreads() {
		return threads;
	}

	/**
	 * Adds a sync tag if it has not been added yet.
	 * 
	 * @param sync
	 */
	public void addSync(OtawaSync sync) {
		if (!syncs.contains(sync)) {
			syncs.add(sync);
			// System.out.println("INFO Added OtawaSync with ID " + sync);
		} else {
			// System.out.println("INFO Did not add OtawaSync with ID " + sync);
		}
	}

	/**
	 * @return the syncs
	 */
	public List<OtawaSync> getSyncs() {
		return syncs;
	}

	/**
	 * 
	 * @param cSection
	 */
	public void addCSection(CSection cSection) {
		cSections.add(cSection);
	}

	/**
	 * @return the cSections
	 */
	public List<CSection> getcSections() {
		return cSections;
	}

}

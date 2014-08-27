package eu.parmerasa.sik.tas2otawa.xml.otawa;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import eu.parmerasa.sik.tas2otawa.xml.Commentable;

@Root(name = "sync")
public class OtawaSync extends Commentable {

	public static final OtawaSync BEGIN = new OtawaSync("BEGIN");
	public static final OtawaSync UNCLEAR = new OtawaSync("UNCLEAR");

	@Attribute(name = "id")
	private final String id;

	@Element(name = "in_loop", required = false)
	private InLoop inloopId;

	@ElementList(inline = true)
	private List<OtawaThreadInSync> threadRefs = new LinkedList<OtawaThreadInSync>();

	public void addThread(OtawaThreadInSync threadRef) {
		threadRefs.add(threadRef);
	}

	/** Storage of all existing OtawaSync instances */
	private static HashMap<String, OtawaSync> instances = new HashMap<String, OtawaSync>();

	/** Get existing instance or create a new one */
	public static OtawaSync getOrCreate(OtawaProgram program, String id) {
		OtawaSync item = instances.get(id);
		if (item == null) {
			item = new OtawaSync(id);
			instances.put(id, item);
			// program.addSync(item);
			// System.out.println("INFO Created OtawaSync with ID " + id);
		}
		return item;
	}

	/**
	 * @param id
	 */
	private OtawaSync(@Attribute(name = "id") String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the inloopId
	 */
	public String getInloopId() {
		return inloopId.getId();
	}

	/**
	 * @param inloopId
	 *            the inloopId to set
	 */
	public void setInloopId(String inloopId) {
		if (this.inloopId == null) {
			this.inloopId = new InLoop(inloopId);
		} else {
			this.inloopId.setId(inloopId);
		}
	}

	/**
	 * @return the threads
	 */
	public List<OtawaThreadInSync> getThreads() {
		return threadRefs;
	}

	/**
	 * Class to represent the in_loop element. There is an explicit class to make the element fully optional, which is
	 * not possible with the @Path annotation.
	 * 
	 * @author Rolf Kiefhaber
	 */
	private static class InLoop {

		@Attribute(name = "id")
		private String id;

		/**
		 * @param id
		 */
		public InLoop(@Attribute(name = "id") String id) {
			this.id = id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}
	}

	/**
	 * Finds a thread tag and, if none is found, creates a new one and adds it
	 * 
	 * @param nonMainThreadIds
	 * @return
	 */
	public OtawaThreadInSync getThreadOrCreate(String nonMainThreadIds) {
		OtawaThreadInSync result = null;

		// System.out.println("Checking for " + nonMainThreadIds + " in " +
		// this.getId());

		for (OtawaThreadInSync foo : getThreads()) {
			// System.out.println("Checking " + nonMainThreadIds + "with " +
			// foo.getId());
			if (foo.getId().equals(nonMainThreadIds)) {
				result = foo;
				// System.out.println("  => Hit!");
				break;
			}
		}

		if (result == null) {
			// System.out.println("  => Miss!");
			result = new OtawaThreadInSync(nonMainThreadIds);
			addThread(result);
		}

		return result;
	}

	/** Returns ID as string representation */
	public String toString() {
		return this.getId();
	}
}

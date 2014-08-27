package eu.parmerasa.sik.tas2otawa.xml.otawa;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import eu.parmerasa.sik.tas2otawa.xml.Commentable;

@Root(name = "wait")
public class OtawaWait extends Commentable {

	@Attribute(name = "id")
	private final String id;

	@Element(name = "sync", required = false)
	private OtawaSyncRef sync;

	@Element(name = "last_sync", required = false)
	private OtawaSyncRef lastSync;

	@Element(name = "last_me", required = false)
	private OtawaSyncRef lastMe;

	@Element(name = "last_em", required = false)
	private OtawaSyncRef lastEm;

	@Element(name = "last_me_in_loop", required = false)
	private OtawaSyncRef lastMeInLoop;

	@Element(name = "last_em_in_loop", required = false)
	private OtawaSyncRef lastEmInLoop;

	/**
	 * @param id
	 */
	public OtawaWait(@Attribute(name = "id") String id) {
		this.id = id;
	}

	public OtawaWait(String threadIds, OtawaSync sync, String last_me,
			String last_em) {
		this(threadIds);
		this.setSync(sync);
		this.lastMe = new OtawaSyncRef(last_me);
		this.lastEm = new OtawaSyncRef(last_em);
	}

	/**
	 * @return the sync
	 */
	public String getSync() {
		return sync.getRef();
	}

	/**
	 * @param sync
	 *            the sync to set
	 */
	public void setSync(OtawaSync sync) {
		this.sync = new OtawaSyncRef(sync.getId());
	}

	/**
	 * @return the lastSync
	 */
	public String getLastSync() {
		return lastSync.getRef();
	}

	/**
	 * @param lastSync
	 *            the lastSync to set
	 */
	public void setLastSync(OtawaSync lastSync) {
		this.lastSync = new OtawaSyncRef(lastSync.getId());
	}

	/**
	 * @return the lastMe
	 */
	public String getLastMe() {
		return lastMe.getRef();
	}

	/**
	 * @param lastMe
	 *            the lastMe to set
	 */
	public void setLastMe(OtawaSync lastMe) {
		this.lastMe = new OtawaSyncRef(lastMe.getId());
	}

	/**
	 * @return the lastEm
	 */
	public String getLastEm() {
		return lastEm.getRef();
	}

	/**
	 * @param lastEm
	 *            the lastEm to set
	 */
	public void setLastEm(OtawaSync lastEm) {
		this.lastEm = new OtawaSyncRef(lastEm.getId());
	}

	/**
	 * @return the lastMeInLoop
	 */
	public String getLastMeInLoop() {
		return lastMeInLoop.getRef();
	}

	/**
	 * @param lastMeInLoop
	 *            the lastMeInLoop to set
	 */
	public void setLastMeInLoop(OtawaSync lastMeInLoop) {
		this.lastMeInLoop = new OtawaSyncRef(lastMeInLoop.getId());
	}

	/**
	 * @return the lastEmInLoop
	 */
	public String getLastEmInLoop() {
		return lastEmInLoop.getRef();
	}

	/**
	 * @param lastEmInLoop
	 *            the lastEmInLoop to set
	 */
	public void setLastEmInLoop(OtawaSync lastEmInLoop) {
		this.lastEmInLoop = new OtawaSyncRef(lastEmInLoop.getId());
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	private static class OtawaSyncRef extends Commentable {

		@Attribute(name = "ref")
		private final String ref;

		/**
		 * @param ref
		 */
		public OtawaSyncRef(@Attribute(name = "ref") String ref) {
			this.ref = ref;
		}

		/**
		 * @return the ref
		 */
		public String getRef() {
			return ref;
		}

	}
}

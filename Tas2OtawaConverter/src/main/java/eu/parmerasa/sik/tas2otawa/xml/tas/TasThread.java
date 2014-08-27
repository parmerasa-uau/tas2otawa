package eu.parmerasa.sik.tas2otawa.xml.tas;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import eu.parmerasa.sik.tas2otawa.xml.Commentable;

@Root(name = "thread")
public class TasThread extends Commentable implements TasObjectWithId {

	@Attribute(name = "cluster")
	private final String cluster;
	@Attribute(name = "core")
	private final String core;
	@Attribute(name = "id")
	private final String id;
	@Attribute(name = "routine")
	private final String routine;

	/**
	 * @param core
	 * @param cluster
	 * @param id
	 * @param main
	 */
	public TasThread(@Attribute(name = "cluster") String cluster,
			@Attribute(name = "core") String core,
			@Attribute(name = "id") String id,
			@Attribute(name = "routine") String routine) {
		this.cluster = cluster;
		this.core = core;
		this.id = id;
		this.routine = routine;
	}

	/**
	 * @return the core
	 */
	public String getCore() {
		return core;
	}

	/**
	 * @return the cluster
	 */
	public String getCluster() {
		return cluster;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the routine
	 */
	public String getRoutine() {
		return routine;
	}

	@Override
	public int compareTo(TasObjectWithId o) {
		return this.getId().compareTo(o.getId());
	}

}

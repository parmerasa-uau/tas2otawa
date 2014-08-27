package eu.parmerasa.sik.tas2otawa.xml.tas;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import eu.parmerasa.sik.tas2otawa.xml.Commentable;

@Root(name = "thread")
public class TasThreadRef extends Commentable implements TasObjectWithId {

	@Attribute(name = "ref")
	private final String id;
	@Attribute(name = "main", required = false)
	private String main;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            Value of attribute ID
	 */
	public TasThreadRef(@Attribute(name = "ref") String id) {
		this.id = id;
	}

	/**
	 * @return Value of attribute main
	 */
	public String getMain() {
		if (main == null)
			return "0";
		else
			return main;
	}

	/**
	 * @param main
	 *            the main to set
	 */
	public void setMain(String main) {
		this.main = main;
	}

	/**
	 * @return Value of attribute ID
	 */
	public String getId() {
		return id;
	}

	@Override
	public int compareTo(TasObjectWithId o) {
		return this.getId().compareTo(o.getId());
	}
}
package eu.parmerasa.sik.tas2otawa.xml.otawa;

import java.util.LinkedList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import eu.parmerasa.sik.tas2otawa.xml.Commentable;

@Root(name = "thread")
public class OtawaThreadInSync extends Commentable {

    @Attribute(name = "id")
    private final String id;

    @ElementList(inline = true, required = false)
    private List<OtawaWait> waits = new LinkedList<OtawaWait>();

    /**
     * @param id
     */
    public OtawaThreadInSync(@Attribute(name = "id") String id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    public void addWait(OtawaWait wait) {
        waits.add(wait);
    }

    /**
     * @param waits
     *            the waits to set
     */
    public void setWaits(List<OtawaWait> waits) {
        this.waits = waits;
    }

    /**
     * @return the waits
     */
    public List<OtawaWait> getWaits() {
        return waits;
    }

}

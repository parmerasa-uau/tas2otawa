/**
 * 
 */
package eu.parmerasa.sik.tas2otawa.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Representation class for csection xml tags. These tags are both used in TAS
 * and OTAWA XML files
 * 
 * @author Rolf Kiefhaber
 */
@Root(name = "csection")
public class CSection extends Commentable {

    @Attribute(name = "id")
    private final String id;

    @Path("thread")
    @Attribute(name = "id")
    private String threadsId;

    @Attribute(name = "description", required = false)
    private String description;
    
    /**
     * Initializes a new CSection object with the given id
     */
    public CSection(@Attribute(name = "id") String id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the threads
     */
    public String getThreadsId() {
        return threadsId;
    }

    /**
     * @param threadId the threadId to set
     */
    public void setThreadId(String threadId) {
        this.threadsId = threadId;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}

package eu.parmerasa.sik.tas2otawa.xml.tas;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import eu.parmerasa.sik.tas2otawa.xml.Commentable;

@Root(name = "task")
public class TasTask extends Commentable {

    @Attribute(name = "function")
    private final String function;
    @Attribute(name = "thread", required=false)
    private final String thread;

    /**
     * @param function
     * @param thread
     */
    public TasTask(@Attribute(name = "function") String function,
            @Attribute(name = "thread") String thread) {
        this.function = function;
        this.thread = thread;
    }

    /**
     * @return the function
     */
    public String getFunction() {
        return function;
    }

    /**
     * @return the thread
     */
    public String getThread() {
        return thread;
    }

}

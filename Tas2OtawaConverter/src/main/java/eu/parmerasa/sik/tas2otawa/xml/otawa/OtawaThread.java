package eu.parmerasa.sik.tas2otawa.xml.otawa;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import eu.parmerasa.sik.tas2otawa.xml.Commentable;

@Root(name = "thread")
public class OtawaThread extends Commentable {

    @Attribute(name = "id")
    private final String id;

    @Element(name = "routine")
    private Routine routine;

    @Element(name = "mapping", required = false)
    private Mapping mapping;

    /**
     * @param id
     */
    public OtawaThread(@Attribute(name = "id") String id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the routineId
     */
    public String getRoutineId() {
        return routine.getId();
    }

    /**
     * @param routineId
     *            the routineId to set
     */
    public void setRoutineId(String routineId) {
        Routine routine = new Routine(routineId);
        this.routine = routine;
    }

    /**
     * @param mapping
     *            the mapping to set
     */
    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    public void setMapping(String cluster, String core) {
        routine.setComment("<mapping cluster=\"" + cluster + "\" core=\""
                + core + "\" />");

        // Mapping mapping = new Mapping(cluster, core);
        // this.mapping = mapping;
    }

    public String getCluster() {
        return mapping.getCluster();
    }

    public String getCore() {
        return mapping.getCore();
    }

    /*
     * private classes
     */

    private static class Routine extends Commentable {

        @Attribute(name = "id")
        private final String id;

        /**
         * @param id
         */
        public Routine(@Attribute(name = "id") String id) {
            this.id = id;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

    }

    private static class Mapping extends Commentable {

        @Attribute(name = "cluster")
        private final String cluster;
        @Attribute(name = "core")
        private final String core;

        /**
         * @param cluster
         * @param core
         */
        public Mapping(@Attribute(name = "cluster") String cluster,
                @Attribute(name = "core") String core) {
            this.cluster = cluster;
            this.core = core;
        }

        /**
         * @return the cluster
         */
        public String getCluster() {
            return cluster;
        }

        /**
         * @return the core
         */
        public String getCore() {
            return core;
        }

    }
}

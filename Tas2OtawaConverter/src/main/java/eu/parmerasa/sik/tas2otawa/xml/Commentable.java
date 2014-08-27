package eu.parmerasa.sik.tas2otawa.xml;

import org.simpleframework.xml.Transient;

/**
 * Abstract superclass for all XML representation classes that have the ability
 * for an XML comment above their tag. Every Otawa XML class should be a
 * subclass of this class.
 * 
 * @author Rolf Kiefhaber
 */
public abstract class Commentable {

    /** The comment to be written above the tag in the XML */
    @Transient
    private transient String comment;

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}

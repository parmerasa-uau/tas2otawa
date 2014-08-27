package eu.parmerasa.sik.tas2otawa;

import java.util.Map;

import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;

import eu.parmerasa.sik.tas2otawa.xml.Commentable;

/**
 * A VisitorStrategy to add context sensitive comments to serialized OTAWA XML
 * 
 * @author Rolf Kiefhaber
 */
public class CommentVisitorStrategy extends VisitorStrategy {

    /**
     * See {@link VisitorStrategy#VisitorStrategy(Visitor)}
     */
    public CommentVisitorStrategy(Visitor visitor) {
        super(visitor);
    }

    /**
     * See {@link VisitorStrategy#VisitorStrategy(Visitor, Strategy)}
     */
    public CommentVisitorStrategy(Visitor visitor, Strategy strategy) {
        super(visitor, strategy);
    }

    /**
     * {@inheritDoc} If the value Object is of type {@link Commentable}, the
     * comment of it is read and set in the {@link OutputNode}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public boolean write(Type type, Object value, NodeMap<OutputNode> node,
            Map map) throws Exception {
        OutputNode outNode = node.getNode();
        if (value instanceof Commentable) {
            Commentable commentable = (Commentable) value;
            outNode.setComment(commentable.getComment());
        }
        return super.write(type, value, node, map);
    }
}

package patterns.visitor;

public interface GenericVisitor<T> {
    public void visit(T t);
} 
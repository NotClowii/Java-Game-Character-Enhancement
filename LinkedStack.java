import java.util.ArrayList;
import java.util.List;

/**
 * ADT: generic LIFO stack (singly linked list). Used for the battle log (newest
 * on top).
 */
public class LinkedStack<T> {
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T d, Node<T> n) {
            data = d;
            next = n;
        }
    }

    private Node<T> top;
    private int size;

    public void push(T item) {
        top = new Node<>(item, top);
        size++;
    }

    public T pop() {
        if (top == null)
            throw new IllegalStateException("Stack is empty");
        T d = top.data;
        top = top.next;
        size--;
        return d;
    }

    public T peek() {
        return top == null ? null : top.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    /** Top-first. */
    public List<T> toList() {
        List<T> out = new ArrayList<>();
        for (Node<T> n = top; n != null; n = n.next)
            out.add(n.data);
        return out;
    }
}
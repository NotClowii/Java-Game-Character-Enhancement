import java.util.ArrayList;
import java.util.List;

/**
 * ADT: generic FIFO queue (singly linked list). Used for the battle turn order.
 */
public class LinkedQueue<T> {
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T d) {
            data = d;
        }
    }

    private Node<T> head, tail;
    private int size;

    public void enqueue(T item) {
        Node<T> n = new Node<>(item);
        if (tail == null)
            head = tail = n;
        else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    public T dequeue() {
        if (head == null)
            throw new IllegalStateException("Queue is empty");
        T d = head.data;
        head = head.next;
        if (head == null)
            tail = null;
        size--;
        return d;
    }

    public T peek() {
        return head == null ? null : head.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public List<T> toList() {
        List<T> out = new ArrayList<>();
        for (Node<T> n = head; n != null; n = n.next)
            out.add(n.data);
        return out;
    }
}
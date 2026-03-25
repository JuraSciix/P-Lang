package plang.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public final class Trie<V> {

    public static final class Node<V> {
        final char key;
        final List<Node<V>> children = new ArrayList<>();

        Node(char key) {
            this.key = key;
        }

        V value;

        public V getValue() {
            if (value == null) {
                throw new IllegalStateException();
            }
            return value;
        }

        public Node<V> findChild(char key) {
            return find(children, key);
        }
    }

    private final List<Node<V>> data = new ArrayList<>();

    public void put(String key, V value) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException();
        }

        List<Node<V>> layer = data;
        Node<V> parent = null;
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            parent = find(layer, ch);
            if (parent == null) {
                parent = new Node<>(ch);
                layer.add(parent);
            }
            layer = parent.children;
        }

        parent.value = value;
    }

    public V get(String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException();
        }

        List<Node<V>> layer = data;
        Node<V> parent = null;
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            parent = find(layer, ch);
            if (parent == null) {
                throw new NoSuchElementException();
            }
            layer = parent.children;
        }

        return parent.value;
    }

    public boolean contains(String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException();
        }

        List<Node<V>> layer = data;
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            Node<V> parent = find(layer, ch);
            if (parent == null) {
                return false;
            }
            layer = parent.children;
        }

        return true;
    }

    public Node<V> findNode(char key) {
        return find(data, key);
    }

    private static <V> Node<V> find(List<Node<V>> data, char ch) {
        for (Node<V> node : data) {
            if (node.key == ch) {
                return node;
            }
        }

        return null;
    }
}

package scripts.dax_api.shared;

public class Pair<T, V> {

    private final T key;
    private final V value;

    public Pair(T key, V value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}

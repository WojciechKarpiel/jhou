package pl.wojciechkarpiel.jhou.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Wrapper for {@link Map} that treats null values as non-existing
 */
public class MapUtil<K, V> {
    private final Map<K, V> map;

    public MapUtil(Map<K, V> map) {
        this.map = map;
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }

    public <R> R withMapping(K key, V value, Supplier<R> action) {
        Optional<V> previous = put(key, value);
        R result = action.get();
        if (previous.isPresent()) {
            put(key, previous.get());
        } else {
            remove(key);
        }
        return result;
    }

    public <R> R withoutMapping(K key, Supplier<R> action) {
        return withMapping(key, null, action);
    }

    public Optional<V> put(K key, V value) {
        return Optional.ofNullable(map.put(key, value));
    }

    public Optional<V> remove(K key) {
        return Optional.ofNullable(map.remove(key));
    }
}

package pl.wojciechkarpiel.jhou.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Needed because of JDK8 compatibility
 */
public class ListUtil {

    public static <T> List<T> of() {
        return new ArrayList<>(0);
    }

    public static <T> List<T> of(T elem) {
        List<T> result = new ArrayList<>(1);
        result.add(elem);
        return result;
    }

    public static <T> List<T> of(T first, T second) {
        List<T> result = new ArrayList<>(2);
        result.add(first);
        result.add(second);
        return result;
    }

    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        List<T> result = new ArrayList<>(elements.length);
        result.addAll(Arrays.asList(elements));
        return result;
    }
}

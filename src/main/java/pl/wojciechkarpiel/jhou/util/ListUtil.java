package pl.wojciechkarpiel.jhou.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Needed because of JDK8 compatibility
 */
public class ListUtil {

    public static <T> List<T> of(T... elements) {
        List<T> result = new ArrayList<>(elements.length);
        result.addAll(Arrays.asList(elements));
        return result;
    }
}

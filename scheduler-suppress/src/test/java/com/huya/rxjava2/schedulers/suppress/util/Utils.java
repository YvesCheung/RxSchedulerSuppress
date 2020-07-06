package com.huya.rxjava2.schedulers.suppress.util;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public class Utils {

    public static <E> boolean all(List<E> list, Predicate<E> predicate) {
        for (E e : list) {
            if (!predicate.test(e)) {
                return false;
            }
        }
        return true;
    }
}

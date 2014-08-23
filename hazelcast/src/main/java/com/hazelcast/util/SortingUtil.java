package com.hazelcast.util;

import com.hazelcast.query.PagingPredicate;

import java.util.Comparator;
import java.util.Map;

/**
 * @author ali 10/12/13
 */
public final class SortingUtil {

    private SortingUtil() {
    }

    public static int compare(Comparator<Map.Entry> comparator, IterationType iterationType, Map.Entry entry1, Map.Entry entry2){
        if (comparator != null) {
            int result = comparator.compare(entry1, entry2);
            if (result != 0) {
                return result;
            }
            return compareIntegers(entry1.getKey().hashCode(), entry2.getKey().hashCode());
        }

        Object comparable1;
        Object comparable2;
        switch (iterationType) {
            case KEY:
                comparable1 = entry1.getKey();
                comparable2 = entry2.getKey();
                break;
            case VALUE:
                comparable1 = entry1.getValue();
                comparable2 = entry2.getValue();
                break;
            default:
                comparable1 = entry1;
                comparable2 = entry2;
                break;
        }

        int result;
        if (comparable1 instanceof Comparable && comparable2 instanceof Comparable) {
            result = ((Comparable) comparable1).compareTo(comparable2);
        } else {
            result = compareIntegers(comparable1.hashCode(), comparable2.hashCode());
        }

        if (result != 0) {
            return result;
        }
        return compareIntegers(entry1.getKey().hashCode(), entry2.getKey().hashCode());
    }

    /**
     * Compares two integers by considering their signs.
     *
     * Suppose that
     *      i1 = -500.000.000
     *      i2 = 2.000.000.000
     *
     * Normally "i1 < i2", but if we use "i1 - i2" for comparison
     * i1 - i2 = -500.000.000 - 2.000.000.000 and we may accept result as "-2.500.000.000".
     * But the actual result is "1.794.967.296" because of overflow between
     * positive and negative integer bounds.
     *
     * So, if we use "i1 - i2" for comparison, since result is greater than 0,
     * "i1" is accepted as bigger that "i2". But in fact "i1" is smaller than "i2".
     * Therefore, "i1 - i2" is not good way for comparison way between signed integers.
     *
     * @param i1 First number to compare with second one
     * @param i2 Second number to compare with first one
     * @return +1 if i1 > i2, -1 if i2 > i1, 0 if i1 and i2 are equals
     */
    private static int compareIntegers(int i1, int i2) {
        if (i1 > i2) {
            return +1;
        } else if (i2 > i1) {
            return -1;
        } else {
            return 0;
        }
    }

    public static Comparator<Map.Entry> newComparator(final Comparator<Map.Entry> comparator, final IterationType iterationType){
        return new Comparator<Map.Entry>(){
            public int compare(Map.Entry entry1, Map.Entry entry2) {
                return SortingUtil.compare(comparator, iterationType, entry1, entry2);
            }
        };
    }

    public static Comparator<Map.Entry> newComparator(final PagingPredicate pagingPredicate){
        return new Comparator<Map.Entry>(){
            public int compare(Map.Entry entry1, Map.Entry entry2) {
                return SortingUtil.compare(pagingPredicate.getComparator(), pagingPredicate.getIterationType(), entry1, entry2);
            }
        };
    }

}

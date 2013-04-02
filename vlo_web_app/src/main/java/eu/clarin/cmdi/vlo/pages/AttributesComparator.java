package eu.clarin.cmdi.vlo.pages;

import java.util.Comparator;
import java.util.List;

/**
 * Comparator that compares fixedValues based on the index in the list and falls back to String comparison otherwise.
 * Example:
 * fixedValues = "A", "Z"
 * List to sort = "G", "A", "Z", "B"
 * result = "A", "Z", "B", "G" 
 *
 */
public class AttributesComparator implements Comparator<String> {
    private final List<String> fixedValues;

    public AttributesComparator(List<String> fixedValues) {
        this.fixedValues = fixedValues;
    }

    public int compare(String s1, String s2) {
        if (s1 == null) {
            return 1;
        }
        if (s2 == null) {
            return -1;
        }
        Integer pos1 = fixedValues.contains(s1) ? fixedValues.indexOf(s1) : Integer.MAX_VALUE;
        Integer pos2 = fixedValues.contains(s2) ? fixedValues.indexOf(s2) : Integer.MAX_VALUE;
        int result = pos1.compareTo(pos2);
        if (result == 0) {
            result = s1.compareTo(s2);
        }
        return result;
    }
}
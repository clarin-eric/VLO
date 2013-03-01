package eu.clarin.cmdi.vlo.pages;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import eu.clarin.cmdi.vlo.FacetConstants;

public class AttributesComparatorTest {

    @Test
    public void testCompare() throws Exception {
        AttributesComparator comp = new AttributesComparator(Arrays.asList(FacetConstants.FIELD_NAME, FacetConstants.FIELD_DESCRIPTION));
        List<String> list = new ArrayList<String>();
        list.add(FacetConstants.FIELD_DESCRIPTION);
        list.add("aap");
        list.add("noot");
        list.add(FacetConstants.FIELD_NAME);
        list.add("id");
        Collections.sort(list, comp);
        assertEquals(FacetConstants.FIELD_NAME, list.get(0));
        assertEquals(FacetConstants.FIELD_DESCRIPTION, list.get(1));
        assertEquals("aap", list.get(2));
        assertEquals("id", list.get(3));
        assertEquals("noot", list.get(4));
    }

}

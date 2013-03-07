package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.VloWebApplication;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.util.tester.WicketTester;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class FacetBoxPanelTest {

    static VloConfig testConfig;

    @Before
    public void setUp() {
        WicketTester wicketTester;
        
        String fileName = VloConfig.class.getResource("/VloConfig.xml").getFile();
        
        testConfig = VloConfig.readTestConfig(fileName);
        
        // optionally, modify the test configuration here
        
        wicketTester = new WicketTester(new VloWebApplication(testConfig));
    }

    @Test
    public void testCalculateFacetBoxPanel() throws Exception {
        FacetBoxPanel panel = new FacetBoxPanel("test", null);
        panel.setMaxNrOfFacetValues(5);
        FacetField facetField = new FacetField("test");
        facetField.add("name5", 5);
        facetField.add("name4", 4);
        facetField.add("name3", 3);
        facetField.add("name2", 2);
        facetField.add("name1", 1);
        facetField.add("name0", 1);
        List<Count> list = panel.getFacetListForBox(facetField, true);
        assertEquals(6, list.size());
        assertEquals("name5", list.get(0).getName());
        assertEquals("name0", list.get(5).getName());

        facetField.insert("name6", 6);
        list = panel.getFacetListForBox(facetField, true);
        assertEquals(5, list.size());
        assertEquals("name6", list.get(0).getName());
        assertEquals("name2", list.get(4).getName());

        facetField.insert("unknown", 7);
        facetField.insert("unspecified", 7);
        list = panel.getFacetListForBox(facetField, true);
        assertEquals(5, list.size());
        assertEquals("name6", list.get(0).getName());
        assertEquals("name2", list.get(4).getName());

        facetField = new FacetField("test");
        facetField.add("unknown", 7);
        facetField.add("name5", 5);
        facetField.add("unspecified", 5);
        facetField.add("name4", 4);
        facetField.add("name3", 3);
        facetField.add("name2", 2);
        list = panel.getFacetListForBox(facetField, true);
        assertEquals(6, list.size());
        assertEquals("unknown", list.get(0).getName());
        assertEquals("name2", list.get(5).getName());

        facetField.insert("name8", 8);
        list = panel.getFacetListForBox(facetField, true);
        assertEquals(5, list.size());
        assertEquals("name8", list.get(0).getName());
        assertEquals("name2", list.get(4).getName());

        facetField = new FacetField("test");
        facetField.add("unknown", 7);
        facetField.add("name5", 5);
        facetField.add("unspecified", 5);
        facetField.add("name4", 4);
        facetField.add("name3", 3);
        facetField.add("name2", 2);
        facetField.add("name1", 1);
        facetField.add("name0", 1);
        list = panel.getFacetListForBox(facetField, true);
        assertEquals(5, list.size());
        assertEquals("name5", list.get(0).getName());
        assertEquals("name4", list.get(1).getName());
        assertEquals("name3", list.get(2).getName());
        assertEquals("name2", list.get(3).getName());
        assertEquals("name1", list.get(4).getName());

        facetField = new FacetField("test");
        facetField.add("unknown", 7);
        facetField.add("name5", 5);
        facetField.add("unspecified", 5);
        facetField.add("name4", 4);
        facetField.add("name3", 3);
        facetField.add("name2", 1);
        facetField.add("Unspecified", 1);
        list = panel.getFacetListForBox(facetField, true);
        assertEquals(5, list.size());
        assertEquals("name5", list.get(0).getName());
        assertEquals("name4", list.get(1).getName());
        assertEquals("name3", list.get(2).getName());
        assertEquals("name2", list.get(3).getName());
        assertEquals("unknown", list.get(4).getName());
    }
}

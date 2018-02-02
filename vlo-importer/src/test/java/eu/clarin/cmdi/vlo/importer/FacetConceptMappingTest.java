package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Test;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.FacetConcept;

public class FacetConceptMappingTest {

    @Test
    public void testMapping() {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        content += "<facetConcepts>\n";
        content += "    <facetConcept name=\"name\">\n";
        content += "        <concept>http://www.isocat.org/datcat/DC-2544</concept>\n";
        content += "        <concept>conceptLink2</concept>\n";
        content += "    </facetConcept>\n";
        content += "    <facetConcept name=\"subject\" isCaseInsensitive=\"true\" allowMultipleValues=\"false\">\n";
        content += "        <concept>conceptLink1</concept>\n";
        content += "        <pattern>/xpath/pattern/text()</pattern>\n";
        content += "    </facetConcept>\n";
        content += "</facetConcepts>\n";

        FacetConceptMapping conceptMapping = new VLOMarshaller().unmarshal(new ByteArrayInputStream(content.getBytes()));
        List<FacetConcept> facetConcepts = conceptMapping.getFacetConcepts();
        assertEquals(2, facetConcepts.size());
        FacetConcept facetConcept = facetConcepts.get(0);
        assertEquals("name", facetConcept.getName());
        assertFalse(facetConcept.isCaseInsensitive());
        assertTrue(facetConcept.isAllowMultipleValues());
        List<String> concepts = facetConcept.getConcepts();
        assertEquals(2, concepts.size());
        assertEquals("http://www.isocat.org/datcat/DC-2544", concepts.get(0));
        assertEquals("conceptLink2", concepts.get(1));
        List<Pattern> patterns = facetConcept.getPatterns();
        assertEquals(0, patterns.size());
        
        facetConcept = facetConcepts.get(1);
        assertEquals("subject", facetConcept.getName());
        assertTrue(facetConcept.isCaseInsensitive());
        assertFalse(facetConcept.isAllowMultipleValues());
        concepts = facetConcept.getConcepts();
        assertEquals(1, concepts.size());
        assertEquals("conceptLink1", concepts.get(0));
        patterns = facetConcept.getPatterns();
        assertEquals(1, patterns.size());
        assertEquals("/xpath/pattern/text()", patterns.get(0).getPattern());
    }

}

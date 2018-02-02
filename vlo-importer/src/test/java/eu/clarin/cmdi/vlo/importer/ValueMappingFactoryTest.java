package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import eu.clarin.cmdi.vlo.importer.mapping.ConditionTargetSet;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping;
import eu.clarin.cmdi.vlo.importer.mapping.ValueMappingFactory;

public class ValueMappingFactoryTest {
    private FacetConceptMapping conceptMapping;
    
    @Before
    public void init() {
        this.conceptMapping = new VLOMarshaller().getFacetConceptMapping(ImporterTestcase.getTestFacetConceptFilePath());
    }
    
    @Test
    public void testGeneralRepresentation() throws IOException {
        String fileName = createTmpFile(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "\n" + 
                "<value-mappings>\n" + 
                "<origin-facet name=\"name\">\n" + 
                "  <value-map>\n" + 
                "   <target-value-set>\n" + 
                "       <target-value facet=\"subject\">blabla1</target-value>\n" + 
                "       <source-value isRegex=\"true\">.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "</origin-facet>\n" + 
                "<origin-facet name=\"collection\">\n" + 
                "  <value-map>\n" + 
                "   <target-value-set>\n" + 
                "       <target-value facet=\"subject\">blabla1</target-value>\n" + 
                "       <target-value facet=\"name\">blabla2</target-value>\n" + 
                "       <target-value facet=\"temporalCoverage\">blabla3</target-value>\n" + 
                "       <source-value>CollectionName</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "</origin-facet>\n" + 
                "</value-mappings>\n"
            );
    
        Map<String, List<ConditionTargetSet>> map = ValueMappingFactory.getValueMappings(fileName, this.conceptMapping);
        
        assertEquals(1, map.get("name").size());
        assertEquals(1, map.get("name").get(0).getTargets().size());
        assertEquals("subject", map.get("name").get(0).getTargets().get(0).getFacetConfiguration().getName());
        assertEquals("blabla1", map.get("name").get(0).getTargets().get(0).getValue());
        
        assertEquals(1, map.get("collection").size());
        assertEquals(3, map.get("collection").get(0).getTargets().size());
        assertEquals("subject", map.get("collection").get(0).getTargets().get(0).getFacetConfiguration().getName());
        assertEquals(true, map.get("collection").get(0).getTargets().get(0).getFacetConfiguration().getAllowMultipleValues());
        assertEquals("name", map.get("collection").get(0).getTargets().get(1).getFacetConfiguration().getName());
        assertEquals("temporalCoverage", map.get("collection").get(0).getTargets().get(2).getFacetConfiguration().getName());

    }
    
    @Test
    public void testAttributeRepresentation() throws IOException {
        String fileName = createTmpFile(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "\n" + 
                "<value-mappings>\n" + 
                "<origin-facet name=\"name\">\n" + 
                "  <value-map>\n" + 
                "   <target-value-set>\n" + 
                "       <target-value facet=\"subject\">blabla1</target-value>\n" + 
                "       <target-value facet=\"name\" overrideExistingValues=\"true\" removeSourceValue=\"false\">blabla2</target-value>\n" + 
                "       <source-value isRegex=\"true\">.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "</origin-facet>\n" + 
                "<origin-facet name=\"collection\">\n" + 
                "  <value-map>\n" + 
                "   <target-facet name=\"name\" overrideExistingValues=\"true\" removeSourceValue=\"false\" />\n" +
                "   <target-value-set>\n" + 
                "       <target-value facet=\"subject\">blabla1</target-value>\n" + 
                "       <target-value facet=\"name\">blabla2</target-value>\n" + 
                "       <source-value isRegex=\"true\">.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "  <value-map>\n" + 
                "   <target-facet name=\"name\" overrideExistingValues=\"true\" removeSourceValue=\"false\" />\n" +
                "   <target-value-set>\n" + 
                "       <target-value>blabla1</target-value>\n" + 
                "       <target-value>blabla2</target-value>\n" + 
                "       <source-value isRegex=\"true\">.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" +                 
                "</origin-facet>\n" + 
                "</value-mappings>\n"
            );
    
        Map<String, List<ConditionTargetSet>> map = ValueMappingFactory.getValueMappings(fileName, this.conceptMapping);
        

        assertEquals(false, map.get("name").get(0).getTargets().get(0).getOverrideExistingValues());
        assertEquals(true, map.get("name").get(0).getTargets().get(0).getRemoveSourceValue());
        
        assertEquals(false, map.get("collection").get(0).getTargets().get(0).getOverrideExistingValues());
        assertEquals(true, map.get("collection").get(0).getTargets().get(0).getRemoveSourceValue());
        assertEquals(true, map.get("collection").get(0).getTargets().get(1).getOverrideExistingValues());
        assertEquals(false, map.get("collection").get(0).getTargets().get(1).getRemoveSourceValue());
        
        assertEquals(2, map.get("collection").size());
        assertEquals("name", map.get("collection").get(1).getTargets().get(0).getFacetConfiguration().getName());
        assertEquals(true, map.get("collection").get(1).getTargets().get(0).getOverrideExistingValues());
        assertEquals(false, map.get("collection").get(1).getTargets().get(0).getRemoveSourceValue());
        assertEquals("name", map.get("collection").get(1).getTargets().get(1).getFacetConfiguration().getName());
        assertEquals(true, map.get("collection").get(1).getTargets().get(1).getOverrideExistingValues());
        assertEquals(false, map.get("collection").get(1).getTargets().get(1).getRemoveSourceValue());
 
    }

    @Test
    public void testConditionMatch() throws IOException {
        String fileName = createTmpFile(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "\n" + 
                "<value-mappings>\n" + 
                "<origin-facet name=\"name\">\n" + 
                "  <value-map>\n" + 
                "   <target-value-set>\n" + 
                "       <target-value facet=\"subject\">blabla1</target-value>\n" + 
                "       <source-value isRegex=\"true\">D.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "   <target-value-set>\n" + 
                "       <target-value facet=\"name\">blabla1</target-value>\n" + 
                "       <source-value>DonauDampfschifffahrtsGesellschaftsKaptitän</source-value>\n" + 
                "   </target-value-set>\n" + 
                "   <target-value-set>\n" + 
                "       <target-value facet=\"name\">blabla1</target-value>\n" + 
                "       <source-value caseSensitive=\"true\">DonauDampfschifffahrtsGesellschaftsKaptitän</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" +                 
                "</origin-facet>\n" + 
                "</value-mappings>\n"
            );
    
        Map<String, List<ConditionTargetSet>> map = ValueMappingFactory.getValueMappings(fileName, this.conceptMapping);
        

        assertEquals(true, map.get("name").get(0).matches("Data"));
        assertEquals(false, map.get("name").get(0).matches("data"));
        
        assertEquals(true, map.get("name").get(1).matches("DonauDampfschifffahrtsGesellschaftsKaptitän"));
        assertEquals(true, map.get("name").get(1).matches("donaudampfschifffahrtsgesellschaftskaptitän"));
        assertEquals(false, map.get("name").get(1).matches("donaudampfschifffahrtsgesellschaftskaptitaen"));
        
        assertEquals(true, map.get("name").get(2).matches("DonauDampfschifffahrtsGesellschaftsKaptitän"));
        assertEquals(false, map.get("name").get(2).matches("donaudampfschifffahrtsgesellschaftskaptitän"));
        assertEquals(false, map.get("name").get(2).matches("donaudampfschifffahrtsgesellschaftskaptitaen"));
    }

    
    private  String createTmpFile(String content) throws IOException{
        File file = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".tmp");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file.getAbsolutePath();
    }
}

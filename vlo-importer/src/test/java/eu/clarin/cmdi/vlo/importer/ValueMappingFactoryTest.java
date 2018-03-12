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
import eu.clarin.cmdi.vlo.importer.mapping.ValueMappingFactoryDOMImpl;
import eu.clarin.cmdi.vlo.importer.mapping.ValueMappingFactorySAXImpl;

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
    
        Map<String, ConditionTargetSet> map = new ValueMappingFactoryDOMImpl().getValueMappings(fileName, this.conceptMapping);
        
        assertEquals(1, map.get("name").getTargetsFor("test").size());
        assertEquals("subject", map.get("name").getTargetsFor("test").get(0).getFacetConfiguration().getName());
        assertEquals("blabla1", map.get("name").getTargetsFor("test").get(0).getValue());
        

        assertEquals(3, map.get("collection").getTargetsFor("CollectionName").size());
        assertEquals("subject", map.get("collection").getTargetsFor("CollectionName").get(0).getFacetConfiguration().getName());
        assertEquals(true, map.get("collection").getTargetsFor("CollectionName").get(0).getFacetConfiguration().getAllowMultipleValues());
        assertEquals("name", map.get("collection").getTargetsFor("CollectionName").get(1).getFacetConfiguration().getName());
        assertEquals("temporalCoverage", map.get("collection").getTargetsFor("CollectionName").get(2).getFacetConfiguration().getName());

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
                "   <target-facet name=\"name\" overrideExistingValues=\"true\" removeSourceValue=\"true\" />\n" +
                "   <target-value-set>\n" + 
                "       <target-value facet=\"subject\">blabla1</target-value>\n" + 
                "       <target-value facet=\"name\">blabla2</target-value>\n" + 
                "       <source-value isRegex=\"true\">a.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "  <value-map>\n" + 
                "   <target-facet name=\"name\" overrideExistingValues=\"true\" />\n" +
                "   <target-value-set>\n" + 
                "       <target-value>blabla1</target-value>\n" + 
                "       <target-value>blabla2</target-value>\n" + 
                "       <source-value isRegex=\"true\">b.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" +                 
                "</origin-facet>\n" + 
                "</value-mappings>\n"
            );
    
        Map<String, ConditionTargetSet> map = new ValueMappingFactoryDOMImpl().getValueMappings(fileName, this.conceptMapping);
        

        assertEquals(false, map.get("name").getTargetsFor("test").get(0).getOverrideExistingValues());
        assertEquals(false, map.get("name").getTargetsFor("test").get(0).getRemoveSourceValue());
        
        assertEquals(false, map.get("collection").getTargetsFor("aa").get(0).getOverrideExistingValues());
        assertEquals(false, map.get("collection").getTargetsFor("aa").get(0).getRemoveSourceValue());
        assertEquals(true, map.get("collection").getTargetsFor("aa").get(1).getOverrideExistingValues());
        assertEquals(true, map.get("collection").getTargetsFor("aa").get(1).getRemoveSourceValue());
        
        assertEquals(2, map.get("collection").getTargetsFor("bb").size());
        assertEquals("name", map.get("collection").getTargetsFor("bb").get(0).getFacetConfiguration().getName());
        assertEquals(true, map.get("collection").getTargetsFor("bb").get(0).getOverrideExistingValues());
        assertEquals(false, map.get("collection").getTargetsFor("bb").get(0).getRemoveSourceValue());
        assertEquals("name", map.get("collection").getTargetsFor("bb").get(1).getFacetConfiguration().getName());
        assertEquals(true, map.get("collection").getTargetsFor("bb").get(1).getOverrideExistingValues());
        assertEquals(false, map.get("collection").getTargetsFor("bb").get(1).getRemoveSourceValue());
 
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
                "       <source-value isRegex=\"true\">C.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "  <value-map>\n" + 
                "   <target-value-set>\n" + 
                "       <target-value facet=\"name\">blabla1</target-value>\n" + 
                "       <source-value>DonauDampfschifffahrtsGesellschaftsKaptitän</source-value>\n" + 
                "   </target-value-set>\n" + 
                "   <target-value-set>\n" + 
                "       <target-value facet=\"name\">blabla2</target-value>\n" + 
                "       <source-value caseSensitive=\"true\">DonauDampfschifffahrtsGesellschaftsKaptitän</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" +                 
                "</origin-facet>\n" + 
                "</value-mappings>\n"
            );
    
        Map<String, ConditionTargetSet> map = new ValueMappingFactoryDOMImpl().getValueMappings(fileName, this.conceptMapping);
        

        assertEquals(1, map.get("name").getTargetsFor("Clarin").size());
        assertEquals(0, map.get("name").getTargetsFor("clarin").size());
        
        assertEquals(2, map.get("name").getTargetsFor("DonauDampfschifffahrtsGesellschaftsKaptitän").size());
        assertEquals(1, map.get("name").getTargetsFor("donaudampfschifffahrtsgesellschaftskaptitän").size());
        assertEquals(0, map.get("name").getTargetsFor("donaudampfschifffahrtsgesellschaftskaptitaen").size());
        

    }

    
    private  String createTmpFile(String content) throws IOException{
        File file = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".tmp");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file.getAbsolutePath();
    }
}

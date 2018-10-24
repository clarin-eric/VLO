package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import eu.clarin.cmdi.vlo.importer.mapping.ValueMappingFactoryDOMImpl;


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
    
        
        FacetMapping facetMapping = new FacetMapping();
        new ValueMappingFactoryDOMImpl().createValueMapping(fileName, this.conceptMapping, facetMapping);
        
        assertEquals(1, facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("test").size());
        assertEquals("subject", facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("test").get(0).getFacetConfiguration().getName());
        assertEquals("blabla1", facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("test").get(0).getValue());
        

        assertEquals(3, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("CollectionName").size());
        assertEquals("subject", facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("CollectionName").get(0).getFacetConfiguration().getName());
        assertEquals(true, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("CollectionName").get(0).getFacetConfiguration().getAllowMultipleValues());
        assertEquals("name", facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("CollectionName").get(1).getFacetConfiguration().getName());
        assertEquals("temporalCoverage", facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("CollectionName").get(2).getFacetConfiguration().getName());

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
    
        FacetMapping facetMapping = new FacetMapping();
        new ValueMappingFactoryDOMImpl().createValueMapping(fileName, this.conceptMapping, facetMapping);

        

        assertEquals(false, facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("test").get(0).getOverrideExistingValues());
        assertEquals(false, facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("test").get(0).getRemoveSourceValue());
        
        assertEquals(false, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("aa").get(0).getOverrideExistingValues());
        assertEquals(false, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("aa").get(0).getRemoveSourceValue());
        assertEquals(true, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("aa").get(1).getOverrideExistingValues());
        assertEquals(true, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("aa").get(1).getRemoveSourceValue());
        
        assertEquals(2, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("bb").size());
        assertEquals("name", facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("bb").get(0).getFacetConfiguration().getName());
        assertEquals(true, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("bb").get(0).getOverrideExistingValues());
        assertEquals(false, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("bb").get(0).getRemoveSourceValue());
        assertEquals("name", facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("bb").get(1).getFacetConfiguration().getName());
        assertEquals(true, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("bb").get(1).getOverrideExistingValues());
        assertEquals(false, facetMapping.getFacetConfiguration("collection").getConditionTargetSet().getTargetsFor("bb").get(1).getRemoveSourceValue());
 
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
    
        FacetMapping facetMapping = new FacetMapping();
        new ValueMappingFactoryDOMImpl().createValueMapping(fileName, this.conceptMapping, facetMapping);

        

        assertEquals(1, facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("Clarin").size());
        assertEquals(0, facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("clarin").size());
        
        assertEquals(2, facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("DonauDampfschifffahrtsGesellschaftsKaptitän").size());
        assertEquals(1, facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("donaudampfschifffahrtsgesellschaftskaptitän").size());
        assertEquals(0, facetMapping.getFacetConfiguration("name").getConditionTargetSet().getTargetsFor("donaudampfschifffahrtsgesellschaftskaptitaen").size());
        

    }

    
    private  String createTmpFile(String content) throws IOException{
        File file = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".tmp");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file.getAbsolutePath();
    }
}

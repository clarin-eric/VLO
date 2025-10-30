package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import eu.clarin.cmdi.vlo.importer.mapping.ConditionTargetSet;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import eu.clarin.cmdi.vlo.importer.mapping.ValueMappingFactoryDOMImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;


public class ValueMappingFactoryTest {
    private FacetConceptMapping conceptMapping;
    
    @BeforeEach
    public void init() {
        this.conceptMapping = new VLOMarshaller().getFacetConceptMapping(ImporterTestcase.getTestFacetConceptFilePath());
    }
    
    @Test
    public void testGeneralRepresentation() throws Exception {
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
    
        
        Map<String, Facet> facetsConfigMap = new HashMap<>();
        FacetsMapping facetMapping = new FacetsMapping(facetsConfigMap);
        new ValueMappingFactoryDOMImpl().createValueMapping(fileName, this.conceptMapping, facetMapping);
        
        assertEquals(1, facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("test").size());
        assertEquals("subject", facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("test").get(0).getFacetConfiguration().getName());
        assertEquals("blabla1", facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("test").get(0).getValue());
        

        assertEquals(3, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("CollectionName").size());
        assertEquals("subject", facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("CollectionName").get(0).getFacetConfiguration().getName());
        assertEquals(true, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("CollectionName").get(0).getFacetConfiguration().getAllowMultipleValues());
        assertEquals("name", facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("CollectionName").get(1).getFacetConfiguration().getName());
        assertEquals("temporalCoverage", facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("CollectionName").get(2).getFacetConfiguration().getName());

    }
    
    @Test
    public void testAttributeRepresentation() throws Exception {
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
    
        Map<String, Facet> facetsConfigMap = new HashMap<>();
        FacetsMapping facetMapping = new FacetsMapping(facetsConfigMap);
        new ValueMappingFactoryDOMImpl().createValueMapping(fileName, this.conceptMapping, facetMapping);

        

        assertEquals(false, facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("test").get(0).getOverrideExistingValues());
        assertEquals(false, facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("test").get(0).getRemoveSourceValue());
        
        assertEquals(false, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("aa").get(0).getOverrideExistingValues());
        assertEquals(false, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("aa").get(0).getRemoveSourceValue());
        assertEquals(true, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("aa").get(1).getOverrideExistingValues());
        assertEquals(true, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("aa").get(1).getRemoveSourceValue());
        
        assertEquals(2, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("bb").size());
        assertEquals("name", facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("bb").get(0).getFacetConfiguration().getName());
        assertEquals(true, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("bb").get(0).getOverrideExistingValues());
        assertEquals(false, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("bb").get(0).getRemoveSourceValue());
        assertEquals("name", facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("bb").get(1).getFacetConfiguration().getName());
        assertEquals(true, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("bb").get(1).getOverrideExistingValues());
        assertEquals(false, facetMapping.getFacetDefinition("collection").getConditionTargetSet().getTargetsFor("bb").get(1).getRemoveSourceValue());
 
    }

    @Test
    public void testConditionMatch() throws Exception {
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
    
        Map<String, Facet> facetsConfigMap = new HashMap<>();
        FacetsMapping facetMapping = new FacetsMapping(facetsConfigMap);
        new ValueMappingFactoryDOMImpl().createValueMapping(fileName, this.conceptMapping, facetMapping);

        

        assertEquals(1, facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("Clarin").size());
        assertEquals(0, facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("clarin").size());
        
        assertEquals(2, facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("DonauDampfschifffahrtsGesellschaftsKaptitän").size());
        assertEquals(1, facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("donaudampfschifffahrtsgesellschaftskaptitän").size());
        assertEquals(0, facetMapping.getFacetDefinition("name").getConditionTargetSet().getTargetsFor("donaudampfschifffahrtsgesellschaftskaptitaen").size());
        

    }
    @Test
    public void testCloneDefaultTarget() throws Exception {
        String fileName = createTmpFile("""
                                        <?xml version="1.0" encoding="UTF-8"?>
                                         <value-mappings>
                                             <origin-facet name="subject">
                                                 <value-map>
                                                     <target-facet name="subject" removeSourceValue="true" overrideExistingValues="false"/>
                                                     <target-value-set>
                                                         <target-value facet="subject" removeSourceValue="true"/>
                                                         <source-value>Web application</source-value>
                                                     </target-value-set>
                                                     <target-value-set>
                                                         <target-value facet="subject">Text processing</target-value>
                                                         <source-value>Text Processing</source-value>
                                                     </target-value-set>
                                                 </value-map>
                                             </origin-facet>
                                         </value-mappings>
                                        """);

        final Map<String, Facet> facetsConfigMap = new HashMap<>();
        final FacetsMapping facetMapping = new FacetsMapping(facetsConfigMap);
        new ValueMappingFactoryDOMImpl().createValueMapping(fileName, this.conceptMapping, facetMapping);
        final ConditionTargetSet cts = facetMapping.getFacetDefinition("subject").getConditionTargetSet();

        final List<TargetFacet> targets = cts.getTargetsFor("Text processing");
        assertEquals(1, targets.size());

        final TargetFacet target = targets.get(0);
        assertEquals("subject", target.getFacetConfiguration().getName());
        assertTrue(target.getRemoveSourceValue());
    }

    
    private  String createTmpFile(String content) throws IOException{
        File file = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".tmp");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file.getAbsolutePath();
    }
}

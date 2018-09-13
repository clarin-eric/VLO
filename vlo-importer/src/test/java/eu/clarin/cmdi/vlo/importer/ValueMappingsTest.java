package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.importer.solr.DummySolrBridgeImpl;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.DataRoot;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.common.SolrInputDocument;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class ValueMappingsTest extends ImporterTestcase {

    protected final static org.slf4j.Logger LOG = LoggerFactory.getLogger(ValueMappingsTest.class);

    @Test
    public void testSimple() throws Exception {
        String session = "";
        session += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        session += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1271859438204\">\n";
        session += "   <Header>\n";
        session += "      <MdCreationDate>2008-05-27</MdCreationDate>\n";
        session += "      <MdSelfLink> testID1Session</MdSelfLink>\n";
        session += "      <MdCollectionDisplayName>CollectionName</MdCollectionDisplayName>\n";
        session += "      <MdProfile>clarin.eu:cr1:p_1271859438204</MdProfile>\n";
        session += "   </Header>\n";
        session += "   <Resources>\n";
        session += "      <ResourceProxyList>\n";
        session += "         <ResourceProxy id=\"d314e408\">\n";
        session += "            <ResourceType mimetype=\"video/x-mpeg1\" >Resource</ResourceType>\n";
        session += "            <ResourceRef>../Media/elan-example1.mpg</ResourceRef>\n";
        session += "         </ResourceProxy>\n";
        session += "      </ResourceProxyList>\n";
        session += "   </Resources>\n";
        session += "   <Components>\n";
        session += "      <cmdp:Session>\n";
        session += "      </cmdp:Session>\n";
        session += "   </Components>\n";
        session += "</CMD>\n";
        File sessionFile = createCmdiFile("testSession", session);
        
        config.setValueMappingsFile(createTmpFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        		"\n" + 
        		"<value-mappings>\n" + 
                "<origin-facet name=\"name\">\n" + 
                "  <value-map>\n" + 
                "   <target-value-set>\n" + 
                "       <source-value isRegex=\"true\">.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "</origin-facet>\n" + 
        		"<origin-facet name=\"collection\">\n" + 
        		"  <value-map>\n" + 
        		"  	<target-value-set>\n" + 
        		"  		<target-value facet=\"subject\">blabla1</target-value>\n" + 
        		"  		<target-value facet=\"name\">blabla2</target-value>\n" + 
        		"  		<target-value facet=\"temporalCoverage\">blabla3</target-value>\n" + 
        		"  		<source-value>CollectionName</source-value>\n" + 
        		"  	</target-value-set>\n" + 
        		"  </value-map>\n" + 
        		"</origin-facet>\n" + 
        		"</value-mappings>\n" + 
        		""));


        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);
        
        assertEquals("blabla1", getValue(doc, fieldNameService.getFieldName(FieldKey.SUBJECT)));   
        assertEquals("blabla2", getValue(doc, fieldNameService.getFieldName(FieldKey.NAME)));
        assertEquals("blabla3", getValue(doc, fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE)));

    }
    
    @Test
    public void testKeepSource() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<cmd:CMD xmlns:cmd=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1280305685235\">\n";
        content += "   <cmd:Header>\n";
        content += "      <cmd:MdProfile>clarin.eu:cr1:p_1280305685235</cmd:MdProfile>\n";
        content += "   </cmd:Header>\n";
        content += "   <cmd:Resources>\n";
        content += "      <cmd:ResourceProxyList>\n";
        content += "         <cmd:ResourceProxy id=\"refLink\">\n";
        content += "            <cmd:ResourceType>Resource</cmd:ResourceType>\n";
        content += "            <cmd:ResourceRef>hdl:1234/567-890-abc</cmd:ResourceRef>\n";
        content += "         </cmd:ResourceProxy>\n";
        content += "      </cmd:ResourceProxyList>\n";
        content += "   </cmd:Resources>\n";
        content += "    <cmd:Components>\n";
        content += "        <cmdp:DynaSAND>\n";
        content += "            <cmdp:Collection>\n";
        content += "                <cmdp:GeneralInfo>\n";
        content += "                    <cmdp:Name>DiDDD</cmdp:Name>\n";
        content += "                    <cmdp:ID>id1234</cmdp:ID>\n";
        content += "                </cmdp:GeneralInfo>\n";
        content += "                <cmdp:Project>\n";
        content += "                    <cmdp:Name>DiDDD-project</cmdp:Name>\n";
        content += "                </cmdp:Project>\n";
        content += "            </cmdp:Collection>\n";
        content += "        </cmdp:DynaSAND>\n";
        content += "    </cmd:Components>\n";
        content += "</cmd:CMD>\n";
        File sessionFile = createCmdiFile("testSession", content);

        
        config.setValueMappingsFile(createTmpFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "\n" + 
                "<value-mappings>\n" + 
                "<origin-facet name=\"projectName\">\n" + 
                "  <value-map>\n" + 
                "   <target-value-set>\n" + 
                "       <target-value facet=\"projectName\" removeSourceValue=\"false\">blabla2</target-value>\n" + 
                "       <source-value isRegex=\"true\">.+</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "</origin-facet>\n" + 
                "</value-mappings>\n" + 
                ""));


        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);
        
        Object[] values = getMultipleValues(doc, fieldNameService.getFieldName(FieldKey.PROJECT_NAME)).toArray();
        assertEquals(2, values.length);
        
 
        assertEquals("blabla2", values[0]);
        assertEquals("DiDDD-project", values[1]);

    }


    @Test
    public void testRegEx() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<cmd:CMD xmlns:cmd=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1280305685235\">\n";
        content += "   <cmd:Header>\n";
        content += "      <cmd:MdProfile>clarin.eu:cr1:p_1280305685235</cmd:MdProfile>\n";
        content += "   </cmd:Header>\n";
        content += "   <cmd:Resources>\n";
        content += "      <cmd:ResourceProxyList>\n";
        content += "         <cmd:ResourceProxy id=\"refLink\">\n";
        content += "            <cmd:ResourceType>Resource</cmd:ResourceType>\n";
        content += "            <cmd:ResourceRef>hdl:1234/567-890-abc</cmd:ResourceRef>\n";
        content += "         </cmd:ResourceProxy>\n";
        content += "      </cmd:ResourceProxyList>\n";
        content += "   </cmd:Resources>\n";
        content += "    <cmd:Components>\n";
        content += "        <cmdp:DynaSAND>\n";
        content += "            <cmdp:Collection>\n";
        content += "                <cmdp:GeneralInfo>\n";
        content += "                    <cmdp:Name>DiDDD</cmdp:Name>\n";
        content += "                    <cmdp:ID>id1234</cmdp:ID>\n";
        content += "                </cmdp:GeneralInfo>\n";
        content += "                <cmdp:Project>\n";
        content += "                    <cmdp:Name>DiDDD-project</cmdp:Name>\n";
        content += "                </cmdp:Project>\n";
        content += "            </cmdp:Collection>\n";
        content += "        </cmdp:DynaSAND>\n";
        content += "    </cmd:Components>\n";
        content += "</cmd:CMD>\n";
        File sessionFile = createCmdiFile("testSession", content);

        config.setValueMappingsFile(createTmpFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        		"\n" + 
        		"<value-mappings>\n" + 
        		"<origin-facet name=\"projectName\">\n" + 
        		"  <value-map>\n" + 
        		"  	<target-value-set>\n" + 
        		"  		<target-value facet=\"subject\">blabla1</target-value>\n" + 
        		"  		<target-value facet=\"projectName\">blabla2</target-value>\n" + 
        		"  		<target-value facet=\"projectName\">blabla3</target-value>\n" + 
        		"  		<source-value isRegex=\"true\">DiDDD.+</source-value>\n" + 
        		"  	</target-value-set>\n" + 
        		"  </value-map>\n" + 
        		"</origin-facet>\n" + 
        		"</value-mappings>\n" + 
        		""));



        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);

        //since this facet permits only one value the value from the cmdi-file should be taken and hence those from the cfm be ignored
        assertEquals("blabla1", getValue(doc, fieldNameService.getFieldName(FieldKey.SUBJECT)));
        
        Object[] values = getMultipleValues(doc, fieldNameService.getFieldName(FieldKey.PROJECT_NAME)).toArray();
        assertEquals(3, values.length);
        
        assertEquals("blabla2", values[0]);
        assertEquals("blabla3", values[1]);
        assertEquals("DiDDD-project", values[2]);
    }

    @Test
    public void testTargetFacet() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<cmd:CMD xmlns:cmd=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1280305685235\">\n";
        content += "   <cmd:Header>\n";
        content += "      <cmd:MdProfile>clarin.eu:cr1:p_1280305685235</cmd:MdProfile>\n";
        content += "   </cmd:Header>\n";
        content += "   <cmd:Resources>\n";
        content += "      <cmd:ResourceProxyList>\n";
        content += "         <cmd:ResourceProxy id=\"refLink\">\n";
        content += "            <cmd:ResourceType>Resource</cmd:ResourceType>\n";
        content += "            <cmd:ResourceRef>hdl:1234/567-890-abc</cmd:ResourceRef>\n";
        content += "         </cmd:ResourceProxy>\n";
        content += "      </cmd:ResourceProxyList>\n";
        content += "   </cmd:Resources>\n";
        content += "    <cmd:Components>\n";
        content += "        <cmdp:DynaSAND>\n";
        content += "            <cmdp:Collection>\n";
        content += "                <cmdp:GeneralInfo>\n";
        content += "                    <cmdp:Name>DiDDD</cmdp:Name>\n";
        content += "                    <cmdp:ID>id1234</cmdp:ID>\n";
        content += "                </cmdp:GeneralInfo>\n";
        content += "                <cmdp:Project>\n";
        content += "                    <cmdp:Name>DiDDD-project</cmdp:Name>\n";
        content += "                </cmdp:Project>\n";
        content += "            </cmdp:Collection>\n";
        content += "        </cmdp:DynaSAND>\n";
        content += "    </cmd:Components>\n";
        content += "</cmd:CMD>\n";
        File sessionFile = createCmdiFile("testSession", content);

        config.setValueMappingsFile(createTmpFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        		"\n" + 
        		"<value-mappings>\n" + 
        		"<origin-facet name=\"projectName\">\n" + 
        		"  <value-map>\n" +
        		"  <target-facet name=\"subject\" />" + 
        		"  <target-facet name=\"projectName\" removeSourceValue=\"true\"/>" + 
        		"  	<target-value-set>\n" + 
        		"  		<target-value>blabla1</target-value>\n" + 
        		"  		<source-value isRegex=\"true\">DiDDD.+</source-value>\n" + 
        		"  	</target-value-set>\n" + 
        		"  </value-map>\n" + 
        		"</origin-facet>\n" + 
        		"</value-mappings>\n" + 
        		""));



        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);

        //since this facet permits only one value the value from the cmdi-file should be taken and hence those from the cfm be ignored
        assertEquals("blabla1", getValue(doc, fieldNameService.getFieldName(FieldKey.SUBJECT)));
        assertEquals("blabla1", getValue(doc, fieldNameService.getFieldName(FieldKey.PROJECT_NAME)));
        

    }
    
    @Test
    public void testMapToNull() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1289827960126\">\n";
        content += "   <Header>\n";
        content += "      <MdSelfLink>testID2</MdSelfLink>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1289827960126</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "      <ResourceProxyList>\n";
        content += "         <ResourceProxy id=\"refLink\">\n";
        content += "            <ResourceType>Resource</ResourceType>\n";
        content += "            <ResourceRef>hdl:1234/567-890-abc</ResourceRef>\n";
        content += "         </ResourceProxy>\n";
        content += "         <ResourceProxy id=\"refLink2\">\n";
        content += "            <ResourceType>Resource</ResourceType>\n";
        content += "            <ResourceRef>file://bla.resource2.txt</ResourceRef>\n";
        content += "         </ResourceProxy>\n";
        content += "      </ResourceProxyList>\n";
        content += "   </Resources>\n";
        content += "   <Components>\n";
        content += "     <cmdp:LrtInventoryResource>\n";
        content += "         <cmdp:LrtCommon>\n";
        content += "             <cmdp:ResourceName>PALIC</cmdp:ResourceName>\n";
        content += "             <cmdp:ResourceType>Televisie</cmdp:ResourceType>\n";
        content += "         </cmdp:LrtCommon>\n";
        content += "     </cmdp:LrtInventoryResource>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File sessionFile = createCmdiFile("testSession", content);

        config.setValueMappingsFile(createTmpFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "\n" + 
                "<value-mappings>\n" + 
                "<origin-facet name=\"resourceClass\">\n" + 
                "  <value-map>\n" +
                "   <target-value-set>\n" + 
                "       <target-value facet=\"resourceClass\" removeSourceValue=\"true\"></target-value>\n" + 
                "       <source-value>Televisie</source-value>\n" + 
                "   </target-value-set>\n" + 
                "  </value-map>\n" + 
                "</origin-facet>\n" + 
                "</value-mappings>\n" + 
                ""));



        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);

        //since this facet permits only one value the value from the cmdi-file should be taken and hence those from the cfm be ignored
        assertEquals(null, getValue(doc, fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS)));

        

    }

    private Object getValue(SolrInputDocument doc, String field) {
        if (doc.getFieldValues(field) != null) {
            assertEquals(1, doc.getFieldValues(field).size());
            return doc.getFieldValue(field);
        } else {
            return null;
        }
    }

    private Collection<Object> getMultipleValues(SolrInputDocument doc, String field) {

        return doc.getFieldValues(field);

    }

    private List<SolrInputDocument> importData(File rootFile) throws Exception {
        
         /* Read configuration in ImporterTestCase.setup and change the setup to
         * suit the test. */
         
        modifyConfig(rootFile);

        final DummySolrBridgeImpl solrBridge = new DummySolrBridgeImpl();
        MetadataImporter importer = new MetadataImporter(config, languageCodeUtils, solrBridge) {
            /*
             * Because in the test, the solr server is not assumed to be 
             * available, override the importer's class startImport method by
             * leaving out interaction with server. 
             * 
             * By invoking the importRecord method, the class being defined here
             * needs to anticipate on an exception possibly thrown by the 
             * importRecord method invoking the sendDocs method. Please note 
             * however, that the latter method is overriden, and the actual 
             * database is being replaced by an array of documents.
             */
            @Override
            void startImport() throws MalformedURLException {

                // make sure the mapping file for testing is used
                config.setFacetConceptsFile(getTestFacetConceptFilePath());

                List<DataRoot> dataRoots = checkDataRoots();
                long start = System.currentTimeMillis();
                try {

                    for (DataRoot dataRoot : dataRoots) {
                        LOG.info("Start of processing: "
                                + dataRoot.getOriginName());
                        List<File> files
                                = getFilesFromDataRoot(dataRoot.getRootFile()).get(0);
                        for (File file : files) {
                            if (config.getMaxFileSize() > 0
                                    && file.length()
                                    > config.getMaxFileSize()) {
                                LOG.info("Skipping " + file.getAbsolutePath()
                                        + " because it is too large.");
                            } else {
                                LOG.debug("PROCESSING FILE: {}", file.getAbsolutePath());
                                
                                 /* Anticipate on the solr exception that will
                                 * never by raised because sendDocs is overriden
                                 * in a suitable way. */
                                 
                                try {
                                    getRecordProcessor().importRecord(file, Optional.of(dataRoot), Optional.empty(), Optional.empty());
                                } catch (DocumentStoreException ex) {
                                    Logger.getLogger(ValueMappingsTest.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                        LOG.info("End of processing: "
                                + dataRoot.getOriginName());
                    }

                } catch (IOException e) {
                    LOG.error("error updating files:\n", e);
                }
            }
        };
        importer.startImport();
        return solrBridge.getDocuments();
    }

    private void modifyConfig(File rootFile) throws URISyntaxException {
        DataRoot dataRoot = new DataRoot();
        dataRoot.setDeleteFirst(false); // cannot delete becanot using real solrServer
        dataRoot.setOriginName("testRoot");
        dataRoot.setRootFile(rootFile);
        dataRoot.setTostrip("");
        dataRoot.setPrefix("http://example.com");
        config.setDataRoots(Collections.singletonList(dataRoot));
        config.setFacetConceptsFile(ImporterTestcase.getTestFacetConceptFilePath());
        
    }
    


}

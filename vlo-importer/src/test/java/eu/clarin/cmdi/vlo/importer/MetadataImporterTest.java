package eu.clarin.cmdi.vlo.importer;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.importer.solr.DummySolrBridgeImpl;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.DataRoot;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class MetadataImporterTest extends ImporterTestcase {

    protected final static org.slf4j.Logger LOG = LoggerFactory.getLogger(MetadataImporterTest.class);

    private final static String TEST_RESOURCE_SECTION
            = "   <cmd:Resources>\n"
            + "     <cmd:ResourceProxyList><cmd:ResourceProxy><cmd:ResourceType>Resource</cmd:ResourceType><cmd:ResourceRef>http://example.org/resource</cmd:ResourceRef></cmd:ResourceProxy></cmd:ResourceProxyList>\n"
            + "   </cmd:Resources>\n";

    @Test
    public void testImporterSimple() throws Exception {
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
        session += "         <cmdp:Name>kleve-route</cmdp:Name>\n";
        session += "         <cmdp:Title>kleve-route-title</cmdp:Title>\n";
        session += "      </cmdp:Session>\n";
        session += "   </Components>\n";
        session += "</CMD>\n";
        File sessionFile = createCmdiFile("testSession", session);

        List<SolrInputDocument> docs = importData(sessionFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("testID1Session", getValue(doc, FieldKey.ID));
        assertEquals("CollectionName", getValue(doc, FieldKey.COLLECTION));
        assertEquals("testRoot", getValue(doc, FieldKey.DATA_PROVIDER));
        assertEquals("kleve-route", getValue(doc, FieldKey.NAME));
        assertEquals(sessionFile.getAbsolutePath(), getValue(doc, FieldKey.FILENAME));
        assertEquals("video/x-mpeg1", getValue(doc, FieldKey.FORMAT));
        assertEquals("video/x-mpeg1|../Media/elan-example1.mpg", getValue(doc, FieldKey.RESOURCE));
    }

    @Test
    public void testImportWithMimeTypeOverride() throws Exception {
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
        content += "            <ResourceRef>http://terminotica.upf.es/CREL/LIC01.htm</ResourceRef>\n";
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
        content += "             <cmdp:ResourceType>Application / Tool</cmdp:ResourceType>\n";
        content += "             <cmdp:ResourceType>Text</cmdp:ResourceType>\n";
        content += "         </cmdp:LrtCommon>\n";
        content += "     </cmdp:LrtInventoryResource>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File rootFile = createCmdiFile("rootFile", content);

        List<SolrInputDocument> docs = importData(rootFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("PALIC", getValue(doc, FieldKey.NAME));
        Collection<Object> fieldValues = doc.getFieldValues(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS));
        assertEquals(2, fieldValues.size());
        List<String> values = new ArrayList(fieldValues);
        Collections.sort(values);
        assertEquals("Application / Tool", values.get(0));
        assertEquals("Text", values.get(1));
        fieldValues = doc.getFieldValues(fieldNameService.getFieldName(FieldKey.RESOURCE));
        assertEquals(2, fieldValues.size());
        values = new ArrayList(fieldValues);
        Collections.sort(values);
        assertEquals("unknown type|file://bla.resource2.txt", values.get(0));
        assertEquals("unknown type|http://terminotica.upf.es/CREL/LIC01.htm", values.get(1));
    }

    @Test
    public void testImportWithNameSpaceGalore() throws Exception {
        String content = "";
        content += "<cmdi:CMD CMDVersion=\"1.1\" xmlns:cmdi=\"http://www.clarin.eu/cmd/1\" xmlns:cmdip=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1290431694629\"\n";
        content += "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.clarin.eu/cmd/ http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1290431694629/xsd\">\n";
        content += "    <cmdi:Header/>\n";
        content += "    <cmdi:Resources>\n";
        content += "        <cmdi:ResourceProxyList>\n";
        content += "            <cmdi:ResourceProxy id=\"TEI\">\n";
        content += "                <cmdi:ResourceType>Resource</cmdi:ResourceType>\n";
        content += "                <cmdi:ResourceRef>http://hdl.handle.net/11858/00-175C-0000-0000-E180-8?urlappend=/TEI</cmdi:ResourceRef>\n";
        content += "            </cmdi:ResourceProxy>\n";
        content += "        </cmdi:ResourceProxyList>\n";
        content += "        <cmdi:JournalFileProxyList/>\n";
        content += "        <cmdi:ResourceRelationList/>\n";
        content += "    </cmdi:Resources>\n";
        content += "    <cmdi:Components>\n";
        content += "        <cmdip:EastRepublican ref=\"TEI\">\n";
        content += "            <cmdip:GeneralInformation>\n";
        content += "                <cmdip:Identifier>hdl:11858/00-175C-0000-0000-E180-8</cmdip:Identifier>\n";
        content += "                <cmdip:Title>L'Est R\u00e9publicain : \u00e9dition du 17 mai 1999</cmdip:Title>\n";
        content += "            </cmdip:GeneralInformation>\n";
        content += "        </cmdip:EastRepublican>\n";
        content += "    </cmdi:Components>\n";
        content += "</cmdi:CMD>\n";

        File rootFile = createCmdiFile("rootFile", content);

        List<SolrInputDocument> docs = importData(rootFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("hdl_58_11858_47_00-175C-0000-0000-E180-8", getValue(doc, FieldKey.ID));
        assertEquals("L'Est R\u00e9publicain : \u00e9dition du 17 mai 1999", getValue(doc, FieldKey.NAME));
        assertEquals("unknown type|http://hdl.handle.net/11858/00-175C-0000-0000-E180-8?urlappend=/TEI", getValue(doc, FieldKey.RESOURCE));
    }

    @Test
    public void testNoIdTakeFileName() throws Exception {
        String session = "";
        session += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        session += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\">\n";
        session += "   <Header>\n";
        session += "      <MdProfile>clarin.eu:cr1:p_1271859438204</MdProfile>\n";
        session += "   </Header>\n";
        session += "   <Resources>\n";
        session += "      <ResourceProxyList>\n";
        session += "          <ResourceProxy>\n";
        session += "             <ResourceType>Resource</ResourceType>\n";
        session += "             <ResourceRef>http://example.org/resource</ResourceRef>\n";
        session += "          </ResourceProxy>\n";
        session += "      </ResourceProxyList>\n";
        session += "   </Resources>\n";
        session += "   <Components>\n";
        session += "      <Session>\n";
        session += "         <Name>kleve-route</Name>\n";
        session += "         <Title>kleve-route-title</Title>\n";
        session += "      </Session>\n";
        session += "   </Components>\n";
        session += "</CMD>\n";
        File sessionFile = createCmdiFile("testSession", session);

        List<SolrInputDocument> docs = importData(sessionFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("testRoot/" + sessionFile.getName(), getValue(doc, FieldKey.ID));
    }

    @Test
    public void testProjectName() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<cmd:CMD xmlns:cmd=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1280305685235\">\n";
        content += "   <cmd:Header>\n";
        content += "      <cmd:MdProfile>clarin.eu:cr1:p_1280305685235</cmd:MdProfile>\n";
        content += "   </cmd:Header>\n";
        content += TEST_RESOURCE_SECTION;
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

        List<SolrInputDocument> docs = importData(sessionFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals(null, getValue(doc, FieldKey.COLLECTION));
        assertEquals("DiDDD-project", getValue(doc, FieldKey.PROJECT_NAME));
    }

    @Test
    public void testMissingResourceProxies() throws Exception {
        String session = "";
        session += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        session += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\">\n";
        session += "   <Header>\n";
        session += "      <MdProfile>clarin.eu:cr1:p_1271859438204</MdProfile>\n";
        session += "   </Header>\n";
        session += "   <Resources/>\n";
        session += "   <Components>\n";
        session += "      <Session>\n";
        session += "         <Name>kleve-route</Name>\n";
        session += "         <Title>kleve-route-title</Title>\n";
        session += "      </Session>\n";
        session += "   </Components>\n";
        session += "</CMD>\n";
        File sessionFile = createCmdiFile("testSession", session);

        List<SolrInputDocument> docs = importData(sessionFile);
        assertEquals(0, docs.size());
    }

    @Test
    public void testDerivedFacetsWithPostProcessing() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<cmd:CMD xmlns:cmd=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1289827960126\">\n"
                + "    <cmd:Header>\n"
                + "        <cmd:MdProfile>clarin.eu:cr1:p_1289827960126</cmd:MdProfile>\n"
                + "    </cmd:Header>\n"
                + TEST_RESOURCE_SECTION
                + "    <cmd:Components>\n"
                + "        <cmdp:LrtInventoryResource>\n"
                + "            <cmdp:LrtCommon>\n"
                + "                <cmdp:Languages>\n"
                + "                    <cmdp:ISO639>\n"
                + "                        <cmdp:iso-639-3-code>nld</cmdp:iso-639-3-code>\n"
                + "                    </cmdp:ISO639>\n"
                + "                </cmdp:Languages>\n"
                + "                <cmdp:Countries>\n"
                + "                </cmdp:Countries>\n"
                + "            </cmdp:LrtCommon>\n"
                + "        </cmdp:LrtInventoryResource>\n"
                + "    </cmd:Components>\n"
                + "</cmd:CMD>";
        File rootFile = createCmdiFile("example", content);

        List<SolrInputDocument> docs = importData(rootFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);

        assertEquals("ISO code mapping to langauge code", "code:nld", getValue(doc, FieldKey.LANGUAGE_CODE));
        assertEquals("Language code -> language name post processing", "Dutch", getValue(doc, FieldKey.LANGUAGE_NAME));
    }

    @Test
    public void testDefaultValuePostProcessing() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<cmd:CMD xmlns:cmd=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1475136016208\">\n"
                + "    <cmd:Header>\n"
                + "        <cmd:MdProfile>clarin.eu:cr1:p_1475136016208</cmd:MdProfile>\n"
                + "    </cmd:Header>\n"
                + TEST_RESOURCE_SECTION
                + "    <cmd:Components>\n"
                + "        <cmdp:EDM>\n"
                + "            <cmdp:edm-Aggregation>\n"
                + "                <cmdp:edm-rights>\n"
                + "                    <cmdp:rightsURI>PUB</cmdp:rightsURI>\n"
                + "                </cmdp:edm-rights>\n"
                + "            </cmdp:edm-Aggregation>\n"
                + "        </cmdp:EDM>\n"
                + "    </cmd:Components>\n"
                + "</cmd:CMD>";
        File rootFile = createCmdiFile("example", content);

        List<SolrInputDocument> docs = importData(rootFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);

        // 'reflective' postprocessing, i.e. we have a post processor that acts on 'null' values, uses value from an already populated field to populate its target field
        assertEquals("PRECONDITION: Availability filled in from doc value", "PUB", getValue(doc, FieldKey.AVAILABILITY));
        assertEquals("Explicit license filled in from availability", "PUB", getValue(doc, FieldKey.LICENSE_TYPE));
    }

    @Test
    public void testMultilingualValues() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<cmd:CMD xmlns:cmd=\"http://www.clarin.eu/cmd/1\" xmlns=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1475136016208\">\n"
                + "    <cmd:Header>\n"
                + "        <cmd:MdProfile>clarin.eu:cr1:p_1475136016208</cmd:MdProfile>\n"
                + "    </cmd:Header>\n"
                + TEST_RESOURCE_SECTION
                + "    <cmd:Components>\n"
                + "        <EDM>\n"
                + "            <ProvidedCHOProxy>\n"
                + "                <edm-ProvidedCHO>\n"
                + "                    <dc-description xml:lang=\"fr\">Line 1</dc-description>\n"
                + "                    <dc-description xml:lang=\"en\">Line 2</dc-description>\n"
                + "                    <dc-description>Line 3</dc-description>\n"
                + "                    <dc-description xml:lang=\"en\">Line 4</dc-description>\n"
                + "                </edm-ProvidedCHO>\n"
                + "            </ProvidedCHOProxy>\n"
                + "        </EDM>\n"
                + "    </cmd:Components>\n"
                + "</cmd:CMD>";
        File rootFile = createCmdiFile("example", content);

        List<SolrInputDocument> docs = importData(rootFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);

        final List<Object> fieldValues = ImmutableList.copyOf(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.DESCRIPTION)));
        assertThat("Order should be preserved except for preferred language priority",
                fieldValues, contains(
                        "{code:eng}Line 2", //English first
                        "{code:eng}Line 4", //English first
                        "{code:fra}Line 1", //Then keep order
                        "{code:und}Line 3")); //Then keep order
    }

    private Object getValue(SolrInputDocument doc, FieldKey key) {
        String field = fieldNameService.getFieldName(key);
        if (doc.getFieldValues(field) != null) {
            assertEquals(1, doc.getFieldValues(field).size());
            return doc.getFieldValue(field);
        } else {
            return null;
        }
    }

    private List<SolrInputDocument> importData(File rootFile) throws Exception {
        /*
         * Read configuration in ImporterTestCase.setup and change the setup to
         * suit the test.
         */
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
                                /*
                                 * Anticipate on the solr exception that will
                                 * never by raised because sendDocs is overriden
                                 * in a suitable way.
                                 */
                                try {
                                    getRecordProcessor().importRecord(file, Optional.of(dataRoot), Optional.empty(), Optional.empty());
                                } catch (DocumentStoreException ex) {
                                    Logger.getLogger(MetadataImporterTest.class.getName()).log(Level.SEVERE, null, ex);
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
        config.setValueMappingsFile(ImporterTestcase.getTestValueMappingsFilePath());

    }

}

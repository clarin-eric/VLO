package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.importer.solr.DummySolrBridgeImpl;
import eu.clarin.cmdi.vlo.FacetConstants.KEY;
import eu.clarin.cmdi.vlo.config.DataRoot;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class CrossfacetMappingTest extends ImporterTestcase {

    protected final static org.slf4j.Logger LOG = LoggerFactory.getLogger(CrossfacetMappingTest.class);

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
        session += "         <cmdp:Name>kleve-route</cmdp:Name>\n";
        session += "         <cmdp:Title>kleve-route-title</cmdp:Title>\n";
        session += "      </cmdp:Session>\n";
        session += "   </Components>\n";
        session += "</CMD>\n";
        File sessionFile = createCmdiFile("testSession", session);

        this.config.setUseCrossMapping(true);
        this.config.setCrossFacetMapUrl(new File(this.getClass().getResource("/cfmTest.xml").toURI()).getAbsolutePath());

        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);

        assertEquals("blabla", getValue(doc, fieldNameService.getFieldName(KEY.FIELD_SUBJECT)));

    }

    @Test
    public void testSingleValueWithOrigin() throws Exception {
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

        this.config.setUseCrossMapping(true);
        this.config.setCrossFacetMapUrl(new File(this.getClass().getResource("/cfmTest.xml").toURI()).getAbsolutePath());

        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);

        //since this facet permits only one value the value from the cmdi-file should be taken and hence those from the cfm be ignored
        assertEquals("kleve-route", getValue(doc, fieldNameService.getFieldName(KEY.FIELD_NAME)));
        //to be sure that it works for a facet where no value is set
        assertEquals("cfmvalue", getValue(doc, fieldNameService.getFieldName(KEY.FIELD_TEMPORAL_COVERAGE)));
    }

    @Test
    public void testSingleValueWithNoOrigin() throws Exception {
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
        session += "   </Components>\n";
        session += "</CMD>\n";
        File sessionFile = createCmdiFile("testSession", session);

        this.config.setUseCrossMapping(true);
        this.config.setCrossFacetMapUrl(new File(this.getClass().getResource("/cfmTest.xml").toURI()).getAbsolutePath());

        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);

        //since this facet permits only one value the value from the cmdi-file should be taken and hence those from the cfm be ignored
        assertEquals("cfmvalue", getValue(doc, fieldNameService.getFieldName(KEY.FIELD_NAME)));

    }

    @Test
    public void testMultipleValues() throws Exception {
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
        session += "      <cmdp:media-corpus-profile>\n";
        session += "         <cmdp:Corpus>\n";
        session += "             <cmdp:Topic>mysubject</cmdp:Topic>\n";
        session += "         </cmdp:Corpus>";
        session += "      </cmdp:media-corpus-profile>\n";
        session += "      <cmdp:Session>\n";
        session += "         <cmdp:Name>kleve-route</cmdp:Name>\n";
        session += "         <cmdp:Title>kleve-route-title</cmdp:Title>\n";
        session += "      </cmdp:Session>\n";
        session += "   </Components>\n";
        session += "</CMD>\n";

        File sessionFile = createCmdiFile("testSession", session);

        this.config.setUseCrossMapping(true);
        this.config.setCrossFacetMapUrl(new File(this.getClass().getResource("/cfmTest.xml").toURI()).getAbsolutePath());

        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);

        Object[] values = getMultipleValues(doc, fieldNameService.getFieldName(KEY.FIELD_SUBJECT)).toArray();

        assertEquals(2, values.length);
        //assertEquals("", getMultipleValues(doc, FacetConstants.FIELD_SUBJECT).);
        assertEquals("blabla", values[0]);
        assertEquals("mysubject", values[1]);

    }

    @Test
    public void testValueSplit() throws Exception {
        String session = "";
        session += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        session += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1271859438204\">\n";
        session += "   <Header>\n";
        session += "      <MdCreationDate>2008-05-27</MdCreationDate>\n";
        session += "      <MdSelfLink> testID1Session</MdSelfLink>\n";
        session += "      <MdCollectionDisplayName>MYName</MdCollectionDisplayName>\n";
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

        this.config.setUseCrossMapping(true);
        this.config.setCrossFacetMapUrl(new File(this.getClass().getResource("/cfmTest.xml").toURI()).getAbsolutePath());

        List<SolrInputDocument> docs = importData(sessionFile);

        SolrInputDocument doc = docs.get(0);

        Object[] values = getMultipleValues(doc, fieldNameService.getFieldName(KEY.FIELD_COLLECTION)).toArray();

        //three values set
        assertEquals(3, values.length);
        //to be sure that it works for a facet where no value is set
        assertEquals("collection1", values[0]);
        assertEquals("collection2", values[1]);
        assertEquals("collection3", values[2]);
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
             * By invoking the processCmdi method, the class being defined here
             * needs to anticipate on an exception possibly thrown by the 
             * processCmdi method invoking the sendDocs method. Please note 
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
                                    processCmdi(file, dataRoot, null);
                                } catch (SolrServerException ex) {
                                    Logger.getLogger(CrossfacetMappingTest.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                        LOG.info("End of processing: "
                                + dataRoot.getOriginName());
                    }

                } catch (IOException e) {
                    LOG.error("error updating files:\n", e);
                } finally {

                }
                long took = (System.currentTimeMillis() - start) / 1000;
                LOG.info("Found " + nrOfFilesWithoutId
                        + " file(s) without an id. (id is generated based on fileName but that may not be unique)");
                LOG.info("Found " + nrOfFilesWithError
                        + " file(s) with errors.");
                LOG.info("Update of " + nrOFDocumentsSent + " took " + took
                        + " secs. Total nr of files analyzed " + nrOfFilesAnalyzed);
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

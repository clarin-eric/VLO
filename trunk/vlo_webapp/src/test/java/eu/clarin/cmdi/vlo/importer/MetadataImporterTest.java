package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import eu.clarin.cmdi.vlo.FacetConstants;

public class MetadataImporterTest extends ImporterTestcase {

    @Test
    public void testImporterSimple() throws Exception {
        String session = "";
        session += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        session += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
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
        session += "      <Session>\n";
        session += "         <Name>kleve-route</Name>\n";
        session += "         <Title>kleve-route-title</Title>\n";
        session += "      </Session>\n";
        session += "   </Components>\n";
        session += "</CMD>\n";
        File sessionFile = createCmdiFile("testSession", session);

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1274880881885/xsd\">\n";
        content += "   <Header>\n";
        content += "      <MdSelfLink>testID2</MdSelfLink>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "      <ResourceProxyList>\n";
        content += "         <ResourceProxy id=\"d28635e19\">\n";
        content += "            <ResourceType>Metadata</ResourceType>\n";
        content += "            <ResourceRef>" + sessionFile.getName() + "</ResourceRef>\n";
        content += "         </ResourceProxy>\n";
        content += "      </ResourceProxyList>\n";
        content += "   </Resources>\n";
        content += "   <Components>\n";
        content += "      <imdi-corpus>\n";
        content += "         <Corpus>\n";
        content += "            <Name>MPI corpora</Name>\n";
        content += "         </Corpus>\n";
        content += "      </imdi-corpus>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File rootFile = createCmdiFile("rootFile", content);

        List<SolrInputDocument> docs = importData(rootFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("testID1Session", getValue(doc, FacetConstants.FIELD_ID));
        assertEquals("CollectionName", getValue(doc, FacetConstants.FIELD_ORIGIN));
        assertEquals("testRoot", getValue(doc, FacetConstants.FIELD_DATA_PROVIDER));
        assertEquals("kleve-route", getValue(doc, FacetConstants.FIELD_NAME));
        assertEquals(sessionFile.getAbsolutePath(), getValue(doc, FacetConstants.FIELD_FILENAME));
        assertEquals("video", getValue(doc, FacetConstants.FIELD_RESOURCE_TYPE));
        assertEquals("video/x-mpeg1|../Media/elan-example1.mpg", getValue(doc, FacetConstants.FIELD_RESOURCE));
    }

    @Test
    public void testImportWithMimeTypeOverride() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
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
        content += "      </ResourceProxyList>\n";
        content += "   </Resources>\n";
        content += "   <Components>\n";
        content += "     <LrtInventoryResource>\n";
        content += "         <LrtCommon>\n";
        content += "             <ResourceName>PALIC</ResourceName>\n";
        content += "             <ResourceType>Application / Tool</ResourceType>\n";
        content += "         </LrtCommon>\n";
        content += "     </LrtInventoryResource>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File rootFile = createCmdiFile("rootFile", content);

        List<SolrInputDocument> docs = importData(rootFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("PALIC", getValue(doc, FacetConstants.FIELD_NAME));
        assertEquals("Application / Tool", getValue(doc, FacetConstants.FIELD_RESOURCE_TYPE));
        assertEquals("unknown type|http://terminotica.upf.es/CREL/LIC01.htm", getValue(doc, FacetConstants.FIELD_RESOURCE));
    }

    @Test
    public void testImportWithNameSpaceGalore() throws Exception {
        String content = "";
        content += "<cmdi:CMD CMDVersion=\"1.1\" xmlns:cmdi=\"http://www.clarin.eu/cmd/\"\n";
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
        content += "        <cmdi:EastRepublican ref=\"TEI\">\n";
        content += "            <cmdi:GeneralInformation>\n";
        content += "                <cmdi:Identifier>hdl:11858/00-175C-0000-0000-E180-8</cmdi:Identifier>\n";
        content += "                <cmdi:Title>L'Est R\u00e9publicain : \u00e9dition du 17 mai 1999</cmdi:Title>\n";
        content += "            </cmdi:GeneralInformation>\n";
        content += "        </cmdi:EastRepublican>\n";
        content += "    </cmdi:Components>\n";
        content += "</cmdi:CMD>\n";

        File rootFile = createCmdiFile("rootFile", content);

        List<SolrInputDocument> docs = importData(rootFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("hdl:11858/00-175C-0000-0000-E180-8", getValue(doc, FacetConstants.FIELD_ID));
        assertEquals("L'Est R\u00e9publicain : \u00e9dition du 17 mai 1999", getValue(doc, FacetConstants.FIELD_NAME));
        assertEquals("unknown type|http://hdl.handle.net/11858/00-175C-0000-0000-E180-8?urlappend=/TEI", getValue(doc,
                FacetConstants.FIELD_RESOURCE));
    }

    @Test
    public void testNoIdTakeFileName() throws Exception {
        String session = "";
        session += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        session += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
        session += "   <Header>\n";
        session += "      <MdProfile>clarin.eu:cr1:p_1271859438204</MdProfile>\n";
        session += "   </Header>\n";
        session += "   <Resources>\n";
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
        assertEquals("testRoot/" + sessionFile.getName(), getValue(doc, FacetConstants.FIELD_ID));
    }

    @Test
    public void testProjectName() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1280305685235</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "   </Resources>\n";
        content += "    <Components>\n";
        content += "        <DynaSAND>\n";
        content += "            <Collection>\n";
        content += "                <GeneralInfo>\n";
        content += "                    <Name>DiDDD</Name>\n";
        content += "                    <ID>id1234</ID>\n";
        content += "                </GeneralInfo>\n";
        content += "                <Project>\n";
        content += "                    <Name>DiDDD-project</Name>\n";
        content += "                </Project>\n";
        content += "            </Collection>\n";
        content += "        </DynaSAND>\n";
        content += "    </Components>\n";
        content += "</CMD>\n";
        File sessionFile = createCmdiFile("testSession", content);

        List<SolrInputDocument> docs = importData(sessionFile);
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("testRoot", getValue(doc, FacetConstants.FIELD_ORIGIN));
        assertEquals("DiDDD-project", getValue(doc, FacetConstants.FIELD_PROJECT_NAME));
    }

    private Object getValue(SolrInputDocument doc, String field) {
        assertEquals(1, doc.getFieldValues(field).size());
        return doc.getFieldValue(field);
    }

    private List<SolrInputDocument> importData(File rootFile) throws MalformedURLException {
        final List<SolrInputDocument> result = new ArrayList<SolrInputDocument>();
        ImporterConfig config = createConfig(rootFile);
        MetadataImporter importer = new MetadataImporter(config) {
            @Override
            protected void initSolrServer() throws MalformedURLException {
                //do nothing no solrserver in test
            }

            @Override
            protected void sendDocs() throws SolrServerException, IOException {
                //overriding here so we can test the docs
                result.addAll(this.docs);
                docs = new ArrayList<SolrInputDocument>();
            }
        };
        importer.startImport();
        return result;
    }

    private ImporterConfig createConfig(File rootFile) {
        ImporterConfig config = new ImporterConfig();
        DataRoot dataRoot = new DataRoot();
        dataRoot.setDeleteFirst(false);//cannot delete not using real solrServer
        dataRoot.setOriginName("testRoot");
        dataRoot.setRootFile(rootFile);
        config.setDataRoots(Collections.singletonList(dataRoot));
        return config;
    }

}

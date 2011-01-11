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
        session += "<CMD>\n";
        session += "   <Header>\n";
        session += "      <MdCreationDate>2008-05-27</MdCreationDate>\n";
        session += "      <MdSelfLink>testID1Session</MdSelfLink>\n";
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
        session += "      </Session>\n";
        session += "   </Components>\n";
        session += "</CMD>\n";
        File sessionFile = createCmdiFile("testSession", session);

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD>\n";
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

        List<SolrInputDocument> docs = importData(rootFile, getIMDIFacetMap());
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("testID1Session", getValue(doc, FacetConstants.FIELD_ID));
        assertEquals("testRoot", getValue(doc, FacetConstants.FIELD_ORIGIN)); //TODO PD make _dataRoot and origin facet, make all none showable fields start with _
        assertEquals("kleve-route", getValue(doc, FacetConstants.FIELD_NAME));
        assertEquals(sessionFile.getAbsolutePath(), getValue(doc, FacetConstants.FIELD_FILENAME));
        assertEquals("video", getValue(doc, FacetConstants.FIELD_RESOURCE_TYPE));
        assertEquals("video/x-mpeg1|../Media/elan-example1.mpg", getValue(doc, FacetConstants.FIELD_RESOURCE));
    }

    @Test
    public void testImportWithMimeTypeOverride() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD>\n";
        content += "   <Header>\n";
        content += "      <MdSelfLink>testID2</MdSelfLink>\n";
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

        List<SolrInputDocument> docs = importData(rootFile, getLrtFacetMap());
        assertEquals(1, docs.size());
        SolrInputDocument doc = docs.get(0);
        assertEquals("PALIC", getValue(doc, FacetConstants.FIELD_NAME));
        assertEquals("Application / Tool", getValue(doc, FacetConstants.FIELD_RESOURCE_TYPE));
        assertEquals("unknown type|http://terminotica.upf.es/CREL/LIC01.htm", getValue(doc, FacetConstants.FIELD_RESOURCE));
    }

    private Object getValue(SolrInputDocument doc, String field) {
        assertEquals(1, doc.getFieldValues(field).size());
        return doc.getFieldValue(field);
    }

    private List<SolrInputDocument> importData(File rootFile, FacetMapping facetMapping) throws MalformedURLException {
        final List<SolrInputDocument> result = new ArrayList<SolrInputDocument>();
        ImporterConfig config = createConfig(rootFile, facetMapping);
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

    private ImporterConfig createConfig(File rootFile, FacetMapping facetMapping) {
        ImporterConfig config = new ImporterConfig();
        DataRoot dataRoot = new DataRoot();
        dataRoot.setFacetMapping(facetMapping);
        dataRoot.setDeleteFirst(false);//cannot delete not using real solrServer
        dataRoot.setOriginName("testRoot");
        dataRoot.setRootFile(rootFile);
        config.setDataRoots(Collections.singletonList(dataRoot));
        return config;
    }

}

package eu.clarin.cmdi.vlo.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.hasItems;

/**
 *
 * @author keeloo
 */
public class DefaultVloConfigFactoryTest {

    public DefaultVloConfigFactoryTest() {
    }

    private VloConfig config;
    private Properties testProps;

    @Before
    public void setUp() throws Exception {
        config = new DefaultVloConfigFactory().newConfig();
        testProps = new Properties();
        testProps.load(getClass().getResourceAsStream("/vloconfig.properties"));
    }

    /**
     * Test the getDataRoots method.<br><br>
     *
     * Use the values defined in the packaged {@literal VloConfig.xml}
     * configuration file.
     */
    @Test
    public void testGetDataRoots() {
        final List<DataRoot> rootsReturned = config.getDataRoots();

        assertNotNull(rootsReturned);
        assertTrue("One or more data roots should be defined", rootsReturned.size() > 0);
        assertNotNull(rootsReturned.get(0).getOriginName());
        assertTrue("Origin name cannot be empty", rootsReturned.get(0).getOriginName().length() > 0);
        assertNotNull(rootsReturned.get(0).getPrefix());
        assertTrue("Prefix cannot be empty", rootsReturned.get(0).getPrefix().length() > 0);
        assertNotNull(rootsReturned.get(0).getRootFile());
        assertTrue("Root file cannot be empty", rootsReturned.get(0).getRootFile().getName().length() > 0);
        assertNotNull(rootsReturned.get(0).getToStrip());
    }

    /**
     * Test the set setDataRoots method
     */
    @Test
    public void testSetDataRoots() {

        List<DataRoot> dataRoots = Arrays.asList(
                new DataRoot("MPI IMDI Archive",
                        new File("/lat/apache/htdocs/oai-harvester/mpi-self-harvest/harvested/results/cmdi/"),
                        "http://catalog.clarin.eu/",
                        "/lat/apache/htdocs/", false),
                new DataRoot("CMDI Providers",
                        new File("/lat/apache/htdocs/oai-harvester/cmdi-providers/harvested/results/cmdi/"),
                        "http://catalog.clarin.eu/",
                        "/lat/apache/htdocs/", false),
                new DataRoot("OLAC Metadata Providers",
                        new File("/lat/apache/htdocs/oai-harvester/olac-and-dc-providers/harvested/results/cmdi/"),
                        "http://catalog.clarin.eu/",
                        "/lat/apache/htdocs/", false));

        System.out.println("setDataRoots");

        config.setDataRoots(dataRoots);

        List rootsReturned = config.getDataRoots();

        assertEquals(dataRoots, rootsReturned);
    }

    /**
     * Test the getPagesInApplicationCache method
     */
    @Test
    public void testGetPagesInApplicationCache() {

        System.out.println("getPagesInApplicationCache");

        int expResult = 40; // as defined in vloconfig.xml
        int result = config.getPagesInApplicationCache();

        assertEquals(expResult, result);
    }

    /**
     * Test the setPagesInApplicationCache method
     */
    @Test
    public void testSetPagesInApplicationCache() {

        System.out.println("setPagesInApplicationCache");

        int param = 999;

        config.setPagesInApplicationCache(param);

        int result = config.getPagesInApplicationCache();

        assertEquals(param, result);
    }

    /**
     * Test the getSessionCacheSize method
     */
    @Test
    public void testGetSessionCacheSize() {

        System.out.println("getPagesInApplicationCache");

        int expResult = 10000; // as defined in vloconfig.xml
        int result = config.getSessionCacheSize();

        assertEquals(expResult, result);
    }

    /**
     * Test the setSessionCacheSize method
     */
    @Test
    public void testSetSessionCacheSize() {

        System.out.println("setPagesInApplicationCache");

        int param = 9999;

        config.setSessionCacheSize(param);

        int result = config.getSessionCacheSize();

        assertEquals(param, result);
    }

    /**
     * Test the getMaxDocsInList method
     */
    @Test
    public void testGetMaxDocsInList() {

        System.out.println("getMaxDocsInList");

        int expResult = 128;
        int result = config.getMaxDocsInList();

        assertEquals(expResult, result);
    }

    /**
     * Test the setMaxDocsInList method
     */
    @Test
    public void testSetMaxDocsInList() {

        System.out.println("setMaxDocsInList");

        int param = 1000;

        config.setMaxDocsInList(param);

        int result = config.getMaxDocsInList();

        assertEquals(param, result);
    }

    /**
     * Test the getMaxDocsInSolrQueue method
     */
    @Test
    public void testGetMaxDocsInSolrQueue() {

        System.out.println("getMaxDocsInSolrQueue");

        int expResult = 128;
        int result = config.getMaxDocsInList();

        assertEquals(expResult, result);
    }

    /**
     * Test the setMaxDocsInSolrQueue method
     */
    @Test
    public void testSetMaxDocsInSolrQueue() {

        System.out.println("setMaxDocsInSolrQueue");

        int param = 1000;

        config.setMaxDocsInList(param);

        int result = config.getMaxDocsInList();

        assertEquals(param, result);
    }

    /**
     * Test the getMaxFileSize method
     */
    @Test
    public void testGetMaxFileSize() {

        System.out.println("getMaxFileSize");

        int expResult = 10000000;
        int result = config.getMaxFileSize();

        assertEquals(expResult, result);
    }

    /**
     * Test the setMaxFileSize method
     */
    @Test
    public void testSetMaxFileSize() {

        System.out.println("setMaxFileSize");

        int param = 10000000;

        config.setMaxFileSize(param);

        int result = config.getMaxFileSize();

        assertEquals(param, result);
    }

    /**
     * Test the getHandleResolver method
     */
    @Test
    public void testGetUseHandleResolver() {

        System.out.println("getUseHandleResolver");

        boolean expResult = false;
        boolean result = config.getUseHandleResolver();

        assertEquals(expResult, result);
    }

    /**
     * Test the setUseHandleResolver method
     */
    @Test
    public void testSetUseHandleResolver() {

        System.out.println("setUseHandleResolver");

        boolean param = true;

        config.setUseHandleResolver(param);

        boolean result = config.getUseHandleResolver();

        assertEquals(param, result);
    }

    /**
     * Test the deleteAllFirst method
     */
    @Test
    public void testDeleteAllFirst() {

        System.out.println("deleteAllFirst");

        boolean expResult = Boolean.valueOf(testProps.getProperty("deleteAllFirst"));
        boolean result = config.getDeleteAllFirst();

        assertEquals(expResult, result);
    }

    /**
     * Test the deleteAllFirst method
     */
    @Test
    public void testSetDeleteAllFirst() {

        System.out.println("setDeleteAllFirst");

        boolean param = true;

        config.setDeleteAllFirst(param);

        boolean result = config.getDeleteAllFirst();

        assertEquals(param, result);
    }

    /**
     * Test the printMapping method
     */
    @Test
    public void testPrintMapping() {

        System.out.println("printMapping");

        boolean expResult = false;
        boolean result = config.printMapping();

        assertEquals(expResult, result);
    }

    /**
     * Test the setPrintMapping method
     */
    @Test
    public void testSetPrintMapping() {
        System.out.println("setPrintMapping");

        boolean param = false;

        config.setPrintMapping(param);

        boolean result = config.printMapping();

        assertEquals(param, result);
    }

    /**
     * Test the getHomeUrl method
     */
    @Test
    public void testGetVloHomeLink() {

        System.out.println("getVloHomeLink");

        String expResult = testProps.getProperty("homeUrl");
        String result = config.getHomeUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setHomeUrl method
     */
    @Test
    public void testSetVloHomeLink() {

        System.out.println("setVloHomeLink");

        String param = "http://www.clarin.eu/vlo";

        config.setHomeUrl(param);

        String result = config.getHomeUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getHelpUrl method
     */
    @Test
    public void testGetHelpUrl() {

        System.out.println("getHelpUrl");

        String expResult = testProps.getProperty("helpUrl");
        String result = config.getHelpUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setHelpUrl method
     */
    @Test
    public void testSetHelpUrl() {

        System.out.println("setHelpUrl");

        String param = "http://www.clarin.eu/vlo";

        config.setHelpUrl(param);

        String result = config.getHelpUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getSolrUrl method
     */
    @Test
    public void testGetSolrUrl() {

        System.out.println("getSolrUrl");

        String expResult = testProps.getProperty("solrUrl");
        String result = config.getSolrUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setHomeUrl method
     */
    @Test
    public void testSetSolrUrl() {

        System.out.println("setSolrUrl");

        String param = "http://localhost:8084/vlo_solr/";

        config.setSolrUrl(param);

        String result = config.getSolrUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getGetComponentRegistryProfileSchema method
     */
    @Test
    public void testGetComponentRegistryProfileSchema() {

        System.out.println("getComponentRegistryProfileSchema");

        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/someId/xsd";
        String result = config.getComponentRegistryProfileSchema("someId");

        assertEquals(expResult, result);
    }

    /**
     * Test the getComponentRegistryRESTURL method
     */
    @Test
    public void testGetComponentRegistryRESTURL() {

        System.out.println("getComponentRegistryRESTURL");

        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/";
        String result = config.getComponentRegistryRESTURL();

        assertEquals(expResult, result);
    }

    /**
     * Test the setComponentRegistryRESTURL method
     */
    @Test
    public void testComponentRegistryRESTURL() {

        System.out.println("setComponentRegistryRESTURL");

        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/";

        config.setComponentRegistryRESTURL(param);

        String result = config.getComponentRegistryRESTURL();

        assertEquals(param, result);
    }

    /**
     * Test the getHandleServerUrl method
     */
    @Test
    public void testGetHandleServerUrl() {

        System.out.println("getHandleServerUrl");

        String expResult = "http://hdl.handle.net/";
        String result = config.getHandleServerUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setHandleServerUrl method
     */
    @Test
    public void testSetHandleServerUrl() {

        System.out.println("setHandleServerUrl");

        String param = "http://hdl.handle.net/";

        config.setHandleServerUrl(param);

        String result = config.getHandleServerUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getLanguageLinkTemplate method
     */
    @Test
    public void testGetLanguageLinkPrefix() {

        System.out.println("getLanguageLinkPrefix");

        String expResult = "https://infra.clarin.eu/content/language_info/data/{}.html";
        String result = config.getLanguageLinkTemplate();

        assertEquals(expResult, result);
    }

    /**
     * Test the setLanguageLinkPrefix method
     */
    @Test
    public void testSetLanguageLinkPrefix() {

        System.out.println("setLanguageLinkPrefix");

        String param = "https://infra.clarin.eu/content/language_info/data/{}.html";

        config.setLanguageLinkTemplate(param);

        String result = config.getLanguageLinkTemplate();

        assertEquals(param, result);
    }

    /**
     * Test the getFeedbackFromUrl method
     */
    @Test
    public void testGetFeedbackFromUrl() {

        System.out.println("getFeedBackFromUrl");

        String expResult = "http://www.clarin.eu/node/3759?url=";
        String result = config.getFeedbackFromUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setFeedbackFromUrl method
     */
    @Test
    public void testSetFeedbackFromUrl() {

        System.out.println("setFeedbackFromUrl");

        String param = "http://www.clarin.eu/node/3759?url=";

        config.setFeedbackFromUrl(param);

        String result = config.getFeedbackFromUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getFederatedContentSearchUrl method
     */
    @Test
    public void testGetFederatedContentSearchUrl() {

        System.out.println("getFederatedContentSearchUrl");

        String expResult = "http://weblicht.sfs.uni-tuebingen.de/Aggregator/";
        String result = config.getFederatedContentSearchUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setFederatedContentSearchUrl method
     */
    @Test
    public void testSetFederatedContentSearchUrl() {

        System.out.println("setFederatedContentSearchUrl");

        String param = "http://weblicht.sfs.uni-tuebingen.de/Aggregator/";

        config.setFederatedContentSearchUrl(param);

        String result = config.getFederatedContentSearchUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getNationalProjectMapping method
     */
    @Test
    public void testGetNationalProjectMapping() {

        System.out.println("getNationalProjectMapping");

        String expResult = "nationalProjectsMapping.xml";
        String result = config.getNationalProjectMapping();

        assertEquals(expResult, result);
    }

    /**
     * Test the setNationalProjectMapping method
     */
    @Test
    public void testSetNationalProjectMapping() {

        System.out.println("setFNationalProjectMapping");

        String param = "nationalProjectsMapping.xml";

        config.setNationalProjectMapping(param);

        String result = config.getNationalProjectMapping();

        assertEquals(param, result);
    }

    /**
     * Test of getFacetFields method
     */
    @Test
    public void testGetFacetFields() {

        System.out.println("getFacetFields");

        List<String> expResult = Arrays.asList(
                "languageCode",
                "collection",
                "resourceClass",
                "modality",
                "format",
                "keywords",
                "genre",
                "subject",
                "country",
                "organisation",
                "nationalProject",
                "dataProvider");

        List<String> result = config.getFacetFields();

        assertEquals(expResult, result);
    }

    @Test
    public void testGetFacetsInSearch() {
        final String[] expItems = new String[]{
            "languageCode",
            "collection",
            "resourceClass",
            "modality",
            "format",
            "keywords",
            "genre",
            "subject",
            "country",
            "organisation",
            "dataProvider",
            "nationalProject"
        };
        List<String> result = config.getFacetsInSearch();

        //order is not important in this case
        assertThat(result, hasItems(expItems));
    }

    /**
     * Test of setFacetFields method, of class VloConfig
     */
    @Test
    public void testSetFacetFields() {

        System.out.println("setFacetFields");

        List<String> expResult = Arrays.asList(
                "language",
                "resourceClass",
                "modality",
                "genre",
                "country",
                "dataProvider",
                "nationalProject",
                "keywords");

        config.setFacetFields(expResult);

        List<String> result = config.getFacetFields();

        assertEquals(expResult, result);
    }

    /**
     * Test the getCountryComponentUrl method.
     */
    @Test
    public void testCountryComponentUrl() {

        System.out.println("getCountryComponentUrl");

        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438104/xml";
        String result = config.getCountryComponentUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setCountryComponentUrl method.
     */
    @Test
    public void testSetCountryComponentUrl() {

        System.out.println("setCountryComponentUrl");

        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438104/xml";

        config.setCountryComponentUrl(param);

        String result = config.getCountryComponentUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getLanguage2LetterCodeComponentUrl method.
     */
    @Test
    public void testGetLanguage2LetterCodeComponentUrl() {

        System.out.println("getLanguage2LetterCodeComponentUrl");

        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438109/xml";
        String result = config.getLanguage2LetterCodeComponentUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setLanguage2LetterCodeComponentUrl method.
     */
    @Test
    public void testSetLanguage2LetterCodeComponentUrl() {

        System.out.println("setLanguage2LetterCodeComponentUrl");

        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438109/xml";

        config.setLanguage2LetterCodeComponentUrl(param);

        String result = config.getLanguage2LetterCodeComponentUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getLanguage3LetterCodeComponentUrl.
     */
    @Test
    public void testGetLanguage3LetterCodeComponentUrl() {

        System.out.println("getLanguage3LetterCodeComponentUrl");

        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110/xml";
        String result = config.getLanguage3LetterCodeComponentUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setLanguage3LetterCodeComponentUrl.
     */
    @Test
    public void testSetLanguage3LetterCodeComponentUrl() {

        System.out.println("setLanguage3LetterCodeComponentUrl");

        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110/xml";

        config.setLanguage3LetterCodeComponentUrl(param);

        String result = config.getLanguage3LetterCodeComponentUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getSilToISO639CodesUrl method.
     */
    @Test
    public void testGetSilToISO639CodesUrl() {

        System.out.println("getSilToISO639CodesUrl");

        String expResult = "https://infra.clarin.eu/CMDI/1.1/xslt/sil_to_iso6393.xml";
        String result = config.getSilToISO639CodesUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setSilToISO639CodesUrl method.
     */
    @Test
    public void testSetSilToISO639CodesUrl() {

        System.out.println("setSilToISO639CodesUrl");

        String param = "http://www.clarin.eu/CMDI/1.1/xslt/sil_to_iso6393.xml";

        config.setSilToISO639CodesUrl(param);

        String result = config.getSilToISO639CodesUrl();

        assertEquals(param, result);
    }

    /**
     * Test the getReverseProxyPrefix method
     */
    @Test
    public void testReverseProxyPrefix() {

        System.out.println("getReverseProxyPrefix");

        String expResult = "";
        String result = config.getReverseProxyPrefix();

        assertEquals(expResult, result);
    }

    /**
     * Test the setReverseProxyPrefix method
     */
    @Test
    public void testSetReverseProxyPrefix() {

        System.out.println("setReverseProxyPrefix");

        String param = "vlodev/";

        config.setReverseProxyPrefix(param);

        String result = config.getReverseProxyPrefix();

        assertEquals(param, result);
    }

    /**
     * Test the getCqlEndpointFilter method
     */
    @Test
    public void testGetCqlEndpointFilter() {

        System.out.println("getCqlEndpointFilter");

        String expResult = "http://cqlservlet.mpi.nl/";
        String result = config.getCqlEndpointFilter();

        assertEquals(expResult, result);
    }

    /**
     * Test the setCqlEndpointFilter method
     */
    @Test
    public void testSetCqlEndpointFilter() {

        System.out.println("setCqlEndpointFilter");

        String param = "http://cqlservlet.mpi.nl/";

        config.setCqlEndpointFilter(param);

        String result = config.getCqlEndpointFilter();

        assertEquals(param, result);
    }

    /**
     * Test the getCqlEndpointAlternative method
     */
    @Test
    public void testGetCqlEndpointAlternative() {

        System.out.println("getCqlEndpointAlternative");

        String expResult = "http://cqlservlet.mpi.nl/";
        String result = config.getCqlEndpointAlternative();

        assertEquals(expResult, result);
    }

    /**
     * Test the setCqlEndpointAlternative method
     */
    @Test
    public void testSetCqlEndpointAlternative() {

        System.out.println("setCqlEndpointAlternative");

        String param = "http://cqlservlet.mpi.nl/";

        config.setCqlEndpointAlternative(param);

        String result = config.getCqlEndpointAlternative();

        assertEquals(param, result);
    }

    @Test
    public void testGetCollectionFacet() {
        final String result = config.getCollectionFacet();
        assertNull(result);
    }

    @Test
    public void testGetIgnoredFields() {
        Set<String> result = config.getIgnoredFields();
        assertEquals(5, result.size());
    }

    @Test
    public void testGetTechnicalFields() {
        Set<String> result = config.getTechnicalFields();
        assertEquals(11, result.size());
    }

    @Test
    public void testGetSimpleSearchFacetFields() {
        List<String> result = config.getSimpleSearchFacetFields();
        assertEquals(5, result.size());
    }
    
    @Test 
    public void testGetLrSwitchboardBaseUrl() {
        String result = config.getLrSwitchboardBaseUrl();
        assertEquals("http://weblicht.sfs.uni-tuebingen.de/clrs/", result);
    }
}

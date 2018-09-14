package eu.clarin.cmdi.vlo.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.hasItems;

/**
 *
 * @author keeloo
 */
public class DefaultVloConfigFactoryTest {

    public static final int IGNORED_FIELDS_COUNT = 7;
    public static final int TECHNICAL_FIELDS_COUNT = 11;
    
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

        config.setDataRoots(dataRoots);

        List rootsReturned = config.getDataRoots();

        assertEquals(dataRoots, rootsReturned);
    }

    /**
     * Test the getPagesInApplicationCache method
     */
    @Test
    public void testGetPagesInApplicationCache() {
        int expResult = 40; // as defined in vloconfig.xml
        int result = config.getPagesInApplicationCache();

        assertEquals(expResult, result);
    }

    /**
     * Test the setPagesInApplicationCache method
     */
    @Test
    public void testSetPagesInApplicationCache() {
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
        int expResult = 10000; // as defined in vloconfig.xml
        int result = config.getSessionCacheSize();

        assertEquals(expResult, result);
    }

    /**
     * Test the setSessionCacheSize method
     */
    @Test
    public void testSetSessionCacheSize() {
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
        int expResult = 1024;
        int result = config.getMaxDocsInList();

        assertEquals(expResult, result);
    }

    /**
     * Test the setMaxDocsInList method
     */
    @Test
    public void testSetMaxDocsInList() {
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
        int expResult = 1024;
        int result = config.getMaxDocsInList();

        assertEquals(expResult, result);
    }

    /**
     * Test the setMaxDocsInSolrQueue method
     */
    @Test
    public void testSetMaxDocsInSolrQueue() {
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
        int expResult = 50 * 1000 * 1000;
        int result = config.getMaxFileSize();

        assertEquals(expResult, result);
    }

    /**
     * Test the setMaxFileSize method
     */
    @Test
    public void testSetMaxFileSize() {
        int param = 99999999;

        config.setMaxFileSize(param);

        int result = config.getMaxFileSize();

        assertEquals(param, result);
    }

    /**
     * Test the getHandleResolver method
     */
    @Test
    public void testGetUseHandleResolver() {
        boolean expResult = false;
        boolean result = config.getUseHandleResolver();

        assertEquals(expResult, result);
    }

    /**
     * Test the setUseHandleResolver method
     */
    @Test
    public void testSetUseHandleResolver() {
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
        boolean expResult = Boolean.valueOf(testProps.getProperty("deleteAllFirst"));
        boolean result = config.getDeleteAllFirst();

        assertEquals(expResult, result);
    }

    /**
     * Test the deleteAllFirst method
     */
    @Test
    public void testSetDeleteAllFirst() {
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
        boolean expResult = false;
        boolean result = config.printMapping();

        assertEquals(expResult, result);
    }

    /**
     * Test the setPrintMapping method
     */
    @Test
    public void testSetPrintMapping() {
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
        String expResult = testProps.getProperty("homeUrl");
        String result = config.getHomeUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setHomeUrl method
     */
    @Test
    public void testSetVloHomeLink() {
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
        String expResult = testProps.getProperty("helpUrl");
        String result = config.getHelpUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setHelpUrl method
     */
    @Test
    public void testSetHelpUrl() {
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
        String expResult = testProps.getProperty("solrUrl");
        String result = config.getSolrUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setHomeUrl method
     */
    @Test
    public void testSetSolrUrl() {
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
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/someId/xsd";
        String result = config.getComponentRegistryProfileSchema("someId");

        assertEquals(expResult, result);
    }

    /**
     * Test the getComponentRegistryRESTURL method
     */
    @Test
    public void testGetComponentRegistryRESTURL() {
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/";
        String result = config.getComponentRegistryRESTURL();

        assertEquals(expResult, result);
    }

    /**
     * Test the setComponentRegistryRESTURL method
     */
    @Test
    public void testComponentRegistryRESTURL() {
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
        String expResult = "http://hdl.handle.net/";
        String result = config.getHandleServerUrl();
        assertEquals(expResult, result);
    }

    /**
     * Test the setHandleServerUrl method
     */
    @Test
    public void testSetHandleServerUrl() {
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
        String expResult = "https://infra.clarin.eu/content/language_info/data/{}.html";
        String result = config.getLanguageLinkTemplate();

        assertEquals(expResult, result);
    }

    /**
     * Test the setLanguageLinkPrefix method
     */
    @Test
    public void testSetLanguageLinkPrefix() {
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
        String expResult = "http://www.clarin.eu/node/3759?url=";
        String result = config.getFeedbackFromUrl();
        assertEquals(expResult, result);
    }

    /**
     * Test the setFeedbackFromUrl method
     */
    @Test
    public void testSetFeedbackFromUrl() {
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
        String expResult = "http://weblicht.sfs.uni-tuebingen.de/Aggregator/";
        String result = config.getFederatedContentSearchUrl();
        assertEquals(expResult, result);
    }

    /**
     * Test the setFederatedContentSearchUrl method
     */
    @Test
    public void testSetFederatedContentSearchUrl() {
        String param = "http://weblicht.sfs.uni-tuebingen.de/Aggregator/";
        config.setFederatedContentSearchUrl(param);
        String result = config.getFederatedContentSearchUrl();
        assertEquals(param, result);
    }

    /**
     * Test of getFacetFields method
     */
    @Test
    public void testGetFacetFields() {
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

        List<String> result = config.getFacetFieldNames();
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
        List<String> keys = Arrays.asList(
                "RESOURCE_CLASS",
                "MODALITY",
                "GENRE",
                "COUNTRY",
                "DATA_PROVIDER",
                "NATIONAL_PROJECT",
                "KEYWORDS");

        List<String> expResult = Arrays.asList(
                "resourceClass",
                "modality",
                "genre",
                "country",
                "dataProvider",
                "nationalProject",
                "keywords");

        config.setFacetFieldKeys(keys);

        List<String> result = config.getFacetFieldNames();

        assertEquals(expResult, result);
    }

    /**
     * Test the getCountryComponentUrl method.
     */
    @Test
    public void testCountryComponentUrl() {
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/components/clarin.eu:cr1:c_1271859438104/xml";
        String result = config.getCountryComponentUrl();

        assertEquals(expResult, result);
    }

    /**
     * Test the setCountryComponentUrl method.
     */
    @Test
    public void testSetCountryComponentUrl() {
        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/components/clarin.eu:cr1:c_1271859438104/xml";
        config.setCountryComponentUrl(param);
        String result = config.getCountryComponentUrl();
        assertEquals(param, result);
    }

    /**
     * Test the getLanguage2LetterCodeComponentUrl method.
     */
    @Test
    public void testGetLanguage2LetterCodeComponentUrl() {
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/components/clarin.eu:cr1:c_1271859438109/xml";
        String result = config.getLanguage2LetterCodeComponentUrl();
        assertEquals(expResult, result);
    }

    /**
     * Test the setLanguage2LetterCodeComponentUrl method.
     */
    @Test
    public void testSetLanguage2LetterCodeComponentUrl() {
        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/components/clarin.eu:cr1:c_1271859438109/xml";
        config.setLanguage2LetterCodeComponentUrl(param);
        String result = config.getLanguage2LetterCodeComponentUrl();
        assertEquals(param, result);
    }

    /**
     * Test the getLanguage3LetterCodeComponentUrl.
     */
    @Test
    public void testGetLanguage3LetterCodeComponentUrl() {
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/components/clarin.eu:cr1:c_1271859438110/xml";
        String result = config.getLanguage3LetterCodeComponentUrl();
        assertEquals(expResult, result);
    }

    /**
     * Test the setLanguage3LetterCodeComponentUrl.
     */
    @Test
    public void testSetLanguage3LetterCodeComponentUrl() {
        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/components/clarin.eu:cr1:c_1271859438110/xml";
        config.setLanguage3LetterCodeComponentUrl(param);
        String result = config.getLanguage3LetterCodeComponentUrl();
        assertEquals(param, result);
    }

    /**
     * Test the getSilToISO639CodesUrl method.
     */
    @Test
    public void testGetSilToISO639CodesUrl() {
        String expResult = "https://infra.clarin.eu/CMDI/1.1/xslt/sil_to_iso6393.xml";
        String result = config.getSilToISO639CodesUrl();
        assertEquals(expResult, result);
    }

    /**
     * Test the setSilToISO639CodesUrl method.
     */
    @Test
    public void testSetSilToISO639CodesUrl() {
        String param = "http://www.clarin.eu/CMDI/1.1/xslt/sil_to_iso6393.xml";
        config.setSilToISO639CodesUrl(param);
        String result = config.getSilToISO639CodesUrl();
        assertEquals(param, result);
    }

    /**
     * Test the getCqlEndpointFilter method
     */
    @Test
    public void testGetCqlEndpointFilter() {
        String expResult = "http://cqlservlet.mpi.nl/";
        String result = config.getCqlEndpointFilter();
        assertEquals(expResult, result);
    }

    /**
     * Test the setCqlEndpointFilter method
     */
    @Test
    public void testSetCqlEndpointFilter() {
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
        String expResult = "http://cqlservlet.mpi.nl/";
        String result = config.getCqlEndpointAlternative();
        assertEquals(expResult, result);
    }

    /**
     * Test the setCqlEndpointAlternative method
     */
    @Test
    public void testSetCqlEndpointAlternative() {
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
    public void testGetIgnoredFieldNames() {
        Set<String> result = config.getIgnoredFieldNames();
        assertEquals(IGNORED_FIELDS_COUNT, result.size());
    }

    @Test
    public void testGetIgnoredFieldKeys() {
        Set<String> result = config.getIgnoredFieldKeys();
        assertEquals(IGNORED_FIELDS_COUNT, result.size());
    }

    @Test
    public void testGetTechnicalFieldNames() {
        Set<String> result = config.getTechnicalFieldNames();
        assertEquals(TECHNICAL_FIELDS_COUNT, result.size());
    }

    @Test
    public void testGetTechnicalFieldKeys() {
        Set<String> result = config.getTechnicalFieldKeys();
        assertEquals(TECHNICAL_FIELDS_COUNT, result.size());
    }

    @Test
    public void testGetVcrSubmitEndpoint() {
        assertEquals("https://clarin.ids-mannheim.de/vcr/service/submit", config.getVcrSubmitEndpoint());
    }

    @Test
    public void testGetVcrMaximumItemsCount() {
        assertEquals(Long.valueOf(1000), config.getVcrMaximumItemsCount());
    }

    @Test
    public void testGetLrSwitchboardBaseUrl() {
        String result = config.getLrSwitchboardBaseUrl();
        assertEquals("http://weblicht.sfs.uni-tuebingen.de/clrs/", result);
    }
}


package eu.clarin.cmdi.vlo.config;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author keeloo
 */
public class VloConfigTest {
    
    public VloConfigTest() {
    }
    
    @Before
    public void setUp() {        
        VloConfig.readPackagedConfig();
    }

    /**
     * Test the getDataRoots method.<br><br>
     * 
     * Use the values defined in the packaged {@literal VloConfig.xml} 
     * configuration file.
     */
    @Test
    public void testGetDataRoots() {
        
        ArrayList<DataRoot> dataRoots;
        dataRoots = new ArrayList<DataRoot> ();
        
        dataRoots.add(new DataRoot("MPI IMDI Archive", 
                                    new File("/lat/apache/htdocs/oai-harvester/mpi-self-harvest/harvested/results/cmdi/"),
                                   "http://catalog.clarin.eu/",
                                   "/lat/apache/htdocs/", false));
        dataRoots.add(new DataRoot("CMDI Providers"  , 
                                    new File("/lat/apache/htdocs/oai-harvester/cmdi-providers/harvested/results/cmdi/"),
                                   "http://catalog.clarin.eu/",
                                   "/lat/apache/htdocs/", false));
        dataRoots.add(new DataRoot("OLAC Metadata Providers", 
                                    new File("/lat/apache/htdocs/oai-harvester/olac-and-dc-providers/harvested/results/cmdi/"),
                                   "http://catalog.clarin.eu/",
                                   "/lat/apache/htdocs/", false));
        
        System.out.println("getDataRoots");
        
        List rootsReturned = VloConfig.getDataRoots();
        
        assertEquals(dataRoots, rootsReturned);
    }

    /**
     * Test the set setDataRoots method
     */
    @Test
    public void testSetDataRoots() {
        
        ArrayList<DataRoot> dataRoots = new ArrayList<DataRoot> ();
        
        dataRoots.add(new DataRoot("MPI IMDI Archive", 
                                    new File("/lat/apache/htdocs/oai-harvester/mpi-self-harvest/harvested/results/cmdi/"),
                                   "http://catalog.clarin.eu/",
                                   "/lat/apache/htdocs/", false)); 
        dataRoots.add(new DataRoot("CMDI Providers"  , 
                                    new File("/lat/apache/htdocs/oai-harvester/cmdi-providers/harvested/results/cmdi/"),
                                   "http://catalog.clarin.eu/",
                                   "/lat/apache/htdocs/", false));
        dataRoots.add(new DataRoot("OLAC Metadata Providers", 
                                    new File("/lat/apache/htdocs/oai-harvester/olac-and-dc-providers/harvested/results/cmdi/"),
                                   "http://catalog.clarin.eu/",
                                   "/lat/apache/htdocs/", false));
        
        System.out.println("setDataRoots");

        VloConfig.setDataRoots(dataRoots);
        
        List rootsReturned = VloConfig.getDataRoots(); 
        
        assertEquals (dataRoots, rootsReturned);
    }

    /**
     * Test the getVloHomeLink method
     */
    @Test
    public void testGetMaxOnHeap() {
        
        System.out.println("getMaxOnHeap");
        
        int expResult = 500;
        int result = VloConfig.getMaxOnHeap();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setVloHomeLink method
     */
    @Test
    public void testSetMaxOnHeap() {
        
        System.out.println("setMaxOnHeap");
        
        int param = 1000;
        
        VloConfig.setMaxOnHeap(param);

        int result = VloConfig.getMaxOnHeap();
        
        assertEquals(param, result);
    }
    
    /**
     * Test the getHandleResolver method
     */
    @Test
    public void testGetUseHandleResolver() {
        
        System.out.println("getMaxOnHeap");
        
        boolean expResult = false;
        boolean result = VloConfig.getUseHandleResolver();
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test the setUseHandleResolver method
     */
    @Test
    public void testSetUseHandleResolver() {
        
        System.out.println("setMaxOnHeap");
        
        boolean param = true;
        
        VloConfig.setUseHandleResolver(param);

        boolean result = VloConfig.getUseHandleResolver();
        
        assertEquals(param, result);
    }

    /**
     * Test the deleteAllFirst method
     */
    @Test
    public void testDeleteAllFirst() {
        
        System.out.println("deleteAllFirst");
        
        boolean expResult = false;
        boolean result = VloConfig.deleteAllFirst();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the deleteAllFirst method
     */
    @Test
    public void testSetDeleteAllFirst() {
        
        System.out.println("setDeleteAllFirst");
        
        boolean param = false;
        
        VloConfig.setDeleteAllFirst(param);

        boolean result = VloConfig.deleteAllFirst();
        
        assertEquals(param, result);
    }

    /**
     * Test the printMapping method
     */
    @Test
    public void testPrintMapping() {
        
        System.out.println("printMapping");
        
        boolean expResult = false;
        boolean result = VloConfig.printMapping();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setPrintMapping method
     */
    @Test
    public void testSetPrintMapping() {
        System.out.println("setPrintMapping");
        
        boolean param = false;
        
        VloConfig.setPrintMapping(param);

        boolean result = VloConfig.printMapping();
        
        assertEquals(param, result);
    }

    /**
     * Test the getVloHomeLink method
     */
    @Test
    public void testGetVloHomeLink() {
        
        System.out.println("getVloHomeLink");
        
        String expResult = "http://www.clarin.eu/vlo";
        String result = VloConfig.getVloHomeLink();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setVloHomeLink method
     */
    @Test
    public void testSetVloHomeLink() {
        
        System.out.println("setSetVloHomeLink");
        
        String param = "http://www.clarin.eu/vlo";
        
        VloConfig.setVloHomeLink(param);

        String result = VloConfig.getVloHomeLink();
        
        assertEquals(param, result);
    }

    /**
     * Test the getSolrUrl method
     */
    @Test
    public void testGetSolrUrl() {
        
        System.out.println("getSolrUrl");
        
        String expResult = "http://localhost:8084/vlo_solr/";
        String result = VloConfig.getSolrUrl();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setVloHomeLink method
     */
    @Test
    public void testSetSolrUrl() {
        
        System.out.println("setSolrUrl");
        
        String param = "http://localhost:8084/vlo_solr/";
        
        VloConfig.setSolrUrl(param);

        String result = VloConfig.getSolrUrl();
        
        assertEquals(param, result);
    }
    
    /**
     * Test the getGetComponentRegistryProfileSchema method
     */
    @Test
    public void testGetComponentRegistryProfileSchema() {
        
        System.out.println("getComponentRegistryProfileSchema");
        
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/someId/xsd";
        String result = VloConfig.getComponentRegistryProfileSchema("someId");
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test the getComponentRegistryRESTURL method
     */
    @Test
    public void testGetComponentRegistryRESTURL() {
        
        System.out.println("getComponentRegistryRESTURL");
        
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/";
        String result = VloConfig.getComponentRegistryRESTURL();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setComponentRegistryRESTURL method
     */
    @Test
    public void testComponentRegistryRESTURL() {
        
        System.out.println("setComponentRegistryRESTURL");
        
        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/";
        
        VloConfig.setComponentRegistryRESTURL(param);

        String result = VloConfig.getComponentRegistryRESTURL();
        
        assertEquals(param, result);
    }
    
    /**
     * Test the getHandleServerUrl method
     */
    @Test
    public void testGetHandleServerUrl() {
        
        System.out.println("getHandleServerUrl");
        
        String expResult = "http://hdl.handle.net/";
        String result = VloConfig.getHandleServerUrl();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setHandleServerUrl method
     */
    @Test
    public void testSetHandleServerUrl() {
        
        System.out.println("setHandleServerUrl");
        
        String param = "http://hdl.handle.net/";
        
        VloConfig.setHandleServerUrl(param);

        String result = VloConfig.getHandleServerUrl();
        
        assertEquals(param, result);
    }
    
    /**
     * Test the getIMDIBrowserUrl method
     */
    @Test
    public void testGetIMDIBrowserUrl() {
        
        System.out.println("getIMDIBrowserUrl");
        
        String expResult;
        try {
            expResult = "http://corpus1.mpi.nl/ds/imdi_browser?openpath=" + URLEncoder.encode("handle", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            expResult = "http://corpus1.mpi.nl/ds/imdi_browser?openpath=" + "handle";
        }
        String result = VloConfig.getIMDIBrowserUrl("handle");

        assertEquals(expResult, result);
    }

    /**
     * Test the setIMDIBrowserUrl method
     */
    @Test
    public void testSetIMDIBrowserUrl() {
        
        System.out.println("setIMDIBrowserUrl");
        
        String param = "http://corpus1.mpi.nl/ds/imdi_browser?openpath=";
        
        VloConfig.setIMDIBrowserUrl(param);

        String expResult;
        try {
            expResult = "http://corpus1.mpi.nl/ds/imdi_browser?openpath=" + URLEncoder.encode("handle", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            expResult = "http://corpus1.mpi.nl/ds/imdi_browser?openpath=" + "handle";
        }
        
        String result = VloConfig.getIMDIBrowserUrl("handle");

        assertEquals(expResult, result);
    }
    
    /**
     * Test the getFederatedContentSearchUrl method
     */
    @Test
    public void testGetFederatedContentSearchUrl() {
        
        System.out.println("getFederatedContentSearchUrl");
        
        String expResult = "http://weblicht.sfs.uni-tuebingen.de/Aggregator/";
        String result = VloConfig.getFederatedContentSearchUrl();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setFederatedContentSearchUrl method
     */
    @Test
    public void testSetFederatedContentSearchUrl() {
        
        System.out.println("setFederatedContentSearchUrl");
        
        String param = "http://weblicht.sfs.uni-tuebingen.de/Aggregator/";
        
        VloConfig.setFederatedContentSearchUrl(param);

        String result = VloConfig.getFederatedContentSearchUrl();
        
        assertEquals(param, result);
    }

    /**
     * Test the getNationalProjectMapping method
     */
    @Test
    public void testGetNationalProjectMapping() {
        
        System.out.println("getNationalProjectMapping");
        
        String expResult = "/nationalProjectsMapping.xml";
        String result = VloConfig.getNationalProjectMapping();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setNationalProjectMapping method
     */
    @Test
    public void testSetNationalProjectMapping() {
        
        System.out.println("setFNationalProjectMapping");
        
        String param = "nationalProjectsMapping.xml";
        
        VloConfig.setNationalProjectMapping(param);

        String result = VloConfig.getNationalProjectMapping();
        
        assertEquals(param, result);
    }    

    /**
     * Test of getFacetFields method
     */
    @Test
    public void testGetFacetFields() {
        
        System.out.println("getFacetFields");
        
        String[] expResult = {
        "collection",
        "language",
        "continent",
        "genre",
        "country",
        "subject",
        "organisation",
        "resourceType",
        "dataProvider",
        "nationalProject"};
    
        String[] result = VloConfig.getFacetFields();
    
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of setFacetFields method, of class VloConfig
     */
    @Test
    public void testSetFacetFields() {
        
        System.out.println("setFacetFields");
        
        String[] expResult = {
        "collection",
        "language",
        "continent",
        "genre",
        "country",
        "subject",
        "organisation",
        "resourceType",
        "dataProvider",
        "nationalProject"};
        
        VloConfig.setFacetFields(expResult);
        
        String result[] = VloConfig.getFacetFields();

        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test the getCountryComponentUrl method.
     */
    @Test
    public void testCountryComponentUrl() {
        
        System.out.println("getCountryComponentUrl");
        
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438104/xml";
        String result = VloConfig.getCountryComponentUrl();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setCountryComponentUrl method.
     */
    @Test
    public void testSetCountryComponentUrl() {
        
        System.out.println("setCountryComponentUrl");
        
        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438104/xml";
        
        VloConfig.setCountryComponentUrl(param);

        String result = VloConfig.getCountryComponentUrl();
        
        assertEquals(param, result);
    }  
    
    /**
     * Test the getLanguage2LetterCodeComponentUrl method.
     */
    @Test
    public void testGetLanguage2LetterCodeComponentUrl() {
        
        System.out.println("getLanguage2LetterCodeComponentUrl");
        
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438109/xml";
        String result = VloConfig.getLanguage2LetterCodeComponentUrl();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setLanguage2LetterCodeComponentUrl method.
     */
    @Test
    public void testSetLanguage2LetterCodeComponentUrl() {
        
        System.out.println("setLanguage2LetterCodeComponentUrl");
        
        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438109/xml";
        
        VloConfig.setLanguage2LetterCodeComponentUrl(param);

        String result = VloConfig.getLanguage2LetterCodeComponentUrl();
        
        assertEquals(param, result);
    }
    
    /**
     * Test the getLanguage3LetterCodeComponentUrl.
     */
    @Test
    public void testGetLanguage3LetterCodeComponentUrl() {
        
        System.out.println("getLanguage3LetterCodeComponentUrl");
        
        String expResult = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110/xml";
        String result = VloConfig.getLanguage3LetterCodeComponentUrl();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setLanguage3LetterCodeComponentUrl.
     */
    @Test
    public void testSetLanguage3LetterCodeComponentUrl() {
        
        System.out.println("setLanguage3LetterCodeComponentUrl");
        
        String param = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110/xml";
        
        VloConfig.setLanguage3LetterCodeComponentUrl(param);

        String result = VloConfig.getLanguage3LetterCodeComponentUrl();
        
        assertEquals(param, result);
    }
    
    /**
     * Test the getSilToISO639CodesUrl method.
     */
    @Test
    public void testGetSilToISO639CodesUrl() {
        
        System.out.println("getSilToISO639CodesUrl");
        
        String expResult = "http://www.clarin.eu/cmd/xslt/sil_to_iso6393.xml";
        String result = VloConfig.getSilToISO639CodesUrl();
        
        assertEquals(expResult, result);
    }

    /**
     * Test the setSilToISO639CodesUrl method.
     */
    @Test
    public void testSetSilToISO639CodesUrl() {
        
        System.out.println("setSilToISO639CodesUrl");
        
        String param = "http://www.clarin.eu/cmd/xslt/sil_to_iso6393.xml";
        
        VloConfig.setSilToISO639CodesUrl(param);

        String result = VloConfig.getSilToISO639CodesUrl();
        
        assertEquals(param, result);
    }

}

package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FacetConstants;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class FacetMappingFactoryTest {

    private final static Logger LOG = LoggerFactory.getLogger(FacetMappingFactoryTest.class);

    @Test
    public void testGetImdiMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438204/xsd");//IMDI Session profile xsd
        List<FacetConfiguration> facets = facetMapping.getFacets();
        assertEquals(16, facets.size());
        int index = 0;
        FacetConfiguration mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_ID, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_COLLECTION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_PROJECT_NAME, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Project/c:Name/text()", mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Project/c:Title/text()", mapping.getPatterns().get(1));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:Name/text()", mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:Session/c:Title/text()", mapping.getPatterns().get(1));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_YEAR, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:Date/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_CONTINENT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Location/c:Continent/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Location/c:Country/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_LANGUAGE, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Content/c:Content_Languages/c:Content_Language/c:Id/text()", mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Actors/c:Actor/c:Actor_Languages/c:Actor_Language/c:Id/text()", mapping.getPatterns().get(1));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:WrittenResource/c:LanguageId/text()", mapping.getPatterns().get(2));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_LANGUAGES, mapping.getName());
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(6, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Project/c:Contact/c:Organisation/text()", mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Actors/c:Actor/c:Contact/c:Organisation/text()", mapping.getPatterns().get(1));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:MediaFile/c:Access/c:Contact/c:Organisation/text()", mapping.getPatterns().get(2));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:WrittenResource/c:Access/c:Contact/c:Organisation/text()", mapping.getPatterns().get(3));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:Source/c:Access/c:Contact/c:Organisation/text()", mapping.getPatterns().get(4));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:Anonyms/c:Access/c:Contact/c:Organisation/text()", mapping.getPatterns().get(5));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Content/c:Genre/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Content/c:Subject/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(18, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:descriptions/c:Description/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_RESOURCE_TYPE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);
        assertEquals("/c:CMD/c:Header/c:MdCollectionDisplayName/text()", mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);
        assertEquals("/c:CMD/c:Header/c:MdProfile/text()", mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("check to see we tested them all", facets.size(), index);
    }

    @Test
    public void testGetOlacMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614026/xsd");
        List<FacetConfiguration> facets = facetMapping.getFacets();
        assertEquals(13, facets.size());
        int index = 0;
        FacetConfiguration mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_ID, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_COLLECTION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdCollectionDisplayName/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:title/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:spatial[@dcterms-type=\"ISO3166\"]/text()", mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:coverage[@dcterms-type=\"ISO3166\"]/text()", mapping.getPatterns().get(1));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_LANGUAGE, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:language/@olac-language", mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:subject/@olac-language", mapping.getPatterns().get(1));
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Languages/c:ISO639/c:iso-639-3-code/text()", mapping.getPatterns().get(2));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_LANGUAGES, mapping.getName());
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:publisher/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:type/@olac-linguistic-type", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:subject/text()", mapping.getPatterns().get(0));
        //assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:subject[@dcterms-type=\"LCSH\"]/text()", mapping.getPatterns().get(1));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:description/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_RESOURCE_TYPE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);
        assertEquals("/c:CMD/c:Header/c:MdCollectionDisplayName/text()", mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);
        assertEquals("/c:CMD/c:Header/c:MdProfile/text()", mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());

        assertEquals("check to see we tested them all", facets.size(), index);
    }

    @Test
    public void testGetLrtMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1289827960126/xsd");
        List<FacetConfiguration> facets = facetMapping.getFacets();
        assertEquals(14, facets.size());
        int index = 0;
        FacetConfiguration mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_ID, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_COLLECTION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:ResourceName/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_YEAR, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:FinalizationYearResourceCreation/text()", mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtLexiconDetails/c:Date/text()", mapping.getPatterns().get(1));
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtServiceDetails/c:Date/text()", mapping.getPatterns().get(2));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Countries/c:Country/text()", mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Countries/c:Country/c:Code/text()", mapping.getPatterns().get(1));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_LANGUAGE, mapping.getName());


        //LOG.info("XXXXX: " + mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
//        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:language/@olac-language", mapping.getPatterns().get(0));
//        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:subject/@olac-language", mapping.getPatterns().get(1));
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Languages/c:ISO639/c:iso-639-3-code/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_LANGUAGES, mapping.getName());
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Institute/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Description/text()", mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtIPR/c:Description/text()", mapping.getPatterns().get(1));
        mapping = facets.get(index++);
        assertEquals(FacetConstants.FIELD_RESOURCE_TYPE, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:ResourceType/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);
        assertEquals("/c:CMD/c:Header/c:MdCollectionDisplayName/text()", mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);
        assertEquals("/c:CMD/c:Header/c:MdProfile/text()", mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("check to see we tested them all", facets.size(), index);
    }

    @Test
    public void testGetIdMapping() throws Exception {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1290431694629/xsd");
        List<FacetConfiguration> facets = facetMapping.getFacets();
        FacetConfiguration facet = facets.get(0);
        assertEquals(FacetConstants.FIELD_ID, facet.getName());
        assertEquals(2, facet.getPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()", facet.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:EastRepublican/c:GeneralInformation/c:Identifier/text()", facet.getPatterns().get(1));
    }

}

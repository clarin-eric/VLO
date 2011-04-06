package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import eu.clarin.cmdi.vlo.FacetConstants;

public class FacetMappingFactoryTest {

    @Test
    public void testGetImdiMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438204/xsd");//IMDI Session profile xsd
        List<FacetConfiguration> facets = facetMapping.getFacets();
        assertEquals(10, facets.size());
        FacetConfiguration mapping = facets.get(0);
        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/Session/Name/text()", mapping.getPatterns().get(0));
        mapping = facets.get(1);
        assertEquals(FacetConstants.FIELD_YEAR, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/Session/Date/text()", mapping.getPatterns().get(0));
        mapping = facets.get(2);
        assertEquals(FacetConstants.FIELD_CONTINENT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/Session/MDGroup/Location/Continent/text()", mapping.getPatterns().get(0));
        mapping = facets.get(3);
        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/Session/MDGroup/Location/Country/text()", mapping.getPatterns().get(0));
        mapping = facets.get(4);
        assertEquals(FacetConstants.FIELD_LANGUAGE, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/CMD/Components/Session/MDGroup/Content/Content_Languages/Content_Language/Id/text()", mapping.getPatterns().get(0));
        assertEquals("/CMD/Components/Session/MDGroup/Actors/Actor/Actor_Languages/Actor_Language/Id/text()", mapping.getPatterns().get(1));
        assertEquals("/CMD/Components/Session/Resources/WrittenResource/LanguageId/text()", mapping.getPatterns().get(2));
        mapping = facets.get(5);
        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(6, mapping.getPatterns().size());
        assertEquals("/CMD/Components/Session/MDGroup/Project/Contact/Organisation/text()", mapping.getPatterns().get(0));
        assertEquals("/CMD/Components/Session/MDGroup/Actors/Actor/Contact/Organisation/text()", mapping.getPatterns().get(1));
        assertEquals("/CMD/Components/Session/Resources/MediaFile/Access/Contact/Organisation/text()", mapping.getPatterns().get(2));
        assertEquals("/CMD/Components/Session/Resources/WrittenResource/Access/Contact/Organisation/text()", mapping.getPatterns().get(3));
        assertEquals("/CMD/Components/Session/Resources/Source/Access/Contact/Organisation/text()", mapping.getPatterns().get(4));
        assertEquals("/CMD/Components/Session/Resources/Anonyms/Access/Contact/Organisation/text()", mapping.getPatterns().get(5));
        mapping = facets.get(6);
        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/Session/MDGroup/Content/Genre/text()", mapping.getPatterns().get(0));
        mapping = facets.get(7);
        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/Session/MDGroup/Content/Subject/text()", mapping.getPatterns().get(0));
        mapping = facets.get(8);
        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(18, mapping.getPatterns().size());
        assertEquals("/CMD/Components/Session/descriptions/Description/text()", mapping.getPatterns().get(0));
        mapping = facets.get(9);
        assertEquals(FacetConstants.FIELD_RESOURCE_TYPE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
    }

    @Test
    public void testGetOlacMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614026/xsd");
        List<FacetConfiguration> facets = facetMapping.getFacets();
        assertEquals(8, facets.size());
        FacetConfiguration mapping = facets.get(0);
        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/OLAC-DcmiTerms/title/text()", mapping.getPatterns().get(0));
        mapping = facets.get(1);
        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/CMD/Components/OLAC-DcmiTerms/spatial[@dcterms-type=\"ISO3166\"]/text()", mapping.getPatterns().get(0));
        assertEquals("/CMD/Components/OLAC-DcmiTerms/coverage[@dcterms-type=\"ISO3166\"]/text()", mapping.getPatterns().get(1));
        mapping = facets.get(2);
        assertEquals(FacetConstants.FIELD_LANGUAGE, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/CMD/Components/OLAC-DcmiTerms/language/@olac-language", mapping.getPatterns().get(0));
        assertEquals("/CMD/Components/OLAC-DcmiTerms/subject/@olac-language", mapping.getPatterns().get(1));
        assertEquals("/CMD/Components/LrtInventoryResource/LrtCommon/Languages/ISO639/iso-639-3-code/text()", mapping.getPatterns().get(2));
        mapping = facets.get(3);
        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/OLAC-DcmiTerms/publisher/text()", mapping.getPatterns().get(0));
        mapping = facets.get(4);
        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/OLAC-DcmiTerms/type/@olac-linguistic-type", mapping.getPatterns().get(0));
        mapping = facets.get(5);
        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/CMD/Components/OLAC-DcmiTerms/subject/@olac-linguistic-field", mapping.getPatterns().get(0));
        assertEquals("/CMD/Components/OLAC-DcmiTerms/subject[@dcterms-type=\"LCSH\"]/text()", mapping.getPatterns().get(1));
        mapping = facets.get(6);
        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/OLAC-DcmiTerms/description/text()", mapping.getPatterns().get(0));
        mapping = facets.get(7);
        assertEquals(FacetConstants.FIELD_RESOURCE_TYPE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
    }

    @Test
    public void testGetLrtMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1289827960126/xsd");
        List<FacetConfiguration> facets = facetMapping.getFacets();
        assertEquals(9, facets.size());
        FacetConfiguration mapping = facets.get(0);
        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/LrtInventoryResource/LrtCommon/ResourceName/text()", mapping.getPatterns().get(0));
        mapping = facets.get(1);
        assertEquals(FacetConstants.FIELD_YEAR, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/CMD/Components/LrtInventoryResource/LrtCommon/FinalizationYearResourceCreation/text()", mapping.getPatterns().get(0));
        assertEquals("/CMD/Components/LrtInventoryResource/LrtLexiconDetails/Date/text()", mapping.getPatterns().get(1));
        assertEquals("/CMD/Components/LrtInventoryResource/LrtServiceDetails/Date/text()", mapping.getPatterns().get(2));
        mapping = facets.get(2);
        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/CMD/Components/LrtInventoryResource/LrtCommon/Countries/Country/text()", mapping.getPatterns().get(0));
        assertEquals("/CMD/Components/LrtInventoryResource/LrtCommon/Countries/Country/Code/text()", mapping.getPatterns().get(1));
        mapping = facets.get(3);
        assertEquals(FacetConstants.FIELD_LANGUAGE, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/CMD/Components/OLAC-DcmiTerms/language/@olac-language", mapping.getPatterns().get(0));
        assertEquals("/CMD/Components/OLAC-DcmiTerms/subject/@olac-language", mapping.getPatterns().get(1));
        assertEquals("/CMD/Components/LrtInventoryResource/LrtCommon/Languages/ISO639/iso-639-3-code/text()", mapping.getPatterns().get(2));
        mapping = facets.get(4);
        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/LrtInventoryResource/LrtCommon/Institute/text()", mapping.getPatterns().get(0));
        mapping = facets.get(5);
        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(6);
        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        mapping = facets.get(7);
        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/CMD/Components/LrtInventoryResource/LrtCommon/Description/text()", mapping.getPatterns().get(0));
        assertEquals("/CMD/Components/LrtInventoryResource/LrtIPR/Description/text()", mapping.getPatterns().get(1));
        mapping = facets.get(8);
        assertEquals(FacetConstants.FIELD_RESOURCE_TYPE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/CMD/Components/LrtInventoryResource/LrtCommon/ResourceType/text()", mapping.getPatterns().get(0));
    }

}

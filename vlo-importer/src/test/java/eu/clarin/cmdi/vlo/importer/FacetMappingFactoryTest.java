package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.FacetConstants;

public class FacetMappingFactoryTest {

    private final static String FACETCONCEPTS_FILENAME = ImporterTestcase.getTestFacetConceptFilePath();

    private final static String IMDI_PROFILE_ID = "clarin.eu:cr1:p_1271859438204";
    private final static String OLAC_PROFILE_ID = "clarin.eu:cr1:p_1288172614026";
    private final static String LRT_PROFILE_ID = "clarin.eu:cr1:p_1289827960126";
    private final static String ID_PROFILE_ID = "clarin.eu:cr1:p_1290431694629";
    private final static String TEXTCORPUSPROFILE_PROFILE_ID = "clarin.eu:cr1:p_1290431694580";

    private final static Logger LOG = LoggerFactory.getLogger(FacetMappingFactoryTest.class);

    @Test
    public void testGetImdiMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping(FACETCONCEPTS_FILENAME, IMDI_PROFILE_ID, true);

        List<FacetConfiguration> facets = facetMapping.getFacets();
        assertEquals(20, facets.size());

        int index = 0;
        FacetConfiguration mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_ID, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SELF_LINK, mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COLLECTION, mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_PROJECT_NAME, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Project/c:Name/text()",
                mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Project/c:Title/text()",
                mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:Name/text()",
                mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:Session/c:Title/text()",
                mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_TEMPORAL_COVERAGE, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:Date/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_CONTINENT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Location/c:Continent/text()",
                mapping.getPatterns()
                .get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Location/c:Country/text()",
                mapping.getPatterns()
                .get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_LANGUAGE_CODE, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Content/c:Content_Languages/c:Content_Language/c:Id/text()",
                mapping.getPatterns().get(0));
        // removed because of container data categories
        // assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Actors/c:Actor/c:Actor_Languages/c:Actor_Language/c:Id/text()",
        // mapping.getPatterns().get(1));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:WrittenResource/c:LanguageId/text()",
                mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(6, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Project/c:Contact/c:Organisation/text()",
                mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Actors/c:Actor/c:Contact/c:Organisation/text()",
                mapping.getPatterns().get(1));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:MediaFile/c:Access/c:Contact/c:Organisation/text()",
                mapping.getPatterns().get(2));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:WrittenResource/c:Access/c:Contact/c:Organisation/text()",
                mapping.getPatterns().get(3));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:Source/c:Access/c:Contact/c:Organisation/text()",
                mapping.getPatterns().get(4));
        assertEquals("/c:CMD/c:Components/c:Session/c:Resources/c:Anonyms/c:Access/c:Contact/c:Organisation/text()",
                mapping.getPatterns().get(5));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Content/c:Genre/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_MODALITY, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Content/c:Modalities/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:MDGroup/c:Content/c:Subject/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(18, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:Session/c:descriptions/c:Description/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_RESOURCE_CLASS, mapping.getName());
        assertEquals(3, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_FORMAT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/c:CMD/c:Header/c:MdCollectionDisplayName/text()",
                mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/c:CMD/c:Header//text()", mapping.getFallbackPatterns().get(0));
        assertEquals("/c:CMD/c:Components//text()", mapping.getFallbackPatterns().get(1));
        assertEquals(2, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/c:CMD/c:Header/c:MdProfile/text()", mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        // test keywords facet mapping
        assertEquals(FacetConstants.FIELD_KEYWORDS, mapping.getName());
        assertEquals("/c:CMD/c:Components/c:mods/c:classification/text()",
                mapping.getFallbackPatterns().get(0));
        assertEquals(3, mapping.getFallbackPatterns().size());

        assertEquals("check to see we tested them all", facets.size(), index);
    }

    @Test
    public void testGetOlacMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping(FACETCONCEPTS_FILENAME, OLAC_PROFILE_ID, true);

        List<FacetConfiguration> facets = facetMapping.getFacets();
        assertEquals(18, facets.size());

        int index = 0;
        FacetConfiguration mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_ID, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SELF_LINK, mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COLLECTION, mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdCollectionDisplayName/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_PROJECT_NAME, mapping.getName());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Components/c:teiHeader/c:fileDesc/c:publicationStmt/c:publisher/c:orgName/c:orgName[@role=\"project\"]/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:title/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_TEMPORAL_COVERAGE, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:created/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(2, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:spatial[@dcterms-type=\"ISO3166\"]/text()",
                mapping
                .getFallbackPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:coverage[@dcterms-type=\"ISO3166\"]/text()",
                mapping
                .getFallbackPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_LANGUAGE_CODE, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:language/text()",
                mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components//c:OLAC-DcmiTerms/c:language/@olac-language", mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:publisher/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(4, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:type/@olac-linguistic-type",
                mapping.getFallbackPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:mods/c:genre/text()",
                mapping.getFallbackPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:subject/text()",
                mapping.getPatterns().get(0));
        // assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:subject[@dcterms-type=\"LCSH\"]/text()", mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:description/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_RESOURCE_CLASS, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_FORMAT, mapping.getName());
        assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:format/text()",
                mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_NATIONAL_PROJECT, mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/c:CMD/c:Header//text()", mapping.getFallbackPatterns().get(0));
        assertEquals("/c:CMD/c:Components//text()", mapping.getFallbackPatterns().get(1));
        assertEquals(2, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/c:CMD/c:Header/c:MdProfile/text()", mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_KEYWORDS, mapping.getName());
        assertEquals("/c:CMD/c:Components/c:mods/c:classification/text()",
                mapping.getFallbackPatterns().get(0));
        assertEquals(3, mapping.getFallbackPatterns().size());

        assertEquals("check to see we tested them all", facets.size(), index);
    }

    @Test
    public void testGetLrtMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping(FACETCONCEPTS_FILENAME, LRT_PROFILE_ID, true);

        List<FacetConfiguration> facets = facetMapping.getFacets();
        assertEquals(17, facets.size());

        int index = 0;
        FacetConfiguration mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_ID, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());

        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SELF_LINK, mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COLLECTION, mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_PROJECT_NAME, mapping.getName());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Components/c:media-session-profile/c:media-session/c:Corpus", mapping.getFallbackPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(6, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:ResourceName/text()",
                mapping.getFallbackPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:mods/c:titleInfo/title/text()",
                mapping.getFallbackPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_TEMPORAL_COVERAGE, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals(
                "/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:FinalizationYearResourceCreation/text()",
                mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtLexiconDetails/c:Date/text()",
                mapping.getPatterns().get(1));
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtServiceDetails/c:Date/text()",
                mapping.getPatterns().get(2));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Countries/c:Country/text()",
                mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Countries/c:Country/c:Code/text()",
                mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_LANGUAGE_CODE, mapping.getName());

        // LOG.info("XXXXX: " + mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        // assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:language/@olac-language", mapping.getPatterns().get(0));
        // assertEquals("/c:CMD/c:Components/c:OLAC-DcmiTerms/c:subject/@olac-language", mapping.getPatterns().get(1));
        assertEquals(
                "/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Languages/c:ISO639/c:iso-639-3-code/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Institute/text()",
                mapping.getPatterns()
                .get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(4, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(8, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals(5, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:Description/text()",
                mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:mods/c:abstract/text()", mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_RESOURCE_CLASS, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:LrtCommon/c:ResourceType/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals("/c:CMD/c:Header/c:MdCollectionDisplayName/text()",
                mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/c:CMD/c:Header//text()", mapping.getFallbackPatterns().get(0));
        assertEquals("/c:CMD/c:Components//text()", mapping.getFallbackPatterns().get(1));
        assertEquals(2, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/c:CMD/c:Header/c:MdProfile/text()", mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_KEYWORDS, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/c:CMD/c:Components/c:LrtInventoryResource/c:tags/c:tag/text()",
                mapping.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:mods/c:classification/text()",
                mapping.getFallbackPatterns().get(0));
        assertEquals("check to see we tested them all", facets.size(), index);
    }

    @Test
    public void testGetIdMapping() throws Exception {

        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping(FACETCONCEPTS_FILENAME, ID_PROFILE_ID, true);

        List<FacetConfiguration> facets = facetMapping.getFacets();

        FacetConfiguration facet = facets.get(0);

        assertEquals(FacetConstants.FIELD_ID, facet.getName());
        assertEquals(2, facet.getPatterns().size());
        assertEquals("/c:CMD/c:Header/c:MdSelfLink/text()", facet.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:EastRepublican/c:GeneralInformation/c:Identifier/text()",
                facet.getPatterns().get(1));
    }

    /**
     * Tests black/white listing approach based on
     * acceptableContext/rejectableContext information in facetconcepts.xml
     *
     * Not in use right now, therefore contains just a dummy test.
     */
    @Test
    public void testConceptBasedBlacklisting() {
        assertTrue(true);
    }

    /**
     * Tests black/white listing approach based on
     * acceptableContext/rejectableContext information in configuration
     */
    @Test
    public void testStringBasedBlacklisting() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping(FACETCONCEPTS_FILENAME, TEXTCORPUSPROFILE_PROFILE_ID, true);
        List<FacetConfiguration> facets = facetMapping.getFacets();

        FacetConfiguration facet = facets.get(5);
        assertEquals(FacetConstants.FIELD_TEMPORAL_COVERAGE, facet.getName());
        assertEquals(2, facet.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:TextCorpusProfile/c:GeneralInfo/c:CompletionYear/text()", facet.getPatterns().get(0));
        assertEquals("/c:CMD/c:Components/c:TextCorpusProfile/c:GeneralInfo/c:PublicationDate/text()", facet.getPatterns().get(1));

        facet = facets.get(13);
        assertEquals(FacetConstants.FIELD_DESCRIPTION, facet.getName());
        assertEquals(1, facet.getPatterns().size());
        assertEquals("/c:CMD/c:Components/c:TextCorpusProfile/c:GeneralInfo/c:Descriptions/c:Description/text()", facet.getPatterns().get(0));
    }
}

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
    private final static String TEIHEADER_PROFILE_ID = "clarin.eu:cr1:p_1380106710826";

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
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SELF_LINK, mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COLLECTION, mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_PROJECT_NAME, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Project/cmdp:Name/text()",
                mapping.getPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Project/cmdp:Title/text()",
                mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Name/text()",
                mapping.getPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Title/text()",
                mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_TEMPORAL_COVERAGE, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Date/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_CONTINENT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Location/cmdp:Continent/text()",
                mapping.getPatterns()
                .get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Location/cmdp:Country/text()",
                mapping.getPatterns()
                .get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_LANGUAGE_CODE, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Content/cmdp:Content_Languages/cmdp:Content_Language/cmdp:Id/text()",
                mapping.getPatterns().get(0));
        // removed because of container data categories
        // assertEquals("/cmd:CMD/cmd:Components/c:Session/c:MDGroup/c:Actors/c:Actor/c:Actor_Languages/c:Actor_Language/c:Id/text()",
        // mapping.getPatterns().get(1));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:WrittenResource/cmdp:LanguageId/text()",
                mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(6, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Project/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Actors/cmdp:Actor/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(1));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:MediaFile/cmdp:Access/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(2));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:WrittenResource/cmdp:Access/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(3));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:Source/cmdp:Access/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(4));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:Anonyms/cmdp:Access/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(5));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Content/cmdp:Genre/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_MODALITY, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Content/cmdp:Modalities/text()", mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Content/cmdp:Subject/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(18, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:descriptions/cmdp:Description/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_RESOURCE_CLASS, mapping.getName());
        assertEquals(3, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_FORMAT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdCollectionDisplayName/text()",
                mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header//text()", mapping.getFallbackPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components//text()", mapping.getFallbackPatterns().get(1));
        assertEquals(2, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdProfile/text()", mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        // test keywords facet mapping
        assertEquals(FacetConstants.FIELD_KEYWORDS, mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:classification/text()",
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
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SELF_LINK, mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COLLECTION, mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdCollectionDisplayName/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_PROJECT_NAME, mapping.getName());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:teiHeader/cmdp:fileDesc/cmdp:publicationStmt/cmdp:publisher/cmdp:orgName/cmdp:orgName[@role=\"project\"]/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:title/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_TEMPORAL_COVERAGE, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:created/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(2, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:spatial[@dcterms-type=\"ISO3166\"]/text()",
                mapping
                .getFallbackPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:coverage[@dcterms-type=\"ISO3166\"]/text()",
                mapping
                .getFallbackPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_LANGUAGE_CODE, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertTrue(mapping.getPatterns().contains("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:language/text()"));
        assertTrue(mapping.getPatterns().contains("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:language/@olac-language"));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:publisher/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_GENRE, mapping.getName());
        assertEquals(4, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:type/@olac-linguistic-type",
                mapping.getFallbackPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:genre/text()",
                mapping.getFallbackPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SUBJECT, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:subject/text()",
                mapping.getPatterns().get(0));
        // assertEquals("/cmd:CMD/cmd:Components/c:OLAC-DcmiTerms/c:subject[@dcterms-type=\"LCSH\"]/text()", mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_DESCRIPTION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:description/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_RESOURCE_CLASS, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_FORMAT, mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:format/text()",
                mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_NATIONAL_PROJECT, mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header//text()", mapping.getFallbackPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components//text()", mapping.getFallbackPatterns().get(1));
        assertEquals(2, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdProfile/text()", mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_KEYWORDS, mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:classification/text()",
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

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_SELF_LINK, mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COLLECTION, mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_PROJECT_NAME, mapping.getName());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:media-session-profile/cmdp:media-session/cmdp:Corpus", mapping.getFallbackPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_NAME, mapping.getName());
        assertEquals(6, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:ResourceName/text()",
                mapping.getFallbackPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:titleInfo/title/text()",
                mapping.getFallbackPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_TEMPORAL_COVERAGE, mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals(
                "/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:FinalizationYearResourceCreation/text()",
                mapping.getPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtLexiconDetails/cmdp:Date/text()",
                mapping.getPatterns().get(1));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtServiceDetails/cmdp:Date/text()",
                mapping.getPatterns().get(2));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_COUNTRY, mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Countries/cmdp:Country/text()",
                mapping.getPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Countries/cmdp:Country/cmdp:Code/text()",
                mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_LANGUAGE_CODE, mapping.getName());

        // LOG.info("XXXXX: " + mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        // assertEquals("/cmd:CMD/cmd:Components/c:OLAC-DcmiTerms/c:language/@olac-language", mapping.getPatterns().get(0));
        // assertEquals("/cmd:CMD/cmd:Components/c:OLAC-DcmiTerms/c:subject/@olac-language", mapping.getPatterns().get(1));
        assertEquals(
                "/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Languages/cmdp:ISO639/cmdp:iso-639-3-code/text()",
                mapping.getPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_ORGANISATION, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Institute/text()",
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
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Description/text()",
                mapping.getPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:abstract/text()", mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_RESOURCE_CLASS, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:ResourceType/text()",
                mapping.getFallbackPatterns().get(0));
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdCollectionDisplayName/text()",
                mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header//text()", mapping.getFallbackPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components//text()", mapping.getFallbackPatterns().get(1));
        assertEquals(2, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdProfile/text()", mapping.getFallbackPatterns().get(0));
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(FacetConstants.FIELD_KEYWORDS, mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:tags/cmdp:tag/text()",
                mapping.getPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:classification/text()",
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
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()", facet.getPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:EastRepublican/cmdp:GeneralInformation/cmdp:Identifier/text()",
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
        assertEquals("/cmd:CMD/cmd:Components/cmdp:TextCorpusProfile/cmdp:GeneralInfo/cmdp:CompletionYear/text()", facet.getPatterns().get(0));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:TextCorpusProfile/cmdp:GeneralInfo/cmdp:PublicationDate/text()", facet.getPatterns().get(1));

        facet = facets.get(13);
        assertEquals(FacetConstants.FIELD_DESCRIPTION, facet.getName());
        assertEquals(1, facet.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:TextCorpusProfile/cmdp:GeneralInfo/cmdp:Descriptions/cmdp:Description/text()", facet.getPatterns().get(0));
    }

    @Test
    public void testConceptLinkAttributMapping() {
        FacetMapping facetMapping = FacetMappingFactory
                .getFacetMapping(FACETCONCEPTS_FILENAME, TEIHEADER_PROFILE_ID, true);
        List<FacetConfiguration> facets = facetMapping.getFacets();

        FacetConfiguration facet = facets.get(17);
        assertEquals(FacetConstants.FIELD_AVAILABILITY, facet.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:teiHeader/cmdp:fileDesc/cmdp:publicationStmt/cmdp:availability/@status", facet.getPatterns().get(0));
    }
}

package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;

import java.io.IOException;
import org.junit.Before;

public class FacetMappingFactoryTest extends ImporterTestcase {

    private final static String FACETCONCEPTS_FILENAME = ImporterTestcase.getTestFacetConceptFilePath();
    private final static String VALUEMAPPINGS_FILENAME = ImporterTestcase.getTestValueMappingsFilePath();

    private final static String IMDI_PROFILE_ID = "clarin.eu:cr1:p_1271859438204";
    private final static String OLAC_PROFILE_ID = "clarin.eu:cr1:p_1288172614026";
    private final static String LRT_PROFILE_ID = "clarin.eu:cr1:p_1289827960126";
    private final static String ID_PROFILE_ID = "clarin.eu:cr1:p_1290431694629";
    private final static String TEXTCORPUSPROFILE_PROFILE_ID = "clarin.eu:cr1:p_1290431694580";
    private final static String TEIHEADER_PROFILE_ID = "clarin.eu:cr1:p_1380106710826";
    private final static String CLAVAS_PROFILE_ID = "clarin.eu:cr1:p_1493735943959";

    private FacetMappingFactory facetMappingFactory;

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        config.setFacetConceptsFile(FACETCONCEPTS_FILENAME);
        config.setValueMappingsFile(VALUEMAPPINGS_FILENAME);
        facetMappingFactory = new FacetMappingFactory(config, marshaller);
    }

    @Test
    public void testGetImdiMapping() {
        FacetMapping facetMapping = facetMappingFactory
                .getFacetMapping(IMDI_PROFILE_ID, true);

        List<FacetConfiguration> facets = new ArrayList<FacetConfiguration>(facetMapping.getFacets());
        assertEquals(23, facets.size());

        int index = 0;
        FacetConfiguration mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.ID), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.SELF_LINK), mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getFallbackPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.COLLECTION), mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.PROJECT_NAME), mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Project/cmdp:Name/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Project/cmdp:Title/text()",
                mapping.getPatterns().get(1).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.NAME), mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Name/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Title/text()",
                mapping.getPatterns().get(1).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE), mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Date/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.CONTINENT), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Location/cmdp:Continent/text()",
                mapping.getPatterns()
                        .get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.COUNTRY), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Location/cmdp:Country/text()",
                mapping.getPatterns()
                        .get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE), mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Content/cmdp:Content_Languages/cmdp:Content_Language/cmdp:Id/text()",
                mapping.getPatterns().get(0).getPattern());
        // removed because of container data categories
        // assertEquals("/cmd:CMD/cmd:Components/c:Session/c:MDGroup/c:Actors/c:Actor/c:Actor_Languages/c:Actor_Language/c:Id/text()",
        // mapping.getPatterns().get(1));
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:WrittenResource/cmdp:LanguageId/text()",
                mapping.getPatterns().get(1).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.LANGUAGE_NAME), mapping.getName());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.ORGANISATION), mapping.getName());
        assertEquals(6, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Project/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Actors/cmdp:Actor/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(1).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:MediaFile/cmdp:Access/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(2).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:WrittenResource/cmdp:Access/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(3).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:Source/cmdp:Access/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(4).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:Anonyms/cmdp:Access/cmdp:Contact/cmdp:Organisation/text()",
                mapping.getPatterns().get(5).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.GENRE), mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Content/cmdp:Genre/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.MODALITY), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Content/cmdp:Modalities/text()", mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.SUBJECT), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:MDGroup/cmdp:Content/cmdp:Subject/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.DESCRIPTION), mapping.getName());
        assertEquals(18, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:descriptions/cmdp:Description/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS), mapping.getName());
        assertEquals(3, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.FORMAT), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header//text()", mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components//text()", mapping.getFallbackPatterns().get(1).getPattern());
        assertEquals(2, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdProfile/text()", mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        // test keywords facet mapping
        assertEquals(fieldNameService.getFieldName(FieldKey.KEYWORDS), mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:classification/text()",
                mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals(3, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);
        
        mapping = facets.get(index++);
        
        mapping = facets.get(index++);

        // test license facet mapping
        assertEquals(fieldNameService.getFieldName(FieldKey.LICENSE), mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:Session/cmdp:Resources/cmdp:MediaFile/cmdp:Access/cmdp:Availability/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals(4, mapping.getPatterns().size());

        assertEquals("check to see we tested them all", facets.size(), index);
    }

    @Test
    public void testGetOlacMapping() {
        FacetMapping facetMapping = facetMappingFactory
                .getFacetMapping(OLAC_PROFILE_ID, true);

        List<FacetConfiguration> facets = new ArrayList<FacetConfiguration>(facetMapping.getFacets());
        assertEquals(23, facets.size());

        int index = 0;
        FacetConfiguration mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.ID), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.SELF_LINK), mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getFallbackPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.COLLECTION), mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdCollectionDisplayName/text()",
                mapping.getFallbackPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.PROJECT_NAME), mapping.getName());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:teiHeader/cmdp:fileDesc/cmdp:publicationStmt/cmdp:publisher/cmdp:orgName/cmdp:orgName[@role=\"project\"]/text()",
                mapping.getFallbackPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.NAME), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:title/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE), mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:created/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);
        
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.COUNTRY), mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(2, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:spatial[@dcterms-type=\"ISO3166\"]/text()",
                mapping
                        .getFallbackPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:coverage[@dcterms-type=\"ISO3166\"]/text()",
                mapping
                        .getFallbackPatterns().get(1).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE), mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertTrue(mapping.getPatterns().contains(new Pattern("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:language/text()")));
        assertTrue(mapping.getPatterns().contains(new Pattern("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:language/@olac-language")));
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.LANGUAGE_NAME), mapping.getName());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.ORGANISATION), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:publisher/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.GENRE), mapping.getName());
        assertEquals(4, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:type/@olac-linguistic-type",
                mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:genre/text()",
                mapping.getFallbackPatterns().get(1).getPattern());
        mapping = facets.get(index++);
        
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.SUBJECT), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:subject/text()",
                mapping.getPatterns().get(0).getPattern());
        // assertEquals("/cmd:CMD/cmd:Components/c:OLAC-DcmiTerms/c:subject[@dcterms-type=\"LCSH\"]/text()", mapping.getPatterns().get(1));
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.DESCRIPTION), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:description/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.FORMAT), mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:format/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals(1, mapping.getPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header//text()", mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components//text()", mapping.getFallbackPatterns().get(1).getPattern());
        assertEquals(2, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdProfile/text()", mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.KEYWORDS), mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:classification/text()",
                mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals(3, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);
        
        mapping = facets.get(index++);
        mapping = facets.get(index++);

        // test license facet mapping
        assertEquals(fieldNameService.getFieldName(FieldKey.LICENSE), mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:license/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals(3, mapping.getPatterns().size());

        assertEquals("check to see we tested them all", facets.size(), index);
    }

    @Test
    public void testGetLrtMapping() {
        FacetMapping facetMapping = facetMappingFactory
                .getFacetMapping(LRT_PROFILE_ID, true);

        List<FacetConfiguration> facets = new ArrayList<FacetConfiguration>(facetMapping.getFacets());
        assertEquals(23, facets.size());

        int index = 0;
        FacetConfiguration mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.ID), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.SELF_LINK), mapping.getName());
        assertEquals(0, mapping.getPatterns().size());
        assertEquals(1, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()",
                mapping.getFallbackPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.COLLECTION), mapping.getName());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.PROJECT_NAME), mapping.getName());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:media-session-profile/cmdp:media-session/cmdp:Corpus", mapping.getFallbackPatterns().get(1).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.NAME), mapping.getName());
        assertEquals(6, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:ResourceName/text()",
                mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:titleInfo/title/text()",
                mapping.getFallbackPatterns().get(1).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE), mapping.getName());
        assertEquals(3, mapping.getPatterns().size());
        assertEquals(
                "/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:FinalizationYearResourceCreation/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtLexiconDetails/cmdp:Date/text()",
                mapping.getPatterns().get(1).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtServiceDetails/cmdp:Date/text()",
                mapping.getPatterns().get(2).getPattern());
        mapping = facets.get(index++);
        
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.COUNTRY), mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Countries/cmdp:Country/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Countries/cmdp:Country/cmdp:Code/text()",
                mapping.getPatterns().get(1).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE), mapping.getName());

        // LOG.info("XXXXX: " + mapping.getPatterns().get(0));
        assertEquals(1, mapping.getPatterns().size());
        // assertEquals("/cmd:CMD/cmd:Components/c:OLAC-DcmiTerms/c:language/@olac-language", mapping.getPatterns().get(0));
        // assertEquals("/cmd:CMD/cmd:Components/c:OLAC-DcmiTerms/c:subject/@olac-language", mapping.getPatterns().get(1));
        assertEquals(
                "/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Languages/cmdp:ISO639/cmdp:iso-639-3-code/text()",
                mapping.getPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.LANGUAGE_NAME), mapping.getName());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.ORGANISATION), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Institute/text()",
                mapping.getPatterns()
                        .get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.GENRE), mapping.getName());
        assertEquals(4, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);
        
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.SUBJECT), mapping.getName());
        assertEquals(8, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.DESCRIPTION), mapping.getName());
        assertEquals(2, mapping.getPatterns().size());
        assertEquals(5, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:Description/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:abstract/text()", mapping.getFallbackPatterns().get(0).getPattern());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtCommon/cmdp:ResourceType/text()",
                mapping.getFallbackPatterns().get(0).getPattern());
        mapping = facets.get(index++);
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header//text()", mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components//text()", mapping.getFallbackPatterns().get(1).getPattern());
        assertEquals(2, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals("/cmd:CMD/cmd:Header/cmd:MdProfile/text()", mapping.getFallbackPatterns().get(0).getPattern());
        assertEquals(1, mapping.getFallbackPatterns().size());
        mapping = facets.get(index++);

        assertEquals(fieldNameService.getFieldName(FieldKey.KEYWORDS), mapping.getName());
        assertEquals(1, mapping.getPatterns().size());
        assertEquals(3, mapping.getFallbackPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:tags/cmdp:tag/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:mods/cmdp:classification/text()",
                mapping.getFallbackPatterns().get(0).getPattern());

        mapping = facets.get(index++);
        
        mapping = facets.get(index++);
        // test license type facet mapping
        assertEquals(fieldNameService.getFieldName(FieldKey.LICENSE_TYPE), mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtDistributionClassification/cmdp:DistributionType/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals(1, mapping.getPatterns().size());

        mapping = facets.get(index++);
        // test license facet mapping
        assertEquals(fieldNameService.getFieldName(FieldKey.LICENSE), mapping.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:LrtInventoryResource/cmdp:LrtIPR/cmdp:LicenseType/text()",
                mapping.getPatterns().get(0).getPattern());
        assertEquals(2, mapping.getPatterns().size());

        assertEquals("check to see we tested them all", facets.size(), index);
    }

    @Test
    public void testGetIdMapping() throws Exception {

        FacetMapping facetMapping = facetMappingFactory
                .getFacetMapping(ID_PROFILE_ID, true);

        List<FacetConfiguration> facets = new ArrayList<FacetConfiguration>(facetMapping.getFacets());

        FacetConfiguration facet = facets.get(0);

        assertEquals(fieldNameService.getFieldName(FieldKey.ID), facet.getName());
        assertEquals(2, facet.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()", facet.getPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:EastRepublican/cmdp:GeneralInformation/cmdp:Identifier/text()",
                facet.getPatterns().get(1).getPattern());
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
        FacetMapping facetMapping = facetMappingFactory
                .getFacetMapping(TEXTCORPUSPROFILE_PROFILE_ID, true);
        List<FacetConfiguration> facets = new ArrayList<FacetConfiguration>(facetMapping.getFacets());

        FacetConfiguration facet = facets.get(5);
        assertEquals(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE), facet.getName());
        assertEquals(2, facet.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:TextCorpusProfile/cmdp:GeneralInfo/cmdp:CompletionYear/text()", facet.getPatterns().get(0).getPattern());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:TextCorpusProfile/cmdp:GeneralInfo/cmdp:PublicationDate/text()", facet.getPatterns().get(1).getPattern());

        facet = facets.get(14);
        assertEquals(fieldNameService.getFieldName(FieldKey.DESCRIPTION), facet.getName());
        assertEquals(1, facet.getPatterns().size());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:TextCorpusProfile/cmdp:GeneralInfo/cmdp:Descriptions/cmdp:Description/text()", facet.getPatterns().get(0).getPattern());
    }

    @Test
    public void testConceptLinkAttributMapping() {
        FacetMapping facetMapping = facetMappingFactory
                .getFacetMapping(TEIHEADER_PROFILE_ID, true);
        List<FacetConfiguration> facets = new ArrayList<FacetConfiguration>(facetMapping.getFacets());

        FacetConfiguration facet = facets.get(20);
        assertEquals(fieldNameService.getFieldName(FieldKey.AVAILABILITY), facet.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:teiHeader/cmdp:fileDesc/cmdp:publicationStmt/cmdp:availability/@status", facet.getFallbackPatterns().get(0).getPattern());
    }

    @Test
    public void testCLAVASMapping() throws IOException {
        FacetMapping facetMapping = facetMappingFactory
                .getFacetMapping(CLAVAS_PROFILE_ID, true);
        List<FacetConfiguration> facets = new ArrayList<FacetConfiguration>(facetMapping.getFacets());

        FacetConfiguration facet = facets.get(8);
        assertEquals(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE), facet.getName());
        assertEquals("/cmd:CMD/cmd:Components/cmdp:TestCLAVAS/cmdp:ISO639/cmdp:iso-639-3-code/text()", facet.getPatterns().get(0).getPattern());
        assertEquals("http://hdl.handle.net/11459/CLAVAS_810f8d2a-6723-3ba6-2e57-41d6d3844816", facet.getPatterns().get(0).getVocabulary().getURI().toString());
    }

}

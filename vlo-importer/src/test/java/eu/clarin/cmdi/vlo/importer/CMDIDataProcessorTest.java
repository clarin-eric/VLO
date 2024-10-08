package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.common.SolrInputDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class CMDIDataProcessorTest extends ImporterTestcase {

    protected FacetMappingFactory facetMappingFactory;
    private ResourceStructureGraph resourceStructureGraph;

    private CMDIDataProcessor<SolrInputDocument> getDataParser() {
        return new CMDIParserVTDXML<>(
                MetadataImporter.registerPostProcessors(config, fieldNameService, languageCodeUtils),
                MetadataImporter.registerPostMappingFilters(fieldNameService),
                config, facetMappingFactory, marshaller, new CMDIDataSolrImplFactory(fieldNameService), fieldNameService, true);
    }

    @BeforeEach
    @Override
    public void setup() throws Exception {
        super.setup();
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile(getTestFacetConceptFilePath());
        config.setFacetsConfigFile(getTestFacetsConfigFilePath());
        config.setValueMappingsFile(ImporterTestcase.getTestValueMappingsFilePath());
        facetMappingFactory = new FacetMappingFactory(config, marshaller);
        resourceStructureGraph = new ResourceStructureGraph();
    }

    @Test
    public void testCreateCMDIDataFromCorpus() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1274880881885\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
        content += "   <Header>\n";
        content += "      <MdCreationDate>2003-01-14</MdCreationDate>\n";
        content += "      <MdSelfLink>test-hdl:1839/00-0000-0000-0000-0001-D</MdSelfLink>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1274880881885</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "      <ResourceProxyList>\n";
        content += "         <ResourceProxy id=\"d28635e19\">\n";
        content += "            <ResourceType>Metadata</ResourceType>\n";
        content += "            <ResourceRef>../acqui_data/Corpusstructure/acqui.imdi.cmdi</ResourceRef>\n";
        content += "         </ResourceProxy>\n";
        content += "         <ResourceProxy id=\"d28635e23\">\n";
        content += "            <ResourceType>Metadata</ResourceType>\n";
        content += "            <ResourceRef>../Comprehension/Corpusstructure/comprehension.imdi.cmdi</ResourceRef>\n";
        content += "         </ResourceProxy>\n";
        content += "         <ResourceProxy id=\"d28635e26\">\n";
        content += "            <ResourceType>Metadata</ResourceType>\n";
        content += "            <ResourceRef>../lac_data/Corpusstructure/lac.imdi.cmdi</ResourceRef>\n";
        content += "         </ResourceProxy>\n";
        content += "      </ResourceProxyList>\n";
        content += "      <JournalFileProxyList/>\n";
        content += "      <ResourceRelationList/>\n";
        content += "   </Resources>\n";
        content += "   <Components>\n";
        content += "      <cmdp:imdi-corpus>\n";
        content += "         <cmdp:Corpus>\n";
        content += "            <cmdp:Name>MPI corpora</cmdp:Name>\n";
        content += "            <cmdp:Title>Corpora of the Max-Planck Institute for Psycholinguistics</cmdp:Title>\n";
        content += "            <cmdp:CorpusLink Name=\"Acquisition\">../acqui_data/Corpusstructure/acqui.imdi</cmdp:CorpusLink>\n";
        content += "            <cmdp:CorpusLink Name=\"Comprehension\">../Comprehension/Corpusstructure/comprehension.imdi</cmdp:CorpusLink>\n";
        content += "            <cmdp:CorpusLink Name=\"Language and Cognition\">../lac_data/Corpusstructure/lac.imdi</cmdp:CorpusLink>\n";
        content += "            <cmdp:descriptions>\n";
        content += "               <cmdp:Description LanguageId=\"\">IMDI corpora</cmdp:Description>\n";
        content += "               <cmdp:Description LanguageId=\"\"/>\n";
        content += "            </cmdp:descriptions>\n";
        content += "         </cmdp:Corpus>\n";
        content += "      </cmdp:imdi-corpus>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testCorpus", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        assertEquals("test-hdl_58_1839_47_00-0000-0000-0000-0001-D", data.getId());
        List<Resource> resources = data.getMetadataResources();
        assertEquals(3, resources.size());
        Resource res = resources.get(0);
        assertEquals("../acqui_data/Corpusstructure/acqui.imdi.cmdi", res.getResourceName());
        assertEquals(null, res.getMimeType());
        assertEquals(0, data.getDataResources().size());
        SolrInputDocument doc = data.getDocument();
        // TODO FIX bad test case. Depends on the presence of an internet connection! (BAD!)
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.CLARIN_PROFILE)).contains("imdi-corpus"));
        assertEquals("clarin.eu:cr1:p_1274880881885", doc.getFieldValue(fieldNameService.getFieldName(FieldKey.CLARIN_PROFILE_ID)));
        assertNotNull(doc);
    }

    @Test
    public void testCreateCMDIDataFromSession() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1271859438204\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "     xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438204/xsd\">\n";
        content += "   <Header>\n";
        content += "      <MdCreationDate>2008-05-27</MdCreationDate>\n";
        content += "      <MdSelfLink>test-hdl:1839/00-0000-0000-0009-294C-9</MdSelfLink>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1271859438204</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "      <ResourceProxyList>\n";
        content += "         <ResourceProxy id=\"d314e408\">\n";
        content += "            <ResourceType mimetype=\"video/x-mpeg1\" >Resource</ResourceType>\n";
        content += "            <ResourceRef>../Media/elan-example1.mpg</ResourceRef>\n";
        content += "         </ResourceProxy>\n";
        content += "         <ResourceProxy id=\"d314e471\">\n";
        content += "            <ResourceType mimetype=\"audio/mpeg\" >Resource</ResourceType>\n";
        content += "            <ResourceRef>../Media/elan-example1.mp3</ResourceRef>\n";
        content += "         </ResourceProxy>\n";
        content += "      </ResourceProxyList>\n";
        content += "      <JournalFileProxyList/>\n";
        content += "      <ResourceRelationList/>\n";
        content += "   </Resources>\n";
        content += "   <Components>\n";
        content += "      <cmdp:Session>\n";
        content += "         <cmdp:Name>kleve-route</cmdp:Name>\n";
        content += "         <cmdp:Title>route description to Kleve</cmdp:Title>\n";
        content += "         <cmdp:Date>2002-10-30</cmdp:Date>\n";
        content += "         <cmdp:descriptions>\n";
        content += "            <cmdp:Description xml:lang='eng' LanguageId=\"ISO639-2:eng\">This  recording was made to generate a freely available test resource including speech and gestures. The annotations were created by Peter and Kita who is gesture researcher at the MPI for Psycholinguistics.</cmdp:Description>\n";
        content += "            <cmdp:Description LanguageId=\"ISO639-2:ger\">Diese Aufnahme wurde erzeugt, um eine frei verf\\u00fcgbare Test Resource zur Verf\\u00fcgung stellen zu k\\u00f6nnen, die Sprache und Gestik umfasst. Die Annotationen wurden von Peter und Kita, dem Gestik Researcher am MPI erzeugt.</cmdp:Description>\n";
        content += "         </cmdp:descriptions>\n";
        content += "         <cmdp:MDGroup>\n";
        content += "            <cmdp:Location>\n";
        content += "               <cmdp:Continent>Europe</cmdp:Continent>\n";
        content += "               <cmdp:Country>Netherlands</cmdp:Country>\n";
        content += "               <cmdp:Region/>\n";
        content += "               <cmdp:Address>Wundtlaan 1, Nijmegen</cmdp:Address>\n";
        content += "            </cmdp:Location>\n";
        content += "            <cmdp:Project>\n";
        content += "               <cmdp:Name>Peter Wittenburg</cmdp:Name>\n";
        content += "               <cmdp:Title>Route description test resource</cmdp:Title>\n";
        content += "               <cmdp:Id/>\n";
        content += "               <cmdp:Contact>\n";
        content += "                  <cmdp:Name>Peter Wittenburg</cmdp:Name>\n";
        content += "                  <cmdp:Address>Wundtlaan 1, 6525 XD Nijmegen</cmdp:Address>\n";
        content += "                  <cmdp:Email>peter.wittenburg@mpi.nl</cmdp:Email>\n";
        content += "                  <cmdp:Organisation>Max Planck Institute for Psycholinguistics</cmdp:Organisation>\n";
        content += "               </cmdp:Contact>\n";
        content += "               <cmdp:descriptions>\n";
        content += "                  <cmdp:Description LanguageId=\"\"/>\n";
        content += "               </cmdp:descriptions>\n";
        content += "            </cmdp:Project>\n";
        content += "            <cmdp:Keys>\n";
        content += "               <cmdp:Key Name=\"conversion.IMDI.1.9to3.0.warning\">Unknown mapping of Genre: conversation|explanation|unspecified --&gt; ???</cmdp:Key>\n";
        content += "            </cmdp:Keys>\n";
        content += "            <cmdp:Content>\n";
        content += "               <cmdp:Genre>Demo</cmdp:Genre>\n";
        content += "               <cmdp:SubGenre>Unspecified</cmdp:SubGenre>\n";
        content += "               <cmdp:Task>route description</cmdp:Task>\n";
        content += "               <cmdp:Modalities>Speech; Gestures</cmdp:Modalities>\n";
        content += "               <cmdp:CommunicationContext>\n";
        content += "                  <cmdp:Interactivity>interactive</cmdp:Interactivity>\n";
        content += "                  <cmdp:PlanningType>semi-spontaneous</cmdp:PlanningType>\n";
        content += "                  <cmdp:Involvement>elicited</cmdp:Involvement>\n";
        content += "                  <cmdp:SocialContext>Unspecified</cmdp:SocialContext>\n";
        content += "                  <cmdp:EventStructure>Unspecified</cmdp:EventStructure>\n";
        content += "                  <cmdp:Channel>Unspecified</cmdp:Channel>\n";
        content += "               </cmdp:CommunicationContext>\n";
        content += "               <cmdp:Content_Languages>\n";
        content += "                  <cmdp:descriptions>\n";
        content += "                     <cmdp:Description LanguageId=\"\"/>\n";
        content += "                  </cmdp:descriptions>\n";
        content += "                  <cmdp:Content_Language>\n";
        content += "                     <cmdp:Id>ISO639-3:eng</cmdp:Id>\n";
        content += "                     <cmdp:Name>English</cmdp:Name>\n";
        content += "                     <cmdp:descriptions>\n";
        content += "                        <cmdp:Description LanguageId=\"\"/>\n";
        content += "                     </cmdp:descriptions>\n";
        content += "                  </cmdp:Content_Language>\n";
        content += "               </cmdp:Content_Languages>\n";
        content += "               <cmdp:Keys>\n";
        content += "                  <cmdp:Key Name=\"IMDI__1_9.Interactional\">conversation</cmdp:Key>\n";
        content += "                  <cmdp:Key Name=\"IMDI__1_9.Discursive\">explanation</cmdp:Key>\n";
        content += "                  <cmdp:Key Name=\"IMDI__1_9.Interactional\">Unspecified</cmdp:Key>\n";
        content += "               </cmdp:Keys>\n";
        content += "               <cmdp:descriptions>\n";
        content += "                  <cmdp:Description LanguageId=\"ISO639:eng\">This file was generated from an IMDI 1.9 file and transformed to IMDI 3.0. The substructure of Genre is replaced by two elements named \"Genre\" and \"SubGenre\". The original content of Genre substructure was: Interactional = 'conversation', Discursive = 'explanation', Performance = 'Unspecified'. These values have been added as Keys to the Content information.</cmdp:Description>\n";
        content += "                  <cmdp:Description LanguageId=\"ISO639:eng\">Peter explains how to come from Nijmegen to Kleve by car, such that Kita would be able to get there.</cmdp:Description>\n";
        content += "               </cmdp:descriptions>\n";
        content += "            </cmdp:Content>\n";
        content += "            <cmdp:Actors>\n";
        content += "               <cmdp:descriptions>\n";
        content += "                  <cmdp:Description LanguageId=\"\"/>\n";
        content += "               </cmdp:descriptions>\n";
        content += "               <cmdp:Actor>\n";
        content += "                  <cmdp:Role>interviewee</cmdp:Role>\n";
        content += "                  <cmdp:Name>Peter</cmdp:Name>\n";
        content += "                  <cmdp:FullName>Peter Wittenburg</cmdp:FullName>\n";
        content += "                  <cmdp:Code>W</cmdp:Code>\n";
        content += "                  <cmdp:FamilySocialRole>Unspecified</cmdp:FamilySocialRole>\n";
        content += "                  <cmdp:EthnicGroup/>\n";
        content += "                  <cmdp:Age>Unknown</cmdp:Age>\n";
        content += "                  <cmdp:BirthDate>Unspecified</cmdp:BirthDate>\n";
        content += "                  <cmdp:Sex>Unknown</cmdp:Sex>\n";
        content += "                  <cmdp:Education>university</cmdp:Education>\n";
        content += "                  <cmdp:Anonymized>true</cmdp:Anonymized>\n";
        content += "                  <cmdp:Contact>\n";
        content += "                     <cmdp:Name/>\n";
        content += "                     <cmdp:Address/>\n";
        content += "                     <cmdp:Email/>\n";
        content += "                     <cmdp:Organisation/>\n";
        content += "                  </cmdp:Contact>\n";
        content += "                  <cmdp:Keys/>\n";
        content += "                  <cmdp:descriptions>\n";
        content += "                     <cmdp:Description LanguageId=\"\"/>\n";
        content += "                  </cmdp:descriptions>\n";
        content += "                  <cmdp:Actor_Languages>\n";
        content += "                     <cmdp:descriptions>\n";
        content += "                        <cmdp:Description LanguageId=\"\"/>\n";
        content += "                     </cmdp:descriptions>\n";
        content += "                     <cmdp:Actor_Language>\n";
        content += "                        <cmdp:Id>ISO639-3:nld</cmdp:Id>\n";
        content += "                        <cmdp:Name>Dutch</cmdp:Name>\n";
        content += "                        <cmdp:descriptions>\n";
        content += "                           <cmdp:Description LanguageId=\"\"/>\n";
        content += "                        </cmdp:descriptions>\n";
        content += "                     </cmdp:Actor_Language>\n";
        content += "                     <cmdp:Actor_Language>\n";
        content += "                        <cmdp:Id>ISO639-3:deu</cmdp:Id>\n";
        content += "                        <cmdp:Name>German</cmdp:Name>\n";
        content += "                        <cmdp:descriptions>\n";
        content += "                           <cmdp:Description LanguageId=\"\"/>\n";
        content += "                        </cmdp:descriptions>\n";
        content += "                     </cmdp:Actor_Language>\n";
        content += "                     <cmdp:Actor_Language>\n";
        content += "                        <cmdp:Id>ISO639-3:eng</cmdp:Id>\n";
        content += "                        <cmdp:Name>English</cmdp:Name>\n";
        content += "                        <cmdp:descriptions>\n";
        content += "                           <cmdp:Description LanguageId=\"\"/>\n";
        content += "                        </cmdp:descriptions>\n";
        content += "                     </cmdp:Actor_Language>\n";
        content += "                  </cmdp:Actor_Languages>\n";
        content += "               </cmdp:Actor>\n";
        content += "               <cmdp:Actor>\n";
        content += "                  <cmdp:Role>interviewer</cmdp:Role>\n";
        content += "                  <cmdp:Name>Kita</cmdp:Name>\n";
        content += "                  <cmdp:FullName>Sotaro Kita</cmdp:FullName>\n";
        content += "                  <cmdp:Code>k</cmdp:Code>\n";
        content += "                  <cmdp:FamilySocialRole>Unspecified</cmdp:FamilySocialRole>\n";
        content += "                  <cmdp:EthnicGroup/>\n";
        content += "                  <cmdp:Age>Unknown</cmdp:Age>\n";
        content += "                  <cmdp:BirthDate>Unspecified</cmdp:BirthDate>\n";
        content += "                  <cmdp:Sex>Unknown</cmdp:Sex>\n";
        content += "                  <cmdp:Education>university</cmdp:Education>\n";
        content += "                  <cmdp:Anonymized>true</cmdp:Anonymized>\n";
        content += "                  <cmdp:Contact>\n";
        content += "                     <cmdp:Name/>\n";
        content += "                     <cmdp:Address/>\n";
        content += "                     <cmdp:Email/>\n";
        content += "                     <cmdp:Organisation/>\n";
        content += "                  </cmdp:Contact>\n";
        content += "                  <cmdp:Keys/>\n";
        content += "                  <cmdp:descriptions>\n";
        content += "                     <cmdp:Description LanguageId=\"\"/>\n";
        content += "                  </cmdp:descriptions>\n";
        content += "                  <cmdp:Actor_Languages>\n";
        content += "                     <cmdp:descriptions>\n";
        content += "                        <cmdp:Description LanguageId=\"\"/>\n";
        content += "                     </cmdp:descriptions>\n";
        content += "                     <cmdp:Actor_Language>\n";
        content += "                        <cmdp:Id>ISO639-3:eng</cmdp:Id>\n";
        content += "                        <cmdp:Name>English</cmdp:Name>\n";
        content += "                        <cmdp:descriptions>\n";
        content += "                           <cmdp:Description LanguageId=\"\"/>\n";
        content += "                        </cmdp:descriptions>\n";
        content += "                     </cmdp:Actor_Language>\n";
        content += "                     <cmdp:Actor_Language>\n";
        content += "                        <cmdp:Id>ISO639-3:jpn</cmdp:Id>\n";
        content += "                        <cmdp:Name>Japanese</cmdp:Name>\n";
        content += "                        <cmdp:descriptions>\n";
        content += "                           <cmdp:Description LanguageId=\"\"/>\n";
        content += "                        </cmdp:descriptions>\n";
        content += "                     </cmdp:Actor_Language>\n";
        content += "                  </cmdp:Actor_Languages>\n";
        content += "               </cmdp:Actor>\n";
        content += "               <cmdp:Actor>\n";
        content += "                  <cmdp:Role>Collector</cmdp:Role>\n";
        content += "                  <cmdp:Name>Peter Wittenburg</cmdp:Name>\n";
        content += "                  <cmdp:FullName>Peter Wittenburg</cmdp:FullName>\n";
        content += "                  <cmdp:Code>Unspecified</cmdp:Code>\n";
        content += "                  <cmdp:FamilySocialRole>Unspecified</cmdp:FamilySocialRole>\n";
        content += "                  <cmdp:EthnicGroup/>\n";
        content += "                  <cmdp:Age>Unspecified</cmdp:Age>\n";
        content += "                  <cmdp:BirthDate>Unspecified</cmdp:BirthDate>\n";
        content += "                  <cmdp:Sex>Unspecified</cmdp:Sex>\n";
        content += "                  <cmdp:Education/>\n";
        content += "                  <cmdp:Anonymized>false</cmdp:Anonymized>\n";
        content += "                  <cmdp:Contact>\n";
        content += "                     <cmdp:Name>Peter Wittenburg</cmdp:Name>\n";
        content += "                     <cmdp:Address>Wundtlaan 1, 6525 XD Nijmegen</cmdp:Address>\n";
        content += "                     <cmdp:Email>peter.wittenburg@mpi.nl</cmdp:Email>\n";
        content += "                     <cmdp:Organisation>Max-Planck-Institute for Psycholinguistics</cmdp:Organisation>\n";
        content += "                  </cmdp:Contact>\n";
        content += "                  <cmdp:Keys/>\n";
        content += "                  <cmdp:descriptions>\n";
        content += "                     <cmdp:Description LanguageId=\"\"/>\n";
        content += "                  </cmdp:descriptions>\n";
        content += "                  <cmdp:Actor_Languages/>\n";
        content += "               </cmdp:Actor>\n";
        content += "            </cmdp:Actors>\n";
        content += "         </cmdp:MDGroup>\n";
        content += "         <cmdp:Resources>\n";
        content += "            <cmdp:MediaFile ref=\"d314e408\">\n";
        content += "               <cmdp:ResourceLink>../Media/elan-example1.mpg</cmdp:ResourceLink>\n";
        content += "               <cmdp:Type>video</cmdp:Type>\n";
        content += "               <cmdp:Format>video/x-mpeg1</cmdp:Format>\n";
        content += "               <cmdp:Size/>\n";
        content += "               <cmdp:Quality>Unknown</cmdp:Quality>\n";
        content += "               <cmdp:RecordingConditions>excellent</cmdp:RecordingConditions>\n";
        content += "               <cmdp:TimePosition>\n";
        content += "                  <cmdp:Start>Unknown</cmdp:Start>\n";
        content += "                  <cmdp:End>Unknown</cmdp:End>\n";
        content += "               </cmdp:TimePosition>\n";
        content += "               <cmdp:Access>\n";
        content += "                  <cmdp:Availability>openly available</cmdp:Availability>\n";
        content += "                  <cmdp:Date>2003-02-12</cmdp:Date>\n";
        content += "                  <cmdp:Owner>MPI for Psycholinguistics</cmdp:Owner>\n";
        content += "                  <cmdp:Publisher/>\n";
        content += "                  <cmdp:Contact>\n";
        content += "                     <cmdp:Name>Romuald Skiba</cmdp:Name>\n";
        content += "                     <cmdp:Address/>\n";
        content += "                     <cmdp:Email/>\n";
        content += "                     <cmdp:Organisation/>\n";
        content += "                  </cmdp:Contact>\n";
        content += "                  <cmdp:descriptions>\n";
        content += "                     <cmdp:Description LanguageId=\"\"/>\n";
        content += "                  </cmdp:descriptions>\n";
        content += "               </cmdp:Access>\n";
        content += "               <cmdp:descriptions>\n";
        content += "                  <cmdp:Description LanguageId=\"\"/>\n";
        content += "               </cmdp:descriptions>\n";
        content += "               <cmdp:Keys/>\n";
        content += "            </cmdp:MediaFile>\n";
        content += "            <cmdp:MediaFile ref=\"d314e471\">\n";
        content += "               <cmdp:ResourceLink>../Media/elan-example1.mp4</cmdp:ResourceLink>\n";
        content += "               <cmdp:Type>video</cmdp:Type>\n";
        content += "               <cmdp:Format>video/mp4</cmdp:Format>\n";
        content += "               <cmdp:Size/>\n";
        content += "               <cmdp:Quality>Unknown</cmdp:Quality>\n";
        content += "               <cmdp:RecordingConditions>excellent</cmdp:RecordingConditions>\n";
        content += "               <cmdp:TimePosition>\n";
        content += "                  <cmdp:Start>Unknown</cmdp:Start>\n";
        content += "                  <cmdp:End>Unknown</cmdp:End>\n";
        content += "               </cmdp:TimePosition>\n";
        content += "               <cmdp:Access>\n";
        content += "                  <cmdp:Availability>openly available</cmdp:Availability>\n";
        content += "                  <cmdp:Date>2003-02-12</cmdp:Date>\n";
        content += "                  <cmdp:Owner>MPI for Psycholinguistics</cmdp:Owner>\n";
        content += "                  <cmdp:Publisher/>\n";
        content += "                  <cmdp:Contact>\n";
        content += "                     <cmdp:Name>Romuald Skiba</cmdp:Name>\n";
        content += "                     <cmdp:Address/>\n";
        content += "                     <cmdp:Email/>\n";
        content += "                     <cmdp:Organisation/>\n";
        content += "                  </cmdp:Contact>\n";
        content += "                  <cmdp:descriptions>\n";
        content += "                     <cmdp:Description LanguageId=\"\"/>\n";
        content += "                  </cmdp:descriptions>\n";
        content += "               </cmdp:Access>\n";
        content += "               <cmdp:descriptions>\n";
        content += "                  <cmdp:Description LanguageId=\"\"/>\n";
        content += "               </cmdp:descriptions>\n";
        content += "               <cmdp:Keys/>\n";
        content += "            </cmdp:MediaFile>\n";
        content += "         </cmdp:Resources>\n";
        content += "         <cmdp:References>\n";
        content += "            <cmdp:descriptions>\n";
        content += "               <cmdp:Description LanguageId=\"\"/>\n";
        content += "            </cmdp:descriptions>\n";
        content += "         </cmdp:References>\n";
        content += "      </cmdp:Session>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testSession", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        assertEquals("test-hdl_58_1839_47_00-0000-0000-0009-294C-9", data.getId());
        List<Resource> resources = data.getMetadataResources();
        assertEquals(0, resources.size());
        List<Resource> dataResources = data.getDataResources();
        assertEquals(2, dataResources.size());
        Resource res = dataResources.get(0);
        assertEquals("../Media/elan-example1.mpg", res.getResourceName());
        assertEquals("video/x-mpeg1", res.getMimeType());
        res = dataResources.get(1);
        assertEquals("../Media/elan-example1.mp3", res.getResourceName());
        assertEquals("audio/mpeg", res.getMimeType());
        SolrInputDocument doc = data.getDocument();
        assertNotNull(doc);
        assertEquals("test-hdl:1839/00-0000-0000-0009-294C-9", doc.getFieldValue("_selfLink"));
        assertEquals("kleve-route", doc.getFieldValue("name"));
        assertEquals("Peter Wittenburg", doc.getFieldValue(fieldNameService.getFieldName(FieldKey.PROJECT_NAME)));
        assertEquals("Europe", doc.getFieldValue("continent"));
        assertEquals("code:eng", doc.getFieldValue("languageCode"));
        assertEquals("English", doc.getFieldValue("_languageName"));
        assertEquals("Netherlands", doc.getFieldValue("country"));
        assertEquals("Max Planck Institute for Psycholinguistics", doc.getFieldValue("organisation"));
        assertEquals("demo", doc.getFieldValue("genre"));
        assertEquals(
                "{code:eng}This  recording was made to generate a freely available test resource including speech and gestures. The annotations were created by Peter and Kita who is gesture researcher at the MPI for Psycholinguistics.",
                doc.getFieldValue("description"));
        assertEquals("[2002 TO 2002]", doc.getFieldValue("temporalCoverage"));
        List<String> fieldValues = new ArrayList(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.FORMAT)));
        assertEquals(2, fieldValues.size());
        assertEquals("video/x-mpeg1", fieldValues.get(0));
        assertEquals("video/mp4", fieldValues.get(1));
        assertEquals(null, doc.getFieldValue("subject"));
        assertEquals(18, doc.getFieldNames().size());
    }

    @Test
    public void testCreateCMDISessionSmall() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1271859438204\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "     xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438204/xsd\">\n";
        content += "   <Header>\n";
        content += "      <MdCreationDate>2008-05-27</MdCreationDate>\n";
        content += "      <MdSelfLink>test-hdl:1839/00-0000-0000-0009-294C-9</MdSelfLink>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1271859438204</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "    </Resources>\n";
        content += "   <Components>\n";
        content += "      <cmdp:Session>\n";
        content += "         <cmdp:Name>kleve-route</cmdp:Name>\n";
        content += "      </cmdp:Session>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testSession", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        assertEquals("kleve-route", data.getDocument().getFieldValue(fieldNameService.getFieldName(FieldKey.NAME)));
    }

    @Test
    public void testEmptyFieldsShouldBeNull() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<cmd:CMD xmlns=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1271859438204\" xmlns:cmd=\"http://www.clarin.eu/cmd/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "     xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438204/xsd\" CMDVersion=\"1.2\">\n";
        content += "   <cmd:Header>\n";
        content += "      <cmd:MdCreationDate>2008-05-27</cmd:MdCreationDate>\n";
        content += "      <cmd:MdSelfLink>test-hdl:1839/00-0000-0000-0009-294C-9</cmd:MdSelfLink>\n";
        content += "      <cmd:MdProfile>clarin.eu:cr1:p_1271859438204</cmd:MdProfile>\n";
        content += "   </cmd:Header>\n";
        content += "   <cmd:Resources>\n";
        content += "      <cmd:ResourceProxyList>\n";
        content += "      </cmd:ResourceProxyList>\n";
        content += "      <cmd:JournalFileProxyList/>\n";
        content += "      <cmd:ResourceRelationList/>\n";
        content += "   </cmd:Resources>\n";
        content += "   <cmd:Components>\n";
        content += "      <Session>\n";
        content += "         <Name>kleve-route</Name>\n";
        content += "         <Title>route description to Kleve</Title>\n";
        content += "         <Date></Date>\n";
        content += "         <descriptions>\n";
        content += "            <Description LanguageId=\"ISO639-2:eng\">Test.</Description>\n";
        content += "         </descriptions>\n";
        content += "         <MDGroup>\n";
        content += "            <Location>\n";
        content += "               <Continent>Europe</Continent>\n";
        content += "               <Country>Netherlands</Country>\n";
        content += "               <Region/>\n";
        content += "               <Address>Wundtlaan 1, Nijmegen</Address>\n";
        content += "            </Location>\n";
        content += "            <Project>\n";
        content += "               <Name></Name>\n";
        content += "               <Title></Title>\n";
        content += "               <Id/>\n";
        content += "               <Contact>\n";
        content += "                  <Name></Name>\n";
        content += "                  <Address></Address>\n";
        content += "                  <Email></Email>\n";
        content += "                  <Organisation></Organisation>\n";
        content += "               </Contact>\n";
        content += "               <descriptions>\n";
        content += "                  <Description LanguageId=\"\"/>\n";
        content += "               </descriptions>\n";
        content += "            </Project>\n";
        content += "            <Keys>\n";
        content += "            </Keys>\n";
        content += "            <Content>\n";
        content += "               <Genre>Demo</Genre>\n";
        content += "               <SubGenre>Unspecified</SubGenre>\n";
        content += "               <Task>route description</Task>\n";
        content += "               <Modalities>Speech; Gestures</Modalities>\n";
        content += "               <CommunicationContext>\n";
        content += "               </CommunicationContext>\n";
        content += "               <Content_Languages>\n";
        content += "               </Content_Languages>\n";
        content += "               <descriptions>\n";
        content += "               </descriptions>\n";
        content += "            </Content>\n";
        content += "            <Actors>\n";
        content += "            </Actors>\n";
        content += "         </MDGroup>\n";
        content += "         <Resources>\n";
        content += "         </Resources>\n";
        content += "      </Session>\n";
        content += "   </cmd:Components>\n";
        content += "</cmd:CMD>\n";
        File cmdiFile = createCmdiFile("testSession", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        assertEquals("test-hdl_58_1839_47_00-0000-0000-0009-294C-9", data.getId()); //modified handle -> 'clean' id
        List<Resource> resources = data.getMetadataResources();
        assertEquals(0, resources.size());
        SolrInputDocument doc = data.getDocument();
        assertNotNull(doc);
        assertEquals(10, doc.getFieldNames().size());
        assertEquals("test-hdl:1839/00-0000-0000-0009-294C-9", doc.getFieldValue("_selfLink")); //unmodified handle
        assertEquals("kleve-route", doc.getFieldValue("name"));
        assertEquals("Europe", doc.getFieldValue("continent"));
        assertEquals("Netherlands", doc.getFieldValue("country"));
        assertEquals("demo", doc.getFieldValue("genre"));
        assertEquals("{code:und}Test.", doc.getFieldValue("description"));
        assertEquals("Should be null not empty string", null, doc.getFieldValue("organisation"));
        assertEquals(null, doc.getFieldValue("language"));
        assertEquals(null, doc.getFieldValue("subject"));
        assertEquals(null, doc.getFieldValue("year"));
    }

    @Test
    public void testOlac() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1288172614026\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "     xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n";
        content += "     xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\"\n";
        content += "     xmlns:defns=\"http://www.openarchives.org/OAI/2.0/\"\n";
        content += "     xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614026/xsd\">\n";
        content += "   <Header>\n";
        content += "      <MdCreator>olac2cmdi.xsl</MdCreator>\n";
        content += "      <MdCreationDate>2002-12-14</MdCreationDate>\n";
        content += "      <MdSelfLink>oai:ailla.utexas.edu:1</MdSelfLink>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "      <ResourceProxyList/>\n";
        content += "      <JournalFileProxyList/>\n";
        content += "      <ResourceRelationList/>\n";
        content += "   </Resources>\n";
        content += "   <Components>\n";
        content += "      <cmdp:OLAC-DcmiTerms>\n";
        content += "         <cmdp:creator>Joel Sherzer (recorder)</cmdp:creator>\n";
        content += "         <cmdp:description>\n";
        content += "    Channel: Talking;\n";
        content += "    Genre: Traditional Narrative / Story;\n";
        content += "    Country: Panama;\n";
        content += "    Place of Recording: Mulatuppu;\n";
        content += "    Event: Community Gathering;\n";
        content += "    Institutional Affiliation: University of Texas at Austin;\n";
        content += "    Participant Information: Political Leader;\n";
        content += "      </cmdp:description>\n";
        content += "         <cmdp:description>The one-eyed grandmother is one of many traditional Kuna stories performed in the Kuna gathering house. This story, performed here by Pedro Arias, combines European derived motifs (Tom Thumb and Hansel and Gretel) with themes that seem more Kuna in origin. All are woven together and a moral is provided. Pedro Arias performed this story before a gathered audience in the morning..\n";
        content += "      </cmdp:description>\n";
        content += "         <cmdp:description>Test</cmdp:description>\n";
        content += "         <cmdp:identifier>http://uts.cc.utexas.edu/~ailla/audio/sherzer/one_eyed_grandmother.ram</cmdp:identifier>\n";
        content += "         <cmdp:identifier>http://uts.cc.utexas.edu/~ailla/texts/sherzer/one_eyed_grandmother.pdf</cmdp:identifier>\n";
        content += "         <cmdp:language olac-language=\"x-sil-CHN\"/>\n";
        content += "         <cmdp:language>Chinese</cmdp:language>\n";
        content += "         <cmdp:subject olac-linguistic-field=\"testSubject\">Kuna</cmdp:subject>\n";
        content += "         <cmdp:type olac-linguistic-type=\"Transcription\"/>\n";
        content += "         <cmdp:format>WAV</cmdp:format>\n";
        content += "        <cmdp:type dcterms-type=\"DCMIType\">Sound</cmdp:type>\n";
        content += "      </cmdp:OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        assertEquals("oai_58_ailla.utexas.edu_58_1", data.getId());
        List<Resource> resources = data.getMetadataResources();
        assertEquals(0, resources.size());
        List<Resource> dataResources = data.getDataResources();
        assertEquals(0, dataResources.size());
        SolrInputDocument doc = data.getDocument();
        assertNotNull(doc);
        assertEquals(11, doc.getFieldNames().size());
        assertEquals("oai:ailla.utexas.edu:1", doc.getFieldValue("_selfLink"));
        assertEquals(null, doc.getFieldValue("name"));
        assertEquals(null, doc.getFieldValue("continent"));
        assertEquals(2, doc.getFieldValues("languageCode").size());
        assertTrue(doc.getFieldValues("languageCode").contains("code:zho"));
        assertTrue(doc.getFieldValues("languageCode").contains("name:X-sil-CHN"));
        assertEquals(2, doc.getFieldValues("_languageName").size());
        assertEquals(null, doc.getFieldValue("country"));
        assertEquals(null, doc.getFieldValue("organisation"));
        assertEquals("transcription", doc.getFieldValue("genre"));
        assertEquals("kuna", doc.getFieldValue("subject"));
        Collection<Object> fieldValues = doc.getFieldValues("description");
        assertEquals(3, fieldValues.size());
        List<String> descriptions = new ArrayList(fieldValues);
        Collections.sort(descriptions);
        assertEquals("{code:und}Channel: Talking;\n    Genre: Traditional Narrative / Story;\n    Country: Panama;\n"
                + "    Place of Recording: Mulatuppu;\n    Event: Community Gathering;\n"
                + "    Institutional Affiliation: University of Texas at Austin;\n    Participant Information: Political Leader;", descriptions.get(0));
        assertEquals("{code:und}Test", descriptions.get(1));
        assertEquals("{code:und}The one-eyed grandmother is one of many traditional Kuna stories performed "
                + "in the Kuna gathering house. This story, performed here by Pedro Arias, combines "
                + "European derived motifs (Tom Thumb and Hansel and Gretel) with themes that seem more "
                + "Kuna in origin. All are woven together and a moral is provided. Pedro Arias performed "
                + "this story before a gathered audience in the morning..", descriptions.get(2));
        assertEquals("Sound", doc.getFieldValue(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS)));
    }

    @Test
    public void testOlacMultiFacets() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1288172614026\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <cmdp:OLAC-DcmiTerms>\n";
        content += "         <cmdp:subject olac-linguistic-field=\"testSubject\">Kuna</cmdp:subject>\n";
        content += "         <cmdp:subject dcterms-type=\"LCSH\">testSubjectFallback</cmdp:subject>\n";
        content += "         <cmdp:spatial dcterms-type=\"ISO3166\">testCountry1</cmdp:spatial>\n";
        content += "         <cmdp:coverage dcterms-type=\"ISO3166\">testCountry2</cmdp:coverage>\n";
        content += "         <cmdp:language olac-language=\"language1\">test1</cmdp:language>\n";
        content += "         <cmdp:subject olac-language=\"language2\">test2</cmdp:subject>\n";
        content += "         <cmdp:subject olac-language=\"language2\">test2</cmdp:subject>\n";
        content += "      </cmdp:OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        SolrInputDocument doc = data.getDocument();
        assertNull(doc.getFieldValue("_selfLink"));
        assertEquals(3, doc.getFieldValues(fieldNameService.getFieldName(FieldKey.SUBJECT)).size());
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.SUBJECT)).contains("kuna"));
        assertEquals(2, doc.getFieldValues(fieldNameService.getFieldName(FieldKey.COUNTRY)).size());
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.COUNTRY)).contains("testCountry1"));
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.COUNTRY)).contains("testCountry2"));
        assertEquals(2, doc.getFieldValues(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).size());
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).contains("name:Test1"));
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).contains("name:Language1"));

        content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1288172614026\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <cmdp:OLAC-DcmiTerms>\n";
        content += "         <cmdp:subject dcterms-type=\"LCSH\">testSubjectFallback</cmdp:subject>\n";
        content += "         <cmdp:coverage dcterms-type=\"ISO3166\">testCountry2</cmdp:coverage>\n";
        content += "         <cmdp:language olac-language=\"language1\">test1</cmdp:language>\n";
        content += "         <cmdp:subject olac-language=\"language2\">test2</cmdp:subject>\n";
        content += "      </cmdp:OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        cmdiFile = createCmdiFile("testOlac", content);
        processor = getDataParser();
        data = processor.process(cmdiFile, resourceStructureGraph);
        doc = data.getDocument();
        assertEquals(2, doc.getFieldValues(fieldNameService.getFieldName(FieldKey.SUBJECT)).size());
        assertEquals("testsubjectfallback", doc.getFieldValue(fieldNameService.getFieldName(FieldKey.SUBJECT)));
        assertEquals(1, doc.getFieldValues(fieldNameService.getFieldName(FieldKey.COUNTRY)).size());
        assertEquals("testCountry2", doc.getFieldValue(fieldNameService.getFieldName(FieldKey.COUNTRY)));
        assertEquals(2, doc.getFieldValues(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).size());
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).contains("name:Test1"));
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).contains("name:Language1"));

        content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1288172614026\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <cmdp:OLAC-DcmiTerms>\n";
        content += "         <cmdp:subject dcterms-type=\"LCSH\">testSubjectFallback</cmdp:subject>\n";
        content += "         <cmdp:subject olac-linguistic-field=\"testSubject\">Kuna</cmdp:subject>\n";
        content += "         <cmdp:coverage dcterms-type=\"ISO3166\">testCountry2</cmdp:coverage>\n";
        content += "         <cmdp:spatial dcterms-type=\"ISO3166\">testCountry1</cmdp:spatial>\n";
        content += "         <cmdp:subject olac-language=\"language1\">test2</cmdp:subject>\n";
        content += "         <cmdp:language olac-language=\"language1\">test1</cmdp:language>\n";
        content += "      </cmdp:OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        cmdiFile = createCmdiFile("testOlac", content);
        processor = getDataParser();
        data = processor.process(cmdiFile, resourceStructureGraph);
        doc = data.getDocument();
        assertEquals(3, doc.getFieldValues("subject").size());
        assertEquals("testsubjectfallback", doc.getFieldValue("subject"));
        assertEquals(2, doc.getFieldValues(fieldNameService.getFieldName(FieldKey.COUNTRY)).size());
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.COUNTRY)).contains("testCountry1"));
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.COUNTRY)).contains("testCountry2"));
        assertEquals(2, doc.getFieldValues(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).size());
        assertTrue(doc.getFieldValues(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).contains("name:Test1"));
    }

    @Test
    public void testIgnoreWhiteSpaceFacets() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1288172614026\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <cmdp:OLAC-DcmiTerms>\n";
        content += "         <cmdp:subject olac-linguistic-field=\"\n\n\t\t\t\">Kuna</cmdp:subject>\n";
        content += "      </cmdp:OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        SolrInputDocument doc = data.getDocument();
        assertTrue(doc.getFieldValues("subject").contains("kuna"));
    }

    @Test
    public void testCountryCodesPostProcessing() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1288172614026\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <cmdp:OLAC-DcmiTerms>\n";
        content += "         <cmdp:coverage dcterms-type=\"ISO3166\">NL</cmdp:coverage>\n";
        content += "      </cmdp:OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        SolrInputDocument doc = data.getDocument();
        assertEquals("Netherlands", doc.getFieldValue(fieldNameService.getFieldName(FieldKey.COUNTRY)));
    }

    @Test
    public void testLanguageCodesPostProcessing() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1288172614026\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <cmdp:OLAC-DcmiTerms>\n";
        content += "         <cmdp:language olac-language=\"fr\"/>\n";
        content += "         <cmdp:language olac-language=\"spa\"/>\n";
        content += "      </cmdp:OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        SolrInputDocument doc = data.getDocument();
//        Collection<Object> values = doc.getFieldValues(FacetConstants.DEPRECATED_FIELD_LANGUAGE);
//        assertEquals(2, values.size());
//        Iterator<Object> iter = values.iterator();
//        assertEquals("French", iter.next());
//        assertEquals("Spanish; Castilian", iter.next());
    }

    @Test
    public void testOlacCollection() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1288172614026\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614026/xsd\">\n";
        content += "    <Header>\n";
        content += "        <MdCreator>dir2cmdicollection.py</MdCreator>\n";
        content += "        <MdCreationDate>2010-10-11</MdCreationDate>\n";
        content += "        <MdSelfLink>collection_ATILF_Resources.cmdi</MdSelfLink>\n";
        content += "        <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "    </Header>\n";
        content += "    <Resources>\n";
        content += "        <ResourceProxyList>\n";
        content += "<ResourceProxy id=\"ATILF_Resources_0_oai_atilf_inalf_fr_0001.xml.cmdi\"><ResourceType>Metadata</ResourceType><ResourceRef>ATILF_Resources/0/oai_atilf_inalf_fr_0001.xml.cmdi</ResourceRef></ResourceProxy>\n";
        content += "<ResourceProxy id=\"ATILF_Resources_0_oai_atilf_inalf_fr_0002.xml.cmdi\"><ResourceType>Metadata</ResourceType><ResourceRef>ATILF_Resources/0/oai_atilf_inalf_fr_0002.xml.cmdi</ResourceRef></ResourceProxy>\n";
        content += "<ResourceProxy id=\"ATILF_Resources_0_oai_atilf_inalf_fr_0003.xml.cmdi\"><ResourceType>Metadata</ResourceType><ResourceRef>ATILF_Resources/0/oai_atilf_inalf_fr_0003.xml.cmdi</ResourceRef></ResourceProxy>\n";
        content += "<ResourceProxy id=\"ATILF_Resources_0_oai_atilf_inalf_fr_0004.xml.cmdi\"><ResourceType>Metadata</ResourceType><ResourceRef>ATILF_Resources/0/oai_atilf_inalf_fr_0004.xml.cmdi</ResourceRef></ResourceProxy>\n";
        content += "<ResourceProxy id=\"ATILF_Resources_0_oai_atilf_inalf_fr_0005_a.xml.cmdi\"><ResourceType>Metadata</ResourceType><ResourceRef>ATILF_Resources/0/oai_atilf_inalf_fr_0005_a.xml.cmdi</ResourceRef></ResourceProxy>\n";
        content += "<ResourceProxy id=\"ATILF_Resources_0_oai_atilf_inalf_fr_0005_b.xml.cmdi\"><ResourceType>Metadata</ResourceType><ResourceRef>ATILF_Resources/0/oai_atilf_inalf_fr_0005_b.xml.cmdi</ResourceRef></ResourceProxy>\n";
        content += "<ResourceProxy id=\"ATILF_Resources_0_oai_atilf_inalf_fr_0006.xml.cmdi\"><ResourceType>Metadata</ResourceType><ResourceRef>ATILF_Resources/0/oai_atilf_inalf_fr_0006.xml.cmdi</ResourceRef></ResourceProxy>\n";
        content += "<ResourceProxy id=\"ATILF_Resources_0_oai_atilf_inalf_fr_M277.xml.cmdi\"><ResourceType>Metadata</ResourceType><ResourceRef>ATILF_Resources/0/oai_atilf_inalf_fr_M277.xml.cmdi</ResourceRef></ResourceProxy>\n";
        content += "<ResourceProxy id=\"ATILF_Resources_0_oai_atilf_inalf_fr_M592.xml.cmdi\"><ResourceType>Metadata</ResourceType><ResourceRef>ATILF_Resources/0/oai_atilf_inalf_fr_M592.xml.cmdi</ResourceRef></ResourceProxy>\n";
        content += "        </ResourceProxyList>\n";
        content += "        <JournalFileProxyList/>\n";
        content += "        <ResourceRelationList/>\n";
        content += "    </Resources>\n";
        content += "    <Components>\n";
        content += "        <cmdp:olac></cmdp:olac>\n";
        content += "    </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        assertEquals("collection_ATILF_Resources.cmdi", data.getId());
        assertEquals("collection_ATILF_Resources.cmdi", data.getDocument().getFieldValue("_selfLink"));
        List<Resource> resources = data.getMetadataResources();
        assertEquals(9, resources.size());
        Resource res = resources.get(0);
        assertEquals("ATILF_Resources/0/oai_atilf_inalf_fr_0001.xml.cmdi", res.getResourceName());
        assertEquals(null, res.getMimeType());
        assertEquals(0, data.getDataResources().size());
        SolrInputDocument doc = data.getDocument();
        assertNotNull(doc);
        List<Resource> dataResources = data.getDataResources();
        assertEquals(0, dataResources.size());
    }

    @Test
    public void testLrtCollection() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1289827960126\" ns0:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1289827960126/xsd\" xmlns:ns0=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
        content += "    <Header>\n";
        content += "        <MdCreator>lrt2cmdi.py</MdCreator>\n";
        content += "        <MdCreationDate>2010-11-17</MdCreationDate>\n";
        content += "        <MdSelfLink>clarin.eu:lrt:433</MdSelfLink>\n";
        content += "        <MdProfile>clarin.eu:cr1:p_1289827960126</MdProfile>\n";
        content += "    </Header>\n";
        content += "    <Resources>\n";
        content += "        <ResourceProxyList />\n";
        content += "        <JournalFileProxyList />\n";
        content += "        <ResourceRelationList />\n";
        content += "    </Resources>\n";
        content += "    <Components>\n";
        content += "        <cmdp:LrtInventoryResource>\n";
        content += "            <cmdp:LrtCommon>\n";
        content += "                <cmdp:ResourceName>Corpus of Present-day Written Estonian</cmdp:ResourceName>\n";
        content += "                <cmdp:ResourceType>Written Corpus</cmdp:ResourceType>\n";
        content += "                <cmdp:LanguagesOther />\n";
        content += "                <cmdp:Description xml:lang='en'>written general; 95 mio words; TEI/SGML</cmdp:Description>\n";
        content += "                <cmdp:ContactPerson>Kadri.Muischnek@ut.ee</cmdp:ContactPerson>\n";
        content += "                <cmdp:Format />\n";
        content += "                <cmdp:Institute>Test</cmdp:Institute>\n";
        content += "                <cmdp:MetadataLink />\n";
        content += "                <cmdp:Publications />\n";
        content += "                <cmdp:ReadilyAvailable>true</cmdp:ReadilyAvailable>\n";
        content += "                <cmdp:ReferenceLink />         \n";
        content += "                <cmdp:Languages><cmdp:ISO639><cmdp:iso-639-3-code>est</cmdp:iso-639-3-code></cmdp:ISO639></cmdp:Languages>\n";
        content += "                <cmdp:Countries><cmdp:Country><cmdp:Code>EE</cmdp:Code></cmdp:Country></cmdp:Countries>\n";
        content += "            </cmdp:LrtCommon>\n";
        content += "       </cmdp:LrtInventoryResource>\n";
        content += "    </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        assertEquals("clarin.eu_58_lrt_58_433", data.getId());
        List<Resource> resources = data.getMetadataResources();
        assertEquals(0, resources.size());
        List<Resource> dataResources = data.getDataResources();
        assertEquals(0, dataResources.size());
        SolrInputDocument doc = data.getDocument();
        assertNotNull(doc);
        assertEquals("clarin.eu:lrt:433", doc.getFieldValue("_selfLink"));
        assertEquals("Corpus of Present-day Written Estonian", doc.getFieldValue("name"));
        assertEquals(null, doc.getFieldValue("continent"));
        assertEquals(1, doc.getFieldValues("languageCode").size());
        assertEquals("code:est", doc.getFieldValue("languageCode"));
        assertEquals("Estonian", doc.getFieldValue("_languageName"));
        assertEquals("Estonia", doc.getFieldValue("country"));
        assertEquals("Test", doc.getFieldValue("organisation"));
        assertEquals(null, doc.getFieldValue("year"));
        assertEquals(null, doc.getFieldValue("genre"));
        assertEquals("{code:eng}written general; 95 mio words; TEI/SGML", doc.getFieldValue("description"));
        assertEquals("Written Corpus", doc.getFieldValue(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS)));
        assertEquals(String.format("Expected field set different from %s", doc.getFieldNames()), 11, doc.getFieldNames().size());
    }

    @Test
    public void testConceptLinkAttributMapping() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1380106710826\" ns0:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1380106710826/xsd\" xmlns:ns0=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
        content += "    <Header>\n";
        content += "        <MdCreator>CLARIN-DK-UCPH</MdCreator>\n";
        content += "        <MdCreationDate>2014-11-26</MdCreationDate>\n";
        content += "        <MdSelfLink>hdl:11221/90D0-C024-81C4-3-8@md=cmdi</MdSelfLink>\n";
        content += "        <MdProfile>clarin.eu:cr1:p_1380106710826</MdProfile>\n";
        content += "    </Header>\n";
        content += "    <Resources>\n";
        content += "        <ResourceProxyList />\n";
        content += "        <JournalFileProxyList />\n";
        content += "        <ResourceRelationList />\n";
        content += "    </Resources>\n";
        content += "    <Components>\n";
        content += "        <cmdp:teiHeader>\n";
        content += "            <cmdp:fileDesc>\n";
        content += "                <cmdp:publicationStmt>\n";
        content += "                    <cmdp:availability status=\"restricted\">\n";
        content += "                    </cmdp:availability>\n";
        content += "                </cmdp:publicationStmt>\n";
        content += "            </cmdp:fileDesc>\n";
        content += "       </cmdp:teiHeader>\n";
        content += "    </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testAttributeMapping", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);

        assertEquals("RES", data.getDocument().getFieldValue(fieldNameService.getFieldName(FieldKey.AVAILABILITY)));
    }

    @Test
    public void testReduceAvailability() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1271859438204\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "     xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438204/xsd\">\n";
        content += "   <Header>\n";
        content += "      <MdSelfLink>test-hdl:1839/00-0000-0000-0009-294C-9</MdSelfLink>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1271859438204</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "      <ResourceProxyList>\n";
        content += "      </ResourceProxyList>\n";
        content += "      <JournalFileProxyList/>\n";
        content += "      <ResourceRelationList/>\n";
        content += "   </Resources>\n";
        content += "   <Components>\n";
        content += "      <cmdp:Session>\n";
        content += "         <cmdp:Resources>\n";
        content += "            <cmdp:MediaFile>\n";
        content += "               <cmdp:Access>\n";
        content += "                  <cmdp:Availability>PUB</cmdp:Availability>\n";
        content += "               </cmdp:Access>\n";
        content += "            </cmdp:MediaFile>\n";
        content += "            <cmdp:MediaFile>\n";
        content += "               <cmdp:Access>\n";
        content += "                  <cmdp:Availability>RES</cmdp:Availability>\n";
        content += "               </cmdp:Access>\n";
        content += "            </cmdp:MediaFile>\n";
        content += "            <cmdp:MediaFile>\n";
        content += "               <cmdp:Access>\n";
        content += "                  <cmdp:Availability>ACA</cmdp:Availability>\n";
        content += "               </cmdp:Access>\n";
        content += "            </cmdp:MediaFile>\n";
        content += "         </cmdp:Resources>\n";
        content += "      </cmdp:Session>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testSession", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        assertEquals("RES", data.getDocument().getFieldValue(fieldNameService.getFieldName(FieldKey.AVAILABILITY)));
    }
    
    @Test
    public void testMultilingual() throws Exception {
        // name facet has multilingual="true" but allowMultipleValues="false"
        // only one XPath should match, but multilingual values should all be stored
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1274880881885\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
        content += "   <Header>\n";
        content += "      <MdCreationDate>2003-01-14</MdCreationDate>\n";
        content += "      <MdSelfLink>test-hdl:1839/00-0000-0000-0000-0001-D</MdSelfLink>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1274880881885</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "      <ResourceProxyList>\n";
        content += "      </ResourceProxyList>\n";
        content += "      <JournalFileProxyList/>\n";
        content += "      <ResourceRelationList/>\n";
        content += "   </Resources>\n";
        content += "   <Components>\n";
        content += "      <cmdp:imdi-corpus>\n";
        content += "         <cmdp:Corpus>\n";
        content += "            <cmdp:Name xml:lang=\"nl\">Dutch 1</cmdp:Name>\n";
        content += "            <cmdp:Name xml:lang=\"en\">English 1</cmdp:Name>\n";
        content += "            <cmdp:Title xml:lang=\"nl\">Dutch 2</cmdp:Title>\n";
        content += "            <cmdp:Title xml:lang=\"en\">English 2</cmdp:Title>\n";
        content += "         </cmdp:Corpus>\n";
        content += "      </cmdp:imdi-corpus>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testSession", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        Collection<Object> values = data.getDocument().getFieldValues(fieldNameService.getFieldName(FieldKey.NAME));
        assertEquals(2, values.size());
        Iterator<Object> iterator = values.iterator();
        assertEquals("English 1", iterator.next().toString());
        assertEquals("Dutch 1", iterator.next().toString());
    }

    @Test
    public void testTemporalCoverageExtension() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:cmdp=\"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1288172614026\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "     xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614026/xsd\">\n";
        content += "   <Header>\n";
        content += "      <MdCreationDate>2020-02-14</MdCreationDate>\n";
        content += "      <MdSelfLink>test-hdl:TEST</MdSelfLink>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "      <ResourceProxyList>\n";
        content += "      </ResourceProxyList>\n";
        content += "      <JournalFileProxyList/>\n";
        content += "      <ResourceRelationList/>\n";
        content += "   </Resources>\n";
        content += "   <Components>\n";
        content += "      <cmdp:OLAC-DcmiTerms>\n";
        content += "         <cmdp:issued>2010-2014</cmdp:issued>\n";
        content += "         <cmdp:issued>2016</cmdp:issued>\n";
        content += "      </cmdp:OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testOlacDcmiTerms", content);
        CMDIDataProcessor<SolrInputDocument> processor = getDataParser();
        CMDIData<SolrInputDocument> data = processor.process(cmdiFile, resourceStructureGraph);
        assertEquals("[2010 TO 2016]", data.getDocument().getFieldValue("temporalCoverage"));
    }
}

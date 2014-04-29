package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FacetConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.solr.common.SolrInputDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CMDIDataProcessorTest extends ImporterTestcase {

    private CMDIDataProcessor getDataParser() {
        return new CMDIParserVTDXML(MetadataImporter.POST_PROCESSORS);
    }
    
    @Test
    public void testCreateCMDIDataFromCorpus() throws Exception {
        
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
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
        content += "      <imdi-corpus>\n";
        content += "         <Corpus>\n";
        content += "            <Name>MPI corpora</Name>\n";
        content += "            <Title>Corpora of the Max-Planck Institute for Psycholinguistics</Title>\n";
        content += "            <CorpusLink Name=\"Acquisition\">../acqui_data/Corpusstructure/acqui.imdi</CorpusLink>\n";
        content += "            <CorpusLink Name=\"Comprehension\">../Comprehension/Corpusstructure/comprehension.imdi</CorpusLink>\n";
        content += "            <CorpusLink Name=\"Language and Cognition\">../lac_data/Corpusstructure/lac.imdi</CorpusLink>\n";
        content += "            <descriptions>\n";
        content += "               <Description LanguageId=\"\">IMDI corpora</Description>\n";
        content += "               <Description LanguageId=\"\"/>\n";
        content += "            </descriptions>\n";
        content += "         </Corpus>\n";
        content += "      </imdi-corpus>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testCorpus", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        assertEquals("test-hdl_58_1839_47_00-0000-0000-0000-0001-D", data.getId());
        List<Resource> resources = data.getMetadataResources();
        assertEquals(3, resources.size());
        Resource res = resources.get(0);
        assertEquals("../acqui_data/Corpusstructure/acqui.imdi.cmdi", res.getResourceName());
        assertEquals(null, res.getMimeType());
        assertEquals(0, data.getDataResources().size());
        SolrInputDocument doc = data.getSolrDocument();
        // TODO FIX bad test case. Depends on the presence of an internet connection! (BAD!)
        assertTrue(doc.getFieldValues(FacetConstants.FIELD_CLARIN_PROFILE).contains("imdi-corpus"));
        assertNotNull(doc);
    }

    @Test
    public void testCreateCMDIDataFromSession() throws Exception {
        
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
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
        content += "      <Session>\n";
        content += "         <Name>kleve-route</Name>\n";
        content += "         <Title>route description to Kleve</Title>\n";
        content += "         <Date>2002-10-30</Date>\n";
        content += "         <descriptions>\n";
        content += "            <Description LanguageId=\"ISO639-2:eng\">This  recording was made to generate a freely available test resource including speech and gestures. The annotations were created by Peter and Kita who is gesture researcher at the MPI for Psycholinguistics.</Description>\n";
        content += "            <Description LanguageId=\"ISO639-2:ger\">Diese Aufnahme wurde erzeugt, um eine frei verf\\u00fcgbare Test Resource zur Verf\\u00fcgung stellen zu k\\u00f6nnen, die Sprache und Gestik umfasst. Die Annotationen wurden von Peter und Kita, dem Gestik Researcher am MPI erzeugt.</Description>\n";
        content += "         </descriptions>\n";
        content += "         <MDGroup>\n";
        content += "            <Location>\n";
        content += "               <Continent>Europe</Continent>\n";
        content += "               <Country>Netherlands</Country>\n";
        content += "               <Region/>\n";
        content += "               <Address>Wundtlaan 1, Nijmegen</Address>\n";
        content += "            </Location>\n";
        content += "            <Project>\n";
        content += "               <Name>Peter Wittenburg</Name>\n";
        content += "               <Title>Route description test resource</Title>\n";
        content += "               <Id/>\n";
        content += "               <Contact>\n";
        content += "                  <Name>Peter Wittenburg</Name>\n";
        content += "                  <Address>Wundtlaan 1, 6525 XD Nijmegen</Address>\n";
        content += "                  <Email>peter.wittenburg@mpi.nl</Email>\n";
        content += "                  <Organisation>Max Planck Institute for Psycholinguistics</Organisation>\n";
        content += "               </Contact>\n";
        content += "               <descriptions>\n";
        content += "                  <Description LanguageId=\"\"/>\n";
        content += "               </descriptions>\n";
        content += "            </Project>\n";
        content += "            <Keys>\n";
        content += "               <Key Name=\"conversion.IMDI.1.9to3.0.warning\">Unknown mapping of Genre: conversation|explanation|unspecified --&gt; ???</Key>\n";
        content += "            </Keys>\n";
        content += "            <Content>\n";
        content += "               <Genre>Demo</Genre>\n";
        content += "               <SubGenre>Unspecified</SubGenre>\n";
        content += "               <Task>route description</Task>\n";
        content += "               <Modalities>Speech; Gestures</Modalities>\n";
        content += "               <CommunicationContext>\n";
        content += "                  <Interactivity>interactive</Interactivity>\n";
        content += "                  <PlanningType>semi-spontaneous</PlanningType>\n";
        content += "                  <Involvement>elicited</Involvement>\n";
        content += "                  <SocialContext>Unspecified</SocialContext>\n";
        content += "                  <EventStructure>Unspecified</EventStructure>\n";
        content += "                  <Channel>Unspecified</Channel>\n";
        content += "               </CommunicationContext>\n";
        content += "               <Content_Languages>\n";
        content += "                  <descriptions>\n";
        content += "                     <Description LanguageId=\"\"/>\n";
        content += "                  </descriptions>\n";
        content += "                  <Content_Language>\n";
        content += "                     <Id>ISO639-3:eng</Id>\n";
        content += "                     <Name>English</Name>\n";
        content += "                     <descriptions>\n";
        content += "                        <Description LanguageId=\"\"/>\n";
        content += "                     </descriptions>\n";
        content += "                  </Content_Language>\n";
        content += "               </Content_Languages>\n";
        content += "               <Keys>\n";
        content += "                  <Key Name=\"IMDI__1_9.Interactional\">conversation</Key>\n";
        content += "                  <Key Name=\"IMDI__1_9.Discursive\">explanation</Key>\n";
        content += "                  <Key Name=\"IMDI__1_9.Interactional\">Unspecified</Key>\n";
        content += "               </Keys>\n";
        content += "               <descriptions>\n";
        content += "                  <Description LanguageId=\"ISO639:eng\">This file was generated from an IMDI 1.9 file and transformed to IMDI 3.0. The substructure of Genre is replaced by two elements named \"Genre\" and \"SubGenre\". The original content of Genre substructure was: Interactional = 'conversation', Discursive = 'explanation', Performance = 'Unspecified'. These values have been added as Keys to the Content information.</Description>\n";
        content += "                  <Description LanguageId=\"ISO639:eng\">Peter explains how to come from Nijmegen to Kleve by car, such that Kita would be able to get there.</Description>\n";
        content += "               </descriptions>\n";
        content += "            </Content>\n";
        content += "            <Actors>\n";
        content += "               <descriptions>\n";
        content += "                  <Description LanguageId=\"\"/>\n";
        content += "               </descriptions>\n";
        content += "               <Actor>\n";
        content += "                  <Role>interviewee</Role>\n";
        content += "                  <Name>Peter</Name>\n";
        content += "                  <FullName>Peter Wittenburg</FullName>\n";
        content += "                  <Code>W</Code>\n";
        content += "                  <FamilySocialRole>Unspecified</FamilySocialRole>\n";
        content += "                  <EthnicGroup/>\n";
        content += "                  <Age>Unknown</Age>\n";
        content += "                  <BirthDate>Unspecified</BirthDate>\n";
        content += "                  <Sex>Unknown</Sex>\n";
        content += "                  <Education>university</Education>\n";
        content += "                  <Anonymized>true</Anonymized>\n";
        content += "                  <Contact>\n";
        content += "                     <Name/>\n";
        content += "                     <Address/>\n";
        content += "                     <Email/>\n";
        content += "                     <Organisation/>\n";
        content += "                  </Contact>\n";
        content += "                  <Keys/>\n";
        content += "                  <descriptions>\n";
        content += "                     <Description LanguageId=\"\"/>\n";
        content += "                  </descriptions>\n";
        content += "                  <Actor_Languages>\n";
        content += "                     <descriptions>\n";
        content += "                        <Description LanguageId=\"\"/>\n";
        content += "                     </descriptions>\n";
        content += "                     <Actor_Language>\n";
        content += "                        <Id>ISO639-3:nld</Id>\n";
        content += "                        <Name>Dutch</Name>\n";
        content += "                        <descriptions>\n";
        content += "                           <Description LanguageId=\"\"/>\n";
        content += "                        </descriptions>\n";
        content += "                     </Actor_Language>\n";
        content += "                     <Actor_Language>\n";
        content += "                        <Id>ISO639-3:deu</Id>\n";
        content += "                        <Name>German</Name>\n";
        content += "                        <descriptions>\n";
        content += "                           <Description LanguageId=\"\"/>\n";
        content += "                        </descriptions>\n";
        content += "                     </Actor_Language>\n";
        content += "                     <Actor_Language>\n";
        content += "                        <Id>ISO639-3:eng</Id>\n";
        content += "                        <Name>English</Name>\n";
        content += "                        <descriptions>\n";
        content += "                           <Description LanguageId=\"\"/>\n";
        content += "                        </descriptions>\n";
        content += "                     </Actor_Language>\n";
        content += "                  </Actor_Languages>\n";
        content += "               </Actor>\n";
        content += "               <Actor>\n";
        content += "                  <Role>interviewer</Role>\n";
        content += "                  <Name>Kita</Name>\n";
        content += "                  <FullName>Sotaro Kita</FullName>\n";
        content += "                  <Code>k</Code>\n";
        content += "                  <FamilySocialRole>Unspecified</FamilySocialRole>\n";
        content += "                  <EthnicGroup/>\n";
        content += "                  <Age>Unknown</Age>\n";
        content += "                  <BirthDate>Unspecified</BirthDate>\n";
        content += "                  <Sex>Unknown</Sex>\n";
        content += "                  <Education>university</Education>\n";
        content += "                  <Anonymized>true</Anonymized>\n";
        content += "                  <Contact>\n";
        content += "                     <Name/>\n";
        content += "                     <Address/>\n";
        content += "                     <Email/>\n";
        content += "                     <Organisation/>\n";
        content += "                  </Contact>\n";
        content += "                  <Keys/>\n";
        content += "                  <descriptions>\n";
        content += "                     <Description LanguageId=\"\"/>\n";
        content += "                  </descriptions>\n";
        content += "                  <Actor_Languages>\n";
        content += "                     <descriptions>\n";
        content += "                        <Description LanguageId=\"\"/>\n";
        content += "                     </descriptions>\n";
        content += "                     <Actor_Language>\n";
        content += "                        <Id>ISO639-3:eng</Id>\n";
        content += "                        <Name>English</Name>\n";
        content += "                        <descriptions>\n";
        content += "                           <Description LanguageId=\"\"/>\n";
        content += "                        </descriptions>\n";
        content += "                     </Actor_Language>\n";
        content += "                     <Actor_Language>\n";
        content += "                        <Id>ISO639-3:jpn</Id>\n";
        content += "                        <Name>Japanese</Name>\n";
        content += "                        <descriptions>\n";
        content += "                           <Description LanguageId=\"\"/>\n";
        content += "                        </descriptions>\n";
        content += "                     </Actor_Language>\n";
        content += "                  </Actor_Languages>\n";
        content += "               </Actor>\n";
        content += "               <Actor>\n";
        content += "                  <Role>Collector</Role>\n";
        content += "                  <Name>Peter Wittenburg</Name>\n";
        content += "                  <FullName>Peter Wittenburg</FullName>\n";
        content += "                  <Code>Unspecified</Code>\n";
        content += "                  <FamilySocialRole>Unspecified</FamilySocialRole>\n";
        content += "                  <EthnicGroup/>\n";
        content += "                  <Age>Unspecified</Age>\n";
        content += "                  <BirthDate>Unspecified</BirthDate>\n";
        content += "                  <Sex>Unspecified</Sex>\n";
        content += "                  <Education/>\n";
        content += "                  <Anonymized>false</Anonymized>\n";
        content += "                  <Contact>\n";
        content += "                     <Name>Peter Wittenburg</Name>\n";
        content += "                     <Address>Wundtlaan 1, 6525 XD Nijmegen</Address>\n";
        content += "                     <Email>peter.wittenburg@mpi.nl</Email>\n";
        content += "                     <Organisation>Max-Planck-Institute for Psycholinguistics</Organisation>\n";
        content += "                  </Contact>\n";
        content += "                  <Keys/>\n";
        content += "                  <descriptions>\n";
        content += "                     <Description LanguageId=\"\"/>\n";
        content += "                  </descriptions>\n";
        content += "                  <Actor_Languages/>\n";
        content += "               </Actor>\n";
        content += "            </Actors>\n";
        content += "         </MDGroup>\n";
        content += "         <Resources>\n";
        content += "            <MediaFile ref=\"d314e408\">\n";
        content += "               <ResourceLink>../Media/elan-example1.mpg</ResourceLink>\n";
        content += "               <Type>video</Type>\n";
        content += "               <Format>video/x-mpeg1</Format>\n";
        content += "               <Size/>\n";
        content += "               <Quality>Unknown</Quality>\n";
        content += "               <RecordingConditions>excellent</RecordingConditions>\n";
        content += "               <TimePosition>\n";
        content += "                  <Start>Unknown</Start>\n";
        content += "                  <End>Unknown</End>\n";
        content += "               </TimePosition>\n";
        content += "               <Access>\n";
        content += "                  <Availability>openly available</Availability>\n";
        content += "                  <Date>2003-02-12</Date>\n";
        content += "                  <Owner>MPI for Psycholinguistics</Owner>\n";
        content += "                  <Publisher/>\n";
        content += "                  <Contact>\n";
        content += "                     <Name>Romuald Skiba</Name>\n";
        content += "                     <Address/>\n";
        content += "                     <Email/>\n";
        content += "                     <Organisation/>\n";
        content += "                  </Contact>\n";
        content += "                  <descriptions>\n";
        content += "                     <Description LanguageId=\"\"/>\n";
        content += "                  </descriptions>\n";
        content += "               </Access>\n";
        content += "               <descriptions>\n";
        content += "                  <Description LanguageId=\"\"/>\n";
        content += "               </descriptions>\n";
        content += "               <Keys/>\n";
        content += "            </MediaFile>\n";
        content += "            <MediaFile ref=\"d314e471\">\n";
        content += "               <ResourceLink>../Media/elan-example1.mp4</ResourceLink>\n";
        content += "               <Type>video</Type>\n";
        content += "               <Format>video/mp4</Format>\n";
        content += "               <Size/>\n";
        content += "               <Quality>Unknown</Quality>\n";
        content += "               <RecordingConditions>excellent</RecordingConditions>\n";
        content += "               <TimePosition>\n";
        content += "                  <Start>Unknown</Start>\n";
        content += "                  <End>Unknown</End>\n";
        content += "               </TimePosition>\n";
        content += "               <Access>\n";
        content += "                  <Availability>openly available</Availability>\n";
        content += "                  <Date>2003-02-12</Date>\n";
        content += "                  <Owner>MPI for Psycholinguistics</Owner>\n";
        content += "                  <Publisher/>\n";
        content += "                  <Contact>\n";
        content += "                     <Name>Romuald Skiba</Name>\n";
        content += "                     <Address/>\n";
        content += "                     <Email/>\n";
        content += "                     <Organisation/>\n";
        content += "                  </Contact>\n";
        content += "                  <descriptions>\n";
        content += "                     <Description LanguageId=\"\"/>\n";
        content += "                  </descriptions>\n";
        content += "               </Access>\n";
        content += "               <descriptions>\n";
        content += "                  <Description LanguageId=\"\"/>\n";
        content += "               </descriptions>\n";
        content += "               <Keys/>\n";
        content += "            </MediaFile>\n";
        content += "         </Resources>\n";
        content += "         <References>\n";
        content += "            <descriptions>\n";
        content += "               <Description LanguageId=\"\"/>\n";
        content += "            </descriptions>\n";
        content += "         </References>\n";
        content += "      </Session>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testSession", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
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
        SolrInputDocument doc = data.getSolrDocument();
        assertNotNull(doc);
        assertEquals(15, doc.getFieldNames().size());
        assertEquals("test-hdl:1839/00-0000-0000-0009-294C-9", doc.getFieldValue("_selfLink"));
        assertEquals("kleve-route", doc.getFieldValue("name"));
        assertEquals("Peter Wittenburg", doc.getFieldValue(FacetConstants.FIELD_PROJECT_NAME));
        assertEquals("Europe", doc.getFieldValue("continent"));
        assertEquals("English", doc.getFieldValue("language"));
        assertEquals("Netherlands", doc.getFieldValue("country"));
        assertEquals("Max Planck Institute for Psycholinguistics", doc.getFieldValue("organisation"));
        assertEquals("demo", doc.getFieldValue("genre"));
        assertEquals(
                "This  recording was made to generate a freely available test resource including speech and gestures. The annotations were created by Peter and Kita who is gesture researcher at the MPI for Psycholinguistics.",
                doc.getFieldValue("description"));
        assertEquals("2002", doc.getFieldValue("year"));
        List<String> fieldValues = new ArrayList(doc.getFieldValues(FacetConstants.FIELD_FORMAT));
        assertEquals(2, fieldValues.size());
        assertEquals("video/x-mpeg1", fieldValues.get(0));
        assertEquals("video/mp4", fieldValues.get(1));
        assertEquals(null, doc.getFieldValue("subject"));
    }

    @Test
    public void testCreateCMDISessionSmall() throws Exception {
        
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "     xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438204/xsd\">\n";
        content += "   <Header>\n";
        content += "      <MdCreationDate>2008-05-27</MdCreationDate>\n";
        content += "      <MdSelfLink>test-hdl:1839/00-0000-0000-0009-294C-9</MdSelfLink>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1271859438204</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Resources>\n";
        content += "    </Resources>\n";
        content += "   <Components>\n";
        content += "      <Session>\n";
        content += "         <Name>kleve-route</Name>\n";
        content += "      </Session>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testSession", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        assertEquals("kleve-route", data.getSolrDocument().getFieldValue(FacetConstants.FIELD_NAME));
    }

    @Test
    public void testEmptyFieldsShouldBeNull() throws Exception {
        
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "     xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438204/xsd\">\n";
        content += "   <Header>\n";
        content += "      <MdCreationDate>2008-05-27</MdCreationDate>\n";
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
        content += "   </Components>\n";
        content += "</CMD>\n";
        File cmdiFile = createCmdiFile("testSession", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        assertEquals("test-hdl_58_1839_47_00-0000-0000-0009-294C-9", data.getId()); //modified handle -> 'clean' id
        List<Resource> resources = data.getMetadataResources();
        assertEquals(0, resources.size());
        SolrInputDocument doc = data.getSolrDocument();
        assertNotNull(doc);
        assertEquals(9, doc.getFieldNames().size());
        assertEquals("test-hdl:1839/00-0000-0000-0009-294C-9", doc.getFieldValue("_selfLink")); //unmodified handle
        assertEquals("kleve-route", doc.getFieldValue("name"));
        assertEquals("Europe", doc.getFieldValue("continent"));
        assertEquals("Netherlands", doc.getFieldValue("country"));
        assertEquals("demo", doc.getFieldValue("genre"));
        assertEquals("Test.", doc.getFieldValue("description"));
        assertEquals("Should be null not empty string", null, doc.getFieldValue("organisation"));
        assertEquals(null, doc.getFieldValue("language"));
        assertEquals(null, doc.getFieldValue("subject"));
        assertEquals(null, doc.getFieldValue("year"));
    }

    @Test
    public void testOlac() throws Exception {

        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
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
        content += "      <OLAC-DcmiTerms>\n";
        content += "         <creator>Joel Sherzer (recorder)</creator>\n";
        content += "         <description>\n";
        content += "    Channel: Talking;\n";
        content += "    Genre: Traditional Narrative / Story;\n";
        content += "    Country: Panama;\n";
        content += "    Place of Recording: Mulatuppu;\n";
        content += "    Event: Community Gathering;\n";
        content += "    Institutional Affiliation: University of Texas at Austin;\n";
        content += "    Participant Information: Political Leader;\n";
        content += "      </description>\n";
        content += "         <description>The one-eyed grandmother is one of many traditional Kuna stories performed in the Kuna gathering house. This story, performed here by Pedro Arias, combines European derived motifs (Tom Thumb and Hansel and Gretel) with themes that seem more Kuna in origin. All are woven together and a moral is provided. Pedro Arias performed this story before a gathered audience in the morning..\n";
        content += "      </description>\n";
        content += "         <description>Test</description>\n";
        content += "         <identifier>http://uts.cc.utexas.edu/~ailla/audio/sherzer/one_eyed_grandmother.ram</identifier>\n";
        content += "         <identifier>http://uts.cc.utexas.edu/~ailla/texts/sherzer/one_eyed_grandmother.pdf</identifier>\n";
        content += "         <language olac-language=\"x-sil-CHN\"/>\n";
        content += "         <language>Chinese</language>\n";
        content += "         <subject olac-linguistic-field=\"testSubject\">Kuna</subject>\n";
        content += "         <type olac-linguistic-type=\"Transcription\"/>\n";
        content += "         <format>WAV</format>\n";
        content += "        <type dcterms-type=\"DCMIType\">Sound</type>\n";
        content += "      </OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        assertEquals("oai_58_ailla.utexas.edu_58_1", data.getId());
        List<Resource> resources = data.getMetadataResources();
        assertEquals(0, resources.size());
        List<Resource> dataResources = data.getDataResources();
        assertEquals(0, dataResources.size());
        SolrInputDocument doc = data.getSolrDocument();
        assertNotNull(doc);
        assertEquals(10, doc.getFieldNames().size());
        assertEquals("oai:ailla.utexas.edu:1", doc.getFieldValue("_selfLink"));
        assertEquals(null, doc.getFieldValue("name"));
        assertEquals(null, doc.getFieldValue("continent"));
        assertEquals(1, doc.getFieldValues("language").size());
        assertEquals("Chinese", doc.getFieldValue("language"));
        assertEquals(null, doc.getFieldValue("country"));
        assertEquals(null, doc.getFieldValue("organisation"));
        assertEquals("transcription", doc.getFieldValue("genre"));
        assertEquals("kuna", doc.getFieldValue("subject"));
        Collection<Object> fieldValues = doc.getFieldValues("description");
        assertEquals(3, fieldValues.size());
        List<String> descriptions = new ArrayList(fieldValues);
        Collections.sort(descriptions);
        assertEquals("Channel: Talking;\n    Genre: Traditional Narrative / Story;\n    Country: Panama;\n"
                + "    Place of Recording: Mulatuppu;\n    Event: Community Gathering;\n"
                + "    Institutional Affiliation: University of Texas at Austin;\n    Participant Information: Political Leader;", descriptions.get(0).toString());
        assertEquals("Test", descriptions.get(1).toString());
        assertEquals("The one-eyed grandmother is one of many traditional Kuna stories performed "
                + "in the Kuna gathering house. This story, performed here by Pedro Arias, combines "
                + "European derived motifs (Tom Thumb and Hansel and Gretel) with themes that seem more "
                + "Kuna in origin. All are woven together and a moral is provided. Pedro Arias performed "
                + "this story before a gathered audience in the morning..", descriptions.get(2).toString());
        assertEquals("Sound", doc.getFieldValue(FacetConstants.FIELD_RESOURCE_CLASS));
    }

    @Test
    public void testOlacMultiFacets() throws Exception {
        
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <OLAC-DcmiTerms>\n";
        content += "         <subject olac-linguistic-field=\"testSubject\">Kuna</subject>\n";
        content += "         <subject dcterms-type=\"LCSH\">testSubjectFallback</subject>\n";
        content += "         <spatial dcterms-type=\"ISO3166\">testCountry1</spatial>\n";
        content += "         <coverage dcterms-type=\"ISO3166\">testCountry2</coverage>\n";
        content += "         <language olac-language=\"language1\">test1</language>\n";
        content += "         <subject olac-language=\"language2\">test2</subject>\n";
        content += "         <subject olac-language=\"language2\">test2</subject>\n";
        content += "      </OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        SolrInputDocument doc = data.getSolrDocument();
        assertNull(doc.getFieldValue("_selfLink"));
        assertEquals(3, doc.getFieldValues(FacetConstants.FIELD_SUBJECT).size());
        assertTrue(doc.getFieldValues(FacetConstants.FIELD_SUBJECT).contains("kuna"));
        assertEquals(2, doc.getFieldValues(FacetConstants.FIELD_COUNTRY).size());
        assertTrue(doc.getFieldValues(FacetConstants.FIELD_COUNTRY).contains("testCountry1"));
        assertTrue(doc.getFieldValues(FacetConstants.FIELD_COUNTRY).contains("testCountry2"));
        assertEquals(1, doc.getFieldValues(FacetConstants.FIELD_LANGUAGE).size());
        assertTrue(doc.getFieldValues(FacetConstants.FIELD_LANGUAGE).contains("test1"));

        content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <OLAC-DcmiTerms>\n";
        content += "         <subject dcterms-type=\"LCSH\">testSubjectFallback</subject>\n";
        content += "         <coverage dcterms-type=\"ISO3166\">testCountry2</coverage>\n";
        content += "         <language olac-language=\"language1\">test1</language>\n";
        content += "         <subject olac-language=\"language2\">test2</subject>\n";
        content += "      </OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        cmdiFile = createCmdiFile("testOlac", content);
        processor = getDataParser();
        data = processor.process(cmdiFile);
        doc = data.getSolrDocument();
        assertEquals(2, doc.getFieldValues(FacetConstants.FIELD_SUBJECT).size());
        assertEquals("testsubjectfallback", doc.getFieldValue(FacetConstants.FIELD_SUBJECT));
        assertEquals(1, doc.getFieldValues(FacetConstants.FIELD_COUNTRY).size());
        assertEquals("testCountry2", doc.getFieldValue(FacetConstants.FIELD_COUNTRY));
        assertEquals(1, doc.getFieldValues(FacetConstants.FIELD_LANGUAGE).size());
        assertEquals("test1", doc.getFieldValue(FacetConstants.FIELD_LANGUAGE));

        content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <OLAC-DcmiTerms>\n";
        content += "         <subject dcterms-type=\"LCSH\">testSubjectFallback</subject>\n";
        content += "         <subject olac-linguistic-field=\"testSubject\">Kuna</subject>\n";
        content += "         <coverage dcterms-type=\"ISO3166\">testCountry2</coverage>\n";
        content += "         <spatial dcterms-type=\"ISO3166\">testCountry1</spatial>\n";
        content += "         <subject olac-language=\"language1\">test2</subject>\n";
        content += "         <language olac-language=\"language1\">test1</language>\n";
        content += "      </OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        cmdiFile = createCmdiFile("testOlac", content);
        processor = getDataParser();
        data = processor.process(cmdiFile);
        doc = data.getSolrDocument();
        assertEquals(3, doc.getFieldValues("subject").size());
        assertEquals("testsubjectfallback", doc.getFieldValue("subject"));
        assertEquals(2, doc.getFieldValues(FacetConstants.FIELD_COUNTRY).size());
        assertTrue(doc.getFieldValues(FacetConstants.FIELD_COUNTRY).contains("testCountry1"));
        assertTrue(doc.getFieldValues(FacetConstants.FIELD_COUNTRY).contains("testCountry2"));
        assertEquals(1, doc.getFieldValues(FacetConstants.FIELD_LANGUAGE).size());
        assertTrue(doc.getFieldValues(FacetConstants.FIELD_LANGUAGE).contains("test1"));
    }

    @Test
    public void testIgnoreWhiteSpaceFacets() throws Exception {
       
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <OLAC-DcmiTerms>\n";
        content += "         <subject olac-linguistic-field=\"\n\n\t\t\t\">Kuna</subject>\n";
        content += "      </OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        SolrInputDocument doc = data.getSolrDocument();
        assertTrue(doc.getFieldValues("subject").contains("kuna"));
    }

    @Test
    public void testCountryCodesPostProcessing() throws Exception {
        
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <OLAC-DcmiTerms>\n";
        content += "         <coverage dcterms-type=\"ISO3166\">NL</coverage>\n";
        content += "      </OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        SolrInputDocument doc = data.getSolrDocument();
        assertEquals("Netherlands", doc.getFieldValue(FacetConstants.FIELD_COUNTRY));
    }

    @Test
    public void testLanguageCodesPostProcessing() throws Exception {
        
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "   <Components>\n";
        content += "      <OLAC-DcmiTerms>\n";
        content += "         <language olac-language=\"fr\"/>\n";
        content += "         <language olac-language=\"spa\"/>\n";
        content += "      </OLAC-DcmiTerms>\n";
        content += "   </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        SolrInputDocument doc = data.getSolrDocument();
//        Collection<Object> values = doc.getFieldValues(FacetConstants.FIELD_LANGUAGE);
//        assertEquals(2, values.size());
//        Iterator<Object> iter = values.iterator();
//        assertEquals("French", iter.next());
//        assertEquals("Spanish; Castilian", iter.next());
    }

    @Test
    public void testOlacCollection() throws Exception {
        
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
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
        content += "        <olac></olac>\n";
        content += "    </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        assertEquals("collection_ATILF_Resources.cmdi", data.getId());
        assertEquals("collection_ATILF_Resources.cmdi", data.getSolrDocument().getFieldValue("_selfLink"));
        List<Resource> resources = data.getMetadataResources();
        assertEquals(9, resources.size());
        Resource res = resources.get(0);
        assertEquals("ATILF_Resources/0/oai_atilf_inalf_fr_0001.xml.cmdi", res.getResourceName());
        assertEquals(null, res.getMimeType());
        assertEquals(0, data.getDataResources().size());
        SolrInputDocument doc = data.getSolrDocument();
        assertNotNull(doc);
        List<Resource> dataResources = data.getDataResources();
        assertEquals(0, dataResources.size());
    }

    @Test
    public void testLrtCollection() throws Exception {
       
        // make sure the mapping file for testing is used
        config.setFacetConceptsFile("/facetConceptsTest.xml");

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/\" ns0:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1289827960126/xsd\" xmlns:ns0=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
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
        content += "        <LrtInventoryResource>\n";
        content += "            <LrtCommon>\n";
        content += "                <ResourceName>Corpus of Present-day Written Estonian</ResourceName>\n";
        content += "                <ResourceType>Written Corpus</ResourceType>\n";
        content += "                <LanguagesOther />\n";
        content += "                <Description>written general; 95 mio words; TEI/SGML</Description>\n";
        content += "                <ContactPerson>Kadri.Muischnek@ut.ee</ContactPerson>\n";
        content += "                <Format />\n";
        content += "                <Institute>Test</Institute>\n";
        content += "                <MetadataLink />\n";
        content += "                <Publications />\n";
        content += "                <ReadilyAvailable>true</ReadilyAvailable>\n";
        content += "                <ReferenceLink />         \n";
        content += "                <Languages><ISO639><iso-639-3-code>est</iso-639-3-code></ISO639></Languages>\n";
        content += "                <Countries><Country><Code>EE</Code></Country></Countries>\n";
        content += "            </LrtCommon>\n";
        content += "       </LrtInventoryResource>\n";
        content += "    </Components>\n";
        content += "</CMD>\n";

        File cmdiFile = createCmdiFile("testOlac", content);
        CMDIDataProcessor processor = getDataParser();
        CMDIData data = processor.process(cmdiFile);
        assertEquals("clarin.eu_58_lrt_58_433", data.getId());
        List<Resource> resources = data.getMetadataResources();
        assertEquals(0, resources.size());
        List<Resource> dataResources = data.getDataResources();
        assertEquals(0, dataResources.size());
        SolrInputDocument doc = data.getSolrDocument();
        assertNotNull(doc);
        assertEquals(10, doc.getFieldNames().size());
        assertEquals("clarin.eu:lrt:433", doc.getFieldValue("_selfLink"));
        assertEquals("Corpus of Present-day Written Estonian", doc.getFieldValue("name"));
        assertEquals(null, doc.getFieldValue("continent"));
        assertEquals(1, doc.getFieldValues("language").size());
        assertEquals("Estonian", doc.getFieldValue("language"));
        assertEquals("Estonia", doc.getFieldValue("country"));
        assertEquals("Test", doc.getFieldValue("organisation"));
        assertEquals(null, doc.getFieldValue("year"));
        assertEquals(null, doc.getFieldValue("genre"));
        assertEquals("written general; 95 mio words; TEI/SGML", doc.getFieldValue("description"));
        assertEquals("Written Corpus", doc.getFieldValue(FacetConstants.FIELD_RESOURCE_CLASS));
    }
}

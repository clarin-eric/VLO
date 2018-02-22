package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class CMDIParserVTDXMLTest extends ImporterTestcase {

    @Test
    public void testExtractXsdFromHeader() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\">\n";
        content += "   <Header>\n";
        content += "      <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>\n";
        content += "   </Header>\n";
        content += "</CMD>\n";
        String xsd = getXsd(content);
        assertEquals("clarin.eu:cr1:p_1288172614026", xsd);
    }

    @Test
    public void testExtractXsdFromSchemaLocation() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n";
        content += "     xsi:schemaLocation=\"http://www.clarin.eu/cmd http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614026/xsd\">\n";
        content += "</CMD>\n";
        String xsd = getXsd(content);
        assertEquals("clarin.eu:cr1:p_1288172614026", xsd);
    }

    @Test
    public void testExtractXsdFromNoSchemaLocation() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n";
        content += "     xsi:noNamespaceSchemaLocation=\"http://www.meertens.knaw.nl/oai/cmdi/diddd_sub_location_profile.xsd\">\n";
        content += "</CMD>\n";
        String xsd = getXsd(content);
        assertEquals(null, xsd);
    }

    @Test
    public void testNoXsd() throws Exception {
        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD xmlns=\"http://www.clarin.eu/cmd/1\">\n";
        content += "</CMD>\n";
        String xsd = getXsd(content);
        assertNull(xsd);
    }

    private String getXsd(String content) throws Exception {
        VTDGen vg = new VTDGen();
        vg.setDoc(content.getBytes());
        vg.parse(true);
        VTDNav nav = vg.getNav();
        final VloConfig newVloConfig = new VloConfig();
        newVloConfig.setValueMappingsFile(super.getTestValueMappingsFilePath());
        final FacetMappingFactory facetMappingFactory = new FacetMappingFactory(newVloConfig, marshaller);
        CMDIParserVTDXML parser = new CMDIParserVTDXML(null, newVloConfig, facetMappingFactory, marshaller, true);
        String xsd = parser.extractXsd(nav);
        return xsd;
    }
}

package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import eu.clarin.cmdi.vlo.config.DataRoot;

public class MetadataImporterMultiDatarootsTest extends ImporterTestcase {
	
	
	private File createFile(String creationDate, String name, String title) throws Exception{
		String session = "";
        session += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        session += "<CMD xmlns=\"http://www.clarin.eu/cmd/\">\n";
        session += "   <Header>\n";
        session += "      <MdCreationDate>" + creationDate + "</MdCreationDate>\n";
        session += "      <MdSelfLink> testID1Session</MdSelfLink>\n";
        session += "      <MdCollectionDisplayName>CollectionName</MdCollectionDisplayName>\n";
        session += "      <MdProfile>clarin.eu:cr1:p_1271859438204</MdProfile>\n";
        session += "   </Header>\n";
        session += "   <Resources>\n";
        session += "      <ResourceProxyList>\n";
        session += "         <ResourceProxy id=\"d314e408\">\n";
        session += "            <ResourceType mimetype=\"video/x-mpeg1\" >Resource</ResourceType>\n";
        session += "            <ResourceRef>../Media/elan-example1.mpg</ResourceRef>\n";
        session += "         </ResourceProxy>\n";
        session += "      </ResourceProxyList>\n";
        session += "   </Resources>\n";
        session += "   <Components>\n";
        session += "      <Session>\n";
        session += "         <Name>" + name + "</Name>\n";
        session += "         <Title>" + title + "</Title>\n";
        session += "      </Session>\n";
        session += "   </Components>\n";
        session += "</CMD>\n";
        return createCmdiFile("testSession", session);
	}
   
	//It doesnt import any data
	//tests if checkDataRoots returns only existing dataRoots
    @Test
    public void testImporterSimpleMulitDataRoots() throws Exception {
        
        File file1 = createFile("2008-05-27", "someName", "someTitle");
        File file2 = createFile("2010-11-06", "someOtherName", "someOtherTitle");
        

        List<DataRoot> dataRoots = setDataRoots(new File[]{file1, file2});
        
        //2 existing and 2 non existing dataroots are added
        //after calling checkDataRoots(), 2 existing must be returned
        assertTrue(dataRoots.size() == 2);
        assertTrue(dataRoots.get(0).getOriginName().equals("existing-root1"));
        assertTrue(dataRoots.get(1).getOriginName().equals("existing-root2"));
    }
    

    
    private List<DataRoot> setDataRoots(File... roots) throws MalformedURLException {
                
        modifyConfig_multi_dataroots(roots);
        
        MetadataImporter importer = new MetadataImporter(config, languageCodeUtils);
        
        List<DataRoot> dataRoots = importer.checkDataRoots();
        
        return dataRoots;
    }

    private DataRoot createDataRoot(File rootFile, String originName, String prefix){
    	DataRoot dataRoot = new DataRoot();
        dataRoot.setDeleteFirst(false); // cannot delete becanot using real solrServer
        dataRoot.setOriginName(originName);
        dataRoot.setRootFile(rootFile);
        dataRoot.setTostrip("");
        dataRoot.setPrefix(prefix);
        
        return dataRoot;
    	
    }
    
    private void modifyConfig_multi_dataroots(File... rootFiles) {
    	List<DataRoot> dataroots = new LinkedList<DataRoot>();
    	
    	dataroots.add(createDataRoot(
    			new File("C:\\Users\\dostojic\\AppData\\Local\\Temp\\dummy1.cmdi"),
    			"non-existing-root1", 
    			"http://idontexist.com"));
    	
    	int cnt = 1;
    	for(File root: rootFiles){
    		dataroots.add(createDataRoot(root, "existing-root" + cnt, "http://example" + cnt++ + ".com"));
    	}
        
    	
    	dataroots.add(createDataRoot(
    			new File("C:\\Users\\dostojic\\AppData\\Local\\Temp\\dummy2.cmdi"),
    			"non-existing-root2", 
    			"http://meneither.com"));
        
        config.setDataRoots(dataroots);
    }

}

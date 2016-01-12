package eu.clarin.vlo.sitemap.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.vlo.sitemap.pojo.Sitemap.URL;

public class SOLRService {
	
	public static final Logger _logger = LoggerFactory.getLogger(SOLRService.class);

	static final String SERVER = "https://minerva.arz.oeaw.ac.at/tomcat/vlo-solr/core0/select?";
	static final String RECORD_URL = "https://vlo.clarin.eu/record?docId=";
	
	static final String GET_IDS = "fl=id&rows=";
	
	static final int MAX_NUM_OF_RECORDS = 1000000; //1M
	
	
	private VTDGen vg;
	
	public SOLRService(){
		vg = new VTDGen();
	}
	
	public List<URL> getRecordURLS() throws Exception{

		List<URL> ids = new ArrayList<URL>(MAX_NUM_OF_RECORDS);
		
		boolean parseSuccess = vg.parseHttpUrl(SERVER + GET_IDS + MAX_NUM_OF_RECORDS, false);
		
		if(!parseSuccess)
			throw new RuntimeException("error parsing result from: " + SERVER + GET_IDS + MAX_NUM_OF_RECORDS);
		
		VTDNav nav = vg.getNav();		
		AutoPilot ap = new AutoPilot(nav);
				
		ap.selectXPath("//response/result/doc/str");
				
		int i = -1;
		while((i = ap.evalXPath()) != -1){
			String id = nav.toNormalizedString(nav.getText());
			ids.add(new URL(RECORD_URL + id));
		}
		
		return ids;
			
	}

}

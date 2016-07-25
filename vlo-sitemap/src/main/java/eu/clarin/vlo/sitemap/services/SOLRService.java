package eu.clarin.vlo.sitemap.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.vlo.sitemap.gen.Config;
import eu.clarin.vlo.sitemap.pojo.Sitemap.URL;

public class SOLRService {
	
	public static final Logger _logger = LoggerFactory.getLogger(SOLRService.class);
	
	//to increase performances, should be increased in future
	static final int MAX_NUM_OF_RECORDS = 1500000; //1M
	static final String GET_IDS = "fl=id&rows=";
	
	
	private VTDGen vg;
	
	public SOLRService(){
		vg = new VTDGen();
	}
	
	public List<URL> getRecordURLS() throws Exception{

		List<URL> ids = new ArrayList<URL>(MAX_NUM_OF_RECORDS);
		
                //TODO: paginate
		boolean parseSuccess = vg.parseHttpUrl(Config.SOLR_QUERY_URL + GET_IDS + MAX_NUM_OF_RECORDS, false);
		
		if(!parseSuccess)
			throw new RuntimeException("Error retrieving or parsing result from: " + Config.SOLR_QUERY_URL + GET_IDS + MAX_NUM_OF_RECORDS);
		
		VTDNav nav = vg.getNav();		
		AutoPilot ap = new AutoPilot(nav);
				
		ap.selectXPath("//response/result/doc/str");
				
		int i = -1;
		while((i = ap.evalXPath()) != -1){
			String id = nav.toNormalizedString(nav.getText());
			ids.add(new URL(Config.RECORD_URL_TEMPLATE + id));
		}
		
		return ids;
			
	}

}

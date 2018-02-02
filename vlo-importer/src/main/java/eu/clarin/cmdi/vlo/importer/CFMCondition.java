/**
 * 
 */
package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;

/**
 * @author WolfgangWalter SAUER (wowasa) <wolfgang.sauer@oeaw.ac.at>
 *
 */
public class CFMCondition {
	String ifValue;
	List<FacetValuePair> fvp = new ArrayList<FacetValuePair>();
	
	
	CFMCondition(String ifValue){
		this.ifValue = ifValue;
	}
	
	public String getIfValue(){
		return this.ifValue;
	}
	
	public void addFacetValuePair(FacetConfiguration fc, String value){
		this.fvp.add(new FacetValuePair(fc, value));
	}
	
	public List<FacetValuePair>getFacetValuePairs(){
		return this.fvp;
	}
	
	public class FacetValuePair{
		private FacetConfiguration fc;
		private String value;
		
		public FacetValuePair(FacetConfiguration fc, String value){
			this.fc = fc;
			this.value = value;
		}
		
		public FacetConfiguration getFacetConfiguration(){
			return this.fc;
		}
		
		public String getValue(){
			return this.value;
		}
	}

}

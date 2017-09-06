/**
 * 
 */
package eu.clarin.cmdi.vlo.importer.processor;

import java.util.List;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;

/**
 * @author WolfgangWalter SAUER (wowasa) <wolfgang.sauer@oeaw.ac.at>
 *
 */
public class CrossFacetMap extends PostProcessorsWithVocabularyMap {

	/**
	 * @param config
	 */
	public CrossFacetMap(VloConfig config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see eu.clarin.cmdi.vlo.importer.processor.PostProcessor#process(java.lang.String, eu.clarin.cmdi.vlo.importer.CMDIData)
	 */
	@Override
	public List<String> process(String value, CMDIData cmdiData) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.clarin.cmdi.vlo.importer.processor.PostProcessor#doesProcessNoValue()
	 */
	@Override
	public boolean doesProcessNoValue() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.clarin.cmdi.vlo.importer.processor.PostProcessorsWithVocabularyMap#getNormalizationMapURL()
	 */
	@Override
	public String getNormalizationMapURL() {
		// TODO Auto-generated method stub
		return this.getConfig().getCrossFacetMapUrl();
	}

}

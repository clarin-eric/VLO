package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.ArrayList;
import java.util.List;

import eu.clarin.cmdi.vlo.importer.jaxb.*;

/**
 * @author @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class Blabla {
	
	private ValueMappings vm;
	
	public Blabla() {
		List<Target> targets;
		List<ConditionTargetSet> conditionTargetSets;
		
		ConditionTargetSet conditionTargetSet;
		
		for(OriginFacet originFacet : vm.getOriginFacet()) {
			for(ValueMap valueMap : originFacet.getValueMap()) {
				conditionTargetSets = new ArrayList<ConditionTargetSet>();
				for(TargetValueSet targetValueSet : valueMap.getTargetValueSet()) {
					conditionTargetSet = new ConditionTargetSet();
					conditionTargetSets.add(conditionTargetSet);
					for(SourceValue sourceValue : targetValueSet.getSouceValues()) {
						if(sourceValue.isIsRegex()) {
							conditionTargetSet.addCondtion(new RegExCondition(sourceValue.getContent()));
						}
						else {
							conditionTargetSet.addCondtion(new StringCondition(sourceValue.getContent(), sourceValue.isCaseSensitive()));
						}
					}
					
					for(TargetValue targetValue : targetValueSet.getTargetValues()) {
						Target target = new Target();
						
						target.setOverwriteExistingValues(targetValue.isOverrideExistingValues());
						
						
					}
				}
			}
		}
	}
	
	

}

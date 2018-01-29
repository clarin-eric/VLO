package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.ArrayList;
import java.util.Vector;

public class ConditionTargetSet {
	private final Vector<AbstractCondition> conditions;
	private final Vector<TargetFacet> targets;
	
	public ConditionTargetSet() {
		this.conditions = new Vector<AbstractCondition>();
		this.targets = new Vector<TargetFacet>();
	}
	
	public void addCondtion(AbstractCondition condition) {
		this.conditions.add(condition);
	}
	
	public void addTarget(TargetFacet target) {
		this.targets.add(target);
	}
	public Vector<TargetFacet> getTargets(){
		return this.targets;
	}
	
	public boolean matches(String expression) {
		for(AbstractCondition condition : this.conditions) {
			if(condition.matches(expression))
				return true;
		}
		return false;
	}

}

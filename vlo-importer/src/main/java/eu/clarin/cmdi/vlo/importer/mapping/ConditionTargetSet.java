package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.ArrayList;
import java.util.List;

public class ConditionTargetSet {
	private final List<AbstractCondition> conditions;
	private final List<Target> targets;
	
	public ConditionTargetSet() {
		this.conditions = new ArrayList<AbstractCondition>();
		this.targets = new ArrayList<Target>();
	}
	
	public void addCondtion(AbstractCondition condition) {
		this.conditions.add(condition);
	}
	
	public void addTarget(Target target) {
		this.targets.add(target);
	}
	public List<Target> getTargets(){
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

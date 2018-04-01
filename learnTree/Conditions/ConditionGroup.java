package learnTree.Conditions;

import java.util.ArrayList;

public class ConditionGroup<I> {
	
	private ArrayList<Condition<I>> conditions;
	private String name;
	
	public ConditionGroup(String name) {
		this.conditions = new ArrayList<Condition<I>>();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public ConditionGroup(ArrayList<Condition<I>> conditions,String name) {
		this.conditions = conditions;
		this.name = name;

	}
	
	public Condition<I> getCondition(int i) {
		return conditions.get(i);
	}
	
	public ArrayList<Condition<I>> getConditions() {
		return conditions;
	}
	
}

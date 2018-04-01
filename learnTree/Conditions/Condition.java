package learnTree.Conditions;

import java.io.Serializable;

// I = Input Type = InputPhoto

public class Condition<I> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	SPred<I> predicate;
	String name;
	
	public Condition(SPred<I> p, String name) {
		this.predicate = p;
		this.name = name;
	}
	
	public boolean checkCondition(I input) {
		return predicate.test(input);
	}

	public SPred<I> getPredicate() {
		return predicate;
	}
	
	public String getName() {
		return name;
	}
	
}

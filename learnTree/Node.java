package learnTree;

import java.util.HashMap;

public abstract class Node<D> { 
	
	JunctionNode father=null;
	D data; 
	boolean isLeaf; 
	HashMap<Character, Integer> visitedTagsAmount; //Ni(L)  tag,amount
	int numOfVisits = 0; //N(L)
	boolean isTrue;
	
	public Node() {} 
	
	public abstract Node<D> copyTree();
	
	public boolean isLeaf() { 
		return isLeaf;
	}
	
	public abstract D getData(); 

	public abstract void setData(D data);
	
	public JunctionNode getFather() {
		return father;
	}

	public void setFather(JunctionNode father) {
		this.father = father;
	}
	
	public boolean isTrue() {
		return isTrue;
	}
	
	public void setNumOfVisits(int n) {
		numOfVisits = n;
	}
	
	public int getNumOfVisits() {
		return numOfVisits;
	}
	
}

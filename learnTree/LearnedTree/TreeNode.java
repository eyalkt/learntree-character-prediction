package learnTree.LearnedTree;

import java.io.Serializable;

import learnTree.InputPhoto;

public abstract class TreeNode<D> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	D data; 
	boolean isLeaf; 

	public TreeNode() {} 

	public boolean isLeaf() { 
		return isLeaf;
	}
	
	public abstract D getData(); 
	public abstract char revealTag(InputPhoto input);

}

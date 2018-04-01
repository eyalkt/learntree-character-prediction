package learnTree.LearnedTree;

import learnTree.InputPhoto;
import learnTree.JunctionNode;
import learnTree.LeafNode;
import learnTree.Conditions.Condition;

public class JunctionTreeNode extends TreeNode<Condition<InputPhoto>>  {

	private static final long serialVersionUID = 1L;
	private TreeNode<?> trueNode;
	private TreeNode<?> falseNode;
	
	public JunctionTreeNode(JunctionNode node) {
		this.data = node.getData();
		
		if(!node.getTrueNode().isLeaf()) this.trueNode = new JunctionTreeNode((JunctionNode) node.getTrueNode());
		else this.trueNode = new LeafTreeNode((LeafNode) node.getTrueNode());
		
		if(!node.getFalseNode().isLeaf()) this.falseNode = new JunctionTreeNode((JunctionNode) node.getFalseNode());
		else this.falseNode = new LeafTreeNode((LeafNode) node.getFalseNode());
		
		this.isLeaf = false;
	}



	@Override
	public Condition<InputPhoto> getData() {
		return data;
	}
	
	 public char revealTag(InputPhoto input) {
			if (data.checkCondition(input)) {
				return trueNode.revealTag(input);
			}
			else return falseNode.revealTag(input);
	}
	 

}

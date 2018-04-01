package learnTree;

import learnTree.Conditions.Condition;

public class JunctionNode extends Node<Condition<InputPhoto>> {

	private Node<?> trueNode;
	private Node<?> falseNode;
	
	public JunctionNode(Condition<InputPhoto> cond, Node<?> trueNode, Node<?> falseNode,boolean isTrue) {
		this.data = cond;
		this.trueNode = trueNode;
		this.falseNode = falseNode;
		this.isLeaf = false;
		this.isTrue = isTrue;
	}

	@Override
	public JunctionNode copyTree() {
		return new JunctionNode(this.data, trueNode.copyTree(), falseNode.copyTree(),this.isTrue);
	}

	@Override
	public Condition<InputPhoto> getData() {
		return data;
	}

	@Override
	public void setData(Condition<InputPhoto> data) {
		this.data = data;
	}
	
	public void setSon(JunctionNode newSon) {
		if (newSon.isTrue) trueNode = newSon;
		else falseNode = newSon;
	}

	
	public Node<?> getTrueNode() {
		return trueNode;
	}
	
	public Node<?> getFalseNode() {
		return falseNode;
	}

}

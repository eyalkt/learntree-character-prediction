package learnTree.LearnedTree;

import learnTree.InputPhoto;
import learnTree.LeafNode;

public class LeafTreeNode extends TreeNode<Character> {

	private static final long serialVersionUID = 1L;
	
	public LeafTreeNode(LeafNode node) {
		this.data = node.getData();
		this.isLeaf = true;
	}

	@Override
	public Character getData() {
		return data;
	}

	@Override
	public char revealTag(InputPhoto input) {
		return data;
	}

}

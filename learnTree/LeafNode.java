package learnTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import learnTree.Conditions.Condition;

public class LeafNode extends Node<Character> {
	
	private ArrayList<InputPhoto> photos;
	final double log2 = Math.log(2);
	private double NIG;
	JunctionNode replacer;

	
	public LeafNode(char tag, boolean isTrue ) {
		this.data=tag;
		this.isLeaf = true;
		this.isTrue=isTrue;
		visitedTagsAmount = new HashMap<Character,Integer>(); //tag,amount
		photos = new ArrayList<InputPhoto>();
		this.NIG=0;
		replacer=null;
	}
	
	public void changeTag() {
		char tag=' ';
		int maxTag = 0;
		for (Map.Entry<Character, Integer> entry : visitedTagsAmount.entrySet()){
			if(entry.getValue()>maxTag) {
				tag = entry.getKey();
				maxTag = entry.getValue();
			}
		}
		this.data=tag;
	}

	
	public ArrayList<InputPhoto> getPhotos() {
		return photos;
	}

	public void setPhotos(ArrayList<InputPhoto> photos) {
		this.photos = photos;
	}

	public void visitLeaf(InputPhoto p) {
		numOfVisits++;
		int x = 0;
		photos.add(p);
		if (visitedTagsAmount.containsKey(p.getTag())) x = visitedTagsAmount.get(p.getTag());
		visitedTagsAmount.put(p.getTag(), ++x);
	}

	@Override
	public LeafNode copyTree() {
		return new LeafNode(this.data , this.isTrue );
	}
	
	public JunctionNode transformToJunction(Condition<InputPhoto> cond, LeafNode trueNode, LeafNode falseNode) {
		return new JunctionNode(cond, trueNode, falseNode, isTrue );
	}

	@Override
	public Character getData() {
		return this.data;
	}

	@Override
	public void setData(Character data) {
		this.data=data;
	}
	
	public double getEntropy() {
		double Ni=0;
		double entropy=0;
		Set<Character> keys = visitedTagsAmount.keySet();
		for (char key:keys) {
			Ni= visitedTagsAmount.get(key);
			double log = Ni>0 ? (Math.log(numOfVisits/Ni))/log2 : 0; // TODO why /log2 ?
			entropy+=(log*(Ni/numOfVisits));
		}
		return entropy;
	}
	
	public double getNIG() {
		return NIG;
	}

	public void setNIG(double nIG) {
		NIG = nIG;
	}
	
	
}

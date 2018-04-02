package learnTree;

import java.util.ArrayList;
import java.util.Hashtable;

import learnTree.Conditions.Condition;
import learnTree.Conditions.ConditionGroup;
import learnTree.Conditions.SPred;
import learnTree.LearnedTree.JunctionTreeNode;
import learnTree.LearnedTree.TreeNode;

import java.util.Set;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class LearnTree {

	private static double maxTreeSize;
	private static int treeSize;
	private static int selectedConditionGroup;
	private static ConditionGroup<InputPhoto> conditionGroup;
	private static ArrayList<InputPhoto> trainingSet; 
	private static String outputFileName; 
	private static Node<?> learningTree;
	private static String inputFileName;
	private static ArrayList<LeafNode> leaves; 
	private static ArrayList<InputPhoto> validationSet;
	private final static int INPUT_PHOTO_LENGTH = 28;
	private final static int INPUT_PHOTO_WIDTH = 28;	
	private static TreeNode<?> theTree;
	private static int maxSuccesses;


	public static void main(String[] args) {

		trainingSet = new ArrayList<InputPhoto>();
		leaves = new ArrayList<LeafNode>();

		parseArgs(args);
		initConditions();
		buildTree();
		printStats();
		produceTreeOutputFile();

	}

	private static void printStats() {
		int successRate = (maxSuccesses*100)/validationSet.size();
		System.out.println("num: "+(trainingSet.size()+validationSet.size()));
		System.out.println("error: "+(100-successRate));
		System.out.println("size: "+treeSize);

	}

	private static void produceTreeOutputFile() {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFileName))) {
			oos.writeObject(theTree);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	//////////////////////
	// Parsing the args //
	//////////////////////
	
	private static void parseArgs(String[] args) {

		if (args.length != 5) {
			System.err.println("ERROR: incorrect num of args.");
			System.exit(1);
		}

		outputFileName = args[4];
		inputFileName = args[3];

		try {
			selectedConditionGroup = Integer.parseInt(args[0]);
			if (selectedConditionGroup > 2 || selectedConditionGroup < 1) 
				throw new NumberFormatException(": "+String.valueOf(selectedConditionGroup));
		} catch (NumberFormatException e) {
			String problem = e.getMessage().substring(e.getMessage().indexOf(':'));
			System.err.println("No such condition group: expected a number 1 or 2. received "+problem);
			System.exit(1);
		}

		int L = 0;
		try {
			L = Integer.parseInt(args[2]); 
			if (L < 0) throw new NumberFormatException(": " + L);
		} catch (NumberFormatException e) {
			String problem = e.getMessage().substring(e.getMessage().indexOf(':'));
			System.err.println("L: expected a positive number. received "+problem);
			System.exit(1);
		}
		maxTreeSize = Math.pow(2, L);

		int P = 0;
		try {
			P = Integer.parseInt(args[1]);
			if (100 < P || P < 0) throw new NumberFormatException(": "+ P);
		} catch (NumberFormatException e) {
			String problem = e.getMessage().substring(e.getMessage().indexOf(':'));
			System.err.println("P: expected a number between 0 and 100. received "+problem);
			System.exit(1);
		}
		
		parseTrainingSet(inputFileName, P);
	}

	private static void parseTrainingSet(String fileName, int validationPrecent) {

		String line = "";
		String spliter = ",";
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			while ((line = br.readLine()) != null) {
				ArrayList<Integer> photo = new ArrayList<Integer>();
				String[] inputPhoto = line.split(spliter);
				char tag = inputPhoto[0].charAt(0);
				for (int i = 1; i<inputPhoto.length; i++) photo.add(Integer.parseInt(inputPhoto[i]));
				InputPhoto p = new InputPhoto(tag, photo, INPUT_PHOTO_LENGTH, INPUT_PHOTO_WIDTH);
				trainingSet.add(p);

			}
		} catch (IOException e) {
			System.err.println("ERROR: input file not found");
			System.exit(1);
		}
		validationSet = chooseValidationSet(validationPrecent);
	}

	private static ArrayList<InputPhoto> chooseValidationSet(int validationPrecent) { //also updates the trainingSet
		ArrayList<InputPhoto> vSet = new ArrayList<InputPhoto>();
		double vSetSize = (((double)validationPrecent/100)*trainingSet.size());
		for (int i = 0; i<vSetSize; i++) {
			InputPhoto input = trainingSet.remove((int)(Math.random()*trainingSet.size()));
			vSet.add(input);
		}
		return vSet;
	}

	/////////////////////////////////////////////////
	// algorithm core - building the learning tree //
	/////////////////////////////////////////////////
	
	private static void buildTree() {

		treeSize=0;
		learningTree = chooseFirstLeaf();
		maxSuccesses = 0;
		int timeToNewTree = 1;
		for (int numOfNodes = 0; numOfNodes <= maxTreeSize ;numOfNodes++) {
			if(numOfNodes==timeToNewTree) {
				timeToNewTree*=2;
				JunctionTreeNode tree = new JunctionTreeNode((JunctionNode)learningTree);
				int num = numOfNodes;
				checkNewTree(tree,num);
			}
			replaceNode();
		}			
	}

	// compare the newly built tree with the best tree made so far
	private static void checkNewTree(JunctionTreeNode tree, int numOfNodes) {

		int currTreeSuccesses = 0;
		for (InputPhoto photo: validationSet) 
			if (tree.revealTag(photo)==photo.getTag()) currTreeSuccesses++;
		if (currTreeSuccesses > maxSuccesses) {
			theTree = tree;
			maxSuccesses = currTreeSuccesses;
			treeSize = numOfNodes;
		}	

	}

	// single algorithm step - replacing a single leaf node with a junction node
	private static void replaceNode() {

		Replacers maximalImprovementPerLeaf = findReplacementCandidates();
		JunctionNode chosenReplacer = maximalImprovementPerLeaf.replacer;
		LeafNode chosenLeafToReplace = maximalImprovementPerLeaf.toReplace;
		leaves.remove(chosenLeafToReplace);
		if (chosenLeafToReplace.getFather() != null)
			chosenLeafToReplace.getFather().setSon(chosenReplacer);
		else learningTree = chosenReplacer;	// first replacement
		leaves.add((LeafNode)(chosenReplacer.getFalseNode()));
		leaves.add((LeafNode)(chosenReplacer.getTrueNode()));
	}

	static class Replacers{
		double improvement;
		JunctionNode replacer;
		LeafNode toReplace;
	};

	// find the best <leaf-to-replace, junction-replacement> tuple
	private static Replacers findReplacementCandidates() {

		Replacers bestReplacementsRanked = new Replacers();
		bestReplacementsRanked.improvement = 0;

		for(LeafNode leaf:leaves) {
			findBestReplacement(leaf, bestReplacementsRanked);					
		}

		return bestReplacementsRanked;
	}

	// given a leaf - find its best junction replacement
	private static void findBestReplacement(LeafNode leaf,Replacers maximalImprovementPerLeaf) {

		if(leaf.getNIG()>maximalImprovementPerLeaf.improvement) {
			maximalImprovementPerLeaf.improvement=leaf.getNIG();
			maximalImprovementPerLeaf.toReplace=leaf;
			maximalImprovementPerLeaf.replacer=leaf.replacer;
			return;
		}
		if(leaf.getNIG()>0)return;
		JunctionNode bestReplacement = null;
		double maxImprovement=0;
		double N = leaf.getNumOfVisits();
		double Hl =leaf.getEntropy();
		if (Hl==0) return;
		for(Condition<InputPhoto> condition:conditionGroup.getConditions()) {
			LeafNode trueNode = new LeafNode(' ', true);
			LeafNode falseNode= new LeafNode(' ', false);
			for (InputPhoto photo:leaf.getPhotos()) { 
				if (condition.checkCondition(photo)) 
					trueNode.visitLeaf(photo);
				else falseNode.visitLeaf(photo);
			}

			double Na = trueNode.getNumOfVisits();
			double Ha = trueNode.getEntropy();
			double Nb = falseNode.getNumOfVisits();
			double Hb = falseNode.getEntropy();
			double Hx = ((Na/N)*Ha) + ((Nb/N)*Hb);
			double IG = Hl-Hx;

			if(IG*N >= maxImprovement) {
				bestReplacement = leaf.transformToJunction(condition, trueNode, falseNode);
				trueNode.changeTag();
				falseNode.changeTag();
				trueNode.setFather(bestReplacement);
				falseNode.setFather(bestReplacement);
				maxImprovement = IG*N;
				leaf.replacer=bestReplacement;
			}
		}
		leaf.setNIG(maxImprovement);
		bestReplacement.setFather(leaf.getFather());
		if(maximalImprovementPerLeaf.improvement<maxImprovement) {
			maximalImprovementPerLeaf.improvement=maxImprovement;
			maximalImprovementPerLeaf.replacer=bestReplacement;
			maximalImprovementPerLeaf.toReplace=leaf;
		}
	}

	// choose the learning tree 1st leaf (1st step of the algorithm)
	private static Node<?> chooseFirstLeaf() {

		LeafNode firstNode = new LeafNode(' ',false);
		Hashtable<Character,Integer> photos = new Hashtable<Character,Integer>(); // tag,amount
		for(InputPhoto photo:trainingSet) {	//find most common tag by counting
			char tag= photo.getTag();
			firstNode.visitLeaf(photo);
			if(photos.containsKey(tag)) {
				int amount= photos.get(tag);
				photos.put(tag, ++amount);
			}
			else photos.put(tag, 1);
		}
		int maxTagAmount = 0; 
		Set<Character> tags = photos.keySet();
		for (char tag:tags) { 
			if(photos.get(tag)>maxTagAmount) {
				maxTagAmount=photos.get(tag);
				firstNode.setData(tag);
			}
		}
		leaves.add(firstNode);

		return firstNode;		
	}

	/////////////////////////////////////////////////////
	// initialize the conditions according to the args //
	/////////////////////////////////////////////////////
	
	private static void initConditions() {

		if(selectedConditionGroup==1) {
			
			conditionGroup = new ConditionGroup<InputPhoto>("group1"); 
			ArrayList<Condition<InputPhoto>> conds = conditionGroup.getConditions();
			for (int i=0; i<INPUT_PHOTO_LENGTH; i++) {
				for (int j=0; j<INPUT_PHOTO_WIDTH; j++) { 
					int x = j;
					int y = i;
					conds.add(new Condition<InputPhoto>((p)-> p.getPhotoPixel(x, y) > 128, "x: "+x+" y: "+y));}
			}
			
		}

		else {
			
			conditionGroup = new ConditionGroup<InputPhoto>("2");
			ArrayList<Condition<InputPhoto>> group2Conds = conditionGroup.getConditions();

			//group 1 conditions
			for (int i=0; i<INPUT_PHOTO_LENGTH; i++) {
				for (int j=0; j<INPUT_PHOTO_WIDTH; j++) { 
					int x = j;
					int y = i;
					group2Conds.add(new Condition<InputPhoto>((p)-> p.getPhotoPixel(x, y) > 128, "x: "+x+" y: "+y));}
			}

			//horizontally exactly 1 hit 
			for (int i=0; i<INPUT_PHOTO_LENGTH; i++) {
				int y = i;
				SPred<InputPhoto> pr = (p) -> {
					int x=0;
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false; 
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return true;	
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					return (x==INPUT_PHOTO_WIDTH); 

				};
				group2Conds.add(new Condition<InputPhoto>(pr, "E: 1 hits exactly "));

			}

			//vertically exactly 1 hit 
			for (int i=0; i<INPUT_PHOTO_WIDTH; i++) {
				int x = i;
				SPred<InputPhoto> pr = (p) -> {
					int y=0;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return true;	
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					return (y==INPUT_PHOTO_WIDTH); 
				};
				group2Conds.add(new Condition<InputPhoto>(pr, "RE: 1 hits exactly "));

			}

			//horizontally exactly 2 hits
			for (int i=0; i<INPUT_PHOTO_LENGTH; i++) {
				int y = i;
				SPred<InputPhoto> pr = (p) -> {
					int x=0;
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false; 
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false;	
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false; 
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return true;	
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					return(x==INPUT_PHOTO_WIDTH);
				};
				group2Conds.add(new Condition<InputPhoto>(pr, "E: 2 hits exactly "));

			}

			//vertically exactly 2 hits 
			for (int i=0; i<INPUT_PHOTO_WIDTH; i++) {
				int x = i;
				SPred<InputPhoto> pr = (p) -> {
					int y=0;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;	
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return true;	
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					return (y==INPUT_PHOTO_WIDTH);
				};
				group2Conds.add(new Condition<InputPhoto>(pr, "RE: 2 hits exactly "));

			}

			//horizontally exactly 3 hits
			for (int i=0; i<INPUT_PHOTO_LENGTH; i++) {
				int y = i;
				SPred<InputPhoto> pr = (p) -> {
					int x=0;
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false;
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false;	
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false; 
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false;	
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false; 
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return true;	
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					return(x==INPUT_PHOTO_WIDTH);

				};
				group2Conds.add(new Condition<InputPhoto>(pr, "E: 3 hits exactly "));

			}

			//horizontally 4 hits at least
			for (int i=0; i<INPUT_PHOTO_LENGTH; i++) {
				int y = i;
				SPred<InputPhoto> pr = (p) -> {
					int x=0;
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false; 
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false;	
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false; 
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false;	
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return false; 
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) x++;
					if (x==INPUT_PHOTO_WIDTH) return true;	
					while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) x++;
					return(x<INPUT_PHOTO_WIDTH);

				};
				group2Conds.add(new Condition<InputPhoto>(pr, "E: 4 hits at least "));

			}

			//vertically exactly 3 hits 
			for (int i=0; i<INPUT_PHOTO_WIDTH; i++) {
				int x = i;
				SPred<InputPhoto> pr = (p) -> {
					int y=0;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false; 
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;	
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return true;	
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return true;	
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					return (y==INPUT_PHOTO_WIDTH);

				};
				group2Conds.add(new Condition<InputPhoto>(pr, "RE: 3 hits exactly "));

			}

			//vertically 4 hits at least
			for (int i=0; i<INPUT_PHOTO_WIDTH; i++) {
				int x = i;
				SPred<InputPhoto> pr = (p) -> {
					int y=0;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false; 
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;	
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return true;	
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return false;
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) > 100) y++;
					if (y==INPUT_PHOTO_WIDTH) return true;	
					while(y<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, y) <= 100) y++;
					return (y<INPUT_PHOTO_WIDTH);

				};
				group2Conds.add(new Condition<InputPhoto>(pr, "RE: 4 hits at least "));

			}

			//main (starting at <0,0>) diagonal exactly 1 hit
			SPred<InputPhoto> pr1 = (p) -> {
				int x=0;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100 ) x++;
				if (x==INPUT_PHOTO_WIDTH) return false; 
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) > 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return true;	
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100) x++;
				return (x==INPUT_PHOTO_WIDTH);

			};
			group2Conds.add(new Condition<InputPhoto>(pr1, "D: 1 hits exactly "));

			//second (starting at <INPUT_PHOTO_WIDTH-1,0>) diagonal exactly 1 hit
			SPred<InputPhoto> pr1R = (p) -> {
				int x=INPUT_PHOTO_WIDTH-1;
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				if (x<0) return false; 
				while(x>=0 && p.getPhotoPixel(x, x) > 100 ) x--;
				if (x<0) return true;	
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				return (x<0);

			};
			group2Conds.add(new Condition<InputPhoto>(pr1R, "RD: 1 hits exactly "));
			
			//second (starting at <INPUT_PHOTO_WIDTH-1,0>) diagonal exactly 2 hits 
			SPred<InputPhoto> pr2R = (p) -> {
				int x=INPUT_PHOTO_WIDTH-1;
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				if (x<0) return false;
				while(x>=0 && p.getPhotoPixel(x, x) > 100 ) x--;
				if (x<0) return false;	
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				if (x<0) return false;
				while(x>=0 && p.getPhotoPixel(x, x) > 100 ) x--;
				if (x<0) return true;	
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				return (x<0);

			};
			group2Conds.add(new Condition<InputPhoto>(pr2R, "RD: 2 hits exactly "));

			//main (starting at <0,0>) diagonal exactly 2 hits 
			SPred<InputPhoto> pr2 = (p) -> {
				int x=0;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return false; 
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) > 100 ) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;	
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) > 100 ) x++;
				if (x==INPUT_PHOTO_WIDTH) return true;	
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100 ) x++;
				return (x==INPUT_PHOTO_WIDTH);

			};
			group2Conds.add(new Condition<InputPhoto>(pr2, "D: 2 hits exactly "));
			
			//main (starting at <0,0>) diagonal exactly 3 hits 
			SPred<InputPhoto> pr3 = (p) -> {
				int x=0;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100 ) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) > 100 ) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;	
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) > 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;	
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) > 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return true;	
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100) x++;
				return (x==INPUT_PHOTO_WIDTH);


			};
			group2Conds.add(new Condition<InputPhoto>(pr3, "D: 3 hits exactly "));
			
			//second (starting at <INPUT_PHOTO_WIDTH-1,0>) diagonal exactly 3 hits 
			SPred<InputPhoto> pr3R = (p) -> {
				int x=INPUT_PHOTO_WIDTH-1;
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				if (x<0) return false;
				while(x>=0 && p.getPhotoPixel(x, x) > 100 ) x--;
				if (x<0) return false;	
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				if (x<0) return false;
				while(x>=0 && p.getPhotoPixel(x, x) > 100 ) x--;
				if (x<0) return false;	
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				if (x<0) return false;
				while(x>=0 && p.getPhotoPixel(x, x) > 100 ) x--;
				if (x<0) return true;	
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				return (x<0);

			};
			group2Conds.add(new Condition<InputPhoto>(pr3R, "RD: 3 hits exactly"));

			//main (starting at <0,0>) diagonal 4 hits at least 
			SPred<InputPhoto> pr4 = (p) -> {
				int x=0;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100 ) x++;
				if (x==INPUT_PHOTO_WIDTH) return false; 
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) > 100 ) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;	
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) > 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;	
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return false;
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) > 100) x++;
				if (x==INPUT_PHOTO_WIDTH) return true;	
				while(x<INPUT_PHOTO_WIDTH && p.getPhotoPixel(x, x) <= 100) x++;
				return (x<INPUT_PHOTO_WIDTH);
			};
			group2Conds.add(new Condition<InputPhoto>(pr4, "D: 4 hits at least "));

			//second (starting at <INPUT_PHOTO_WIDTH-1,0>) diagonal 4 hits at least
			SPred<InputPhoto> pr4R = (p) -> {
				int x=INPUT_PHOTO_WIDTH-1;
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				if (x<0) return false;
				while(x>=0 && p.getPhotoPixel(x, x) > 100 ) x--;
				if (x<0) return false;	
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				if (x<0) return false;
				while(x>=0 && p.getPhotoPixel(x, x) > 100 ) x--;
				if (x<0) return false;	
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				if (x<0) return false;
				while(x>=0 && p.getPhotoPixel(x, x) > 100 ) x--;
				if (x<0) return true;	
				while(x>=0 && p.getPhotoPixel(x, x) <= 100 ) x--;
				return (x>=0);
			};
			group2Conds.add(new Condition<InputPhoto>(pr4R, "RD: 4 hits at least"));

		}

	}
	
}




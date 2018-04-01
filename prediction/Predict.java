package prediction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import learnTree.InputPhoto;
import learnTree.LearnedTree.JunctionTreeNode;

public class Predict {

	private static ArrayList<InputPhoto> testSet;
	private final static int INPUT_PHOTO_LENGTH = 28;
	private final static int INPUT_PHOTO_WIDTH = 28;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("ERROR: incorrect num of args.");
			System.exit(1);
		}
		String treeFileName = args[0];
		String testSetFileName = args[1];
		parseTestSet(testSetFileName);
		predict(treeFileName);

	}

	private static void predict(String treeFileName) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(treeFileName))) {

			JunctionTreeNode t = (JunctionTreeNode) ois.readObject();
			for (int i=0; i<testSet.size(); i++) {	
				int iThink=((JunctionTreeNode) t).revealTag(testSet.get(i));
				System.out.println((iThink-48));
			}

		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private static void parseTestSet(String fileName) {
		testSet = new ArrayList<InputPhoto>();

		String line = "";
		String spliter = ",";
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			while ((line = br.readLine()) != null) {
				ArrayList<Integer> photo = new ArrayList<Integer>();
				String[] inputPhoto = line.split(spliter);
				char tag = inputPhoto[0].charAt(0);
				for (int i = 1; i<inputPhoto.length; i++) photo.add(Integer.parseInt(inputPhoto[i]));
				InputPhoto p = new InputPhoto(tag, photo, INPUT_PHOTO_LENGTH, INPUT_PHOTO_WIDTH);
				testSet.add(p);

			}
		} catch (IOException e) {
			System.err.println("ERROR: input file not found");
			System.exit(1);
		}

	}

}


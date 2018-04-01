package learnTree;

import java.util.ArrayList;

/*
 * represents an photoLength*photoWidth pixels photo
 */
public class InputPhoto {
	private char tag;
	private ArrayList<Integer> photo;
	private int photoLength;
	private int photoWidth;
	
	public InputPhoto (char tag, ArrayList<Integer> photo, int photoLength, int photoWidth) {
		this.tag = tag;
		this.photo = photo;
		this.photoLength = photoLength;
		this.photoWidth = photoWidth;
		
	}
	
	public int getPhotoPixel(int x, int y) {
		return photo.get((photoWidth*y)+x);
	}
	
	public char getTag() {
		return tag;
	}
	
	public ArrayList<Integer> getPhoto() {
		return new ArrayList<Integer>(photo);
	}
}

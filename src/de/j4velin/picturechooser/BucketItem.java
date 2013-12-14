package de.j4velin.picturechooser;

public class BucketItem extends GridItem {

	final int id;
	int images;

	public BucketItem(String n, String p, int i) {
		super(n, p);
		id = i;
	}

}

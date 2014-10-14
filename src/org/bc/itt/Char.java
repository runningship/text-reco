package org.bc.itt;

import java.awt.image.BufferedImage;

public class Char {

	public int left;
	
	public int right;

	public int realLeft;
	
	public int realRight;
	
	public boolean whitespace;
	
	public BufferedImage bi;
	
	public Char(int left, int right) {
		super();
		this.realLeft = left;
		this.realRight = right;
	}
	
}

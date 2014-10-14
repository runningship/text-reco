package org.bc.itt;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ProperArea {

	public int top=Integer.MAX_VALUE;
	public int left=Integer.MAX_VALUE;
	public int bottom;
	public int right;
	
	public List<Point> points = new ArrayList<Point>();
	
	public BufferedImage bi;
	
	public int[][] getData(){
		if(this.bottom-this.top+1<0){
			return null;
		}
		int[][] data = new int[this.bottom-this.top+1][this.right-this.left+1];
		for(Point p : points){
			data[p.row-this.top][p.column-this.left]=1;
		}
		return data;
	}
	
	public int getWidth(){
		return right-left+1;
	}
	
	public int getHeight(){
		return bottom-top+1;
	}
}

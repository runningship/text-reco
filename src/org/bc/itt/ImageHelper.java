package org.bc.itt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageHelper {

	public static Color getBackgroundColor(BufferedImage bi){
		Map<Color,Integer> map = new HashMap<Color,Integer>();
		int width = bi.getWidth();
		int height = bi.getHeight();
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		for (int row = miny; row < height; row++) {
			for (int column = minx; column < width; column++) {
				int pixel = bi.getRGB(column, row);
				Color color = new Color();
				color.red=		(pixel & 0xff0000) >> 16;
				color.green = 	(pixel & 0xff00) >> 8;
				color.blue = 	(pixel & 0xff);
				if(!map.containsKey(color)){
					map.put(color, 1);
				}else{
					map.put(color, map.get(color)+1);
				}
			}
		}
		int max=0;
		Color color = null;
		for(Color key: map.keySet()){
			if(map.get(key)>max){
				color=key;
				max=map.get(key);
			}
		}
		return color;
	}
	
	public static int getColorOffset(Color c1, Color c2){
		int redOffset = Math.abs(c1.red-c2.red);
		int greenOffset = Math.abs(c1.green-c2.green);
		int blueOffset = Math.abs(c1.blue-c2.blue);
		return redOffset+greenOffset+blueOffset;
	}
	
	public static ProperArea loadData(BufferedImage bi,Color background,int colorIngoreOffset){
		int width = bi.getWidth();
		int height = bi.getHeight();
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		List<Point> points = new ArrayList<Point>();
		ProperArea area = new ProperArea();
		for (int row = miny; row < height; row++) {
			for (int column = minx; column < width; column++) {
				int pixel = bi.getRGB(column, row);
				Color color = new Color();
				color.red = (pixel & 0xff0000) >> 16;
				color.green = (pixel & 0xff00) >> 8;
				color.blue = (pixel & 0xff);
//				System.out.println("data["+row+"]["+column+"]="+" new RGB("+color.red+","+color.green+","+color.blue+");");
				if(getColorOffset(color,background)>colorIngoreOffset){
					Point p = new Point();
					p.row = row;
					p.column = column;
					points.add(p);
					if(row>area.bottom){
						area.bottom=row;
					}
					if(row<area.top){
						area.top = row;
					}
					if(column<area.left){
						area.left=column;
					}
					if(column>area.right){
						area.right = column;
					}
				}
			}
		}
		area.points.addAll(points);
		return area;
	}
	
	public static void print(int[][] data){
		if(data==null){
			return;
		}
		for(int i=0;i<data.length;i++){
			for(int j=0;j<data[i].length;j++){
				if(data[i][j]==1){
					System.out.print("*");
				}else{
					System.out.print(" ");
				}
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static int[][] getCharacterData(BufferedImage bi , Color background , int colorIngoreOffset) throws IOException{
		int width = bi.getWidth();
		int height = bi.getHeight();
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		int[][] data = new int[height][width];
		for (int row = miny; row < height; row++) {
			for (int column = minx; column < width; column++) {
				int pixel = bi.getRGB(column, row);
				Color color = new Color();
				color.red = (pixel & 0xff0000) >> 16;
				color.green = (pixel & 0xff00) >> 8;
				color.blue = (pixel & 0xff);
				if(ImageHelper.getColorOffset(color,background)>colorIngoreOffset){
					data[row][column]=1;
				}else{
					data[row][column]=0;
				}
			}
		}
		return data;
	}
}

package org.bc.itt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class LineCutter {

	public final static int defaultLineHeight = 4;
	
	public static List<BufferedImage> cutImageIntoLines(File imageFile) throws IOException{
		BufferedImage bi = ImageIO.read(imageFile);
		Color backgroundColor = ImageHelper.getBackgroundColor(bi);
		int[][] data = ImageHelper.getCharacterData(bi,backgroundColor,RecognizeConstant.FirstLevelCutCharIngoreColorOffset);
		List<Line> lines = getLines(data);
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		for(Line line : lines){
			BufferedImage subImg = bi.getSubimage(0, line.top, bi.getWidth(), line.bottom-line.top+1);
			images.add(subImg);
		}
		return images;
	}
	
	private static List<Line> getLines(int[][] data){
		List<Integer > rows = new ArrayList<Integer>();
		for (int row = 0; row < data.length; row++) {
			int rowVal=0;
			for (int column = 0; column < data[row].length; column++) {
				rowVal += data[row][column];
			}
			if(rowVal!=0){
				//内容行
				rows.add(row);
			}
		}
		if(rows.size()<2){
			return new ArrayList<Line>();
		}
		int top=rows.get(0);
		List<Line> lines = new ArrayList<Line>();
		for(int i=1;i<rows.size();i++){
			if(rows.get(i)-rows.get(i-1)>=defaultLineHeight){
				Line line = new Line(top,rows.get(i-1));
				lines.add(line);
				top = rows.get(i);
			}
		}
		//最后一行
		lines.add(new Line(top,rows.get(rows.size()-1)));
		return lines;
	}
}

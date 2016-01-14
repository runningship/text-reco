package org.bc.itt.sys;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;


public class CharCutter {

	public static List<BufferedImage> processToLines(BufferedImage bitData){
		boolean rowStart=false;
		int rowUp=0;
		List<BufferedImage> lines = new ArrayList<BufferedImage>();
		for(int y=0;y<bitData.getHeight();y++){
			boolean yIsEmpty=true;
			for(int x=0;x<bitData.getWidth();x++){
				if(bitData.getRGB(x, y)==-16777216){
					//黑色
					yIsEmpty = false;
					break;
				}
			}
			if(yIsEmpty==false && rowStart==false){
				rowStart = true;
				rowUp = y;
			}else if(yIsEmpty==true && rowStart==true){
				//row finish
				rowStart = false;
				BufferedImage line = bitData.getSubimage(0, rowUp, bitData.getWidth(), y-rowUp);
				lines.add(line);
			}
		}
		
		return lines;
	}
	
	public static void main(String[] args) throws IOException{
		String dir = "C:\\Users\\Administrator\\Desktop\\text reco\\";
		String fileName = "p1.jpg.r.jpg";
		BufferedImage img = javax.imageio.ImageIO.read(new File(dir+fileName));
		List<BufferedImage> lines = processToLines(img);
		for(int i=0;i<lines.size();i++){
			ImageIO.write(lines.get(i), "png", new File(dir+fileName+"."+i+".jpg"));
		}
	}
}

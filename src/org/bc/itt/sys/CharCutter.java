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
	
	public static List<BufferedImage> processToChar(BufferedImage lineData){
		int left=0;
		boolean columnStart=false;
		List<BufferedImage> chars = new ArrayList<BufferedImage>();
		for(int i=0;i<lineData.getWidth();i++){
			boolean xIsEmpty = true;
			for(int j=0;j<lineData.getHeight();j++){
				int color = lineData.getRGB(i, j);
				if(color==-16777216){
					//black
					xIsEmpty = false;
					break;
				}
			}
			if(xIsEmpty==false && columnStart==false){
				columnStart = true;
				left = i;
			}else if(xIsEmpty==true && columnStart==true){
				//column finish
				columnStart = false;
				BufferedImage charc = lineData.getSubimage(left, 0, i-left, lineData.getHeight());
				chars.add(charc);
			}
		}
		return chars;
	}
	
	
	public static void main(String[] args) throws IOException{
		//testLines();
		testChars();
	}
	
	private static void testChars() throws IOException{
		String dir = "E:\\java\\xinzhouy\\TextRecognizer\\text reco\\";
		String fileName = "p1.jpg.r.jpg.9.jpg";
		BufferedImage img = javax.imageio.ImageIO.read(new File(dir+fileName));
		List<BufferedImage> lines = processToChar(img);
		for(int i=0;i<lines.size();i++){
			ImageIO.write(lines.get(i), "png", new File(dir+fileName+"."+i+".jpg"));
		}
	}
	
	private static void testLines() throws IOException{
		String dir = "C:\\Users\\Administrator\\Desktop\\text reco\\";
		String fileName = "p1.jpg.r.jpg";
		BufferedImage img = javax.imageio.ImageIO.read(new File(dir+fileName));
		List<BufferedImage> lines = processToLines(img);
		for(int i=0;i<lines.size();i++){
			ImageIO.write(lines.get(i), "png", new File(dir+fileName+"."+i+".jpg"));
		}
	}
}

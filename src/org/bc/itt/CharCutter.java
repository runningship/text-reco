package org.bc.itt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharCutter {

	private final static int defaultCharSpace = 2;
	
	public static List<Char> cutLineIntoChars(BufferedImage line , Color backgroundColor) throws IOException{
		return cutLineIntoChars(line,backgroundColor,RecognizeConstant.FirstLevelCutCharIngoreColorOffset);
	}
	public static List<Char> cutLineIntoChars(BufferedImage line , Color backgroundColor,int colorIgnoreOffSet) throws IOException{
		int[][] data = ImageHelper.getCharacterData(line,backgroundColor,colorIgnoreOffSet);
		List<Char> chars = getChars(data);
		for(Char ch : chars){
			if(ch.whitespace){
				continue;
			}
			try{
				BufferedImage subImg = line.getSubimage(ch.left, 0, ch.right-ch.left+1, line.getHeight());
				ch.bi = subImg;
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return chars;
	}
	
	public static List<Integer> getNotEmptyColumns(int[][] lineData){
		List<Integer> columns = new ArrayList<Integer>();
		for(int column=0;column<lineData[0].length;column++){
			int colVal=0;
			for(int row=0;row<lineData.length;row++){
				colVal+=lineData[row][column];
			}
			if(colVal>0){
				columns.add(column);
			}else{
			}
		}
		return columns;
	}
	
	private static List<Char> getChars(int[][] lineData){
		List<Integer> columns = getNotEmptyColumns(lineData);
		List<Char> chars = new ArrayList<Char>();
		int realLeft = columns.get(0);
		for(int i=1;i<columns.size();i++){
			if(columns.get(i)-columns.get(i-1)>=2){
				int realRight = columns.get(i-1);
				Char last = null;
				if(chars.size()>0){
					last = chars.get(chars.size()-1);
				}
				if(realRight-realLeft<=2 && last!=null && last.realRight-last.realLeft<=3 && realLeft-last.realRight<=2){
					// try merger with last ch;
					last.realRight = realRight;
					realLeft = columns.get(i);
				}else{
					Char ch = new Char(realLeft,realRight);
					chars.add(ch);
					realLeft = columns.get(i);
					if(columns.get(i)-columns.get(i-1)>=6){
						//空格
						Char space = new Char(columns.get(i-1),columns.get(i));
						space.whitespace=true;
						chars.add(space);
					}
				}
			}
		}
		
		//最后一个字符
		chars.add(new Char(realLeft,columns.get(columns.size()-1)));
		chars.get(0).left=0;
		for(int i=1;i<chars.size();i++){
			Char ch = chars.get(i);
			Char last = chars.get(i-1);
			if(ch.whitespace){
				last.right += last.realRight + (ch.realRight-ch.realLeft)/2;
				ch.left= last.right+1;
			}else if (last.whitespace){
				ch.left = ch.realLeft - (last.realRight-last.realLeft)/2;
			}else{
				last.right = last.realRight + (ch.realLeft-last.realRight)/2;
				ch.left = last.right+1;
			}
		}
		chars.get(chars.size()-1).right = chars.get(chars.size()-1).realRight;
		return chars;
	}
	
}

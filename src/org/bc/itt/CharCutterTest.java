package org.bc.itt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;

public class CharCutterTest {

	public static void main(String[] args) throws IOException{
		File lineFile = new File("E:\\java\\xinzhouy\\TextRecognizer\\p1.jpg-data\\line-1.jpg");
		BufferedImage bi = ImageIO.read(lineFile);
		List<Char> chars = LineRecognizer.recognize(bi);
		File dir = new File(lineFile.getParent()+"\\"+lineFile.getName().replace(".jpg", "")+"-chars");
		if(!dir.exists()){
			dir.mkdir();
		}
		for(int i=0;i<chars.size();i++){
			Char ch = chars.get(i);
			if(ch.whitespace){
				continue;
			}
			ImageIO.write(ch.bi,"jpg",new File(dir.getAbsolutePath()+"\\"+ch.left+"-"+ch.right+".jpg"));
		}
	}
	
	@Test
	public void testGetEmptyColumns() throws IOException{
		File lineFile = new File("E:\\java\\xinzhouy\\TextRecognizer\\lines.jpg-data\\line-1.jpg");
		BufferedImage line = ImageIO.read(lineFile);
		Color backgroundColor = ImageHelper.getBackgroundColor(line);
		int[][] data = ImageHelper.getCharacterData(line,backgroundColor,RecognizeConstant.FirstLevelCutCharIngoreColorOffset);
		List<Integer> columns = CharCutter.getNotEmptyColumns(data);
		List<Char> chars  =new ArrayList<Char>();
		System.out.println(columns);
		int realLeft = columns.get(0);
		for(int i=1;i<columns.size();i++){
			if(columns.get(i)-columns.get(i-1)>=2){
				int realRight = columns.get(i-1);
				Char last = null;
				if(chars.size()>0){
					last = chars.get(chars.size()-1);
				}
				if(realRight-realLeft<=2 && last!=null && last.realRight-last.realLeft<=1){
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
			Char last = chars.get(i-1);
			Char ch = chars.get(i);
			last.right = last.realRight + (ch.realLeft-last.realRight)/2;
			ch.left = last.right;
		}
		chars.get(chars.size()-1).right = chars.get(chars.size()-1).realRight+1;
		for(Char ch : chars){
			if(ch.whitespace){
				System.out.println();
				continue;
			}
			System.out.println("left="+ch.left+";right="+ch.right);
		}
	}
}

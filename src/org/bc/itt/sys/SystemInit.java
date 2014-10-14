package org.bc.itt.sys;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bc.itt.Char;
import org.bc.itt.CharCutter;
import org.bc.itt.LineCutter;
import org.bc.itt.RecognizeConstant;
import org.junit.Test;

public class SystemInit {

	public static final String stdDir = "E:\\java\\xinzhouy\\TextRecognizer\\std\\images\\";
	
	@Test
	public  void init() throws IOException{
		List<Font> fonts = getFonts();
		for(Font font : fonts){
			List<BufferedImage> lines = LineCutter.cutImageIntoLines(font.fontImage);
			
			List<Char> all = new ArrayList<Char>();
			for(int i=0;i<lines.size();i++){
				List<Char> chars = CharCutter.cutLineIntoChars(lines.get(i), RecognizeConstant.white, 200);
				all.addAll(chars);
			}
			saveChars(font,removeSpace(all));
		}
	}
	
	private List<Char> removeSpace(List<Char> chars){
		List<Char> result = new ArrayList<Char>();
		for(Char ch : chars){
			if(!ch.whitespace){
				result.add(ch);
			}
		}
		return result;
	}
	private void saveChars(Font font,List<Char> chars) throws IOException{
		String fontLineDirPath = font.fontImage.getParentFile().getParentFile().getParent()+"\\chars\\"+font.name+"\\"+font.style;
		File fontLineDir = new File(fontLineDirPath);
		if(!fontLineDir.exists()){
			fontLineDir.mkdirs();
		}
		int offset = 33;
		for(int i=0;i<chars.size();i++){
			Char ch = chars.get(i);
			if(ch.whitespace){
				continue;
			}
			ImageIO.write(ch.bi,"jpg",new File(fontLineDirPath+"\\"+(offset+i)+".jpg"));
		}
	}
	private static List<Font> getFonts(){
		File std = new File(stdDir);
		List<Font> fonts = new ArrayList<Font>();
		for(File fontDir : std.listFiles()){
			if(fontDir.isFile()){
				continue;
			}
			
			for(File fontImg : fontDir.listFiles()){
				Font font = new Font();
				if(fontImg.isDirectory()){
					continue;
				}
				font.fontImage = fontImg;
				font.name = fontDir.getName();
				font.style = fontImg.getName();
				font.style = font.style.substring(0, font.style.indexOf('.'));
				fonts.add(font);
			}
		}
		return fonts;
	}
}

class Font{
	String name;
	String style;
	File fontImage;
}

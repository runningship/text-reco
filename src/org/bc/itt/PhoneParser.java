package org.bc.itt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PhoneParser {

	public static void main(String[] args) throws IOException{
		File lineFile = new File("E:\\java\\xinzhouy\\TextRecognizer\\p1.jpg-data\\line-1.jpg");
		BufferedImage line = ImageIO.read(lineFile);
		Color backgroundColor = ImageHelper.getBackgroundColor(line);
		int[][] data = ImageHelper.getCharacterData(line,backgroundColor,RecognizeConstant.FirstLevelCutCharIngoreColorOffset);
		int width=0;
		int left=0;
		for(int i=0;i<data[0].length;i++){
			if(width<=5){
				width++;
				continue;
			}
			BufferedImage ch = line.getSubimage(left, 0, width+1, data.length);
			ImageHelper.print(ImageHelper.getCharacterData(ch, RecognizeConstant.white, RecognizeConstant.FirstLevelCutCharIngoreColorOffset));
			Result result = Matcher.getInstance().matchAtPaticularStd(ch);
			System.out.println(result.ch+" at "+result.rate);
		}
	}
}

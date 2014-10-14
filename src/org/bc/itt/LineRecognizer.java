package org.bc.itt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class LineRecognizer {

	public static List<Char> recognize(BufferedImage line) throws IOException{
		Color backgroundColor = ImageHelper.getBackgroundColor(line);
		List<Char> chars = CharCutter.cutLineIntoChars(line,backgroundColor);
		List<Char> all = new ArrayList<Char>();
		for(int i=0;i<chars.size();i++){
			Char ch = chars.get(i);
			if(ch.whitespace){
				all.add(ch);
				continue;
			}
			List<Char> secondLevelChars = CharCutter.cutLineIntoChars(ch.bi,backgroundColor ,RecognizeConstant.SecondLevelCutCharIngoreColorOffset);
			if(secondLevelChars.size()<=1){
				all.add(ch);
			}else{
				for(Char innerCh : secondLevelChars){
					innerCh.left+=ch.left;
					innerCh.right += ch.left;
					all.add(innerCh);
				}
			}
		}
		return all;
	}
}

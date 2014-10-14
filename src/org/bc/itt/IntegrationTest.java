package org.bc.itt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

public class IntegrationTest {

	public static void main(String[] args) throws IOException{
//		File lineFile = new File("E:\\java\\xinzhouy\\TextRecognizer\\lines.jpg-data\\line-16.jpg");
		File lineFile = new File("E:\\java\\xinzhouy\\TextRecognizer\\lines.jpg-data\\phone.jpg");
		File dir = new File(lineFile.getParent()+"\\"+lineFile.getName().replace(".jpg", "")+"-chars");
		List<Char> chars = LineRecognizer.recognize(ImageIO.read(lineFile));
		if(!dir.exists()){
			dir.mkdir();
		}
		for(int i=0;i<chars.size();i++){
			Char ch = chars.get(i);
			if(ch.whitespace){
				System.out.print(" ");
				continue;
			}
			File chFile = new File(dir.getAbsolutePath()+"\\"+ch.left+"-"+ch.right+".jpg");
			ImageIO.write(ch.bi,"jpg",chFile);
//			ProperArea area = Matcher.getInstance().getPropArea(ImageIO.read(chFile),RecognizeConstant.DefaultLoadCharIngoreColorOffset);
//			ProperArea area = Matcher.getInstance().getPropArea(ch.bi);
			Result result = Matcher.getInstance().matchAtPaticularStd(ImageIO.read(chFile));
			System.out.print(result.ch);
		}
	}
	
	
}

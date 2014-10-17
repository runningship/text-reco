package org.bc.itt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

public class LineCutterTest {

	public static void main(String[] args) throws IOException{
		String baseDir = "E:\\java\\xinzhouy\\TextRecognizer\\";
		File imgFile = new File(baseDir+"showphone.gif");
		List<BufferedImage> lines = LineCutter.cutImageIntoLines(imgFile);
		for(int i=0;i<lines.size();i++){
			File dir = new File(baseDir+imgFile.getName()+"-data");
			if(!dir.exists()){
				dir.mkdir();
			}
			ImageIO.write(lines.get(i),"jpg",new File(dir.getAbsolutePath()+"\\line-"+(i+1)+".jpg"));
		}
	}
}

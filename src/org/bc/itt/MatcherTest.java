package org.bc.itt;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

public class MatcherTest {

	@Test
	public void matchChar() throws IOException{
		File ch = new File("E:\\java\\xinzhouy\\TextRecognizer\\showphone.gif-data\\0.jpg");
//		ImageHelper.print(propArea.getData());

		long start = System.currentTimeMillis();
		Result result = Matcher.getInstance().matchAtPaticularStd(ImageIO.read(ch));
		System.out.println(result.ch+" at "+result.rate);
		System.out.println("cost "+(System.currentTimeMillis()-start));
//		System.out.println("std is ");
//		ImageHelper.print(Matcher.getInstance().getStdData(result.ch).characterDatas);
		
//		System.out.println("expect is ");
//		ImageHelper.print(Matcher.loadStd("g", 180));
	}
	
	@Test
	public void matchLine() throws IOException{
		File  dir = new File("E:\\java\\xinzhouy\\TextRecognizer\\lines.jpg-data\\line-1-chars");
		for(File ch : dir.listFiles()){
			ProperArea propArea = Matcher.getInstance().getPropArea(ImageIO.read(ch),RecognizeConstant.DefaultLoadCharIngoreColorOffset);
			Result result =Matcher.getInstance().matchAtPaticularStd(ImageIO.read(ch));
			System.out.println(ch.getName()+"->"+result.ch);
		}
	}
	
	@Test
	public void testLoadStdData() throws IOException{
		ImageHelper.print(Matcher.loadStd("upper-g", 210));
		ImageHelper.print(Matcher.loadStd("upper-g", 310));
	}
	
	@Test
	public void testLoadTargetData() throws IOException{
		File ch = new File("E:\\java\\xinzhouy\\TextRecognizer\\lines.jpg-data\\line-8-chars\\36-42.jpg");
		ImageHelper.print(Matcher.getInstance().getPropArea(ImageIO.read(ch), 150).getData());
	}
}

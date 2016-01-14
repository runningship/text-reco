package org.bc.itt.sys;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class BinarizationUtil {

	public static void processByAvg(BufferedImage bi , String destFile) throws ImageFormatException, IOException{
		int h=bi.getHeight();//获取图像的高  
        int w=bi.getWidth();//获取图像的宽  
        int color = 0;
        for (int x = 0; x < w; x++) {  
            for (int y = 0; y < h; y++) {  
                color += bi.getRGB(x, y);  
            }  
        }
        int avg = color/(h*w);
        BufferedImage target = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {  
                if(bi.getRGB(x, y)>avg){
                	target.setRGB(x, y, 16777215);
                }else{
                	target.setRGB(x, y, 0);
                }
            }  
        }
        ImageIO.write(target, "png", new File(destFile));
	}
	
	/**
	 * 
	 * @param bi
	 * @param percent 越大有效点越多
	 * @return
	 * @throws ImageFormatException
	 * @throws IOException
	 */
	public static BufferedImage processByMaxOffset(BufferedImage bi , float percent) throws ImageFormatException, IOException{
		int h=bi.getHeight();//获取图像的高  
        int w=bi.getWidth();//获取图像的宽  
        int max = 0;
        for (int x = 0; x < w; x++) {  
            for (int y = 0; y < h; y++) {
            	int c = getGray(bi.getRGB(x, y));
                if(c>max){
                	max = c;
                }
            }  
        }
        BufferedImage target = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
            	int c = getGray(bi.getRGB(x, y));
            	//越小越黑，越大越白
                if(c>max*percent){
                	target.setRGB(x, y, 16777215);
                }else{
                	target.setRGB(x, y, 0);
                }
            }  
        }
        return target;
        
	}
	
	public static void main(String[] args) throws IOException{
		String dir = "C:\\Users\\Administrator\\Desktop\\text reco\\";
		String fileName = "p1.jpg";
		BufferedImage img = javax.imageio.ImageIO.read(new File(dir+fileName));
		//processByAvg(img , dir+"sign1.png");
		BufferedImage target = processByMaxOffset(img , 0.70f);
		ImageIO.write(target, "png", new File(dir+fileName+".r.jpg"));
	}
	
	private static int getGray(int rgb){  
        String str=Integer.toHexString(rgb);
        int a=Integer.parseInt(str.substring(0,2),16);
        
        int r=Integer.parseInt(str.substring(2,4),16);
        int g=Integer.parseInt(str.substring(4,6),16);  
        int b=Integer.parseInt(str.substring(6,8),16);  
        //or 直接new个color对象  
        Color c=new Color(rgb);  
        r=c.getRed();  
        g=c.getGreen();  
        b=c.getBlue();  
        int top=(r+g+b)/3;
        //System.out.println("color="+str+";a="+a+";avg="+top);
        return (int)(top);  
    }
}

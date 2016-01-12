package org.bc.itt.sys;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class BinarizationUtil {

	public static void process(BufferedImage bi){
		int h=bi.getHeight();//获取图像的高  
        int w=bi.getWidth();//获取图像的宽  
        int[][] gray=new int[w][h];  
        for (int x = 0; x < w; x++) {  
            for (int y = 0; y < h; y++) {  
                gray[x][y]=getGray(bi.getRGB(x, y));  
            }  
        }  
	}
	
	private static int getGray(int rgb){  
        String str=Integer.toHexString(rgb);  
        int r=Integer.parseInt(str.substring(2,4),16);  
        int g=Integer.parseInt(str.substring(4,6),16);  
        int b=Integer.parseInt(str.substring(6,8),16);  
        //or 直接new个color对象  
        Color c=new Color(rgb);  
        r=c.getRed();  
        g=c.getGreen();  
        b=c.getBlue();  
        int top=(r+g+b)/3;  
        return (int)(top);  
    }
}

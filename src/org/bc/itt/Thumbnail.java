package org.bc.itt;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

public class Thumbnail {
      
    /** 
     * ��ԭͼƬ��BufferedImage������������ͼ 
     * source��ԭͼƬ��BufferedImage���� 
     * targetW:����ͼ�Ŀ� 
     * targetH:����ͼ�ĸ� 
     */  
    public static BufferedImage resize(BufferedImage source,int targetW,int targetH,boolean equalProportion){  
        int type=source.getType();  
        BufferedImage target=null;  
        double sx=(double)targetW/source.getWidth();  
        double sy=(double)targetH/source.getHeight();  
        //������ʵ����targetW��targetH��Χ��ʵ�ֵȱ���������  
          //�������Ҫ�ȱ����������������if else���ע�͵�����  
        if(equalProportion){  
            if(sx>sy){  
                sx=sy;  
                targetW=(int)(sx*source.getWidth());  
            }else{  
                sy=sx;  
                targetH=(int)(sx*source.getHeight());  
            }  
        }  
        if(type==BufferedImage.TYPE_CUSTOM){  
            ColorModel cm=source.getColorModel();  
            WritableRaster raster=cm.createCompatibleWritableRaster(targetW,targetH);  
            boolean alphaPremultiplied=cm.isAlphaPremultiplied();  
            target=new BufferedImage(cm,raster,alphaPremultiplied,null);  
        }else{  
            target=new BufferedImage(targetW,targetH,type);  
            Graphics2D g=target.createGraphics();  
            g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);  
            g.drawRenderedImage(source,AffineTransform.getScaleInstance(sx,sy));  
            g.dispose();  
        }  
        return target;  
    }  
    
    @Test
    public void zoonIn() throws IOException{
    	File ch = new File("E:\\java\\xinzhouy\\TextRecognizer\\p1.jpg-data\\line-1-chars\\209-227.jpg");
    	BufferedImage bi = ImageIO.read(ch);
    	ProperArea prop = Matcher.getInstance().getPropArea(bi, 210);
    	bi  = bi.getSubimage(prop.left, prop.top, prop.getWidth(), prop.getHeight());
    	BufferedImage result = Thumbnail.resize(bi, 7, 8, false);
    	prop = Matcher.getInstance().getPropArea(result, 210);
    	ImageHelper.print(prop.getData());
    	ImageHelper.print(Matcher.loadStd("upper-c", 210));
    	ImageHelper.print(Matcher.loadStd("upper-g", 210));
    	ImageIO.write(result, "jpg", new File("E:\\java\\xinzhouy\\TextRecognizer\\p1.jpg-data\\line-1-chars\\result.jpg"));
    }
}

package org.bc.itt.sys;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.imageio.ImageIO;

public class FeatrueCaculator {

	/**
	 * @param bi 图片左右已经到边，上下空白待截取
	 */
	public static int[][] getDataArea(BufferedImage bi){
		int top=0;
		int bottom = bi.getHeight();
		int left=bi.getWidth();
		int right=0;
		for(int y=0;y<bi.getHeight();y++){
			for(int x = 0;x<bi.getWidth();x++){
				int color = bi.getRGB(x, y);
				if(color==-16777216){
					//black;
					if(top==0){
						top = y;
					}
					if(x<left){
						left=x;
					}
					right=x;
					bottom=y;
				}
			}
		}
		BufferedImage subImg = bi.getSubimage(left, top, right-left+1, bottom-top+1);
		int[][] data = new int[subImg.getHeight()][subImg.getWidth()];
		for(int y=0;y<subImg.getHeight();y++){
			for(int x = 0;x<subImg.getWidth();x++){
				int color = subImg.getRGB(x, y);
				if(color==-16777216){
					//black;
					data[y][x] = 1;
				}else{
					data[y][x] = -1;
				}
			}
		}
		return data;
	}
	
	public static Eigenvalue getEigenvalue(int[][] data){
		int yCenter1 =0;
		int yCenter2=0;
		int xCenter1=0;
		int xCenter2=0;
		int rowCount = data.length;
		if(rowCount%2==0){
			yCenter1=rowCount/2-1;
			yCenter2 = rowCount/2;
		}else{
			yCenter1=rowCount/2;
			yCenter2 = rowCount/2;
		}
		int column = data[0].length;
		if(column%2==0){
			xCenter1=column/2-1;
			xCenter2 = column/2;
		}else{
			xCenter1=column/2;
			xCenter2 = column/2;
		}
		int one=0;
		int two=0;
		int three=0;
		int four=0;
		for(int row=0;row<data.length;row++){
			for(int col=0;col<data[row].length;col++){
				if(data[row][col]==-1){
					continue;
				}
				if(row<=yCenter1 && col<=xCenter1){
					one++;
				}
				if(row<=yCenter1 && col>=xCenter2){
					two++;
				}
				if(row>=yCenter2 && col<=xCenter1){
					three++;
				}
				if(row>=yCenter2 && col>=xCenter2){
					four++;
				}
			}
		}
		Eigenvalue eigen = new Eigenvalue();
		int total = rowCount * column;
		eigen.first = one*1.0f/total;
		eigen.second = two*1.0f/total;
		eigen.third = three*1.0f/total;
		eigen.fourth = four*1.0f/total;
		return eigen;
	}
	public static void main(String[] args) throws IOException{
		String dir = "D:\\code\\text-reco\\text reco\\";
		String fileName = "p1.jpg.r.jpg.9.jpg.40.jpg";
		BufferedImage img = javax.imageio.ImageIO.read(new File(dir+fileName));
		int[][] data = getDataArea(img);
		for(int row=0;row<data.length;row++){
			for(int col=0;col<data[row].length;col++){
				if(data[row][col]==1){
					System.out.print("o");
				}else{
					System.out.print(" ");
				}
			}
			System.out.println();
		}
		Eigenvalue eigen = getEigenvalue(data);
		System.out.println(eigen.toStringValue());
	}
	
	/**
	 * 计算横向基准率
	 */
	public static void getXReferenceRate(int[][] data){
		List<List<Float>> rowRates = new ArrayList<List<Float>>();
		for(int y=0;y<data.length;y++){
			List<Float> rate = getReferenceRata(data[y]);
			rowRates.add(rate);
		}
	}
	
	public static void match(List<List<Float>> rowRates1 , List<List<Float>> rowRates2){
	}
	
	private static List<Float> getReferenceRata(int[] row) {
		List<Integer> list = new ArrayList<Integer>();
		for(int i=0;i<row.length;i++){
			if(list.isEmpty()){
				list.add(row[i]);
			}
			Integer last = list.get(list.size()-1);
			if(last*row[i]>0){
				//相同
				if(last>0){
					last++;
				}else{
					last--;
				}
			}else{
				//不同
				list.add(row[i]);
			}
		}
		List<Float> rates = new ArrayList<Float>();
		for(Integer count: list){
			rates.add(count*1.0f/row.length);
		}
		return rates;
	}

	/**
	 * 计算众向基准率
	 */
	public static void getYReferenceRate(){
		
	}
}

package org.bc.itt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class Matcher {

	private static List<StdData> stdList = new ArrayList<StdData>();
	private static Map<String,String> charNameMap = new HashMap<String,String>();
	private static Matcher instance = new Matcher();
	
	private int stdColorOffset = 210;
	private Matcher(){
		charNameMap.put("backslash", "\\");
		charNameMap.put("double-quote", "\"");
		charNameMap.put("slash", "/");
		charNameMap.put("lt", "<");
		charNameMap.put("gt", ">");
		charNameMap.put("single-quote","'");
		charNameMap.put("star","*");
		try {
			loadStdData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ProperArea getPropArea(BufferedImage bi,int targetColorOffset) throws IOException{
//		BufferedImage bi = ImageIO.read(file);
		ProperArea area = ImageHelper.loadData(bi,RecognizeConstant.white,targetColorOffset);
		BufferedImage propImg = bi.getSubimage(area.left,area.top, area.getWidth(),area.getHeight());
		area.bi = propImg;
//		ImageIO.write(propImg, "jpg", file);
		return area;
	}
	
//	public Result match(BufferedImage bi) throws IOException{
//		List<Result> list = new ArrayList<Result>();
//		for(int i=150;i<=310;i++){
//			reloadStdData(i);
//			list.add(matchAtPaticularStd(bi));
//		}
//		Result max = new Result("",0);
//		for(Result data : list){
//			if(data.rate>max.rate){
//				max = data;
//			}
//		}
//		return max;
//	}
	public Result matchAtPaticularStd(BufferedImage bi) throws IOException{
		List<Result> list = new ArrayList<Result>();
		for(int background=120;background<400;background+=40){
			long start = System.currentTimeMillis();
			ProperArea area = Matcher.getInstance().getPropArea(bi,background);
//			System.out.println("inner cost : "+(System.currentTimeMillis() - start));
			Result result = innerMatch(area,background);
			
//			System.out.println(result.ch + " at rate "+result.rate +" for background "+background);
			list.add(result);
		}
		Result max = new Result("",0);
		for(Result data : list){
			if(data.rate>max.rate){
				max = data;
			}
		}
		return max;
	}
	private Result innerMatch(ProperArea propArea, int targetColorOffset){
		Map<StdData,Float> rates = new HashMap<StdData,Float>();
		for(StdData std : stdList){
			int width = std.characterDatas[0].length;
			int height = std.characterDatas.length;
			BufferedImage stdImg = null;
			if(width<=4 && height<=4){
				//��׼̫С��
				if(propArea.getHeight()>=4 || propArea.getWidth()>=4){
					//��СĿ��ͼ���Ӱ��Ŀ��ͼƬ����
					continue;
				}else{
					//Ŀ��ͼƬҲ��С��ֱ��ʹ��
					stdImg = propArea.bi;
				}
			}else {
				if(propArea.getWidth()<width || propArea.getHeight()<height){
					//��׼��Ŀ��С,��Ӧ�÷Ŵ�Ŀ��ͼƬ
//					stdImg=propArea.bi;
					rates.put(std, ScanMatcher.match(std, new StdData(propArea.getData())));
//					System.out.println("target is smaller than std");
					continue;
				}else{
					//��СĿ��ͼƬ�����׼ͼƬ
					float rateX = propArea.getWidth()/std.getWidth();
					float rateY = propArea.getHeight()/std.getHeight();
					if(Math.abs(rateX-rateY)>1){
						//�������
						continue;
					}
					stdImg = getStdImage(std,propArea);
				}
			}
			ProperArea area = ImageHelper.loadData(stdImg,RecognizeConstant.white,targetColorOffset);
//			if(targetColorOffset==180 && std.ch.equals("(")){
//				System.out.println("std target data for "+std.ch);
//				ImageHelper.print(std.characterDatas);
//				ImageHelper.print(area.getData());
//				ImageHelper.print(propArea.getData());
//			}
			if(area.getData()==null){
				continue;
			}
//			System.out.println(std.fontName+","+std.fontStyle+","+std.ch);
			rates.put(std,match(std,new StdData(area.getData())));
		}
		float max=0;
		StdData result=null;
		for(StdData ch : rates.keySet()){
			if(rates.get(ch)>max){
				max=rates.get(ch);
				result = ch;
			}
		}
//		if(result!=null){
//			if(result.startsWith("upper-")){
//				result = result.replace("upper-", "").toUpperCase();
//			}
//			if(result.startsWith("italic-")){
//				result = result.replace("italic-", "");
//			}
//			if(charNameMap.containsKey(result)){
//				result = charNameMap.get(result);
//			}
//		}
		return new Result(result.ch,max);
	}
	
	private static float match(StdData std,StdData target){
		float matchPoints=0;
		for(int row=0;row<target.characterDatas.length;row++){
			if(std.characterDatas.length<=row){
				break;
			}
			for(int col=0;col<target.characterDatas[row].length;col++){
				if(std.characterDatas[row].length<=col){
					continue;
				}
				if(target.characterDatas[row][col]==std.characterDatas[row][col]){
					matchPoints++;
				}
			}
		}
		float rate = matchPoints/target.getPixel();
		
		float matchLines = 0;
		float matchColumns = 0;
		for(int i=0;i<std.getFlagRows().size();i++){
			if(target.getFlagRows().size()>i){
				if(match(std.getFlagRows().get(i),target.getFlagRows().get(i)) && 
						match(target.getFlagRows().get(i),std.getFlagRows().get(i))){
					matchLines++;
				}
			}
		}
		for(int i=0;i<std.getFlagColumns().size();i++){
			if(target.getFlagColumns().size()>i){
				if(match(std.getFlagColumns().get(i),target.getFlagColumns().get(i)) && 
						match(target.getFlagColumns().get(i),std.getFlagColumns().get(i))){
					matchColumns++;
				}
			}
		}
		float lineMatchRate = matchLines/(float)target.getFlagRows().size();
		float columnMatchRate = matchColumns/(float)target.getFlagColumns().size();
//		if(std.ch.equals("+")){
//			System.out.println();
//			ImageHelper.print(target.characterDatas);
//		}
		return (rate+lineMatchRate+columnMatchRate)/3.0f;
//		return rate;
	}
	
	private static boolean match(FlagRow std, FlagRow target) {
		for(int i=0;i<target.getFlagValues().size();i++){
			if(std.getFlagValues().size()>i){
				if(std.getFlagValues().get(i).val!=target.getFlagValues().get(i).val){
					return false;
				}
			}else{
				return false;
			}
		}
		return true;
	}
	
	private static boolean match(FlagColumn std, FlagColumn target) {
		for(int i=0;i<target.getFlagValues().size();i++){
			if(std.getFlagValues().size()>i){
				if(std.getFlagValues().get(i).val!=target.getFlagValues().get(i).val){
					return false;
				}
			}else{
				return false;
			}
		}
		return true;
	}
	
	private BufferedImage getStdImage(StdData std, ProperArea propArea) {
		int width = std.characterDatas[0].length;
		int height = std.characterDatas.length;
		float rateW = propArea.getWidth()/width;
		float rateH = propArea.getHeight()/height;
		float rate = rateW>rateH ? rateW:rateH;
		return Thumbnail.resize(propArea.bi, width, height, false);
	}

	private void loadStdData() throws IOException {
		String dirStr = "E:\\java\\xinzhouy\\TextRecognizer\\std\\chars\\";
		File dir = new File(dirStr);
		for(File file : dir.listFiles()){
			if(file.isDirectory()){
				loadFont(file);
			}
		}
	}
	
	private void loadFont(File file) throws IOException {
		for(File fontStyleDir : file.listFiles()){
			if(fontStyleDir.isDirectory()){
				loadFontStyle(fontStyleDir);
			}
		}
	}

	private void loadFontStyle(File fontStyleDir) throws IOException {
		for(File ch : fontStyleDir.listFiles()){
			if(ch.isDirectory()){
				continue;
			}
			int[][] data = loadFile(ch);
			if(data!=null){
				StdData stdData = new StdData(data);
				int val = Integer.valueOf(ch.getName().replace(".jpg", ""));
				if(val>57 || val<48){
					//not a number,need number only
					continue;
				}
				stdData.ch=String.valueOf((char)val);
				stdData.fontStyle = ch.getParentFile().getName();
				stdData.fontName = ch.getParentFile().getParentFile().getName();
				stdList.add(stdData);
			}
		}
	}

	public static Matcher getInstance(){
		return instance;
	}
	
	private int[][] loadFile(File file) throws IOException{
		ProperArea area = ImageHelper.loadData(ImageIO.read(file),RecognizeConstant.white,stdColorOffset);
		return area.getData();
	}
	
	public static int[][] loadStd(String fileName,int background) throws IOException{
		File file = new File("E:\\java\\s1\\Image\\data\\std\\"+fileName+".jpg");
		ProperArea area = ImageHelper.loadData(ImageIO.read(file),RecognizeConstant.white,background);
		return area.getData();
	}
	
	public StdData getStdData(String ch){
		StdData result = null;
		for(StdData std : stdList){
			if(std.ch.equals(ch)){
				result = std;
			}
		}
		return result;
	}
	
	public void reloadStdData(int background) throws IOException{
		this.stdColorOffset = background;
		stdList.clear();
		this.loadStdData();
	}
}

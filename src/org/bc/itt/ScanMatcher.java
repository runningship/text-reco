package org.bc.itt;

public class ScanMatcher {

	//target ±È std Ð¡
	public static float match(StdData std,StdData target){
		float rate=0.0f;
//		ImageHelper.print(target.characterDatas);
		for(int xOffset=0;xOffset<=std.characterDatas[0].length-target.characterDatas[0].length;xOffset++){
			for(int yOffset=0;yOffset<=std.characterDatas.length-target.characterDatas.length;yOffset++){
				float ratex = match(std,target,xOffset,yOffset);
				if(ratex>rate){
					rate = ratex;
				}
			}
		}
		return rate;
	}
	
	private static float match(StdData std,StdData target,int xOffset, int yOffset){
		float matchPoints=0;
		for(int row=0;row<target.characterDatas.length;row++){
//			if(std.characterDatas.length<=row){
//				break;
//			}
			for(int col=0;col<target.characterDatas[row].length;col++){
//				if(std.characterDatas[row].length<=col){
//					continue;
//				}
				try{
					if(target.characterDatas[row][col]==std.characterDatas[row+yOffset][col+xOffset]){
						matchPoints++;
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
		float rate = matchPoints/std.getPixel();
		return rate;
	}
}

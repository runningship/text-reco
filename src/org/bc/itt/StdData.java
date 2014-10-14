package org.bc.itt;

import java.util.ArrayList;
import java.util.List;

public class StdData {

	public String ch;
	
	public int[][] characterDatas;
	
	public String fontName;
	
	public String fontStyle;
	
	private List<FlagRow> list = new ArrayList<FlagRow>();
	
	private List<FlagColumn> flagColumnList = new ArrayList<FlagColumn>();
	
	public List<FlagRow> getFlagRows(){
		return list;
	}
	
	public List<FlagColumn> getFlagColumns(){
		return flagColumnList;
	}

	public StdData(int[][] characterDatas) {
		super();
		this.characterDatas = characterDatas;
		for(int row=0;row<characterDatas.length;row++){
			FlagRow fr = new FlagRow();
			for(int col=0;col<characterDatas[row].length;col++){
				fr.add(characterDatas[row][col]);
			}
			fr.removeLastEmpty();
			if(!fr.getFlagValues().isEmpty()){
				list.add(fr);
			}
		}
		for(int col=0;col<characterDatas[0].length;col++){
			FlagColumn fc = new FlagColumn();
			for(int row=0;row<characterDatas.length;row++){
				fc.add(characterDatas[row][col]);
			}
			fc.removeLastEmpty();
			if(!fc.getFlagValues().isEmpty()){
				flagColumnList.add(fc);
			}
		}
	}
	
	public int getHeight(){
		return this.characterDatas.length;
	}
	
	public int getWidth(){
		return this.characterDatas[0].length;
	}
	public float getPixel(){
		return characterDatas.length*characterDatas[0].length;
	}
	
	public StdData(){
		
	}
	
	public void printFlagRows(){
		for(FlagRow fr : list){
			System.out.println(fr.getValueAsString());
		}
	}
}

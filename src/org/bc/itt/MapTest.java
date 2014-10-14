package org.bc.itt;

public class MapTest {

	public static void main(String[] args){
		String text = " hello everyone ,this is a test string for count char numbers.";
		int[] charCounts = new int[26];
		int basePoint = "A".codePointAt(0);
		for(int i=0;i<text.length();i++){
			int index = text.codePointAt(i)-basePoint;
			if(index>=0 && index<charCounts.length){
				charCounts[index]++;
			}
		}
		for(int i=0;i<charCounts.length;i++){
			System.out.println((char)(i+basePoint)+":"+charCounts[i]);
		}
	}
}

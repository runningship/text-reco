package org.bc.itt;

import java.util.ArrayList;
import java.util.List;

/**
 * 特征值列
 * @author runningship
 *
 */
public class FlagColumn {
	private List<FlagValue> list = new ArrayList<FlagValue>();
	
	public void add(int flag){
		FlagValue last = getLastFlagValue();
		if(last==null){
			last = new FlagValue();
			last.val = flag;
			last.count=1;
			list.add(last);
		}else{
			if(last.val==flag){
				last.count++;
//				if(last.count>=3){
//					//看成一列
//					last.val=2;
//				}
			}else{
				FlagValue fv = new FlagValue();
				fv.val = flag;
				fv.count = 1;
				list.add(fv);
			}
		}
	}
	
	private FlagValue getLastFlagValue(){
		if(list.isEmpty()){
			return null;
		}
		return list.get(list.size()-1);
	}
	
	public List<FlagValue> getFlagValues(){
		return list;
	}
	
	public void removeLastEmpty(){
		if(list.isEmpty()){
			return;
		}
		if(list.get(list.size()-1).val==0){
			list.remove(list.size()-1);
		}
	}
	
	public String getValueAsString(){
		String str = "";
		for(FlagValue fv : list){
			str+=fv.val;
		}
		return str;
	}
}

package org.bc.itt.sys;

public class Eigenvalue {

	//没一个概率值精确的位数
	private int weishu=8;
	//上下左右
	public float up;
	public float left;
	public float right;
	public float bottom;
	
	//1,2,3,4象限
	public float first;
	public float second;
	public float third;
	public float fourth;
	
	public String toStringValue(){
		int t = (int) Math.pow(10, weishu);
		String format = "%0"+weishu+"d";
		return String.format(format,Math.round(first*t)) + "-"+String.format(format,Math.round(second*t)) 
				+ "-"+String.format(format,Math.round(third*t))+ "-"+String.format(format,Math.round(fourth*t));
	}
	@Override
	public String toString() {
		return "Eigenvalue [up=" + up + ", left=" + left + ", right=" + right + ", bottom=" + bottom + ", first=" + first + ", second=" + second + ", third=" + third + ", fourth=" + fourth + "]";
	}
}

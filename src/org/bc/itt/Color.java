package org.bc.itt;

public class Color {

	public int red;
	
	public int green;
	
	public int blue;

	public Color(){
		
	}
	public Color(int red, int green, int blue) {
		super();
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Color){
			Color other = (Color)obj;
			return red==other.red && green==other.green && blue==other.blue;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (""+red+green+blue).hashCode();
	}
	
	
}

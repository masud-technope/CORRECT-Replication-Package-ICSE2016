package ca.usask.cs.srlab.correct.utility;

import java.text.DecimalFormat;

public class FormatterTester {
	
	public static void main(String[] args){
		System.out.println(String.format("%1.2g", 0.055));
		DecimalFormat df=new DecimalFormat("#.##");
		System.out.println(df.format(0.0050));
	}
}

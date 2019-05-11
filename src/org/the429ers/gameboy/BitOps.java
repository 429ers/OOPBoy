package org.the429ers.gameboy;

public class BitOps {
	static long extract(long in, int left, int right) {
		int leftShift = 63 - left;
		return (in << leftShift) >>> (leftShift + right); 
	} 
	
	
}


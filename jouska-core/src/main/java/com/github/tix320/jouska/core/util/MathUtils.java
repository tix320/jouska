package com.github.tix320.jouska.core.util;

public class MathUtils {

	public static boolean isPowerOfTwo(int n) {
		return (n & (n - 1)) == 0;
	}

	public static int nextPowerOf2(int n) {
		int count = 0;

		// First n in the below
		// condition is for the
		// case where n is 0
		if (n > 0 && (n & (n - 1)) == 0)
			return n;

		while (n != 0) {
			n >>= 1;
			count += 1;
		}

		return 1 << count;
	}

	public static double log2(double x) {
		return Math.log(x) / Math.log(2);
	}

}

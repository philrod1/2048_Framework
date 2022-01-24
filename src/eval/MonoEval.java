package eval;

import java.awt.Point;

import data.LongInfo;

public class MonoEval implements Evaluator {

	private static long M0 = 0x000F000F000F000Fl;
	private static long M1 = 0x00F000F000F000F0l;
	private static long M2 = 0x0F000F000F000F00l;
	private static long M3 = 0xF000F000F000F000l;

	@Override
	public double evaluate(long state) {
		
//		long r = reverse(state);
//		long t = transpose(state);
//		long rt = reverse(t);
		
		double m = mono(state);
//		double mr = mono(r);
//		double mt = mono(t);
//		double mrt = mono(rt);
		
		if (LongInfo.gameOver(state)) {
			return 0;
		}
		
//		return approxScore(state) * (
//			Math.min(
//				Math.min(m, mt), 
//				Math.min(mr, mrt)
//			)
//		);
		return approxScore(state) * m;
	}
	
	private double mono(long state) {
		state = snake(state);
		Point[] points = new Point[16];
		double d2 = 0;
		int v1 = 1 << ((int)state & 0xf);
		Point p1 = new Point(0, v1);
		points[0] = p1;
		for(int i = 1 ; i < 16 ; i++) {
			int v2 = 1 << ((int)(state >> (i * 4)) & 0xf);
//			if (v2 == 1) {
//				v2 = v1;
//			}
			Point p2 = new Point(i, v2);
			d2 += p1.distance(p2);
			p1 = p2;
			points[i] = p1;
		}
		double d1 = points[0].distance(points[15]);
		return d1 / d2;
	}

	private long snake(long state) {
		long result = state & 0xFFFF0000FFFF0000l;
		long mask1 = 0x0000000F0000000Fl; 
		long mask2 = 0x000000F0000000F0l; 
		long mask3 = 0x00000F0000000F00l; 
		long mask4 = 0x0000F0000000F000l;

		result |= (state & mask1) << 12;
		result |= (state & mask2) << 4;
		result |= (state & mask3) >> 4;
		result |= (state & mask4) >> 12;
		
		return result;
	}

	private final long transpose(long s) {
		long a1 = s  & 0xF0F00F0FF0F00F0FL;
		long a2 = s  & 0x0000F0F00000F0F0L;
		long a3 = s  & 0x0F0F00000F0F0000L;
		long a  = a1 | (a2 << 12) | (a3 >> 12);
		long b1 = a  & 0xFF00FF0000FF00FFL;
		long b2 = a  & 0x00FF00FF00000000L;
		long b3 = a  & 0x00000000FF00FF00L;
		return b1 | (b2 >> 24) | (b3 << 24);
	}
	
	private int approxScore(long state) {
		int score = 0;
		for(int i = 0 ; i < 16 ; i++) {
			int n = (int) ((state >> (i*4)) & 0xF);
			if(n > 0) {
				score += (n-1)*(1<<n);
			}
		}
		return score;
	}
	
	private static long reverse(long state) {
		long result = (state & M0) << 12;
		result |= (state & M1) << 4;
		result |= (state & M2) >>> 4;
		result |= (state & M3) >>> 12;
		return result;
	}


}

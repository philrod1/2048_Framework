package eval;

import java.awt.Point;


public class FastEvaluator implements Evaluator {

	public final float[] rowValues = new float[65536];
	
	private static final long ROW_MASK = 0xFFFFL;
	
	public FastEvaluator() {
		initTables();
	}
	
	@Override
	public double evaluate(long state) {
		return scoreHeuristicBoard(state);
	}
	
	private  final void initTables() {
		for (int row = 0; row < 65536; ++row) {
	        int[] line = new int[]{
	                ((row >>  0) & 0xf),
	                ((row >>  4) & 0xf),
	                ((row >>  8) & 0xf),
	                ((row >> 12) & 0xf)
	        };

	        float score = 0.0f;
	        for (int i = 0; i < 4; ++i) {
	            int rank = line[i];
	            if (rank > 1) {
	                score += (rank - 1) * (1 << rank);
	            }
	        }

	        int maxi = 0;
	        for (int i = 1; i < 4; ++i) {
	            if (line[i] > line[maxi]) maxi = i;
	        }

	        int edge = (maxi == 0 || maxi == 3) ? 1 : 0;
			double mono = Math.abs(mono(line));
			double smooth = Math.abs(smoothness(line));

	        rowValues[row] = (float) mono * score;
	    }
	}
	
	private static final double smoothness (int[] line) {
		double smoothness = 0;
		for (int x = 0; x < 3; x++) {
			if(line[x] > 0) {
				if (line[x] == line[x+1]) {
					smoothness += line[x];
				}
			}
		}
		return smoothness;
	}
	
	private static final double mono(int[] line) {
		Point[] points = new Point[] {
				new Point(0,line[0]),
				new Point(1,line[1]),
				new Point(2,line[2]),
				new Point(3,line[3])
		};
		
		double d1 = points[0].distance(points[3]);
		double d2 = points[0].distance(points[1])
				  + points[1].distance(points[2])
				  + points[2].distance(points[3]);
		return 1 / (d2 / d1);
	}
	
	private  final float rowHelper(long board, float[] table) {
	    return table[(int) ((board >>  0) & ROW_MASK)] +
	           table[(int) ((board >> 16) & ROW_MASK)] +
	           table[(int) ((board >> 32) & ROW_MASK)] +
	           table[(int) ((board >> 48) & ROW_MASK)];
	}
	
	public final float scoreHeuristicBoard(long board) {
	    return rowHelper(board , rowValues) 
	    	 + rowHelper(transpose(board), rowValues);
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
}

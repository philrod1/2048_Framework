package eval;

import model.State;

public class CondensedEvaluator implements Evaluator {

    private final SimpleLearn3 ml = null;//new SimpleLearn3();
    private final double[][] weights = new double[2][65536];
    private static final long ROW_MASK = 0xFFFF;

    public CondensedEvaluator() {
//        for (int r0 = 0 ; r0 < 65536 ; r0++) {
//            for (int r1 = 0 ; r1 < 65536 ; r1++) {
//                for (int r2 = 0 ; r2 < 65536 ; r2++) {
//                    for (int r3 = 0 ; r3 < 65536 ; r3++) {
//                        long board = rowsToLong(r0, r1, r2, r3);
//                        System.out.println(Long.toHexString(board));
//                        double value = ml.evaluate(board);
//                        update(getBoard(board), value, 1);
//                    }
//                }
//            }
//        }
    }

    private long rowsToLong(long ... rows) {
        return (
                   (rows[0] & 0xFFFF) << 48)
                | ((rows[1] & 0xFFFF) << 32)
                | ((rows[2] & 0xFFFF) << 16)
                | ((rows[3] & 0xFFFF)
        );
    }

    public double evaluate(State game) {
        return evaluate(game.toLong());
    }

    private double evaluate(char[] b) {
        double score = 0;
        char[] t = transpose(b);
        char[] r = reverse(b);
        char[] f = flip(b);
        char[] rt = reverse(t);
        char[] fr = flip(r);
        char[] ft = flip(t);
        char[] tfr = transpose(fr);
        char[][] boards = new char[][] {
                b,t,r,f,rt,fr,ft,tfr
        };

		for (int i = 0; i < 2; i++) {
            for (int j = 0 ; j < boards.length ; j++) {
                score += weights[i][boards[j][i]];
            }
        }
        return score;

    }

    private static char[] transpose(char[] b) {
        return new char[]{
                (char) (((b[0] & 0xF000))       | ((b[1] & 0xF000) >> 4) | ((b[2] & 0xF000) >> 8) | (b[3] & 0xF000) >> 12),
                (char) (((b[0] & 0xF00)  << 4 ) | ((b[1] & 0xF00))       | ((b[2] & 0xF00)  >> 4) | ((b[3] & 0xF00) >> 8)),
                (char) (((b[0] & 0xF0)   << 8 ) | ((b[1] & 0xF0)   << 4) | ((b[2] & 0xF0)       ) | ((b[3] & 0xF0)  >> 4)),
                (char) (((b[0] & 0xF)    << 12) | ((b[1] & 0xF)    << 8) | ((b[2] & 0xF)    << 4) | ((b[3] & 0xF)       ))
        };
    }

    private static char[] flip(char[] b) {
        return new char[] {b[3],b[2],b[1],b[0]};
    }

    private static char[] reverse(char[] b) {
        return new char[] {
                reverseRow(b[0]),
                reverseRow(b[1]),
                reverseRow(b[2]),
                reverseRow(b[3])
        };
    }

    private static char reverseRow(char row) {
        return (char) (((row & 0xF) << 12) | ((row & 0xF0) << 4) | ((row & 0xF00) >> 4) | ((row & 0xF000) >> 12));
    }

    private void update(char[] b, double expectedValue, double learningRate) {

//        final double delta = (expectedValue - evaluate(b)) * learningRate;
        final double delta = expectedValue * learningRate;

        char[] t = transpose(b);
        char[] r = reverse(b);
        char[] f = flip(b);
        char[] rt = reverse(t);
        char[] fr = flip(r);
        char[] ft = flip(t);
        char[] tfr = transpose(fr);
        char[][] boards = new char[][] { b, t, r, f, rt, fr, ft, tfr};
        for (int i = 0; i < weights.length; i++) {
            for (char[] board : boards) {
                weights[i][board[0]] += delta;
            }
        }
    }

    @Override
    public double evaluate(long state) {
        char[] b = new char[] {
                (char) ((state & 0xFFFF000000000000L) >> 48),
                (char) ((state & 0xFFFF00000000L) >> 32),
                (char) ((state & 0xFFFF0000L) >> 16),
                (char) (state & 0xFFFFL),
        };
        return evaluate(b);
    }

    public char[] getBoard(long board) {
        return new char[] {
                (char) (board & ROW_MASK),
                (char) ((board >> 16) & ROW_MASK),
                (char) ((board >> 32) & ROW_MASK),
                (char) ((board >> 48) & ROW_MASK)
        };
    }
}

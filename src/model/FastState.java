package model;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
/*
 * https://github.com/nneonneo/2048-ai/blob/master/2048.cpp
 */
public class FastState extends AbstractState {

    private long board;

    private static final long ROW_MASK = 0xFFFFL;

    private static final char[] rowLeftTable = new char[65536];
    private static final char[] rowRightTable = new char[65536];
    private static final int[]  scoreTable = new int[65536];

    static {
        initTables();
    }

    public FastState() {
        reset();
    }

    private static void initTables() {
        for (int row = 0; row < 65536; ++row) {
            char[] line = new char[] {
                    (char) (row & 0xf),
                    (char) ((row >>  4) & 0xf),
                    (char) ((row >>  8) & 0xf),
                    (char) ((row >> 12) & 0xf)
            };

            int score = 0;
            for (int i = 0; i < 4; ++i) {
                int rank = line[i];
                if (rank >= 2) {
                    score += (rank - 1) * (1 << rank);
                }
            }
            scoreTable[row] = score;

            // execute a move to the left
            for (int i = 0; i < 3; ++i) {
                int j;
                for (j = i + 1; j < 4; ++j) {
                    if (line[j] != 0) break;
                }
                if (j == 4) break; // no more tiles to the right

                if (line[i] == 0) {
                    line[i] = line[j];
                    line[j] = 0;
                    i--; // retry this entry
                } else if (line[i] == line[j]) {
                    if(line[i] != 0xf) {
                        /* Pretend that 32768 + 32768 = 32768 (representational limit). */
                        line[i]++;
                    }
                    line[j] = 0;
                }
            }

            char result = (char) (line[0] |
                                (line[1] <<  4) |
                                (line[2] <<  8) |
                                (line[3] << 12));
            char revResult = reverseRow(result);
            char revRow = reverseRow((char) row);

            rowLeftTable[row] = (char) (row ^ result);
            rowRightTable[revRow] = (char) (revRow ^ revResult);
        }
    }

    private static long transpose(long board) {
        long a1 = board & 0xF0F00F0FF0F00F0FL;
        long a2 = board & 0x0000F0F00000F0F0L;
        long a3 = board & 0x0F0F00000F0F0000L;
        long a = a1 | (a2 << 12) | (a3 >> 12);
        long b1 = a & 0xFF00FF0000FF00FFL;
        long b2 = a & 0x00FF00FF00000000L;
        long b3 = a & 0x00000000FF00FF00L;
        return b1 | (b2 >> 24) | (b3 << 24);
    }

    private static char reverseRow(char row) {
        return (char) ((row >> 12) | ((row >> 4) & 0x00F0)  | ((row << 4) & 0x0F00) | (row << 12));
    }

    private static long moveLeft(long board) {
        long ret = board;
        ret ^= ((long)(rowLeftTable[(int) (board & ROW_MASK)]));
        ret ^= ((long)(rowLeftTable[(int) ((board >> 16) & ROW_MASK)])) << 16;
        ret ^= ((long)(rowLeftTable[(int) ((board >> 32) & ROW_MASK)])) << 32;
        ret ^= ((long)(rowLeftTable[(int) ((board >> 48) & ROW_MASK)])) << 48;
        return ret;
    }

    private static long moveRight(long board) {
        long ret = board;
        ret ^= ((long)(rowRightTable[(int) ((board >> 48) & ROW_MASK)])) << 48;
        ret ^= ((long)(rowRightTable[(int) ((board >> 32) & ROW_MASK)])) << 32;
        ret ^= ((long)(rowRightTable[(int) ((board >> 16) & ROW_MASK)])) << 16;
        ret ^= ((long)(rowRightTable[(int) (board & ROW_MASK)]));
        return ret;
    }

    private static long moveUp(long board) {
        return transpose(moveLeft(transpose(board)));
    }

    private static long moveDown(long board) {
        return transpose(moveRight(transpose(board)));
    }

    private static int countEmpty(long board) {
        if (board == 0) {
            return 16;
        }
        board |= (board >> 2) & 0x3333333333333333L;
        board |= (board >> 1);
        board = ~board & 0x1111111111111111L;
        board += board >> 32;
        board += board >> 16;
        board += board >> 8;
        board += board >> 4;
        return (int) (board & 0xf);
    }
    
    private long addRandomTile(long board) {
        long tile = (Math.random() < 0.9) ? 1 : 2;
        int index = (int)(Math.random() * countEmpty(board));
        long tmp = board;
        int count = 0;
        while (true) {
            while ((tmp & 0xf) != 0) {
                tmp >>= 4;
                tile <<= 4;
                ++count;
            }
            if (index == 0) break;
            --index;
            tmp >>= 4;
            tile <<= 4;
            ++count;
        }
        newTilePosition = new Point(count % 4,count / 4);
        return board | tile;
    }

    @Override
    public void reset() {
        super.reset();
        board = 0;
        board = addRandomTile(board);
        board = addRandomTile(board);
    }

    @Override
    public int getScore() {
        return scoreTable[(int) ( board        & ROW_MASK)]
             + scoreTable[(int) ((board >> 16) & ROW_MASK)]
             + scoreTable[(int) ((board >> 32) & ROW_MASK)]
             + scoreTable[(int) ((board >> 48) & ROW_MASK)];
    }

    @Override
    public List<MOVE> getMoves() {
        List<MOVE> moves = new LinkedList<>();
        if (board != moveUp(board)) {
            moves.add(MOVE.UP);
        }
        if (board != moveDown(board)) {
            moves.add(MOVE.DOWN);
        }
        if (board != moveLeft(board)) {
            moves.add(MOVE.LEFT);
        }
        if (board != moveRight(board)) {
            moves.add(MOVE.RIGHT);
        }
        return moves;
    }

    @Override
    public void move(MOVE move) {
        long prev = board;
        switch (move) {
            case UP -> board = moveUp(board);
            case DOWN -> board = moveDown(board);
            case LEFT -> board = moveLeft(board);
            case RIGHT -> board = moveRight(board);
        }
        if (prev != board) {
            board = addRandomTile(board);
        }
    }

    @Override
    public int getValue(int x, int y) {
        int val = (int) ((board >> (y * 16 + x * 4)) & 0xF);
        return val == 0 ? 0 : 1 << val;
    }

    @Override
    public void fromLong(long data) {
        board = ((data & ROW_MASK) << 48)
                | ((data >> 16) & ROW_MASK) << 32
                | ((data >> 32) & ROW_MASK) << 16
                | ((data >> 48) & ROW_MASK);
//        board = data;
    }

    @Override
    public long toLong() {
        return ((board & ROW_MASK) << 48)
                | ((board >> 16) & ROW_MASK) << 32
                | ((board >> 32) & ROW_MASK) << 16
                | ((board >> 48) & ROW_MASK);
//        return board;
    }

    @Override
    public void fromStateData(StateData stateData) {
        long data = stateData.board;
        board = ((data & ROW_MASK) << 48)
                | ((data >> 16) & ROW_MASK) << 32
                | ((data >> 32) & ROW_MASK) << 16
                | ((data >> 48) & ROW_MASK);
    }

    public char[] getBoard() {
        return new char[] {
                (char) (board & ROW_MASK),
                (char) ((board >> 16) & ROW_MASK),
                (char) ((board >> 32) & ROW_MASK),
                (char) ((board >> 48) & ROW_MASK)
        };
    }

    public int slide2(MOVE move, char[] board, char[] newBoard) {

        int score1 = scoreTable[board[0]]
                   + scoreTable[board[1]]
                   + scoreTable[board[2]]
                   + scoreTable[board[3]];

        long prev = (((long)board[0]) << 48)
                  | (((long)board[1]) << 32)
                  | (((long)board[2]) << 16)
                  | board[3];

        long next = 0;
        switch (move) {
            case DOWN ->    next = moveUp(prev);
            case UP ->  next = moveDown(prev);
            case LEFT ->  next = moveLeft(prev);
            case RIGHT -> next = moveRight(prev);
        }

        newBoard[3] = (char) (next & ROW_MASK);
        newBoard[2] = (char) ((next >> 16) & ROW_MASK);
        newBoard[1] = (char) ((next >> 32) & ROW_MASK);
        newBoard[0] = (char) ((next >> 48) & ROW_MASK);

        int score2 = scoreTable[newBoard[0]]
                   + scoreTable[newBoard[1]]
                   + scoreTable[newBoard[2]]
                   + scoreTable[newBoard[3]];

        return score2 - score1;
    }
}

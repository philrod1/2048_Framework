package controller;

import model.AbstractState;
import model.BinaryState;
import model.FastState;

import static model.AbstractState.MOVE.*;

public class StateCompare {
    public static void main(String[] args) {
        BinaryState bs = new BinaryState();
        FastState fs = new FastState();
        long board = 0x123456789ABCDEF0L;
        bs.fromLong(board);
        fs.fromLong(board);

        for (int y = 0 ; y < 4 ; y++) {
            for (int x = 0 ; x < 4 ; x++) {
                System.out.print(bs.getValue(x, y) + " ");
            }
            System.out.print("  ");
            for (int x = 0 ; x < 4 ; x++) {
                System.out.print(fs.getValue(x, y) + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println(bs.toLong());
        System.out.println(fs.toLong());

        for (char row : bs.getBoard()) {
            System.out.println(Integer.toHexString(row));
        }
        System.out.println();
        for (char row : fs.getBoard()) {
            System.out.println(Integer.toHexString(row));
        }

        System.out.println(bs.getMoves());
        System.out.println(fs.getMoves());
        char[] newBoard = new char[4];
        int result = bs.slide2(DOWN, bs.getBoard(), newBoard);

        System.out.println(result);
        for (char row : newBoard) {
            System.out.println(Integer.toHexString(row));
        }

        newBoard = new char[4];
        result = fs.slide2(DOWN, fs.getBoard(), newBoard);

        System.out.println(result);
        for (char row : newBoard) {
            System.out.println(Integer.toHexString(row));
        }
    }
}

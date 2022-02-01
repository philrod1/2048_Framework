package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import data.LongInfo;

public class BinaryState extends AbstractState {
	
	private char[] board = new char[4];
	private static final int[] AND_MASK = new int[]{0xFFF0,0xFF0F,0xF0FF,0x0FFF};
	private final Random rng = new Random();
	
	private int highTile = 1;
	
	public BinaryState() {
		reset();
	}
	
	public BinaryState(int seedTile) {
		reset();
	}
	
	public BinaryState(long data, int score) {
		fromLong(data);
		this.score = score;
	}
	
	public BinaryState(BinaryState that) {
		for (int i = 0 ; i < 4 ; i++) {
			this.board[i] = that.board[i];
		}
		this.score = that.score;
	}
	
	public int getHighTile() {
		return highTile;
	}
	
	public char[] getBoard() {
		return new char[]{board[0],board[1],board[2],board[3]};
	}
	
	@Override
	public List<MOVE> getMoves() {
		List<MOVE> moves = new ArrayList<MOVE>();
		int tempScore = score;
		for(MOVE move : MOVE.values()) {
			char[] newBoard = slide(move, board);
			if(!areEqual(board, newBoard)) {
				moves.add(move);
			}
		}
		score = tempScore;
		return moves;
	}

	@Override
	public long toLong() {
		long data = board[0];
		data <<= 16;
		data |= board[1];
		data <<= 16;
		data |= board[2];
		data <<= 16;
		data |= board[3];
		return data;
	}
	
	private char[] slide(MOVE dir, char[] board) {
		if(dir == null)
			return board;
		char[] newBoard = new char[4];
		int[][] results = new int[4][4];
		switch(dir) {
		case DOWN:
			for(int i = 0 ; i < 4 ; i++) {
				results[i] = slide(
						(board[0] >> i * 4) & 0xf, 
						(board[1] >> i * 4) & 0xf, 
						(board[2] >> i * 4) & 0xf, 
						(board[3] >> i * 4) & 0xf);
			}
			for(int i = 0 ; i < 4 ; i++) {
				newBoard[i] = (char)(
						(results[3][i] << 12) | 
						(results[2][i] << 8)  | 
						(results[1][i] << 4)  | 
						 results[0][i]);
			}
			break;
		case UP:
			for(int i = 0 ; i < 4 ; i++) {
				results[i] = slide(
						(board[3] >> i * 4) & 0xf, 
						(board[2] >> i * 4) & 0xf, 
						(board[1] >> i * 4) & 0xf, 
						(board[0] >> i * 4) & 0xf);
			}
			for(int i = 0 ; i < 4 ; i++) {
				newBoard[3-i] = (char)(
						(results[3][i] << 12) | 
						(results[2][i] << 8)  | 
						(results[1][i] << 4)  | 
						 results[0][i]);
			}
			break;
		case LEFT:
			for(int i = 0 ; i < 4 ; i++) {
				int[] result = slide(
						(board[i] >> 12) & 0xf, 
						(board[i] >> 8)  & 0xf, 
						(board[i] >> 4)  & 0xf, 
						 board[i]        & 0xf);
				newBoard[i] = (char)(
						(result[0] << 12) | 
						(result[1] << 8)  | 
						(result[2] << 4)  | 
						 result[3]);
			}
			break;
		case RIGHT:
			for(int i = 0 ; i < 4 ; i++) {
				int[] result = slide(
						 board[i]        & 0xf, 
						(board[i] >> 4)  & 0xf, 
						(board[i] >> 8)  & 0xf, 
						(board[i] >> 12) & 0xf);
				newBoard[i] = (char) (
						(result[3] << 12) | 
						(result[2] << 8)  | 
						(result[1] << 4)  | 
						 result[0]);
			}
			break;
		}
		return newBoard;
	}
	
	public int slide2(MOVE dir, char[] board, char[] newBoard) {
		if(dir == null){
			for(int i = 0 ; i < 4 ; i++) {
				newBoard[i] = board[i];
			}
			return 0;
		}
		int reward = 0;
		int[][] results = new int[4][5];
		switch(dir) {
		case DOWN:
			for(int i = 0 ; i < 4 ; i++) {
				results[i] = slide2(
						(board[0] >> i * 4) & 0xf, 
						(board[1] >> i * 4) & 0xf, 
						(board[2] >> i * 4) & 0xf, 
						(board[3] >> i * 4) & 0xf);
			}
			for(int i = 0 ; i < 4 ; i++) {
				newBoard[i] = (char)(
						(results[3][i] << 12) | 
						(results[2][i] << 8)  | 
						(results[1][i] << 4)  | 
						 results[0][i]);
				reward += results[i][4];
			}
			break;
		case UP:
			for(int i = 0 ; i < 4 ; i++) {
				results[i] = slide2(
						(board[3] >> i * 4) & 0xf, 
						(board[2] >> i * 4) & 0xf, 
						(board[1] >> i * 4) & 0xf, 
						(board[0] >> i * 4) & 0xf);
			}
			for(int i = 0 ; i < 4 ; i++) {
				newBoard[3-i] = (char)(
						(results[3][i] << 12) | 
						(results[2][i] << 8)  | 
						(results[1][i] << 4)  | 
						 results[0][i]);
				reward += results[i][4];
			}
			break;
		case LEFT:
			for(int i = 0 ; i < 4 ; i++) {
				int[] result = slide2(
						(board[i] >> 12) & 0xf, 
						(board[i] >> 8)  & 0xf, 
						(board[i] >> 4)  & 0xf, 
						 board[i]        & 0xf);
				newBoard[i] = (char)(
						(result[0] << 12) | 
						(result[1] << 8)  | 
						(result[2] << 4)  | 
						 result[3]);
				reward += result[4];
			}
			break;
		case RIGHT:
			for(int i = 0 ; i < 4 ; i++) {
				int[] result = slide2(
						 board[i]        & 0xf, 
						(board[i] >> 4)  & 0xf, 
						(board[i] >> 8)  & 0xf, 
						(board[i] >> 12) & 0xf);
				newBoard[i] = (char) (
						(result[3] << 12) | 
						(result[2] << 8)  | 
						(result[1] << 4)  | 
						 result[0]);
				reward += result[4];
			}
			break;
		}
		return reward;
	}
	
	private int[] slide2(int a, int b, int c, int d) {
		int r = 0;
		if (a + b + c + d == 0) return new int[]{a,b,c,d,r};
		for(int i = 0 ; d == 0 && i < 3 ; i++) {
			d = c;
			c = b;
			b = a;
			a = 0;
		}
		for(int i = 0 ; c == 0 && i < 2 ; i++) {
			c = b;
			b = a;
			a = 0;
		}
		if (b == 0) {
			b = a;
			a = 0;
		}
		if(c == d) {
			d++;
//			highTile = Math.max(highTile, d);
			r += 1 << d;
			c = b;
			b = a;
			a = 0;
		}
		if(a + b + c == 0) return new int[]{a,b,c,d,r};
		while(c == 0) {
			c = b;
			b = a;
			a = 0;
		}
		if(b == c) {
			c++;
//			highTile = Math.max(highTile, c);
			r += 1 << c;
			b = a;
			a = 0;
		}
		if(a + b == 0) return new int[]{a,b,c,d,r};
		while(b == 0) {
			b = a;
			a = 0;
		}
		if(a == b) {
			b++;
//			highTile = Math.max(highTile, b);
			r += 1 << b;
			a = 0;
		}
		return new int[]{a,b,c,d,r};
	}
	
	private int[] slide(int a, int b, int c, int d) {
		if (a + b + c + d == 0) return new int[]{a,b,c,d};
		for(int i = 0 ; d == 0 && i < 3 ; i++) {
			d = c;
			c = b;
			b = a;
			a = 0;
		}
		for(int i = 0 ; c == 0 && i < 2 ; i++) {
			c = b;
			b = a;
			a = 0;
		}
		if (b == 0) {
			b = a;
			a = 0;
		}
		if(c == d) {
			d++;
			highTile = Math.max(highTile, d);
			score += 1 << d;
			c = b;
			b = a;
			a = 0;
		}
		if(a + b + c == 0) return new int[]{a,b,c,d};
		while(c == 0) {
			c = b;
			b = a;
			a = 0;
		}
		if(b == c) {
			c++;
			highTile = Math.max(highTile, c);
			score += 1 << c;
			b = a;
			a = 0;
		}
		if(a + b == 0) return new int[]{a,b,c,d};
		while(b == 0) {
			b = a;
			a = 0;
		}
		if(a == b) {
			b++;
			highTile = Math.max(highTile, b);
			score += 1 << b;
			a = 0;
		}
		return new int[]{a,b,c,d};
	}

	@Override
	public void reset() {
		super.reset();
		board = new char[4];
		for (int i = 0; i < N_START_TILES; i++) {
			addRandomTile();
		}
		score = 0;
	}
	
	public void bump() {
		super.reset();
		board = new char[4];
		fromLong(0xabcdL);
		highTile = 0xd;
		for (int i = 0; i < N_START_TILES; i++) {
			addRandomTile();
		}
		score = 0;
		score = LongInfo.approxScore(0xabcdL);
	}

	private void setValue(int x, int y, int i) {
		board[y] &= AND_MASK[x];
		board[y] |= (char)(log2(i) << (x*4));
	}

	@Override
	public void move(MOVE dir) {
		StateData data = new StateData(toLong(), score);
		char[] newBoard = slide(dir, board);
		if (!areEqual(board, newBoard)) {
			undoStack.push(data);
			redoStack.clear();
			board = newBoard;
			addRandomTile();
		}
	}
	
	public boolean addRandomTile() {
		List<Point> free = new ArrayList<Point>();
		for(int y = 0 ; y < 4 ; y++) {
			for(int x = 0 ; x < 4 ; x++) {
				if(getVal(x, y) == 0) {
					free.add(new Point(x, y));
				}
			}
		}
		if (free.isEmpty()) {
			return false;
		}
		Point p = free.get(rng.nextInt(free.size()));
		newTilePosition = p;
		setValue(p.x, p.y, rng.nextFloat() < 0.9 ? 2 : 4);
		return true;
	}
	
	private boolean areEqual(char[] b1, char[] b2) {
		return (b1[0] == b2[0]) && 
			   (b1[1] == b2[1]) && 
			   (b1[2] == b2[2]) && 
			   (b1[3] == b2[3]);
	}

	@Override
	public int getValue(int x, int y) {
		int val = getVal(x, y);
		return val == 0 ? 0 : 1 << val;
	}
	
	public int highVal() {
		int highVal = 0;
		for(int y = 0 ; y < 4 ; y++) {
			for(int x = 0 ; x < 4 ; x++) {
				highVal = Math.max(highVal, getVal(x, y));
			}
		}
		return highVal;
	}
	
	private char getVal(int x, int y) {
		return (char)((board[y]>>(x*4))&0xF);
	}

	@Override
	public void fromLong(long data) {
		board[3] = (char) data;
		for(int i = 1 ; i < 4 ; i++) {
			data >>= 16;
			board[3-i] = (char) data;
		}
	}

	@Override
	public boolean equals(State that) {
		return this.toLong() == that.toLong();
	}

	@Override
	public StateData toStateData() {
		return new StateData(toLong(), score);
	}

	@Override
	public void fromStateData(StateData stateData) {
		fromLong(stateData.board);
		score = stateData.score;
	}

	public int halfMove(MOVE dir) {
		StateData data = new StateData(toLong(), score);
		int oldScore = score;
		char[] newBoard = slide(dir, board);
		if(!areEqual(board, newBoard)) {
			undoStack.push(data);
			redoStack.clear();
			board = newBoard;
		}
		return score - oldScore;
	}
}

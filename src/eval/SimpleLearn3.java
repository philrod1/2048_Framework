package eval;

import java.awt.Point;
import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;

import data.NTupleNetwork;
import model.AbstractState.MOVE;
import model.BinaryState;
import model.State;
import data.CircularArray;
import data.Plot;

public class SimpleLearn3 implements Evaluator {

//	private final String name = "double-trouble.bin";
//	private final String description = "Double Trouble Network";
//	private final String name = "bumpy.bin";
//	private final String description = "Bumpy Network";
//	private final String name = "mega-trouble.bin";
//	private final String description = "Mega Trouble Network";
//  private final String name = "beast-mode2.bin";
//	private final String description = "Beast Mode 2 Network";

//  private final String name = "matsuzaki.bin";
//	private final String description = "Beast Mode Network";

//	private final String name = "simple-side.bin";
//	private final String description = "Simple Side Network";
//	private final String name = "two-lines.bin";
//	private final String description = "Two Lines Network";
//	private final String name = "score-test.bin";
//	private final String description = "Score Test Network";
//	private final String[][] shapes = new String[][] {
//		{
//				"****",
//				"....",
//				"....",
//				"...."
//		},
//		{
//				"....",
//				"****",
//				"....",
//				"...."
//		},
//		{
//				"****",
//				"**..",
//				"....",
//				"...."
//		},
//		{
//				"****",
//				".**.",
//				"....",
//				"...."
//		},
//		{
//				"....",
//				"****",
//				"**..",
//				"....",
//		},
//		{
//				"....",
//				"****",
//				".**.",
//				"....",
//		},
//		{
//				"....",
//				"....",
//				"****",
//				"**..",
//		},
//		{
//				"....",
//				"....",
//				"****",
//				".**.",
//		}
//	};
	private final String name = "test.bin";
	private final String description = "Test Network";
 	private final String[][] shapes = new String[][] {
		{
				"****",
				"....",
				"....",
				"...."
		},
		{
				"....",
				"****",
				"....",
				"...."
		}
	};
		
	private NTupleNetwork network;
//	private NTupleNetwork simpleNetwork = new NTupleNetwork(simple, "Side-learn Simple");

	public SimpleLearn3() {

		System.out.println("SimpleLearn3");
//		load("simple.dat");
		load();
		System.out.println(network);
//		FileWriter log = null;
//		try {
//			log = new FileWriter(new File("weights.js" ), true);
//			log.write("const weights = [\n\t[\n\t\t");
//			for(double v : weights[0]) {
//				log.write(v + ",\n\t\t");
//			}
//			log.write("\t], [\n\t\t");
//			for(double v : weights[1]) {
//				log.write(v + ",\n\t\t");
//			}
//			log.write("\t];");
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				log.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		learn();
	}
	
	private void learn() {
		int nGames = 2000000;
		Plot plot = new Plot(nGames, 1200, 900);
		JFrame frame = new JFrame("Plot.");
		frame.getContentPane().add(plot);
		frame.pack();
		frame.setVisible(true);
		frame.setBounds(10, 10, 1200, 900);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		CircularArray stats = new CircularArray(10000);

		for (int i = 0; i < nGames; i++) {
			if (i % 10000 == 0) {
				save();
			}
			int[] r = learningGame(0.0001);
			stats.add(r[0]);
			plot.addPoint(r[0], stats.average(), r[1]);
		}
		save();
	}

	private static final ExecutorService exec = Executors.newSingleThreadExecutor();
	private void save() {
		System.out.println("Saving");
//		exec.submit(() -> {
			try (OutputStream file = new FileOutputStream(name);
				 OutputStream buffer = new BufferedOutputStream(file);
				 ObjectOutput output = new ObjectOutputStream(buffer)) {
				output.writeObject(network);
			} catch (IOException e) {
				e.printStackTrace();
			}
//		});
	}
	
	private void load() {
		System.out.println("Loading");
		InputStream file = null;
		if (!(new File(name).exists())) {
			System.out.println("New network");
			String[][] shapes = new String[][] {
					{
							"****",
							"....",
							"....",
							"....",
					},
					{
							"....",
							"****",
							"....",
							"....",
					}
			};
			network = new NTupleNetwork(shapes, description);
			save();
			return;
		}
		System.out.println("Load existing file");
		try {
			file = new FileInputStream(name);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			network = (NTupleNetwork) input.readObject();
			input.close();
			buffer.close();

//			file = new FileInputStream("simple-side.bin");
//			buffer = new BufferedInputStream(file);
//			input = new ObjectInputStream(buffer);
//			simpleNetwork = (NTupleNetwork) input.readObject();
//			input.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				assert file != null;
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	private int[] learningGame(double learningRate) {
		BinaryState game = new BinaryState();
		List<MOVE> moves = game.getMoves();
		while (!moves.isEmpty()) {
			char[] halfMoveBoard = new char[4];
			MOVE move = getBestMove(game, moves, halfMoveBoard);
			game.move(move);
			moves = game.getMoves();
			double correctActionValue = 0;
			if (!moves.isEmpty()) {
				correctActionValue = getBestValueAction(game, moves);
			}
			update(halfMoveBoard, correctActionValue, learningRate);
		}
		int highTile = 0;
		long result = game.toLong();
		for(int i = 0 ; i < 16 ; i++) {
			highTile = Math.max(highTile, (int)((result >> (4*i)) & 0xF));
		}
		return new int[]{game.getScore(),highTile};
	}

	private void update(char[] b, double expectedValue, double learningRate) {

		final double delta = (expectedValue - evaluate(b)) * learningRate;
		
		char[] t = transpose(b);
		char[] r = reverse(b);
		char[] f = flip(b);
		char[] rt = reverse(t);
		char[] fr = flip(r);
		char[] ft = flip(t);
		char[] tfr = transpose(fr);
		char[][] boards = new char[][] { b, t, r, f, rt, fr, ft, tfr};
		for (int i = 0; i < network.tuples.length; i++) {
			for (char[] board : boards) {
				network.weights[i][getAddress(board, network.tuples[i])] += delta;
			}
		}
//		for (int i = 0; i < simpleNetwork.tuples.length; i++) {
//			for (char[] board : boards) {
//				simpleNetwork.weights[i][getAddress(board, simpleNetwork.tuples[i])] = evaluate(board);
//			}
//		}
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

	private double getBestValueAction(BinaryState game, List<MOVE> moves) {
		double bestScore = Double.NEGATIVE_INFINITY;
		char[] board = game.getBoard();
		for(MOVE move : moves) {
			char[] newBoard = new char[4];
			int reward = game.slide2(move, board, newBoard);
			double result = reward + evaluate(newBoard);// + rollout(newBoard);
			if(result > bestScore) {
				bestScore = result;
			}
		}
		return bestScore;
	}

	private int rollout(char[] newBoard) {
		Random rng = new Random();
		BinaryState game = new BinaryState();
		long state = 0;
		for (int i = 0 ; i < 4 ; i++) {
			state |= ((long)(newBoard[i])) << (i * 16);
		}
		game.fromLong(state);

		List<MOVE> moves = game.getMoves();
		while (!moves.isEmpty()) {
			game.move(moves.get(rng.nextInt(moves.size())));
			moves = game.getMoves();
		}

		return game.getScore();
	}


	private MOVE getBestMove(BinaryState game, List<MOVE> moves, char[] halfMoveBoard) {
		double bestScore = Double.NEGATIVE_INFINITY;
		MOVE bestMove = null;
		char[] board = game.getBoard();
		for(MOVE move : moves) {
			char[] newBoard = new char[4];
			int reward = game.slide2(move, board, newBoard);
			double result = reward + evaluate(newBoard);
			if(result > bestScore) {
				bestScore = result;
				bestMove = move;
				for (int i = 0 ; i < 4 ; i++) {
					halfMoveBoard[i] = newBoard[i];
				}
			}
		}
		return bestMove;

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
//		lock.lock();
		for (int i = 0; i < network.tuples.length; i++) {
//		for (int i = 0; i < 2; i++) {
			for (int j = 0 ; j < boards.length ; j++) {
				score += network.weights[i][getAddress(boards[j], network.tuples[i])];
			}
		}
		return score;

	}
	
	private static int getAddress(char[] board, Point... points){
		int i = 0;
		int address = 0;
		for(Point p : points) {
			address |= ((board[p.y] >> (p.x * 4)) & 0xF) << (i++ * 4);
		}
		return address;
	}


	private static char[] transpose(char[] b) {
		return new char[]{
				(char) (((b[0] & 0xF000))       | ((b[1] & 0xF000) >> 4) | ((b[2] & 0xF000) >> 8) | (b[3] & 0xF000) >> 12),
				(char) (((b[0] & 0xF00)  << 4 ) | ((b[1] & 0xF00))       | ((b[2] & 0xF00)  >> 4) | ((b[3] & 0xF00) >> 8)),
				(char) (((b[0] & 0xF0)   << 8 ) | ((b[1] & 0xF0)   << 4) | ((b[2] & 0xF0)       ) | ((b[3] & 0xF0)  >> 4)),
				(char) (((b[0] & 0xF)    << 12) | ((b[1] & 0xF)    << 8) | ((b[2] & 0xF)    << 4) | ((b[3] & 0xF)       ))
		};
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
}
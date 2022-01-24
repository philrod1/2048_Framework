package data;

import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.RandomAI;
import model.AbstractState.MOVE;
import model.BinaryState;
import ai.DepthLimitedSearch;
import ai.Player;
import eval.MonoEval;

public class Statistics {
	private final int nGames;
	private double totalScore = 0;
	private int highScore = 0;
	private int highTile = 0;
	private final int[] results;
	private final int[] highTiles = new int[15];
	private final Player player;
	private final BinaryState game = new BinaryState();
	private int n;
	private double mean;
	private double standardDeviation;
	
	public Statistics (int nGames, Player player) {
		this.nGames = nGames;
		results = new int[nGames];
		this.player = player;
		n = 0;
	}
	
	public void reset() {
		n = 0;
		highScore = 0;
		highTile = 0;
		standardDeviation = 0;
		mean = 0;
		for(int t = 0 ; t < 15 ; t++) {
			highTiles[t] = 0;
		}
		game.reset();
	}
	public class GamePlayer implements Callable<Point> {

		@Override
		public Point call() throws Exception {
			BinaryState game = new BinaryState();
			List<MOVE> moves = game.getMoves();
			while(!moves.isEmpty()) {
				MOVE move = player.getMove(game);
				game.move(move);
				moves = game.getMoves();
			}
			return new Point(game.getScore(), game.getHighestTileValue());
		}

		
	}
	
	public void begin() {
		long start = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(1);
		List<Future<Point>> list = new ArrayList<Future<Point>>();
		for (int i = 0; i < nGames; i++) {
			Callable<Point> worker = new GamePlayer();
			Future<Point> submit = executor.submit(worker);
			list.add(submit);
		}
	    int i = 0;
		for (Future<Point> future : list) {
			try {
				Point result = future.get();
				results[i] = result.x;
				totalScore += result.x;
				highScore = Math.max(highScore, results[i]);
				int ht = result.y;
				highTile = Math.max(highTile, ht);
				highTiles[Integer.numberOfTrailingZeros(ht)-1]++;
				System.out.println(i++);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		standardDeviation = calculateStandardDeviation();
		System.out.println(System.currentTimeMillis() - start);
	}

	private double calculateStandardDeviation() {
		mean = totalScore / nGames;
		double sumSqDiff = 0.0;
		for(int i = 0 ; i < nGames ; i++) {
			double diff = results[i] - mean;
			sumSqDiff += diff * diff;
		}
		return Math.sqrt(sumSqDiff / nGames);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(  "100 Mono 5 128");
		sb.append(  "\nMean:               " + mean);
		sb.append("\nStandard deviation: " + standardDeviation);
		sb.append("\nHighest score:      " + highScore);
		sb.append("\nHighest tile:       " + highTile);
		sb.append("\nTile counts:        |");
		for(int t : highTiles) {
			sb.append(t + "|");
		}
		return sb.toString();
	}
	
	public static void main (String[] args) {
		Statistics s = new Statistics(100, new RandomAI());
		s.begin();
		System.out.println(s);
		FileWriter results = null;
		try {
			results = new FileWriter(new File("Mono_5_128.txt"), false);
			results.write(s.toString());
			results.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

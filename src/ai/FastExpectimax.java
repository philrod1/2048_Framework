package ai;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.AbstractState.MOVE;
import model.State;
import view.ComboObserver;
import view.ComboPanel;
import controller.Controller;
import data.LongInfo;
import eval.Evaluator;

public class FastExpectimax extends AbstractPlayer implements ComboObserver {
	
	private final float probability = 0.0001f;
	private int cacheDepth  = 3;
	private int depth = 4;
	
	private final JPanel panel = new FEPanel();
	
	private static long ROW_MASK = 0xFFFFL;
	private static long COL_MASK = 0x000F000F000F000FL;
	
	private final int[] rowLeft    = new int[65536];
	private final int[] rowRight   = new int[65536];
	private final long[] columnUp   = new long[65536];
	private final long[] columnDown = new long[65536];
	
	private static Evaluator evaluator = null;
	
	private final float[] heuristicTable = new float[65536];
	private final float[] scoreTable     = new float[65536];
		
	public FastExpectimax() {
		initTables();
	}
	
	@Override
	public MOVE getMove(State game) {
		switch(bestMove(game.toLong())) {
		case 0: return MOVE.DOWN;
		case 1: return MOVE.UP;
		case 2: return MOVE.LEFT;
		case 3: return MOVE.RIGHT;
		default: return null;
		}
	}
	
	//For fast expectimax 'borrowed' functions
	private static long transpose(long s) {
		return LongInfo.transpose(s);
	}
	
	private static int countEmpty(long s) {
		return LongInfo.count_empty(s);
	}
	
	private void initTables() {
	    for (int row = 0; row < 65536; ++row) {
	        int[] line = new int[]{
	                ((row >>  0) & 0xf),
	                ((row >>  4) & 0xf),
	                ((row >>  8) & 0xf),
	                ((row >> 12) & 0xf)
	        };

	        float hScore = 0.0f;
	        float score = 0.0f;
	        for (int i = 0; i < 4; ++i) {
	            int rank = line[i];
	            if (rank == 0) {
	                hScore += 10000.0f;
	            } else if (rank >= 2) {
	                // the score is the total sum of the tile and all intermediate merged tiles
	                score += (rank - 1) * (1 << rank);
	            }
	        }
	        scoreTable[row] = score;

	        int maxi = 0;
	        for (int i = 1; i < 4; ++i) {
	            if (line[i] > line[maxi]) maxi = i;
	        }

	        if (maxi == 0 || maxi == 3) hScore += 20000.0f;

	        // Check if maxi's are close to each other, and of diff ranks (eg 128 256)
	        for (int i = 1; i < 4; ++i) {
	            if ((line[i] == line[i - 1] + 1) || (line[i] == line[i - 1] - 1)) hScore += 1000.0f;
	        }

	        // Check if the values are ordered:
	        if ((line[0] < line[1]) && (line[1] < line[2]) && (line[2] < line[3])) hScore += 10000.0f;
	        if ((line[0] > line[1]) && (line[1] > line[2]) && (line[2] > line[3])) hScore += 10000.0f;

	        heuristicTable[row] = hScore;


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
	            } else if (line[i] == line[j] && line[i] != 0xf) {
	                line[i]++;
	                line[j] = 0;
	            }
	        }

	        int result = ( (line[0] <<  0) |
	                       			(line[1] <<  4) |
	                       			(line[2] <<  8) |
	                       			(line[3] << 12) );
	        int rev_result = reverseRow(result);
	        int rev_row = reverseRow(row);

	        rowLeft[row] = (row ^ result);
	        rowRight[rev_row] = (rev_row ^ rev_result);
	        columnUp[row] = unpackColumn( row) ^ unpackColumn(result);
	        columnDown[rev_row] = unpackColumn(rev_row) ^ unpackColumn(rev_result);
	    }
	}
	
	private final long unpackColumn(int row) {
	    long tmp = row;
	    return (tmp | (tmp << 12L) | (tmp << 24L) | (tmp << 36L)) & COL_MASK;
	}

	private final char reverseRow(int row) {
	    return (char) ((row >> 12) | ((row >> 4) & 0x00F0) | ((row << 4) & 0x0F00) | (row << 12));
	}
	
	private final long executeMoveUp(long board) {
	    long ret = board;
	    long t = transpose(board);
	    ret ^= columnUp[(int) ((t >>  0) & ROW_MASK)] <<  0;
	    ret ^= columnUp[(int) ((t >> 16) & ROW_MASK)] <<  4;
	    ret ^= columnUp[(int) ((t >> 32) & ROW_MASK)] <<  8;
	    ret ^= columnUp[(int) ((t >> 48) & ROW_MASK)] << 12;
	    return ret;
	}

	private final long executeMoveDown(long board) {
	    long ret = board;
	    long t = transpose(board);
	    ret ^= columnDown[(int) ((t >>  0) & ROW_MASK)] <<  0;
	    ret ^= columnDown[(int) ((t >> 16) & ROW_MASK)] <<  4;
	    ret ^= columnDown[(int) ((t >> 32) & ROW_MASK)] <<  8;
	    ret ^= columnDown[(int) ((t >> 48) & ROW_MASK)] << 12;
	    return ret;
	}

	private final long executeMoveLeft(long board) {
	    long ret = board;
	    ret ^= (long)(rowLeft[(int) ((board >>  0) & ROW_MASK)]) <<  0;
	    ret ^= (long)(rowLeft[(int) ((board >> 16) & ROW_MASK)]) << 16;
	    ret ^= (long)(rowLeft[(int) ((board >> 32) & ROW_MASK)]) << 32;
	    ret ^= (long)(rowLeft[(int) ((board >> 48) & ROW_MASK)]) << 48;
	    return ret;
	}

	private final long executeMoveRight(long board) {
		long ret = board;
	    ret ^= (long)(rowRight[(int) ((board >>  0) & ROW_MASK)]) <<  0;
	    ret ^= (long)(rowRight[(int) ((board >> 16) & ROW_MASK)]) << 16;
	    ret ^= (long)(rowRight[(int) ((board >> 32) & ROW_MASK)]) << 32;
	    ret ^= (long)(rowRight[(int) ((board >> 48) & ROW_MASK)]) << 48;
	    return ret;
	}

	/* Execute a move. */
	private final long executeMove(int move, long board) {
		switch(move) {
	    case 0: // up
	        return executeMoveUp(board);
	    case 1: // down
	        return executeMoveDown(board);
	    case 2: // left
	        return executeMoveLeft(board);
	    case 3: // right
	        return executeMoveRight(board);
	    default:
	        return board;
	    }
	}
	
	private static final float scoreHelper(long board, float[] table) {
	    return table[(int) ((board >>  0) & ROW_MASK)] +
	            table[(int) ((board >> 16) & ROW_MASK)] +
	            table[(int) ((board >> 32) & ROW_MASK)] +
	            table[(int) ((board >> 48) & ROW_MASK)];
	}
	
	private float scoreHeuristicBoard(long board) {
		if(evaluator == null) {
		    return scoreHelper(          board , heuristicTable) +
	           scoreHelper(transpose(board), heuristicTable) +
	           100000.0f;
		} else {
			double bestScore = Double.NEGATIVE_INFINITY;
			for(long next : LongInfo.expand2(board)) {
				bestScore = Math.max(evaluator.evaluate(next), bestScore);
			}
			return (float) bestScore;
		}
	}

	private float scoreTileChooseNode(EvalState state, long board, float cProb) {
	    int num_open = countEmpty(board);
	    cProb /= num_open;

	    float res = 0.0f;
	    long tmp = board;
	    long tile_2 = 1;
	    while (tile_2 > 0) {
	        if ((tmp & 0xf) == 0) {
	            res += scoreMoveNode(state, board |  tile_2      , cProb * 0.9f) * 0.9f;
	            res += scoreMoveNode(state, board | (tile_2 << 1), cProb * 0.1f) * 0.1f;
	        }
	        tmp >>= 4;
	        tile_2 <<= 4;
	    }
	    return res / num_open;
	}
	
	private float scoreMoveNode(EvalState state, long board, float cprob) {
	    if (cprob < state.probThreshold || state.currentDepth >= depth) {
	        if(state.currentDepth > state.maxDepth)
	            state.maxDepth = state.currentDepth;
	        return scoreHeuristicBoard(board);
	    }

	    if (state.currentDepth > cacheDepth) {
	        final Float hit = state.transpositionTable.get(board);
	        if(hit != null) {
	            return hit;
	        }
	    }

	    float best = 0.0f;
	    state.currentDepth++;
	    for (int move = 0 ; move < 4 ; move++) {
	        long newboard = executeMove(move, board);
	        if (board != newboard) {
	            best = Math.max(best, scoreTileChooseNode(state, newboard, cprob));
	        }
	    }
	    state.currentDepth--;
	    if (state.currentDepth > cacheDepth) {
	        state.transpositionTable.put(board, best);
	    }

	    return best;
	}
	
	private float scoreMove(EvalState state, long board, int move) {
	    long newboard = executeMove(move, board);
	    if (board == newboard) {
	        return -1000000;
	    }
	    state.probThreshold = probability;
	    return (float) (scoreTileChooseNode(state, newboard, 1.0f) + 1e-6);
	}

	private float scoreMoveTopLevel(long board, int move) {
	    EvalState state = new EvalState();
	    return scoreMove(state, board, move);
	}

	/* Find the best move for a given board. */
	private int bestMove(long board) {
	    float best = 0;
	    int bestmove = -1;
	    for (int move = 0 ; move < 4 ; move++) {
	        float res = scoreMoveTopLevel(board, move);
	        if(res > best) {
	            best = res;
	            bestmove = move;
	        }
	    }
	    return bestmove;
	}

	
	private class EvalState {
		private final Map<Long, Float> transpositionTable = new HashMap<Long, Float>();
		private float probThreshold;
		private int maxDepth;
		private int currentDepth;
	}
	
	private class FEPanel extends JPanel {
		private static final long serialVersionUID = 4875042479359355572L;
		
		public FEPanel () {
			setFocusable(false);
			setLayout(new BoxLayout(FEPanel.this, BoxLayout.Y_AXIS));
			Vector<String> names = new Vector<String>();
			for(Evaluator e : Controller.evaluators) {
				names.add(e.getClass().getSimpleName());
			}
			names.add(0,"Internal");
			add(new ComboPanel("Evaluator", names, FastExpectimax.this), 0);
			
			names = new Vector<String>();
			for(int i = 1 ; i < 15 ; i++) {
				names.add(""+i);
			}
			add(new ComboPanel("Search Depth", names, FastExpectimax.this), 1);
			
			names.add(0,"0");
			add(new ComboPanel("Cache Depth", names, FastExpectimax.this), 2);
			
			
		}
	}

	@Override
	public void comboChanged(String id, int index) {
		switch(id) {
		case "Evaluator":
			if(index == 0) {
				evaluator = null;
			} else {
				evaluator = Controller.evaluators.get(index-1);
			}
			break;
		case "Search Depth":
			depth = index + 1;
			break;
		case "Cache Depth":
			depth = index;
			break;
		default:	
		}
	}
	@Override
	public JPanel getPlayerPanel() {
		return panel;
	}
}

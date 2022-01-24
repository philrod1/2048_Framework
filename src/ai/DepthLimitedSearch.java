package ai;

import java.awt.Graphics;
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

public class DepthLimitedSearch extends AbstractPlayer implements ComboObserver {

	private int iterations = 1;
	private int depth = 1;
	private Evaluator evaluator = Controller.evaluators.get(0);
	
	public DepthLimitedSearch () {
		panel = new ADLSPanel();
	}
	
//	public DepthLimitedSearch (int iterations, int depth, Evaluator evaluator) {
//		super();
//		this.iterations = iterations;
//		this.depth = depth;
//		this.evaluator = evaluator;
//	}

	@Override
	public MOVE getMove(State game) {
		double bestResult = Double.NEGATIVE_INFINITY;
		MOVE bestMove = null;
		
		Map<MOVE, Long> options = LongInfo.expand(game.toLong());
		
		for (MOVE move : options.keySet()) {
			double result = 0;
			for (int i = 0 ; i < iterations ; i++) {
				result += dls(options.get(move), depth, i);
			}
			if (result/iterations > bestResult) {
				bestResult = result/iterations;
				bestMove = move;
			}
		}
		if (bestMove == null) {
			for (MOVE move : options.keySet()) {
				long op = options.get(move);
				double eval = 0;
				eval = evaluator.evaluate(op);
				if(eval > bestResult) {
					bestResult = eval;
					bestMove = move;
				}
			}
			if (bestMove == null) {
				return options.keySet().iterator().next();
			}
		}
		return bestMove;
	}

	public double dls(long state, int depth, int i) {
		if(depth == 0) {
			return evaluator.evaluate(state);
		}
		long child = LongInfo.addRandomTile(state);
		double bestResult = Double.NEGATIVE_INFINITY;
		if(LongInfo.gameOver(child)) return bestResult;
		Map<MOVE, Long> options = LongInfo.expand(child);
		for(MOVE move : options.keySet()) {
			double result = dls(options.get(move), depth - 1, i);
			bestResult = Math.max(bestResult, result);
		}
		return bestResult;
	}
	


	@Override
	public void comboChanged(String id, int index) {
		switch(id) {
		case "Evaluator":
			evaluator = Controller.evaluators.get(index);
			break;
		case "Search Depth":
			depth = index + 1;
			break;
		case "Iterations":
			iterations = 1 << index;
			break;
		default:	
		}
	}
	
	private class ADLSPanel extends JPanel {
		private static final long serialVersionUID = 4875042479359355572L;
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
		}
		
		public ADLSPanel () {
			setFocusable(false);
			setLayout(new BoxLayout(ADLSPanel.this, BoxLayout.Y_AXIS));
			Vector<String> names = new Vector<String>();
			for(Evaluator e : Controller.evaluators) {
				names.add(e.getClass().getSimpleName());
			}
			add(new ComboPanel("Evaluator", names, DepthLimitedSearch.this), 0);
			
			names = new Vector<String>();
			for(int i = 1 ; i < 15 ; i++) {
				names.add(""+i);
			}
			add(new ComboPanel("Search Depth", names, DepthLimitedSearch.this), 1);
						
			names = new Vector<String>();
			for(int i = 1 ; i < 1025 ; i *= 2) {
				names.add(""+i);
			}
			add(new ComboPanel("Iterations", names, DepthLimitedSearch.this), 2);
		}
	}

}

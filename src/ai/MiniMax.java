package ai;

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

public class MiniMax extends AbstractPlayer implements ComboObserver {
	
	private int depth = 1;
	private final JPanel panel = new MiniMaxPanel();
	private Evaluator evaluator = Controller.evaluators.get(0);
	
	@Override
	public JPanel getPlayerPanel() {
		return panel;
	}

	@Override
	public MOVE getMove(State game) {
		double bestResult = Double.NEGATIVE_INFINITY;
		MOVE bestMove = null;
		Map<MOVE, Long> options = LongInfo.expand(game.toLong());
		if(options.size() == 1) {
			return options.keySet().iterator().next();
		}
		for(MOVE move : options.keySet()) {
			double result = min(options.get(move), -10000, 10000, depth);
//			System.out.println(move + " = " + result);
			if(result > bestResult) {
				bestResult = result;
				bestMove = move;
			}
		}
		if(bestMove == null) {
			System.out.println("Options: " + options.size());
		}
		return bestMove;
	}

	private double min(long state, double a, double b, int d) {
		for (long child : LongInfo.expandMIN(state)) {
			b = Math.min(max(child, a, b, d-1), b);
			if (a >= b)
				return b;
		}
		return b;
	}

	private double max(long state, double a, double b, int d) {
		if(d<=0 || LongInfo.gameOver(state)) {
			return evaluator.evaluate(state);
		}
		for (long child : LongInfo.expand2(state)) {
			a = Math.max(a, min(child, a, b, d));
			if (a >= b)
				return a;
		}
		return a;
	}

	@Override
	public void comboChanged(String id, int index) {
		switch(id) {
		case "Evaluator":
			evaluator = Controller.evaluators.get(index);
			break;
		case "Max Depth":
			depth = index + 1;
			break;
		default:	
		}
	}
	
	private class MiniMaxPanel extends JPanel {
		private static final long serialVersionUID = 4875042479359355572L;

		
		public MiniMaxPanel () {
			setFocusable(false);
			setLayout(new BoxLayout(MiniMaxPanel.this, BoxLayout.Y_AXIS));
			Vector<String> names = new Vector<String>();
			for(Evaluator e : Controller.evaluators) {
				names.add(e.getClass().getSimpleName());
			}
			add(new ComboPanel("Evaluator", names, MiniMax.this), 0);
			
			names = new Vector<String>();
			for(int i = 1 ; i < 11 ; i++) {
				names.add(""+i);
			}
			add(new ComboPanel("Max Depth", names, MiniMax.this), 1);
			
			
		}
	}

}

package ai;

import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.AbstractState.MOVE;
import model.State;
import view.ComboObserver;
import view.ComboPanel;
import controller.Controller;
import eval.Evaluator;

public class SimpleAI extends AbstractPlayer implements ComboObserver {
	
	private final JPanel panel = new SimpleAIPanel();
	private Evaluator eval = Controller.evaluators.get(0);
	private int time = 1;

	@Override
	public MOVE getMove(State game) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MOVE bestMove = null;
		double bestScore = -1000000.0;
		for(MOVE move : game.getMoves()) {
			game.move(move);
			double score = eval.evaluate(game.toLong());
			if(score > bestScore) {
				bestScore = score;
				bestMove = move;
			}
			game.undo();
		}
		return bestMove;
	}
	
	@Override
	public JPanel getPlayerPanel() {
		return panel;
	}
	
	@Override
	public void comboChanged(String id, int index) {
		switch(id) {
		case "Evaluator":
			eval = Controller.evaluators.get(index);
			break;
		case "Delay (ms)":
			time = 1 << index;
			break;
		default:	
		}
	}

	private class SimpleAIPanel extends JPanel {
		private static final long serialVersionUID = 4875042479359355572L;
		
		public SimpleAIPanel () {
			setFocusable(false);
			setLayout(new BoxLayout(SimpleAIPanel.this, BoxLayout.Y_AXIS));
			Vector<String> names = new Vector<String>();
			for(Evaluator e : Controller.evaluators) {
				names.add(e.getClass().getSimpleName());
			}
			add(new ComboPanel("Evaluator", names, SimpleAI.this), 0);
			
			names = new Vector<String>();
			for(int i = 0 ; i < 20 ; i++) {
				names.add(""+(1<<i));
			}
			add(new ComboPanel("Delay (ms)", names, SimpleAI.this), 1);
		}
	}
}

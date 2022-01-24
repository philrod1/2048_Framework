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

public class HalfStep extends AbstractPlayer implements ComboObserver {
	
	private Evaluator evaluator = Controller.evaluators.get(0);
	private long delay = 0;
	
	public HalfStep () {
		panel = new HalfStepPanel();
	}

	@Override
	public MOVE getMove(State game) {
		// Slow the player down - it's way too fast to watch.
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
		}

		long state = game.toLong();

		Map<MOVE, Long> halfSteps = LongInfo.expand(state);
		double bestVal = 0;

		MOVE bestMove = halfSteps.keySet().iterator().next();

		// For each half-step move, convert from my long representation to
		// your double[] representation and find the best learned value
		for (MOVE move : halfSteps.keySet()) {
			long s2 = halfSteps.get(move);
			double val = evaluator.evaluate(s2);
			if (val > bestVal) {
				bestVal = val;
				bestMove = move;
			}
		}

		return bestMove;
	}
	
	@Override
	public void comboChanged(String id, int index) {
		switch(id) {
		case "Evaluator":
			evaluator = Controller.evaluators.get(index);
			break;
		case "Delay (ms)":
			delay = 1 << index;
			break;
		default:	
		}
	}

	private class HalfStepPanel extends JPanel {
		private static final long serialVersionUID = 4875042479359355572L;
		
		public HalfStepPanel () {
			setFocusable(false);
			setLayout(new BoxLayout(HalfStepPanel.this, BoxLayout.Y_AXIS));
			Vector<String> names = new Vector<String>();for(Evaluator e : Controller.evaluators) {
				names.add(e.getClass().getSimpleName());
			}
			add(new ComboPanel("Evaluator", names, HalfStep.this), 0);
			names = new Vector<String>();
			names.add("0");
			for(int i = 0 ; i < 20 ; i++) {
				names.add(""+(1<<i));
			}
			add(new ComboPanel("Delay (ms)", names, HalfStep.this), 1);
		}
	}

}

package ai;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;

import model.AbstractState.MOVE;
import model.State;
import view.ComboObserver;
import view.ComboPanel;

public class RandomAI implements ComboObserver, Player {

	private Random rng = new Random();
	private int time = 1;
	private final JPanel panel = new RandomAIPanel();

	@Override
	public MOVE getMove(State game) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<MOVE> moves = game.getMoves();
		return moves.get(rng .nextInt(moves.size()));
	}
	
	@Override
	public JPanel getPlayerPanel() {
		return panel ;
	}
	
	@Override
	public void comboChanged(String id, int index) {
		switch(id) {
		case "Delay (ms)":
			time = 1 << index;
			break;
		default:	
		}
	}

	private class RandomAIPanel extends JPanel {
		private static final long serialVersionUID = 4875042479359355572L;
		
		public RandomAIPanel () {
			setFocusable(false);
			setLayout(new BorderLayout());
			Vector<String> names = new Vector<String>();
			names = new Vector<String>();
			for(int i = 0 ; i < 20 ; i++) {
				names.add(""+(1<<i));
			}
			add(new ComboPanel("Delay (ms)", names, RandomAI.this), BorderLayout.CENTER);
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

}

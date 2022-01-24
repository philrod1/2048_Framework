package ai;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.AbstractState.MOVE;
import model.BinaryState;
import model.State;
import view.ComboObserver;
import view.ComboPanel;
import controller.Controller;
import eval.Evaluator;

public class MCTSPlayer extends AbstractPlayer implements ComboObserver {

	private int time;
	private final JPanel panel = new MCTSPanel();
	private Evaluator eval = Controller.evaluators.get(0);
	private final Random r = new Random();
	private final double epsilon = 1e-6;
	private State game;

	private double fudge = 5000;

	public MCTSPlayer () {
		time = 64;
	}
	
	@Override
	public JPanel getPlayerPanel() {
		return panel;
	}

	@Override
	public MOVE getMove(State state) {
		game = new BinaryState(state.toLong(), state.getScore());
		
		List<MOVE> moves = game.getMoves();
		if(moves.size() == 1) {
			return moves.get(0);
		}
		
		TreeNode root = new TreeNode(null, 0);
		long stop = System.currentTimeMillis() + time;
		
		while(System.currentTimeMillis() < stop) {
			root.selectAction();
		}
		
		double bestValue = Double.NEGATIVE_INFINITY;
		
		MOVE bestMove = null;
		for (TreeNode child : root.children) {
			double value = child.reward / child.nVisits;
//			System.out.println(child.move + " : " + value + " (" + child.nVisits + ")");
			if (value > bestValue) {
				bestValue = value;
				bestMove = child.move;
			}
		}

//		System.out.println("----------------------------------------------");
		return bestMove;
	}

	private class TreeNode {
		private final MOVE move;

		private TreeNode[] children;
		private int nVisits;
		private double reward;
		
		private int depth;

		private TreeNode(MOVE move, int depth) {
			this.move = move;
			reward = 0.0;
			nVisits = 0;
			this.depth = depth;
		}

		public void selectAction() {
			List<TreeNode> visited = new LinkedList<TreeNode>();
			TreeNode cur = this;
			
			while (!cur.isLeaf()) {
				cur = cur.select();
				game.move(cur.move);
				visited.add(cur);
			}
			
			if (game.getMoves().isEmpty()) {
				reward = eval.evaluate(game.toLong()) / fudge;
			} else {
				cur.expand();
				TreeNode newNode = cur.select();
				game.move(newNode.move);
				visited.add(newNode);
				reward = rollOut();
			}

			for (TreeNode node : visited) {
				game.undo();
				node.updateStats(reward);
			}
			
			this.updateStats(reward);
		}

		public void expand() {
			List<MOVE> moves = game.getMoves();
			if (moves.size() == 0) {
				System.err.println("Error expanding " + moves.size() + " children.");
			}
			children = new TreeNode[moves.size()];
			int i = 0;
			for (MOVE m : moves) {
				children[i++] = new TreeNode(m, depth+1);
				children[i-1].updateStats(children[i-1].rollOut());
			}
		}

		private TreeNode select() {
			TreeNode selected = children[0];
			double bestValue = Double.NEGATIVE_INFINITY;
			if (children.length == 0) {
				System.err.println("No children to select.");
			}
			for (TreeNode c : children) {
				
				double uctValue = c.reward / (c.nVisits + epsilon)
						+ Math.sqrt(Math.log(nVisits + 1) / (c.nVisits + epsilon)) 
						+ r.nextDouble() * epsilon;
				
				if (uctValue > bestValue) {
					selected = c;
					bestValue = uctValue;
				}
			}
			return selected;
		}

		public boolean isLeaf() {
			return children == null;
		}

		public double rollOut() {
			return eval.evaluate(game.toLong())/fudge;
		}

		public void updateStats(double reward) {
			nVisits++;
			this.reward += reward;
		}
		
		@Override
		public String toString() {
			return depth + " " + nVisits + " " + move;
		}
	}

	@Override
	public void comboChanged(String id, int index) {
		switch(id) {
		case "Default Policy":
			eval = Controller.evaluators.get(index);
			break;
		case "Time (ms)":
			time = 1 << index;
			break;
		default:	
		}
	}
	
	private class MCTSPanel extends JPanel {
		private static final long serialVersionUID = 4875042479359355572L;

		
		public MCTSPanel () {
			setFocusable(false);
			setLayout(new BoxLayout(MCTSPanel.this, BoxLayout.Y_AXIS));
			Vector<String> names = new Vector<String>();
			for(Evaluator e : Controller.evaluators) {
				names.add(e.getClass().getSimpleName());
			}
			add(new ComboPanel("Default Policy", names, MCTSPlayer.this), 0);
			
			names = new Vector<String>();
			for(int i = 0 ; i < 20 ; i++) {
				names.add(""+(1<<i));
			}
			add(new ComboPanel("Time (ms)", names, MCTSPlayer.this), 1);
			
			
		}
	}
	
	
}

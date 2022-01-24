package ai;

import java.util.List;

import static model.AbstractState.MOVE.*;
import model.AbstractState.MOVE;
import model.State;

public class UpPlayer extends AbstractPlayer {

	private boolean left = true;
	private MOVE move = UP;
	
	@Override
	public MOVE getMove(State game) {
		List<MOVE> moves = game.getMoves();
		if(moves.size()==1) {
			return moves.get(0);
		}
		if(move != UP && moves.contains(UP)) {
			move = UP;
			return UP;
		}
		if(left) {
			if(moves.contains(LEFT)) {
				move = LEFT;
				return LEFT;
			} else {
				left = false;
				move = RIGHT;
				return RIGHT;
			}
		} else {
			if(moves.contains(RIGHT)) {
				move = RIGHT;
				return RIGHT;
			} else {
				left = true;
				move = LEFT;
				return LEFT;
			}
		}
				
	}

}

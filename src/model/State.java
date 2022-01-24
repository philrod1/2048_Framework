package model;

import java.awt.Point;
import java.util.List;

import model.AbstractState.MOVE;
import eval.Evaluator;


public interface State {
	List<MOVE> getMoves();
	StateData[] nextStates();
	void reset();
	int  getScore();
	void move(MOVE dir);
	int  getValue(int x, int y);
	long toLong();
	void fromLong(long data);
	StateData toStateData();
	void fromStateData(StateData stateData);
	void undo();
	void redo();
	double rollout();
	/**
	 * This method is used to test if a move is possible.  If the
	 * result of a move is that the states are equal, that move is
	 * not legal.  As such, it doesn't need to compare scores.
	 * @param that: A child of 'this::State'
	 * @return true if the board hasn't changed, false otherwise.
	 */
	boolean equals(State that);
	int getHighestTileValue();
	Point getNewTilePosition();
	Evaluator getEval();
	void setEval(Evaluator eval);
}

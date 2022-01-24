package eval;

import data.LongInfo;

public class ScoreEvaluator implements Evaluator {

	@Override
	public double evaluate(long state) {
		return LongInfo.approxScore(state);
	}

}

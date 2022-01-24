package eval;

import data.LongInfo;

public class Rollout implements Evaluator {

	@Override
	public double evaluate(long state) {
		return LongInfo.rollout(state);
	}

}

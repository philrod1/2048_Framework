package eval;

import data.LongInfo;

public class GuidedRollout implements Evaluator {

	@Override
	public double evaluate(long state) {
		return LongInfo.guidedRollout(state);
	}

}

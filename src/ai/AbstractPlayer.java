package ai;

import javax.swing.JPanel;

public abstract class AbstractPlayer implements Player {

	protected JPanel panel = new JPanel();
	@Override
	public void reset(){}
	@Override
	public JPanel getPlayerPanel() {
		return panel;
	}

}

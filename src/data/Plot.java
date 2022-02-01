package data;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class Plot extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4867596383274947424L;
	private final int maxX;
	private final int maxY = 500;
	private BufferedImage img = null;
	private final int opacity = 255;
	private final Color[] colours = new Color[] {
		new Color(0, 0, 0, opacity),		// 0
		new Color(255, 0, 0, opacity),		// 2
		new Color(255, 0, 221, opacity),	// 4
		new Color(102, 0, 255, opacity),	// 8
		new Color(255, 94, 0, opacity),	// 16
		new Color(195, 0, 255, opacity),	// 32
		new Color(0, 255, 144, opacity),	// 64
		new Color(255, 187, 0, opacity),	// 128
		new Color(8, 0, 255, opacity),		// 256
		new Color(230, 255, 0, opacity),	// 512
		new Color(0, 178, 255, opacity),	// 1024
		new Color(136, 255, 0, opacity),	// 2048
		new Color(0, 85, 255, opacity),	// 4096
		new Color(0, 255, 238, opacity),	// 8192
		new Color(255, 55, 51, opacity),	// 16384
		new Color(42, 255, 0, opacity),	// 32768
		new Color(55, 145, 228, opacity)	// 65536
	};
	
	private final int steps = 1000;
	private int i = 0;
	
	private int count = 0;
	private final Tuple[] tuples = new Tuple[steps];
	
	public Plot(int nGames, int width, int height) {
		maxX = nGames;
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0 ; x < width ; x++) {
			for (int y = 0 ; y < height ; y++) {
				img.setRGB(x, y, colours[0].getRGB());
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		double scaleX = (double)getWidth() / maxX;
		double scaleY = (double)getHeight() / 200;
		
		for (int i = 0 ; i < steps ; i++) {
			
			Tuple t = tuples[i];
			
			if (t == null) {
				g2.drawImage(img, 0, 0, null);
				g2.dispose();
				g.dispose();
				return;
			}
			int x = (int)(t.i * scaleX);
			System.out.println(t);
			System.out.println(Math.log(t.a) + " ... " + Math.sqrt(t.a));
			int y = (int) (((Math.log(t.s) * Math.log(t.s)) * scaleY));
			try {
				img.setRGB(x, getHeight() - y, colours[t.h].getRGB());
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Bounds: " + getWidth() + ", " + getHeight());
				System.out.println("Point " + x + ", " + y);
				e.printStackTrace();

			}
			
			x = (int)(t.i * scaleX);
			y = getHeight() - ((int) (((Math.log(t.a) * Math.log(t.a)) * scaleY)));
			int c = Color.YELLOW.getRGB();
			img.setRGB(x, y, c);
			img.setRGB(x+1, y, c);
			img.setRGB(x, y+1, c);
			img.setRGB(x+1, y+1, c);
			
		}
		g2.drawImage(img, 0, 0, null);
		g2.dispose();
		g.dispose();
	}
	public void addPoint(int score, int average, int highTile) {
		tuples[count % steps] = new Tuple(i++, highTile, score, average);
		count++;
		if (count % steps == 0) {
			repaint();
		}
	}
	
	private static class Tuple {
		final int i, h, s, a;
		public Tuple (int i, int h, int s, int a) {
			this.i = i;
			this.h = h;
			this.s = s;
			this.a = a;
		}
		public String toString() {
			return i + " " + h + " " + s + " " + a;
		}
	}
}
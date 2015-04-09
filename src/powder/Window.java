package powder;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

public class Window extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static Window window = new Window();
	public static void main(String[] args) {
		window.setVisible(true);
	}
	
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setTitle("Powder Game");
		setResizable(false);
		
		Display game = new Display();
		game.setSize(Display.width, Display.height);
		add(game, BorderLayout.CENTER);
		
		resize();
	}
	
	public void resize() {
		getContentPane().setPreferredSize(new Dimension(Display.width*Display.img_scale, Display.height*Display.img_scale));
		pack();
	}
}

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
	
	public Display game;
	public SideMenu menu;
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setTitle("Powder Game");
		setResizable(false);
		
		game = new Display();
		add(game, BorderLayout.CENTER);
		
		menu = new SideMenu();
		menu.setPreferredSize(new Dimension(menu.width, menu.height));
		add(menu, BorderLayout.EAST);
		
		resize();
	}
	
	public void resize() {
		getContentPane().setPreferredSize(new Dimension(Display.width*Display.img_scale + menu.width, Display.height*Display.img_scale));
		pack();
	}
}

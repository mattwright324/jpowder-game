package main.java.powder;

import javax.swing.*;

import java.awt.*;

public class Window extends JFrame {
	
	public static void main(String[] args) {
        Window.window.setVisible(true);
    }
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static Window window = new Window();
	
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

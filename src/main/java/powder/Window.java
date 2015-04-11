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
	public BottomMenu menub;
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setTitle("JPowder");
		setResizable(false);
		
		game = new Display();
		add(game, BorderLayout.CENTER);
		
		menu = new SideMenu();
		menu.setPreferredSize(new Dimension(SideMenu.width, SideMenu.height));
		add(menu, BorderLayout.EAST);
		
		menub = new BottomMenu();
		menub.setPreferredSize(new Dimension(BottomMenu.width, BottomMenu.height));
		add(menub, BorderLayout.SOUTH);
		
		resize();
	}
	
	public void resize() {
		getContentPane().setPreferredSize(new Dimension(Display.width*Display.img_scale + SideMenu.width, Display.height*Display.img_scale + BottomMenu.height));
		pack();
	}
}

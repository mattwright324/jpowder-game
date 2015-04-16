package main.java.powder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Window extends JFrame {
	
	public static BufferedImage heatColorStrip, iconImg;
	public static Window window = new Window();
	
	public static Point mouse = new Point(0,0);
	
	static Display game;
	static SideMenu menu;
	static BottomMenu menub;
	
	public static void main(String[] args) {
		try {
			// Gradle knows best
			heatColorStrip = ImageIO.read(ClassLoader.getSystemResourceAsStream("img/powder/colorstrip.png"));
		} catch (IOException e) {}
		try {
			iconImg = ImageIO.read(ClassLoader.getSystemResourceAsStream("img/powder/jpowder.png"));
			window.setIconImage(iconImg);
		} catch(IOException e) {}
    }
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setTitle("JPowder");
		setResizable(false);
		setVisible(true);
		
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
	
	public static void updateMouseInFrame(Point p, Component c) {
		SwingUtilities.convertPointToScreen(p, c);
		SwingUtilities.convertPointFromScreen(p, window);
		mouse = p;
		menub.repaint();
	}
	
}

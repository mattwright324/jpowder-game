package mattw.powder.old;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Deprecated
public class Window extends JFrame {
	
	public static BufferedImage heatColorStrip;
	public static Window window = new Window();
	
	public static Point mouse = new Point(0,0);
	
	static Display game;
	static SideMenu menu;
	static BottomMenu menub;
	
	public static void main(String[] args) {
		try {
			heatColorStrip = ImageIO.read(ClassLoader.getSystemResourceAsStream("mattw/powder/img/colorstrip.png"));
		} catch (IOException e) {}
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
		try {
			setIconImage(ImageIO.read(ClassLoader.getSystemResourceAsStream("mattw/powder/old/img/jpowder1.png")));
		} catch (IOException e) {}
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
		// getContentPane().setPreferredSize(new Dimension(Display.width*Display.img_scale + SideMenu.width, Display.height*Display.img_scale + BottomMenu.height));
		pack();
	}
	
	public static void updateMouseInFrame(Point p, Component c) {
		SwingUtilities.convertPointToScreen(p, c);
		SwingUtilities.convertPointFromScreen(p, window);
		mouse = p;
		menub.repaint();
	}
	
}

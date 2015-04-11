package main.java.powder;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import main.java.powder.elements.Element;

public class BottomMenu extends JPanel implements ActionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	static int width = Display.width + SideMenu.width;
	static int height = 50;
	
	public Graphics2D g2d;
	
	public BottomMenu() {
		setFocusable(true);
		addMouseListener(this);
	}
	
	public Rectangle clear = new Rectangle(5, 5, 40, 40);
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		
		g2d.setPaint(new GradientPaint(0,0, Color.BLACK, 0, height, Color.WHITE));
		g2d.fillRect(0, 0, Display.width*2, height);
		
		g2d.setPaint(new GradientPaint(0,0, Color.RED, 0, height, Color.WHITE));
		g2d.fillRect(Display.width*2, 0, Display.width*2 + width, height);
		
		g2d.setColor(Color.GRAY);
		g2d.draw(clear);
		g2d.setColor(Color.WHITE);
		g2d.drawString("NEW", 10, 30);
	}
	
	public void mouseClicked(MouseEvent e) {
		if(clear.contains(e.getPoint())) {
			Cells.setAllOfAs(-1, Element.none);
		}
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}
	
	public void mousePressed(MouseEvent e) {
		
	}

	public void mouseReleased(MouseEvent e) {
		
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}
	
}

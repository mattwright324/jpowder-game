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
	public Rectangle resize = new Rectangle(5+45, 5, 40, 40);
	public Rectangle pause = new Rectangle(5+90, 5, 40, 40);
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		
		g2d.setPaint(new GradientPaint(0,0, Color.BLACK, 0, height, Color.WHITE));
		g2d.fillRect(0, 0, Display.width*2 + SideMenu.width, height);
		
		g2d.setColor(Color.GRAY);
		g2d.draw(clear);
		g2d.draw(resize);
		g2d.draw(pause);
		g2d.setColor(Color.WHITE);
		g2d.drawString("NEW", clear.x+5, 30);
		g2d.drawString("SIZE", resize.x+5, 30);
		g2d.fillRect(pause.x+12, pause.y+5, 5, pause.height-9);
		g2d.fillRect(pause.x+22, pause.y+5, 5, pause.height-9);
	}
	
	public void mouseClicked(MouseEvent e) {
		if(clear.contains(e.getPoint())) Cells.setAllOfAs(-1, Element.none);
		if(resize.contains(e.getPoint())) Display.toggle_size();
		if(pause.contains(e.getPoint())) Display.toggle_pause();
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

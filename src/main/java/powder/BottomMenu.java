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
	
	public int b_w = 40;
	public int b_h = 18;
	public int b_y = height-b_h-5;
	public int b_txt_center = b_y+b_h/2+5;
	
	public Rectangle clear = new Rectangle(5, b_y, b_w, b_h);
	public Rectangle resize = new Rectangle(5+45, b_y, b_w, b_h);
	public Rectangle pause = new Rectangle(5+90, b_y, b_w, b_h);
	public Rectangle view = new Rectangle(5+135, b_y, b_w, b_h);
	
	// Try to display elements in bottom bar and have categories on the right.
	public Rectangle el_test = new Rectangle(5, 5, b_w, b_h);
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		
		g2d.setPaint(new GradientPaint(0,0, Color.BLACK, 0, height, Color.WHITE));
		g2d.fillRect(0, 0, Display.width*2 + SideMenu.width, height);
		
		g2d.setColor(new Color(32,64,128,128));
		g2d.fill(clear);
		g2d.fill(resize);
		g2d.fill(pause);
		g2d.fill(view);
		g2d.fill(el_test);
		g2d.setColor(Color.WHITE);
		g2d.drawString("NEW", clear.x+5, b_txt_center);
		g2d.drawString("SIZE", resize.x+5, b_txt_center);
		g2d.fillRect(pause.x+12, pause.y+5, 5, pause.height-9);
		g2d.fillRect(pause.x+22, pause.y+5, 5, pause.height-9);
		g2d.drawString("VIEW", view.x+5, b_txt_center);
	}
	
	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}
	
	public void mousePressed(MouseEvent e) {
		if(clear.contains(e.getPoint())) {
			Game.paused = true;
			Cells.setAllOfAs(-1, Element.none);
		}
		if(resize.contains(e.getPoint())) Display.toggle_size();
		if(pause.contains(e.getPoint())) Display.toggle_pause();
		if(view.contains(e.getPoint())) if(Display.view == 0) Display.setView(1); else Display.setView(0); 
	}

	public void mouseReleased(MouseEvent e) {
		
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}
	
}

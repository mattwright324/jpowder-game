package main.java.powder;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import main.java.powder.elements.Element;

public class BottomMenu extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
	
	private static final long serialVersionUID = 1L;
	
	static int width = Display.width + SideMenu.width;
	static int height = 50;
	
	public Graphics2D g2d;
	
	public BottomMenu() {
		setFocusable(true);
		addMouseListener(this);
		addMouseMotionListener(this);
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
	
	public List<Button> buttons = new ArrayList<Button>();
	
	public Point mouse = new Point(0,0);
	
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
		makeButtons();
		draw_buttons();
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
		else if(resize.contains(e.getPoint())) Display.toggle_size();
		else if(pause.contains(e.getPoint())) Display.toggle_pause();
		else if(view.contains(e.getPoint())) if(Display.view == 0) Display.setView(1); else Display.setView(0); {
			for(Button b : buttons) {
				if(b.contains(e.getPoint())) {
					if(SwingUtilities.isLeftMouseButton(e))
						Display.left = b.el;
					if(SwingUtilities.isRightMouseButton(e))
						Display.right = b.el;
				}
			}
		}
	}
	
	public void makeButtons() {
		// Could switch this out with element categories in SideMenu; SideMenu.getCategory() where returns Element[]
		buttons.clear();
		int i=0;
		for(Element e : SideMenu.selected) {
			Button b = new Button(getWidth()-b_w-(5+(b_w+5)*i++)+(getWidth()-mouse.x-(getWidth()/2)), 5, b_w, b_h);
			b.setElement(e);
			buttons.add(b);
		}
	}
	
	public void draw_buttons() {
		for(Button b : buttons) {
			Color c = b.el.getColor();
			g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
			g2d.setPaintMode();
			g2d.fill(b);
			g2d.setColor(Color.WHITE);
			g2d.drawString(b.el.shortName, b.x+2, b.y+b_h/2+5);
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}
	
	public class Button extends Rectangle {
		private static final long serialVersionUID = 1L;
		public Button(int x, int y, int w, int h) {
			super(x, y, w, h);
		}
		public void setElement(Element e) {
			el = e;
		}
		public Element el;
	}
	
	public void mouseDragged(MouseEvent e) {
		mouse = e.getPoint();
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		mouse = e.getPoint();
		repaint();
	}
}

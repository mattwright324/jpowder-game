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
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import main.java.powder.elements.Element;
import main.java.powder.walls.Wall;

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
	public Rectangle resize = new Rectangle(5+(b_w+5)*1, b_y, b_w, b_h);
	public Rectangle pause = new Rectangle(5+(b_w+5)*2, b_y, b_w, b_h);
	public Rectangle view = new Rectangle(5+(b_w+5)*3, b_y, b_w, b_h);
	public Rectangle help = new Rectangle(5+(b_w+5)*4, b_y, b_w, b_h);
	
	public JFrame helpWindow = new JFrame();
	
	public List<Button> buttons = new ArrayList<Button>();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		
		g2d.setPaint(new GradientPaint(0,0, Color.BLACK, 0, height, Color.WHITE));
		g2d.fillRect(0, 0, Display.width*2 + SideMenu.width, height);
		
		g2d.setColor(new Color(32,64,128,128));
		g2d.fill(clear);
		g2d.fill(resize);
		g2d.fill(view);
		g2d.fill(help);
		if(Game.paused) g2d.setColor(new Color(255,64,128,128));
		g2d.fill(pause);
		makeButtons();
		draw_buttons();
		g2d.setColor(Color.WHITE);
		g2d.drawString("NEW", clear.x+5, b_txt_center);
		g2d.drawString("SIZE", resize.x+5, b_txt_center);
		g2d.fillRect(pause.x+12, pause.y+5, 5, pause.height-9);
		g2d.fillRect(pause.x+22, pause.y+5, 5, pause.height-9);
		g2d.drawString("VIEW", view.x+5, b_txt_center);
		g2d.drawString("KEYS", help.x+5, b_txt_center);
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
			//Cells.clearScreen();
		}
		if(resize.contains(e.getPoint())) Display.toggle_size();
		if(pause.contains(e.getPoint())) Display.toggle_pause();
		if(view.contains(e.getPoint())) if(Display.view == 0) Display.setView(1); else Display.setView(0); {
			for(Button b : buttons) {
				if(b.contains(e.getPoint())) {
					if(SwingUtilities.isLeftMouseButton(e))
						Display.left = b.item;
					if(SwingUtilities.isRightMouseButton(e))
						Display.right = b.item;
				}
			}
		}
		if(help.contains(e.getPoint())) {
			Display.help = !Display.help;
		}
	}
	
	public void makeButtons() {
		buttons.clear();
		int i=0;
		for(Item e : SideMenu.selected) {
			int x = Window.mouse.x;
			if(x > Window.window.getWidth()/2) x = Window.window.getWidth()/2;
			Button b = new Button(getWidth()-b_w-(5+(b_w+5)*i++)+(getWidth()-x-(getWidth()/2)), 5, b_w, b_h);
			b.setItem(e);
			buttons.add(b);
		}
	}
	
	public void draw_buttons() {
		for(Button b : buttons) {
			Color c = Color.GRAY;
			if(b.item instanceof Element) c = ((Element) b.item).getColor();
			if(b.item instanceof Wall) c = ((Wall) b.item).color;
			g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
			g2d.setPaintMode();
			g2d.fill(b);
			g2d.setColor(Color.WHITE);
			g2d.drawString(b.item.name, b.x+2, b.y+b_h/2+5);
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}
	
	public class Button extends Rectangle {
		private static final long serialVersionUID = 1L;
		public Item item;
		
		public Button(int x, int y, int w, int h) {
			super(x, y, w, h);
		}
		
		public void setItem(Item i) {
			item = i;
		}
	}
	
	public void mouseDragged(MouseEvent e) {
		Window.updateMouseInFrame(e.getPoint(), this);
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		Window.updateMouseInFrame(e.getPoint(), this);
		repaint();
	}
}

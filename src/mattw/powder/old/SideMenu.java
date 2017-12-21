package mattw.powder.old;

import mattw.powder.old.elements.Elements;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public class SideMenu extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
	
	private static final long serialVersionUID = 1L;
	
	static int width = 60;
	static int height = Display.height;
	
	public BufferedImage img;
	public Graphics2D g2d;
	public Graphics2D b2d;
	
	public boolean init = true;
	
	public SideMenu() {
		setFocusable(true);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void init() {
		init = false;
		img = new BufferedImage(width, height*2, BufferedImage.TYPE_INT_ARGB);
		b2d = img.createGraphics();
	}
	
	public int b_h = 40;
	public int b_w = width-10;
	public int b_txt = b_h/2;
	public int b_txtn = b_h/2+15;
	
	public Rectangle sl = new Rectangle(5, 5, b_w, b_h);
	public Rectangle ll = new Rectangle(5, 5+(b_h+5)*1, b_w, b_h);
	public Rectangle gl = new Rectangle(5, 5+(b_h+5)*2, b_w, b_h);
	public Rectangle pl = new Rectangle(5, 5+(b_h+5)*3, b_w, b_h);
	public Rectangle ral = new Rectangle(5, 5+(b_h+5)*4, b_w, b_h);
	public Rectangle tl = new Rectangle(5, 5+(b_h+5)*5, b_w, b_h);
	
	static Item[] selected = Elements.solid;
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		if(init) init();
		
		b2d.setPaint(new GradientPaint(0, 0, Color.BLACK, width, 0, Color.WHITE));
		b2d.fillRect(0, 0, width, height*2);
		
		b2d.setColor(new Color(128, 128, 128, 128));
		b2d.fill(sl);
		b2d.fill(ll);
		b2d.fill(gl);
		b2d.fill(pl);
		b2d.fill(ral);
		b2d.fill(tl);
		
		int line = 0;
		
		b2d.setColor(Color.WHITE);
		b2d.drawString("Solid", 10, b_txt+(b_h+5)*line);
		b2d.drawString(Elements.solid.length+"", 10, b_txtn+(b_h+5)*line++);
		
		b2d.drawString("Liquid", 10, b_txt+(b_h+5)*line);
		b2d.drawString(Elements.liquid.length+"", 10, b_txtn+(b_h+5)*line++);
		
		b2d.drawString("Gass", 10, b_txt+(b_h+5)*line);
		b2d.drawString(Elements.gasses.length+"", 10, b_txtn+(b_h+5)*line++);
		
		b2d.drawString("Powder", 10, b_txt+(b_h+5)*line);
		b2d.drawString(Elements.powder.length+"", 10, b_txtn+(b_h+5)*line++);
		
		b2d.drawString("Radio", 10, b_txt+(b_h+5)*line);
		b2d.drawString(Elements.radio.length+"", 10, b_txtn+(b_h+5)*line++);
		
		b2d.drawString("Tools", 10, b_txt+(b_h+5)*line);
		b2d.drawString(Elements.tools.length+"", 10, b_txtn+(b_h+5)*line++);
		
		b2d.setPaintMode();
		g2d.drawImage(img, null, 0, 0);
	}
	
	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		Point p = e.getPoint();
		if(sl.contains(p)) selected = Elements.solid;
		if(ll.contains(p)) selected = Elements.liquid;
		if(gl.contains(p)) selected = Elements.gasses;
		if(pl.contains(p)) selected = Elements.powder;
		if(ral.contains(p)) selected = Elements.radio;
		if(tl.contains(p)) selected = Elements.tools;
		mattw.powder.old.Window.menub.repaint();
	}

	public void mouseReleased(MouseEvent e) {
		
	}

	public void mouseDragged(MouseEvent e) {
		mattw.powder.old.Window.updateMouseInFrame(e.getPoint(), this);
	}

	public void mouseMoved(MouseEvent e) {
		mattw.powder.old.Window.updateMouseInFrame(e.getPoint(), this);
	}
	
}

package main.java.powder;

import main.java.powder.elements.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class SideMenu extends JPanel implements ActionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	static int width = 55;
	static int height = Display.height;
	
	public BufferedImage img;
	public Graphics2D g2d;
	public Graphics2D b2d;
	
	public boolean init = true;
	
	public SideMenu() {
		setFocusable(true);
		addMouseListener(this);
	}

	public void init() {
		init = false;
		img = new BufferedImage(width, height*2, BufferedImage.TYPE_INT_ARGB);
		b2d = img.createGraphics();
	}
	
	public int b_h = 40;
	public int b_w = 45;
	public int b_txt = b_h/2;
	public int b_txtn = b_h/2+15;
	
	public Rectangle sl = new Rectangle(5, 5, b_h, b_w);
	public Rectangle ll = new Rectangle(5, 5+(b_h+5)*1, b_w, b_h);
	public Rectangle gl = new Rectangle(5, 5+(b_h+5)*2, b_w, b_h);
	public Rectangle pl = new Rectangle(5, 5+(b_h+5)*3, b_w, b_h);
	public Rectangle tl = new Rectangle(5, 5+(b_h+5)*4, b_w, b_h);
	
	static Element[] selected = Element.solids;
	
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
		b2d.fill(tl);
		
		b2d.setColor(Color.WHITE);
		b2d.drawString("Solid", 10, b_txt);
		b2d.drawString(Element.solids.length+"", 10, b_txtn);
		
		b2d.drawString("Liquid", 8, b_txt+(b_h+5)*1);
		b2d.drawString(Element.liquids.length+"", 10, b_txtn+(b_h+5)*1);
		
		b2d.drawString("Gass", 10, b_txt+(b_h+5)*2);
		b2d.drawString(Element.gasses.length+"", 10, b_txtn+(b_h+5)*2);
		
		b2d.drawString("Powder", 8, b_txt+(b_h+5)*3);
		b2d.drawString(Element.powders.length+"", 10, b_txtn+(b_h+5)*3);
		
		b2d.drawString("Tools", 10, b_txt+(b_h+5)*4);
		b2d.drawString(Element.tools.length+"", 10, b_txtn+(b_h+5)*4);
		/*for (int key : Element.el_map.keySet()) {
			Element e = Element.el_map.get(key);
			b2d.setPaintMode();
			b2d.setColor(e.getColor());
			b2d.fillRect(5, 5+key*20, getWidth()-10, 15);
			b2d.setColor(Color.WHITE);
			b2d.setXORMode(Color.BLACK);
			b2d.drawString(e.shortName, 12, 17+key*20);
		}*/
		
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
		//int id = (e.getPoint().y-5)/20;
		//if (Element.el_map.containsKey(id)) Display.left = Element.el_map.get(id);
		if(sl.contains(p)) selected = Element.solids;
		if(ll.contains(p)) selected = Element.liquids;
		if(gl.contains(p)) selected = Element.gasses;
		if(pl.contains(p)) selected = Element.powders;
		if(tl.contains(p)) selected = Element.tools;
	}

	public void mouseReleased(MouseEvent e) {
		
	}
	
}

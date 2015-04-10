package powder;

import powder.elements.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class SideMenu extends JPanel implements ActionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	public int width = 70;
	public int height = Display.height;
	
	public BufferedImage img;
	public Graphics2D g2d;
	public Graphics2D b2d;
	
	public Timer timer = new Timer(30, this);
	public boolean init = true;
	
	public SideMenu() {
		setFocusable(true);
		addMouseListener(this);
		timer.start();
	}

	public void init() {
		init = false;
		img = new BufferedImage(width, height*2, BufferedImage.TYPE_INT_ARGB);
		b2d = img.createGraphics();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		if(init) init();
		
		b2d.setColor(Color.GRAY);
		b2d.setPaint(new GradientPaint(0, 0, Color.WHITE, width, height*2, Color.WHITE.darker()));
		b2d.fillRect(0, 0, width, height);
		
		for(int key : Game.el_map.keySet()) {
			Element e = Game.el_map.get(key);
			b2d.setPaintMode();
			b2d.setColor(e.getColor());
			b2d.fillRect(5, 5+key*20, getWidth()-10, 15);
			b2d.setColor(Color.WHITE);
			b2d.setXORMode(Color.BLACK);
			b2d.drawString(key+" "+e.shortName, 8, 17+key*20);
		}
		
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
		int id = (e.getPoint().y-5)/20;
		if(Game.el_map.containsKey(id)) Display.left = Game.el_map.get(id);
	}

	public void mouseReleased(MouseEvent e) {
		
	}
	
}

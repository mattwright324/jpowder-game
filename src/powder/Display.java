package powder;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import powder.Game.Cell;
import powder.Game.Element;
import powder.Game.Particle;

public class Display extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	
	private static final long serialVersionUID = 1L;
	
	static int width = 612;
	static int height = 384;
	
	static int img_scale = 1;
	
	static int cell_w = 0;
	static int cell_h = 0;
	
	static boolean small = true;
	
	static void makeSmall() {
		cell_w = 0;
		cell_h = 0;
		img_scale = 1;
		img = new BufferedImage(width*img_scale, height*img_scale, BufferedImage.TYPE_4BYTE_ABGR);
		b2d = img.createGraphics();
		small = true;
		Window.window.resize();
	}
	
	static void makeLarge() {
		cell_w = 1;
		cell_h = 1;
		img_scale = 2;
		img = new BufferedImage(width*img_scale, height*img_scale, BufferedImage.TYPE_4BYTE_ABGR);
		b2d = img.createGraphics();
		small = false;
		Window.window.resize();
	}
	
	public Timer timer = new Timer(1, this);
	
	public Point mouse = new Point(0,0);
	
	public Game game = new Game();
	
	public Display() {
		Game.make_cells();
		game.startUpdateThread();
		timer.start();
		dfps.start();
		
		setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		addMouseMotionListener(this);
		
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		setCursor(blankCursor);
	}
	
	static Graphics2D w2d;
	static BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
	static Graphics2D b2d = img.createGraphics();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		w2d = (Graphics2D) g;
		
		b2d.setColor(Color.BLACK);
		b2d.fillRect(0, 0, getWidth(), getHeight());
		size = 0;
		for(int w=0; w<width; w++)
			for(int h=0; h<height; h++)
				draw_cell(Game.cells[w][h]);
		
		b2d.setColor(Color.LIGHT_GRAY);
		int sx = mstart.x * img_scale; int ex = (mstop.x-mstart.x) * img_scale;
		int sy = mstart.y * img_scale; int ey = (mstop.y-mstart.y) * img_scale;
		b2d.drawRect(sx, sy, ex, ey);
		b2d.setColor(new Color(244,244,244,32));
		b2d.fillRect(sx, sy, ex, ey);
		
		w2d.drawImage(img, null, 0, 0);
		w2d.setColor(Color.WHITE);
		w2d.setXORMode(Color.BLACK);
		int line = 1;
		w2d.drawString("FramesPS: "+dfps.average(), 5, 15*line++);
		w2d.drawString("UpdatesPS: "+Game.gfps.fps(), 5, 15*line++);
		w2d.drawString("'0' to '7' Place: "+left.shortName, 5, 15*line++);
		w2d.drawString("'S'        Size: "+(small ? "Default" : "Large"), 5, 15*line++);
		w2d.drawString("'Space'    Game: "+(Game.paused ? "Paused" : "Playing"), 5, 15*line++);
		w2d.drawString("Parts: "+size, 5, 15*line++);
		
		Particle p = Game.getParticleAt(mouse.x, mouse.y);
		w2d.drawString("X: "+mouse.x+" Y:"+mouse.y, 5, getHeight()-30);
		w2d.drawString(p!=null ? (p.el.shortName+", Temp:"+p.celcius+", Life: "+p.life) : "Empty", 5, getHeight()-15);
		dfps.add();
	}
	
	public int size = 0;
	public void draw_cell(Cell c) {
		if(c.part!=null) {
			if(c.part.el.remove)
				c.part = null;
			else {
				size++;
				b2d.setColor(c.part.getColor());
				b2d.drawRect(c.screen_x(), c.screen_y(), cell_w, cell_h);
			}
		}
	}
	
	public Element left = Game.dust;
	public Element right = Game.none;
	
	public void place(Element e) {
		for(int x=mstart.x; x<=mstop.x; x++)
			for(int y=mstart.y; y<=mstop.y; y++)
				Game.setParticleAt(x, y, new Particle(e, x, y), e==Game.none);
	}
	
	public void mouseDragged(MouseEvent e) {
		mouse = e.getPoint();
		mstart = new Point(mouse.x-draw_size/2,mouse.y-draw_size/2);
		mstop = new Point(mstart.x+draw_size, mstart.y+draw_size);
		if(SwingUtilities.isLeftMouseButton(e))
			place(left);
		if(SwingUtilities.isRightMouseButton(e))
			place(right);
	}

	public void mouseMoved(MouseEvent e) {
		mouse = e.getPoint();
		mstart = new Point(mouse.x-draw_size/2,mouse.y-draw_size/2);
		mstop = new Point(mstart.x+draw_size, mstart.y+draw_size);
	}

	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		if(SwingUtilities.isLeftMouseButton(e))
			place(left);
		if(SwingUtilities.isRightMouseButton(e))
			place(right);
	}

	public void mouseReleased(MouseEvent e) {
		
	}

	public void keyPressed(KeyEvent e) {
		
	}

	public void keyReleased(KeyEvent e) {
		
	}

	public void keyTyped(KeyEvent e) {
		int key = e.getKeyChar();
		if(key==' ') Game.paused = !Game.paused;
		if(key=='s') if(small) makeLarge(); else makeSmall();
		
		if(key=='0') left = Game.none;
		if(key=='1') left = Game.dust;
		if(key=='2') left = Game.salt;
		if(key=='3') left = Game.dmnd;
		if(key=='4') left = Game.metl;
		if(key=='5') left = Game.gas;
		if(key=='6') left = Game.warp;
		if(key=='7') left = Game.phot;
	}
	
	public int draw_size = 0;
	
	public Point mstart = new Point(0,0), mstop = new Point(0,0);
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		draw_size-=e.getWheelRotation();
		if(draw_size<0) draw_size = 0;
		mstart = new Point(mouse.x-draw_size/2,mouse.y-draw_size/2);
		mstop = new Point(mstart.x+draw_size, mstart.y+draw_size);
	}
	
	public void actionPerformed(ActionEvent e) {
		repaint();
	}
	
	static FPS dfps = new FPS();
	static class FPS extends Thread {
		public double seconds = 0;
		public long total = 0;
		public double avg = 0;
		
		public long count = 0;
		public long fps = 0;
		public long last_fps = System.currentTimeMillis();
		
		public void add() {
			count++;
		}
		
		public void run() {
			while(isAlive()) {
				if(System.currentTimeMillis()-last_fps > 1000) {
					seconds+=(System.currentTimeMillis()-last_fps) / 1000.0;
					
					fps = count;
					count = 0;
					last_fps = System.currentTimeMillis();
					
					total+=fps;
					avg = total / seconds;
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
			}
		}
		
		public long fps() {
			return fps;
		}
		
		public void resetAverage() {
			seconds = 0;
			total = 0;
			avg = 0;
		}
		
		public double average() {
			return Math.round(avg * 100.0) / 100.0;
		}
	}
}

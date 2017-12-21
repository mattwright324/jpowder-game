package mattw.powder.old;

public class Counter extends Thread {
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
				if(seconds > 60) resetAverage();
			}
			try {
				Thread.sleep(25);
			} catch (InterruptedException ignored) {
			}
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

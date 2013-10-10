package eventserver;


public class Subscriber {
	protected synchronized void notifyOfEvent(){
		notify();
	}
	
	/**
	 * Wait for an event to take place
	 */
	public synchronized void waitForEvent() {
		try {
			wait();
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Wait for an event to take place
	 * @param timeout time limit for waiting
	 */
	public synchronized void waitForEvent(long timeout) {
		try {
			wait(timeout);
		} catch (InterruptedException e) {
		}
	}
}

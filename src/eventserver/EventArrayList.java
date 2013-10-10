package eventserver;

import java.util.ArrayList;

public class EventArrayList<E> implements EventList<E> {

	private class MyObserver implements Observer<E> {
		private ArrayList<E> events;
		private int next;

		protected MyObserver(EventArrayList<E> subject) {
			this.events = subject.events;
			this.next = subject.events.size();
		}

		public boolean expired() {
			return false;
		}
		
		public boolean hasNext() {
			return next < events.size();
		}
		
		public E next() {
			return events.get(next++);
		}
	}
	
	ArrayList<E> events;

	public EventArrayList() {
		events = new ArrayList<E>();
	}

	public Observer<E> observe() {
		return new MyObserver(this);
	}
	
	public void add(E e) {
		events.add(e);
	}
}

package eventserver;

public class FiniteEventList<E> implements EventList<E> {

	private class MyObserver implements Observer<E> {
		private int nextid;
		private int nextind;
		private FiniteEventList<E> subject;
		
		protected MyObserver(FiniteEventList<E> subject) {
			this.subject = subject;
			this.nextid = subject.tailid;
			this.nextind = subject.tail;
		}
		
		public boolean expired() {
			return nextid < subject.headid;
		}
		
		public boolean hasNext() {
			return nextid < subject.tailid && !expired();
		}
		
		public E next() {
			E r = subject.get(nextind);
			nextid++;
			nextind = (nextind+1) % subject.size;
			return r;
		}
	}
	
	protected int head = 0;
	protected int tail = 0;
	protected int headid = 0;
	protected int tailid = 0;
	protected final int size;
	protected Object[] events;
	
	public FiniteEventList(int size) {
		this.size = size;
		events = new Object[size];
	}

	public Observer<E> observe() {
		return new MyObserver(this);
	}
	
	public void add(E e) {
		events[tail] = e;
		
		if (head == tail && headid != tailid) {
			headid++;
			head = (head + 1) % events.length;
		}
		tail = (tail+1) % events.length;
		tailid++;
	}
	
	@SuppressWarnings("unchecked")
	protected E get(int ind) {
		return (E) events[ind];
	}
}

package eventserver;

public interface EventList<E> {
	public Observer<E> observe();
	
	public void add(E e);
}

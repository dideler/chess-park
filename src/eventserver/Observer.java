package eventserver;

public interface Observer<E> {
	public boolean expired();

	public boolean hasNext();

	public E next();
}
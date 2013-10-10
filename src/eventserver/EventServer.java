package eventserver;

import java.util.HashSet;
import java.util.Set;

public class EventServer<E> {
	
	public EventList<E> eventlist;
	public Set<Subscriber> subscribers; // Subscriber list.
	
	/**
	 * Initialise the server with an event list
	 * @param eventlist
	 */
	EventServer(EventList<E> eventlist) {
		this.eventlist = eventlist;
		this.subscribers = new HashSet<Subscriber>();
	}
	
	/**
	 * Initialise the server with a set size
	 * @param listSize The size of the event list
	 */
	public EventServer(int listSize) {
		this(new FiniteEventList<E>(listSize));
	}
	
	/**
	 * Initialise the server with an array event list
	 * @param eventlist
	 */
	public EventServer() {
		this(new EventArrayList<E>());
	}
	
	/**
	 * Post an event to the server
	 * @param e
	 */
	public synchronized void addEvent(E e) {
		eventlist.add(e);
		for (Subscriber s : subscribers) {
			s.notifyOfEvent();
		}
	}
	
	/**
	 * Post a collection of events to the server
	 * @param e
	 */
	public synchronized void addAllEvents(Iterable<E> coll) {
		for(E e : coll) {
			eventlist.add(e);
		}
		for (Subscriber s : subscribers) {
			s.notifyOfEvent();
		}
	}
	
	/**
	 * Post an array of events to the server
	 * @param e
	 */
	public synchronized void addAllEvents(E[] coll) {
		for(E e : coll) {
			eventlist.add(e);
		}
		for (Subscriber s : subscribers) {
			s.notifyOfEvent();
		}
	}
	
	/**
	 * Get an observer for the event list
	 * @return
	 */
	public synchronized Observer<E> observe() {
		return eventlist.observe();
	}
	
	/**
	 * Add object to the subscriber list
	 * @param subscriber
	 */
	public synchronized void subscribe(Subscriber subscriber) {
		subscribers.add(subscriber);
	}
	
	/**
	 * Remove object from the subscriber list
	 * @param subscriber
	 */
	public synchronized void unsubscribe(Subscriber subscriber) {
		subscribers.remove(subscriber);
	}
}

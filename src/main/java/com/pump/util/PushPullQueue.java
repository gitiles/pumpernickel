/**
 * This software is released as part of the Pumpernickel project.
 * 
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://github.com/mickleness/pumpernickel/raw/master/License.txt
 * 
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package com.pump.util;

/**
 * This is a one-element list; at most only one element can exist in this queue
 * at any time. It is intended to be used by two threads for handing off
 * information.
 * <p>
 * In the future this can be replaced with a <code>SynchronousQueue</code> when
 * this codebase is adapted to Java 1.5. But for Java 1.4 I ended up adapting my
 * own solution.
 * 
 * @param <T>
 *            the type of object in this queue.
 */
public class PushPullQueue<T> {
	private T object;

	/**
	 * @return true if there is no object waiting to be removed.
	 */
	public boolean isEmpty() {
		synchronized (this) {
			return object == null;
		}
	}

	/**
	 * Pulls an element from the queue.
	 * 
	 * @param timeout
	 *            the duration to wait, or a non-positive value if this request
	 *            should never timeout.
	 * @return an object recently added to this queue. This will never return
	 *         null.
	 */
	public T pull(long timeout) {
		long start = System.currentTimeMillis();
		while (true) {
			synchronized (this) {
				if (object != null) {
					T returnValue = object;
					object = null;
					notifyAll();
					return returnValue;
				}

				long elapsed = System.currentTimeMillis() - start;
				if (elapsed > timeout)
					throw new RuntimeException("elapsed = " + elapsed
							+ ", timeout = " + timeout);

				long currentTimeout = Math.max(10, timeout - elapsed);
				try {
					wait(currentTimeout);
				} catch (InterruptedException e) {
				}
			}
			iteratePull();
		}
	}

	/**
	 * This pushes an object on the queue and immediately returns. Unlike the
	 * other push() method this does <i>not</i> wait for another thread to pull
	 * this value before returning.
	 * 
	 * @param newObject
	 *            the newObject to add to the queue.
	 */
	public void push(T newObject) {
		synchronized (this) {
			if (this.object != null)
				throw new IllegalArgumentException(
						"illegal attempt to replace " + this.object + " with "
								+ object);
			this.object = newObject;
			notifyAll();
		}
	}

	/**
	 * This puts on object on the queue, and then blocks until another thread
	 * removes it.
	 * 
	 * @param newObject
	 *            the object to add to the queue.
	 * @param timeout
	 *            the duration to wait, or a non-positive value if this method
	 *            should never timeout.
	 */
	public void push(T newObject, long timeout) {
		long start = System.currentTimeMillis();
		synchronized (this) {
			if (this.object != null)
				throw new IllegalArgumentException(
						"illegal attempt to replace " + this.object + " with "
								+ object);
			this.object = newObject;
			notifyAll();
		}
		while (true) {
			iteratePush();
			synchronized (this) {
				if (object == null) {
					return;
				}

				long elapsed = System.currentTimeMillis() - start;
				if (elapsed > timeout)
					throw new RuntimeException("elapsed = " + elapsed
							+ ", timeout = " + timeout);

				long currentTimeout = Math.max(timeout - elapsed, 10);
				try {
					this.wait(currentTimeout);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * This method is called during <code>push(...)</code>.
	 * <p>
	 * Currently this does nothing, but subclasses can override it to abort push
	 * operations by throwing a <code>RuntimeException</code> if this operation
	 * is aborted.
	 * 
	 */
	protected void iteratePush() {
	}

	/**
	 * This method is called during <code>pull(...)</code>.
	 * <p>
	 * Currently this does nothing, but subclasses can override it to abort pull
	 * operations by throwing a <code>RuntimeException</code> if this operation
	 * is aborted.
	 * 
	 */
	protected void iteratePull() {
	}
}
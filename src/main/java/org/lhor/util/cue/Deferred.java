package org.lhor.util.cue;


/**
 * The producer's interface to the Promises/A+ API.
 * <p>
 * The producer is able to return a Promise of a value at a later time.
 * (Similar to a {@link java.util.concurrent.Future} but without checked
 * exceptions.)
 * </p>
 * <p>
 * Unlike the Promises/A+ API, in Java there can be multiple threads waiting
 * on a Promise to be resolved. To cancel the operation and un-park any
 * threads waiting on this Deferred's Promise to be resolved, simply invoke
 * {@link #reject()}.
 * </p>
 *
 * @param <T>
 */
public interface Deferred<T> {
  /**
   * Resolves the Deferred's Promise with the given value.
   * <p>
   * If the Promise was already resolved or rejected, this will have no
   * effect.
   * </p>
   *
   * @param t nullable, the value to resolve the Promise with
   */
  void resolve(T t);

  /**
   * Resolves the Deferred's Promise instance with the same resolution as
   * the given Promise's.
   * <p>
   * The given Promise may be resolved with a null value, but the given
   * Promise itself must not be null.
   * </p>
   * <p>
   * If the given Promise is one produced by this Deferred instance, an
   * {@link IllegalArgumentException} will be thrown.
   * </p>
   *
   * @param tPromise non-null, a Promise instance
   * @throws NullPointerException if tPromise is null
   * @throws IllegalArgumentException if the Deferred's own Promise was given
   */
  void resolveFrom(Promise<T> tPromise);

  /**
   * Rejects the Deferred's Promise with no underlying reason.
   * <p>
   * The Promise will still throw a {@link RejectedException}, but invoking
   * {@link Throwable#getCause()} will return null.
   * </p>
   */
  void reject();

  /**
   * Rejects the Deferred's Promise with the given Exception as the reason.
   * <p>
   * The given reason may be null, which will behave the same as
   * {@link #reject()}.
   * </p>
   *
   * @param e
   */
  void reject(Exception e);

  /**
   * Returns the Deferred's Promise.
   * <p>
   * A Deferred may produce references to the exact same Promise instance,
   * as all instances will be resolved the same way. However, new instances
   * may also be produced each time this method is invoked, so the object
   * identity behavior (<code>promise1 == promise2</code>) should not be
   * relied on.
   * </p>
   *
   * @return a non-null instance of a Promise
   */
  Promise<T> promise();
}

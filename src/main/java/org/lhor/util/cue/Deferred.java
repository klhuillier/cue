/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


/**
 * The producer's interface to the Promises/A+ API.
 * <p>
 * The producer is able to return a Promise of a value at a later time.
 * </p>
 * <p>
 * Unlike the Promises/A+ API, in Java there can be multiple threads waiting
 * on a Promise to be resolved. To cancel the operation and un-park any
 * threads waiting on this Deferred's Promise to be resolved, simply invoke
 * {@link #reject()}.
 * </p>
 *
 * @param <T> the value type the Deferred and its immediate Promise will be
 *            resolved with
 */
public interface Deferred<T> {
  /**
   * Resolves the Deferred's Promise with the given value.
   * <p>
   * If the Promise was already resolved or rejected, this will have no
   * effect.
   * </p>
   * <p>
   * It is strongly suggested, but in no way enforced, that the value be
   * immutable or at least treated as immutable. This is because multiple
   * callbacks can be registered on the same Promise:
   * <pre>
   * // This may print the expected time, epoch, or something entirely
   * // unexpected (because Calendar is not thread-safe), depending on when
   * // clear() takes effect:
   * Promise&lt;Calendar&gt; promise = getTimestamp();
   * promise.then(time -> time.clear());
   * promise.then(time -> System.out.println(time));
   * </pre>
   * </p>
   *
   * @param t nullable, the value to resolve the Promise with
   */
  void resolve(T t);

  /**
   * Resolves the Deferred's Promise instance with the same resolution as
   * the given Promise's.
   * <p>
   * Resolution will occur in separate thread after the given promise has
   * been resolved.
   * </p>
   * <p>
   * The given Promise may be resolved with a null value, but the given
   * Promise itself must not be null.
   * </p>
   * <p>
   * If the given Promise is one produced by this Deferred instance, an
   * {@link IllegalArgumentException} will be thrown.
   * </p>
   * <p>
   * It is possible to create a circular resolution which will never complete
   * unless a Deferred is resolved in another manner. e.g., the following will
   * never complete:
   *  <pre>
   *  deferred1.resolveFrom(deferred2.promise());
   *  deferred2.resolveFrom(deferred1.promise());
   *  </pre>
   * It is possible to cause both to become resolved by later invoking
   * <code>deferred1.resolve(value);</code>, in which case the attempt to
   * resolve from deferred2 will not have any effect.
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
   * {@link RejectedException#getReason()} will return null.
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
   * @param e an exception or null that is why the promise is to be rejected
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

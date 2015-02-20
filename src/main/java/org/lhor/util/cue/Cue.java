/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


import java.util.List;
import java.util.concurrent.Future;


/**
 * The main interface for the Cue library, producing Deferred/Promise pairs as
 * well as several ways to produce Promises that Cue itself will resolve.
 * <p>
 * The main method to use from an instance of Cue is {@link Cue#defer()} which
 * will provide a Deferred object that can be resolved in a separate thread managed
 * by the invoker. A Deferred can allow one to provide a Promise to consumers
 * while a producer will resolve the Deferred, resolving the Promise.
 * </p>
 */
public interface Cue {
  /**
   * Produces a new Deferred instance which can be fulfilled later by a producer
   * while handing off the Promise to any interested consumers at any time.
   *
   * @param <T> fulfillment type
   * @return new instance
   */
  <T> Deferred<T> defer();

  /**
   * Produces a Promise which will only be resolved when all given Promises are
   * resolved.
   * <p>
   * As the returned Promise goes through the List, if it encounters a rejection,
   * it will also be rejected with the same reason as the first rejection it
   * finds.
   * </p>
   * <p>
   * This method will make use of a thread from the Cue thread pool.
   * </p>
   *
   * @param promises non-null, possibly empty list of promises in any state
   * @param <T> fulfillment type of all promises
   * @return new promise resolved based on resolution of all given promises
   */
  <T> Promise<List<T>> all(List<Promise<T>> promises);

  /**
   * Produces a Promise which will only be resolved when all given Futures are
   * resolved.
   * <p>
   * If any one of the Futures throws an Exception, the returned Promise will be
   * rejected with the reason being the first Exception found in the List.
   * </p>
   * <p>
   * This method will make use of a thread from the Cue thread pool.
   * </p>
   *
   * @param futures non-null, possibly empty list of futures in any state
   * @param <T> return type of all futures
   * @return new promise resolved based on returned values of all given futures
   */
  <T> Promise<List<T>> allFutures(List<Future<T>> futures);

  /**
   * Produces a Promise which will be resolved immediately with the provided value.
   *
   * @param value fulfillment value
   * @param <T> fulfillment type
   * @return new promise fulfilled with the given value
   */
  <T> Promise<T> when(T value);

  /**
   * Produces a Promise which will be resolved when the given Future is resolved.
   * <p>
   * This method will make use of a thread from the Cue thread pool.
   * </p>
   *
   * @param future future to resolve from
   * @param <T> return type of the future, fulfillment type of the promise
   * @return a new promise that will be resolved when the future is finished
   */
  <T> Promise<T> whenFuture(Future<T> future);

  /**
   * Produces a Promise which will be resolved immediately with the provided
   * rejection reason.
   *
   * @param ex possibly null reason for rejection
   * @param <T> fulfillment type of the promise
   * @return a new promise rejected with the given reason
   */
  <T> Promise<T> reject(Exception ex);
}

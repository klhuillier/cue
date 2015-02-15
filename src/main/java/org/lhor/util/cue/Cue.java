package org.lhor.util.cue;


import java.util.List;
import java.util.concurrent.Future;


/**
 * An implementation of the <a href="https://promisesaplus.com/">Promise/A+</a>
 * specification in Java, and inspired by (and named for)
 * <a href="https://github.com/kriskowal/q/wiki/API-Reference">the Q library</a>.
 * <p>
 * This attempts to be a fairly faithful reproduction of the Javascript Promise/A+
 * specification, which facilitates asynchronous work that rejoins when the work is
 * completed. Obviously, not all features can be reproduced or even should be, since
 * there are many differences between the languages.
 * </p>
 * <p>
 * Unlike the Javascript version--which due to the language specification does not
 * have any mechanism of dispatching new threads and parking existing threads--this
 * version can dispatch work in a separate thread pool provided by an
 * ExecutorService.
 * </p>
 * <p>
 * Instances of Cue are normally served up by Google Guice by including
 * {@link CueModule} in your injector's configuration. Without Guice, you can
 * get an instance via {@link Cue#newCue()}.
 * </p>
 * <p>
 * The main method to use from an instance of Cue is {@link Cue#defer()} which
 * will provide a Deferred object that can be resolved in a separate thread managed
 * by the invoker. A Deferred can allow one to provide a Promise to consumers
 * while a producer will resolve the Deferred, resolving the Promise.
 * </p>
 * <p>
 * Promises produced can either be blocking or non-blocking. Blocking Promise
 * methods will not return until the Promise is resolved. Non-blocking Promise
 * methods will return immediately and the callbacks will wait to execute until
 * the Promise has been resolved.
 * </p>
 */
public interface Cue {
  /**
   * Produces a new Deferred instance which can be fulfilled later by a producer
   * while handing off the Promise to any interested consumers at any time.
   *
   * @param <T>
   * @return
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
   *
   * @param promises
   * @param <T>
   * @return
   */
  <T> Promise<List<T>> all(List<Promise<T>> promises);

  /**
   * Produces a Promise which will only be resolved when all given Promises are
   * resolved.
   * <p>
   * As the returned Promise goes through the array, if it encounters a rejection,
   * it will also be rejected with the same reason as the first rejection it
   * finds.
   * </p>
   *
   * @param promise
   * @param promises
   * @param <T>
   * @return
   */
  <T> Promise<List<T>> all(Promise<T> promise, Promise<T>... promises);

  /**
   * Produces a Promise which will only be resolved when all given Futures are
   * resolved.
   * <p>
   * If any one of the Futures throws an Exception, the returned Promise will be
   * rejected with the reason being the first Exception found in the List.
   * </p>
   *
   * @param futures
   * @param <T>
   * @return
   */
  <T> Promise<List<T>> allFutures(List<Future<T>> futures);

  /**
   * Produces a Promise which will only be resolved when all given Futures are
   * resolved.
   * <p>
   * If any one of the Futures throws an Exception, the returned Promise will be
   * rejected with the reason being the first Exception found in the List.
   * </p>
   *
   * @param future
   * @param futures
   * @param <T>
   * @return
   */
  <T> Promise<List<T>> allFutures(Future<T> future, Future<T>... futures);

  /**
   * Produces a Promise which will be resolved immediately with the provided value.
   *
   * @param value
   * @param <T>
   * @return
   */
  <T> Promise<T> when(T value);

  /**
   * Produces a Promise which will be resolved when the given Future is resolved.
   *
   * @param future
   * @param <T>
   * @return
   */
  <T> Promise<T> whenFuture(Future<T> future);

  /**
   * Produces a Promise which will be resolved immediately with the provided
   * rejection reason.
   *
   * @param ex
   * @param <T>
   * @return
   */
  <T> Promise<T> reject(Exception ex);

  /**
   * Produces a new instance of Cue.
   * <p>
   * Instances can be retrieved from this method, but it is recommended to use the
   * {@link CueModule Guice module}.
   * </p>
   *
   * @return
   */
  public static Cue newCue() { return new CueImpl(); }
}

package org.lhor.util.cue;


import java.util.concurrent.Future;


/**
 * Basically a wrapper around a Future to be able to treat a Future as a Promise.
 * <p>
 * The major difference between the interfaces is the thrown Exception types. As a
 * Promise, this implementation will catch any Future exceptions and rethrow them
 * as RejectedExceptions.
 * </p>
 *
 * @param <T>
 */
final class FuturePromise<T> implements Promise<T> {
  private final Future<T> future;
  private final Deferred<T> deferred = new DeferredImpl<>();
  private final Promise<T> promise = deferred.promise();

  protected FuturePromise(Future<T> future) {
    this.future = future;
  }

  @Override
  public <O> Promise<O> then(Callback<T, O> callback) {
    resolve();
    return promise.then(callback);
  }

  @Override
  public Promise<T> then(VoidCallback<T> callback) {
    resolve();
    return promise.then(callback);
  }

  @Override
  public <O> Promise<O> then(NullCallback<O> callback) {
    resolve();
    return promise.then(callback);
  }

  @Override
  public Promise<T> then(NullVoidCallback callback) {
    resolve();
    return promise.then(callback);
  }

  @Override
  public Promise<T> fail(Errback<T> errback) {
    resolve();
    return promise.fail(errback);
  }

  @Override
  public Promise<T> fail(VoidErrback errback) {
    resolve();
    return promise.fail(errback);
  }

  @Override
  public Promise<T> always(NullVoidCallback callback) {
    resolve();
    return promise.always(callback);
  }

  @Override
  public T done() {
    resolve();
    return promise.done();
  }

  @Override
  public T done(NullVoidCallback callback) {
    resolve();
    return promise.done(callback);
  }

  @Override
  public T done(VoidErrback errback) {
    resolve();
    return promise.done(errback);
  }

  @Override
  public T done(NullVoidCallback callback, VoidErrback errback) {
    resolve();
    return done(callback, errback);
  }

  private void resolve() {
    try {
      deferred.resolve(future.get());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (Exception e) {
      deferred.reject(e);
    }
  }
}

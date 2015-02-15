package org.lhor.util.cue;


import net.jcip.annotations.Immutable;


@Immutable
final class DeferredImpl<T> implements Deferred<T> {
  private final ResolvedState<T> state = new ResolvedState<>();
  private final PromiseImpl<T> promise = new PromiseImpl<>(state);

  @Override
  public void resolve(T t) {
    state.offerFulfillment(t);
  }

  @Override
  public void resolveFrom(Promise<T> tPromise) {
    if (promise == tPromise) {
      throw new IllegalArgumentException("Cannot resolve a Deferred with its own Promise");
    }
    resolve(tPromise.done());
  }

  @Override
  public void reject() {
    reject(null);
  }

  @Override
  public void reject(Exception e) {
    state.offerRejection(e);
  }

  @Override
  public Promise<T> promise() {
    return promise;
  }
}

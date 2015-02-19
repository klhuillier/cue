package org.lhor.util.cue;


import net.jcip.annotations.Immutable;


@Immutable
final class DeferredImpl<T> implements Deferred<T> {
  private final EventSink eventSink;
  private final ResolvedStateImpl<T> state;
  private final Promise<T> promise;

  // Not auto-injected, the Provider needs to provide the same state to this and the Promise
  public DeferredImpl(EventSink eventSink, ResolvedStateImpl<T> state, Promise<T> promise) {
    if (eventSink == null) {
      throw new NullPointerException("eventSink");
    } else if (state == null) {
      throw new NullPointerException("state");
    } else if (promise == null) {
      throw new NullPointerException("promise");
    }
    this.eventSink = eventSink;
    this.state = state;
    this.promise = promise;
  }

  @Override
  public void resolve(T t) {
    state.offerFulfillment(t);
    eventSink.stateResolved(state);
  }

  @Override
  public void resolveFrom(Promise<T> tPromise) {
    if (tPromise == null) {
      throw new NullPointerException("tPromise");
    } else if (promise == tPromise) {
      throw new IllegalArgumentException("Cannot resolve a Deferred with its own Promise");
    }
    tPromise.then(this::resolve)
            .fail((VoidErrback) this::reject);
  }

  @Override
  public void reject() {
    reject(null);
  }

  @Override
  public void reject(Exception e) {
    state.offerRejection(e);
    eventSink.stateResolved(state);
  }

  @Override
  public Promise<T> promise() {
    return promise;
  }
}

package org.lhor.util.cue;


import net.jcip.annotations.Immutable;
import java.util.logging.Level;
import java.util.logging.Logger;


@Immutable
final class PromiseImpl<T> implements Promise<T> {
  private final Cue cue;
  private final EventSink eventSink;
  private final ResolvedState<T> state;
  private final Logger log = Logger.getLogger(PromiseImpl.class.getName());

  public PromiseImpl(Cue cue, EventSink eventSink, ResolvedState<T> state) {
    if (cue == null) {
      throw new NullPointerException("cue");
    } else if (eventSink == null) {
      throw new NullPointerException("eventSink");
    } else if (state == null) {
      throw new NullPointerException("state");
    }
    this.cue = cue;
    this.eventSink = eventSink;
    this.state = state;
  }

  @Override
  public <O> Promise<O> then(Callback<T, O> callback) {
    if (callback == null) {
      throw new NullPointerException("callback");
    }

    Deferred<O> deferred = cue.defer();
    eventSink.register(state, () -> {
      T tValue;
      try {
        tValue = state.get();
      } catch (Exception e) {
        log.log(Level.FINE, "Promise was rejected, skipping callback", e);
        deferred.reject(e);
        return;
      }
      try {
        log.fine("Invoking callback with value: " + tValue);
        O oValue = callback.call(tValue);
        log.fine("Resolving promise from callback: " + oValue);
        deferred.resolve(oValue);
      } catch (Exception e) {
        log.log(Level.FINE, "Callback threw exception, rejecting promise", e);
        deferred.reject(e);
      }
    });
    return deferred.promise();
  }

  @Override
  public Promise<T> then(NullVoidCallback callback) {
    if (callback == null) {
      throw new NullPointerException("callback");
    }

    Deferred<T> deferred = cue.defer();
    eventSink.register(state, () -> {
      T tValue;
      try {
        tValue = state.get();
      } catch (Exception e) {
        log.log(Level.FINE, "Promise was rejected, skipping callback", e);
        deferred.reject(e);
        return;
      }
      try {
        log.fine(String.format("Invoking callback with no value (Promise resolved with %s)", tValue));
        callback.call();
        log.fine("Resolving promise with same value: " + tValue);
        deferred.resolve(tValue);
      } catch (Exception e) {
        log.log(Level.FINE, "Callback threw exception, rejecting promise", e);
        deferred.reject(e);
      }
    });
    return deferred.promise();
  }

  @Override
  public <O> Promise<O> then(NullCallback<O> callback) {
    if (callback == null) {
      throw new NullPointerException("callback");
    }

    Deferred<O> deferred = cue.defer();
    eventSink.register(state, () -> {
      T tValue;
      try {
        tValue = state.get();
      } catch (Exception e) {
        log.log(Level.FINE, "Promise was rejected, skipping callback", e);
        deferred.reject(e);
        return;
      }
      try {
        log.fine(String.format("Invoking callback with no value, (Promise resolved with %s)", tValue));
        O oValue = callback.call();
        log.fine("Resolving promise from callback: " + oValue);
        deferred.resolve(oValue);
      } catch (Exception e) {
        log.log(Level.FINE, "Callback threw exception, rejecting promise", e);
        deferred.reject(e);
      }
    });
    return deferred.promise();
  }

  @Override
  public Promise<T> then(VoidCallback<T> callback) {
    if (callback == null) {
      throw new NullPointerException("callback");
    }

    Deferred<T> deferred = cue.defer();
    eventSink.register(state, () -> {
      T tValue;
      try {
        tValue = state.get();
      } catch (Exception e) {
        log.log(Level.FINE, "Promise was rejected, skipping callback", e);
        deferred.reject(e);
        return;
      }
      try {
        log.fine("Invoking callback with value: " + tValue);
        callback.call(tValue);
        log.fine("Resolving promise with same value: " + tValue);
        deferred.resolve(tValue);
      } catch (Exception e) {
        log.log(Level.FINE, "Callback threw exception, rejecting promise", e);
        deferred.reject(e);
      }
    });
    return deferred.promise();
  }

  @Override
  public Promise<T> fail(VoidErrback errback) {
    if (errback == null) {
      throw new NullPointerException("errback");
    }

    Deferred<T> deferred = cue.defer();
    eventSink.register(state, () -> {
      try {
        T value = state.getValue();
        if (state.isFulfilled()) {
          log.fine("Skipping errback, Promise resolved with value: " + value);
          deferred.resolve(value);
          return;
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        deferred.reject(e);
        return;
      }

      try {
        Exception reason = state.getReason();
        log.log(Level.FINE, "Promise rejected, calling errback with reason", reason);
        errback.call(reason);
        log.log(Level.FINE, "Rejecting Promise with same reason", reason);
        deferred.reject(reason);
      } catch (Exception e) {
        log.log(Level.FINE, "Errback threw an exception, rejecting Promise with reason", e);
        deferred.reject(e);
      }
    });
    return deferred.promise();
  }

  @Override
  public Promise<T> fail(Errback<T> errback) {
    if (errback == null) {
      throw new NullPointerException("errback");
    }

    Deferred<T> deferred = cue.defer();
    eventSink.register(state, () -> {
      try {
        T value = state.getValue();
        if (state.isFulfilled()) {
          log.fine("Skipping errback, Promise resolved with value: " + value);
          deferred.resolve(value);
          return;
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        deferred.reject(e);
        return;
      }

      try {
        Exception reason = state.getReason();
        log.log(Level.FINE, "Promise rejected, calling errback with reason", reason);
        T replacement = errback.call(reason);
        log.fine("Resolving Promise with replacement value from errback: " + replacement);
        deferred.resolve(replacement);
      } catch (Exception e) {
        log.log(Level.FINE, "Errback threw exception, rejecting Promise", e);
        deferred.reject(e);
      }
    });
    return deferred.promise();
  }

  @Override
  public Promise<T> always(NullVoidCallback callback) {
    if (callback == null) {
      throw new NullPointerException("callback");
    }

    Deferred<T> deferred = cue.defer();
    eventSink.register(state, () -> {
      boolean interrupted = false;
      try {
        state.getValue();
      } catch (InterruptedException e) {
        interrupted = true;
      }

      try {
        log.fine("Promise resolved, invoking always callback");
        callback.call();
      } catch (Exception e) {
        // This is generally a problem. Always callbacks should generally not throw.
        log.log(Level.WARNING, "Always callback threw an exception", e);
      }

      if (interrupted) {
        log.fine("Always callback finished, resetting interrupted flag and resolving next Promise with same state");
        Thread.currentThread().interrupt();
      } else {
        log.fine("Always callback finished, resolving next Promise with same state");
      }
      deferred.resolveFrom(this);
    });
    return this;
  }

  @Override
  public T done() {
    try {
      log.fine("Attempting to end Promise chain, waiting for a value");
      T value = state.get();
      log.fine("Promise chain completed, returning value " + value);
      return value;
    } catch (Exception e) {
      log.log(Level.FINE, "Promise chain rejected, throwing RejectedException with reason", e);
      throw RejectedException.wrap(e);
    }
  }
}

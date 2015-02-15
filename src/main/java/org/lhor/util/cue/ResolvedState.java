package org.lhor.util.cue;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


final class ResolvedState<T> {
  // For parking and waiting for a value to be resolved. Any other field would also work,
  // but some tools flag this because they are java.util.concurrent.* types which normally
  // do not require synchronization.
  private final Object lock = new Object();
  private final AtomicBoolean resolved = new AtomicBoolean(false);
  private final AtomicReference<T> value = new AtomicReference<>(null);
  private final AtomicReference<RejectedException> reason = new AtomicReference<>(null);

  public ResolvedState() {
  }

  public ResolvedState(T t) {
    value.set(t);
    resolved.set(true);
  }

  public ResolvedState(Exception ex) {
    reason.set(RejectedException.wrap(ex));
    resolved.set(true);
  }

  protected boolean isResolved() {
    synchronized (lock) {
      return resolved.get();
    }
  }

  protected boolean isFulfilled() {
    synchronized (lock) {
      // We actually have to test reason because a Promise can be resolved with a null value.
      // reason is always wrapped in a RejectedException so it cannot be null if rejected.
      return isResolved() && reason.get() == null;
    }
  }

  protected boolean isRejected() {
    synchronized (lock) {
      return reason.get() != null;
    }
  }

  protected void offerFulfillment(T t) {
    synchronized (lock) {
      if (resolved.compareAndSet(false, true) && value.compareAndSet(null, t)) {
        lock.notifyAll();
      }
    }
  }

  protected void offerRejection(Exception ex) {
    synchronized (lock) {
      if (resolved.compareAndSet(false, true) && reason.compareAndSet(null, RejectedException.wrap(ex))) {
        lock.notifyAll();
      }
    }
  }

  protected void parkUntilResolved() {
    synchronized (lock) {
      while (!isResolved()) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        }
      }
    }
  }

  protected T get() throws RejectedException {
    parkUntilResolved();
    T result = value.get();
    if (result != null) {
      return result;
    }
    throw reason.get();
  }

  protected T getValue() {
    parkUntilResolved();
    return value.get();
  }

  protected RejectedException getReason() {
    parkUntilResolved();
    return reason.get();
  }
}

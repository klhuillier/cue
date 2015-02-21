/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The main implementation of ResolvedState for a Deferred/Promise pair.
 * <hr>
 * <h4>Notes on thread-safety</h4>
 * <p>
 * The write to resolution is visible to all threads because of the memory-fence
 * behavior of AtomicReference. However, the status check of isResolved(),
 * isFulfilled(), and isRejected() are not blocking and have no guaranteed
 * ordering. During invocation of one of these methods, another thread may be
 * simultaneously setting the resolution state. This is okay, because the status
 * checks will simply return false the same as they would if the resolving thread
 * had to wait for a lock held by the status checking thread.
 * </p>
 * <p>
 * No explicit synchronization is necessary here. Once the state is resolved,
 * the latch will be released. Any other threads blocked waiting for resolution
 * will be awakened and any subsequent threads waiting for resolution will
 * immediately proceed through the latch. There will be a brief period of time
 * between the compare-and-set of the resolution while the latch will somewhat
 * erroneously block on await (since the state is now resolved), but the
 * resulting behavior will be correct as the latch will be released immediately
 * after the compare-and-set thread continues.
 * </p>
 *
 * @param <T> fulfillment type
 */
@ThreadSafe
final class ResolvedStateImpl<T> implements ResolvedState<T> {
  private final AtomicReference<Resolution<T>> resolution = new AtomicReference<>(null);
  private final CountDownLatch latch = new CountDownLatch(1);

  private interface Resolution<T> {
    T getValue();
    Exception getReason();
  }

  /**
   * A simple holder for the resolved state of a fulfilled Promise, the value
   * of which may be null.
   *
   * @param <T> fulfillment type
   */
  @Immutable
  private static final class FulfilledResolution<T> implements Resolution<T> {
    private final T value;

    FulfilledResolution(T value) {
      this.value = value;
    }

    @Override
    public T getValue() {
      return value;
    }

    @Override
    public Exception getReason() {
      return null;
    }
  }

  /**
   * A simple holder for the resolved state of a rejected Promise, the value
   * of which must be an Exception and may be a RejectedException with no
   * reason.
   *
   * @param <T> fulfillment type (which will not be available due to rejection)
   */
  @Immutable
  private static final class RejectedResolution<T> implements Resolution<T> {
    private final Exception reason;

    RejectedResolution(Exception reason) {
      this.reason = reason;
    }

    @Override
    public T getValue() {
      return null;
    }

    @Override
    public Exception getReason() {
      return reason;
    }
  }

  @Override
  public boolean isResolved() {
    return resolution.get() != null;
  }

  @Override
  public boolean isFulfilled() {
    return resolution.get() instanceof FulfilledResolution;
  }

  @Override
  public boolean isRejected() {
    return resolution.get() instanceof RejectedResolution;
  }

  /**
   * Attempts to fulfill the state with the provided value.
   * <p>
   * The provided value may be null and the state will be fulfilled with null as
   * the value.
   * </p>
   * <p>
   * If the state is already resolved (either fulfilled or rejected), nothing will
   * occur.
   * </p>
   *
   * @param t nullable value to fulfill the promise with
   */
  public void offerFulfillment(T t) {
    if (resolution.compareAndSet(null, new FulfilledResolution<>(t))) {
      latch.countDown();
    }
  }

  /**
   * Attempts to reject the state with the provided reason.
   * <p>
   * If the state is already resolved (either fulfilled or rejected), nothing will
   * occur.
   * </p>
   *
   * @param reason nullable reason for rejecting the promise
   */
  public void offerRejection(Exception reason) {
    if (reason == null) {
      reason = RejectedException.wrap(null);
    }
    if (resolution.compareAndSet(null, new RejectedResolution<>(reason))) {
      latch.countDown();
    }
  }

  @Override
  public T get() throws Exception {
    latch.await();
    Resolution<T> res = resolution.get();
    if (res instanceof RejectedResolution) {
      throw res.getReason();
    }
    return res.getValue();
  }

  @Override
  public T getValue() throws InterruptedException {
    latch.await();
    return resolution.get().getValue();
  }

  @Override
  public Exception getReason() throws InterruptedException {
    latch.await();
    return resolution.get().getReason();
  }
}

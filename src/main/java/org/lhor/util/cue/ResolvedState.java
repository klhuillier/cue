package org.lhor.util.cue;


/**
 * Maintains the fulfilled value or rejected reason of a Promise.
 * <p>
 * The resolved state is shared by the Deferred and its Promise. This
 * interface provides read-only access that the Promise needs, while the
 * concrete type can expose additional methods to allow the Deferred to
 * attempt to provide a resolved value (fulfillment or rejection).
 * </p>
 * <p>
 * There are two key requirements for ResolvedState. First, it must be
 * write-once. That is, once the instance is resolved, its state must be
 * regarded as immutable. (If it is fulfilled with a mutable Object, that
 * object can be modified freely.) If the state has a value, that value
 * cannot be replaced and a rejection reason cannot be provided. If the
 * state is rejected, the reason cannot be replaced and a fulfillment value
 * cannot be provided.
 * </p>
 * <p>
 * The second key requirement is that it must be threadsafe because no
 * fewer than two threads will be interacting with any instance, potentially
 * at the same time. The producer thread will offer a resolution value
 * (usually through a Deferred) while the consumer thread will be awaiting
 * resolution (usually through a Promise). A single Promise can have multiple
 * callbacks registered, so there could potentially be dozens of threads
 * waiting on a resolution.
 * </p>
 * <p>
 * An instance is "resolved" when if was either fulfilled or rejected. An
 * instance may be resolved with a value of null, but it must be given an
 * Exception if it is rejected.
 * </p>
 *
 * @param <T>
 */
interface ResolvedState<T> {
  /**
   * @return true if the state was fulfilled or rejected
   */
  boolean isResolved();

  /**
   * @return true if the state was fulfilled
   */
  boolean isFulfilled();

  /**
   * @return true if the state was rejected and there is a reason
   */
  boolean isRejected();

  /**
   * When the state is resolved, the value is returned if the state
   * is fulfilled or the rejection reason is rethrown.
   * <p>
   * This method will block the current thread until the state is resolved.
   * </p>
   * <p>
   * If state was rejected with a null exception, a new RejectedException
   * will be thrown with no wrapped reason.
   * </p>
   *
   * @return fulfillment value
   * @throws Exception rejection reason
   */
  T get() throws Exception;

  /**
   * When the state is resolved, the value is returned if the state is
   * fulfilled, otherwise null is returned.
   * <p>
   * This method will block the current thread until the state is resolved.
   * </p>
   * <p>
   * Because it is possible to fulfill a Promise with null for the value,
   * and because this will return null in the case of a rejection, it is
   * important to test {@link ResolvedState#isFulfilled()} if relying on
   * this method, otherwise it is safer to use {@link ResolvedState#get()}.
   * </p>
   *
   * @return the fulfillment value or null
   */
  T getValue() throws InterruptedException;

  /**
   * When the state is resolved, the rejection reason is returned if the
   * state is rejected (possibly with null), otherwise null is returned.
   * <p>
   * This method will block the current thread until the state is resolved.
   * </p>
   *
   * @return the rejection reason or null
   */
  Exception getReason() throws InterruptedException;
}

package org.lhor.util.cue;


/**
 * The event sink is how we can use a reasonably sized (possibly singular) thread pool
 * but still dispatch many listeners waiting for promises to resolve.
 * <p>
 * This essentially works similar to an ExecutorService. You can submit callbacks and
 * they will be invoked in a separate thread. The difference is that the callbacks are
 * not given a thread until the ResolvedState is ready for them to do work, allowing
 * for a thread pool of any size to handle as many callbacks as necessary.
 * </p>
 * <p>
 * Rather than having a spin-locked thread running continuously and checking for
 * resolved statuses, the sink requires notification when a state is resolved. An
 * implementation of Deferred, or equivalent that depends on this interface, must
 * send notification on resolution.
 * </p>
 * <p>
 * The ordering in which threads are dispatched is indeterminate. On resolution,
 * callbacks may be executed in any order. Furthermore, callbacks registered after
 * resolution will be executed in the shared thread pool, and their execution may
 * be delayed for an indeterminate period of time depending on implementation,
 * operating system scheduling, the Java runtime, etc. Rely on the
 * {@link Promise#done()} method if you need to ensure a callback has time to
 * complete its work.
 * </p>
 * <p>
 * Implementations <i>must</i> be thread-safe.
 * </p>
 */
interface EventSink {
  void register(ResolvedState<?> state, Runnable runnable);
  void stateResolved(ResolvedState<?> state);
}

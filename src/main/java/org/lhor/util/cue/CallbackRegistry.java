/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


/**
 * This registry is how we can use a reasonably sized (possibly singular) thread pool
 * but still accept many callbacks waiting for promises to resolve.
 * <p>
 * This essentially works similar to an ExecutorService. You can submit callbacks and
 * they will be invoked in a separate thread. The difference is that the callbacks are
 * not given a thread until the ResolvedState is ready for them to do work, allowing
 * for a thread pool of any size to handle as many callbacks as necessary. (Assuming
 * the callbacks return in a reasonable time.)
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
 * operating system scheduling, the Java runtime, how many other callbacks are
 * waiting, etc. Rely on the {@link Promise#done()} method if you need to ensure
 * a callback has time to complete its work.
 * </p>
 * <p>
 * Implementations <i>must</i> be thread-safe.
 * </p>
 */
interface CallbackRegistry {
  void register(ResolvedState<?> state, Runnable runnable);
  void stateResolved(ResolvedState<?> state);
}

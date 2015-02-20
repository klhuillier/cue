/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;


@ThreadSafe
final class CallbackRegistryImpl implements CallbackRegistry {
  /** Thread pool to run the ready runnables in */
  private final ExecutorService executorService;

  /** Maps states to a list of runnables waiting to execute whenever the state is resolved */
  @GuardedBy("lock")
  private final WeakHashMap<ResolvedState<?>, ArrayList<Runnable>> invokers = new WeakHashMap<>();
  /** A list of states that are known to have been resolved already */
  @GuardedBy("lock")
  private final WeakHashMap<ResolvedState<?>, Object> resolved = new WeakHashMap<>();
  /** A queue of runnables whose states are resolved and could be run immediately */
  private final ConcurrentLinkedQueue<Runnable> ready = new ConcurrentLinkedQueue<>();

  /** Used to synchronize most state. Also used as a dummy object for the resolved map */
  private final Object lock = new Object();

  @Inject
  public CallbackRegistryImpl(@CueExecutors ExecutorService executorService) {
    if (executorService == null) {
      throw new NullPointerException("executorService");
    }
    this.executorService = executorService;
  }

  @Override
  public void register(ResolvedState<?> state, Runnable invoker) {
    if (state == null) {
      throw new NullPointerException("state");
    } else if (invoker == null) {
      throw new NullPointerException("invoker");
    }

    synchronized (lock) {
      if (resolved.containsKey(state)) {
        ready.add(invoker);
      } else {
        ArrayList<Runnable> list = invokers.get(state);
        if (list == null) {
          list = new ArrayList<>();
          invokers.put(state, list);
        }
        list.add(invoker);
      }
    }
    submitWorkers();
  }

  @Override
  public void stateResolved(ResolvedState<?> state) {
    if (state == null) {
      throw new NullPointerException("state");
    }

    synchronized (lock) {
      ArrayList<Runnable> waiting = invokers.remove(state);
      if (waiting != null) {
        waiting.forEach(ready::add);
      }
      resolved.put(state, lock);
    }
    submitWorkers();
  }

  private void submitWorkers() {
    for (Runnable run = ready.poll(); run != null; run = ready.poll()) {
      executorService.submit(run);
    }
  }
}

package org.lhor.util.cue;


import com.google.inject.Inject;
import net.jcip.annotations.ThreadSafe;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;


@ThreadSafe
final class EventSinkImpl implements EventSink {
  /** Thread pool to run the ready runnables in */
  private final ExecutorService executorService;

  /** Maps states to a list of runnables waiting to execute whenever the state is resolved */
  private final WeakHashMap<ResolvedState<?>, ArrayList<Runnable>> invokers = new WeakHashMap<>();
  /** A list of states that are known to have been resolved already */
  private final WeakHashMap<ResolvedState<?>, Object> resolved = new WeakHashMap<>();
  /** A queue of runnables whose states are resolved and could be run immediately */
  private final ConcurrentLinkedQueue<Runnable> ready = new ConcurrentLinkedQueue<>();

  /** Used to synchronize most state. Also used as a dummy object for the resolved map */
  private final Object lock = new Object();

  @Inject
  public EventSinkImpl(@CueExecutors ExecutorService executorService) {
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

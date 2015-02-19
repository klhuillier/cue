package org.lhor.util.cue;


import java.util.ArrayList;


class MockEventSink implements EventSink {
  private ArrayList<Runnable> runnables = new ArrayList<>();

  @Override
  public void register(ResolvedState<?> state, Runnable runnable) {
    runnables.add(runnable);
  }

  public ArrayList<Runnable> drainRunnables() {
    ArrayList<Runnable> result = new ArrayList<>(runnables);
    runnables.clear();
    return result;
  }

  @Override
  public void stateResolved(ResolvedState<?> state) {
  }
}

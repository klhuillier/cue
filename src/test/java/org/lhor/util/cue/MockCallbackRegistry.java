/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


import java.util.ArrayList;


class MockCallbackRegistry implements CallbackRegistry {
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

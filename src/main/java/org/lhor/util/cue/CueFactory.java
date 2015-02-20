/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


import net.jcip.annotations.Immutable;
import javax.inject.Provider;
import java.util.concurrent.ExecutorService;


/**
 * A Factory for providing {@link Cue} instances without the use of the
 * Guice and {@link CueModule}.
 */
@Immutable
public final class CueFactory implements Provider<Cue> {
  private final ExecutorService executorService;

  // These are all little-s singletons maintained by the Factory, which makes the
  // get() method quite simple.
  private final Provider<Deferred> deferredProvider;
  private final CallbackRegistry callbackRegistry;
  private final Cue cue;

  public CueFactory(ExecutorService executorService) {
    if (executorService == null) {
      throw new NullPointerException("executorService");
    }
    this.executorService = executorService;
    callbackRegistry = new CallbackRegistryImpl(executorService);
    deferredProvider = new DeferredProvider();
    cue = new CueImpl(executorService, deferredProvider);
  }

  @Override
  public Cue get() {
    return cue;
  }

  private final class DeferredProvider implements Provider<Deferred> {
    @Override
    @SuppressWarnings("unchecked")
    public Deferred get() {
      ResolvedStateImpl resolvedState = new ResolvedStateImpl();
      Promise promise = new PromiseImpl<>(cue, callbackRegistry, resolvedState);
      return new DeferredImpl(callbackRegistry, resolvedState, promise);
    }
  }
}

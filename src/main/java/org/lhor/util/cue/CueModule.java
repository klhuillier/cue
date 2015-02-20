/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


import com.google.inject.AbstractModule;
import net.jcip.annotations.Immutable;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;


/**
 * A Guice module for Cue.
 * <p>
 * It is recommended the thread pool you provide can expand to provide several
 * threads and not be an instance of
 * {@link java.util.concurrent.Executors#newSingleThreadExecutor()}, especially
 * if using {@link Cue#whenFuture(java.util.concurrent.Future)} to adapt Futures
 * to Promises.
 * </p>
 */
@Immutable
public final class CueModule extends AbstractModule {
  private final ExecutorService executorService;

  /**
   * @param executorService not-null thread pool
   */
  public CueModule(ExecutorService executorService) {
    if (executorService == null) {
      throw new NullPointerException("executorService");
    }
    this.executorService = executorService;
  }

  @Override
  protected void configure() {
    bind(Cue.class).to(CueImpl.class).in(Singleton.class);
    bind(ExecutorService.class).annotatedWith(CueExecutors.class).toInstance(executorService);
    bind(Deferred.class).toProvider(DeferredProvider.class);
    bind(CallbackRegistry.class).to(CallbackRegistryImpl.class).in(Singleton.class);
  }

  @Immutable
  private static final class DeferredProvider implements Provider<Deferred> {
    private final Provider<Cue> cueProvider;
    private final Provider<CallbackRegistry> registryProvider;
    private final Provider<ResolvedStateImpl> resolvedStateProvider;

    @Inject
    public DeferredProvider(Provider<Cue> cueProvider, Provider<CallbackRegistry> registryProvider,
                            Provider<ResolvedStateImpl> resolvedStateProvider) {
      this.cueProvider = cueProvider;
      this.registryProvider = registryProvider;
      this.resolvedStateProvider = resolvedStateProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Deferred get() {
      Cue cue = cueProvider.get();
      CallbackRegistry callbackRegistry = registryProvider.get();
      // Raw types are used because there is no type parameter info available
      ResolvedStateImpl resolvedState = resolvedStateProvider.get();
      Promise promise = new PromiseImpl<>(cue, callbackRegistry, resolvedState);
      return new DeferredImpl(callbackRegistry, resolvedState, promise);
    }
  }
}

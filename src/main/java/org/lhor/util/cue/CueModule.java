package org.lhor.util.cue;


import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.jcip.annotations.Immutable;
import java.util.concurrent.ExecutorService;


/**
 * A Guice module for Cue.
 */
@Immutable
public final class CueModule extends AbstractModule {
  private final ExecutorService executorService;

  public CueModule(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  protected void configure() {
    bind(Cue.class).to(CueImpl.class).in(Singleton.class);
    bind(ExecutorService.class).annotatedWith(CueExecutors.class).toInstance(executorService);
    bind(Deferred.class).toProvider(DeferredProvider.class);
    bind(EventSink.class).to(EventSinkImpl.class).in(Singleton.class);
  }

  private static final class DeferredProvider implements Provider<Deferred> {
    private final Provider<Cue> cueProvider;
    private final Provider<EventSink> eventSinkProvider;
    private final Provider<ResolvedStateImpl> resolvedStateProvider;

    @Inject
    public DeferredProvider(Provider<Cue> cueProvider, Provider<EventSink> eventSinkProvider,
                            Provider<ResolvedStateImpl> resolvedStateProvider) {
      this.cueProvider = cueProvider;
      this.eventSinkProvider = eventSinkProvider;
      this.resolvedStateProvider = resolvedStateProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Deferred get() {
      Cue cue = cueProvider.get();
      EventSink eventSink = eventSinkProvider.get();
      // Raw types are used because there is no type parameter info available
      ResolvedStateImpl resolvedState = resolvedStateProvider.get();
      Promise promise = new PromiseImpl<>(cue, eventSink, resolvedState);
      return new DeferredImpl(eventSink, resolvedState, promise);
    }
  }
}

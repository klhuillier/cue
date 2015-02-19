package org.lhor.util.cue;


import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.Test;

import java.util.concurrent.ForkJoinPool;


public class PreconditionsTests {
  private final ForkJoinPool executorService = ForkJoinPool.commonPool();
  private final Injector injector = Guice.createInjector(new CueModule(executorService));
  private final Cue cue = injector.getInstance(Cue.class);
  private final Deferred<Object> deferred = cue.defer();
  private final Promise<Object> promise = deferred.promise();
  private final EventSink eventSink = injector.getInstance(EventSink.class);
  private final ResolvedStateImpl<Object> resolvedState = new ResolvedStateImpl<>();

  @Test(expected = NullPointerException.class)
  public void testCueModuleExecutorsNpe() {
    new CueModule(null);
  }

  @Test(expected = NullPointerException.class)
  public void testCueImplExecutorsNpe() {
    new CueImpl(null, () -> null);
  }

  @Test(expected = NullPointerException.class)
  public void testCueImplDeferredProviderNpe() {
    new CueImpl(executorService, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCueImplAllPromisesNpe() {
    cue.all(null);
  }

  @Test(expected = NullPointerException.class)
  public void testCueImplAllFuturesNpe() {
    cue.allFutures(null);
  }

  @Test(expected = NullPointerException.class)
  public void testCueImplWhenFutureNpe() {
    cue.whenFuture(null);
  }

  @Test(expected = NullPointerException.class)
  public void testDeferredImplEventSinkNpe() {
    new DeferredImpl<>(null, resolvedState, promise);
  }

  @Test(expected = NullPointerException.class)
  public void testDeferredImplStateNpe() {
    new DeferredImpl<>(eventSink, null, promise);
  }

  @Test(expected = NullPointerException.class)
  public void testDeferredImplPromiseNpe() {
    new DeferredImpl<>(eventSink, resolvedState, null);
  }

  @Test(expected = NullPointerException.class)
  public void testDeferredImplResolveFromNpe() {
    deferred.resolveFrom(null);
  }

  @Test(expected = NullPointerException.class)
  public void testEventSinkExecutorsNpe() {
    new EventSinkImpl(null);
  }

  @Test(expected = NullPointerException.class)
  public void testEventSinkRegisterStateNpe() {
    eventSink.register(null, () -> {});
  }

  @Test(expected = NullPointerException.class)
  public void testEventSinkRegisterCallbackNpe() {
    eventSink.register(resolvedState, null);
  }

  @Test(expected = NullPointerException.class)
  public void testEventSinkResolvedNpe() {
    eventSink.stateResolved(null);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplCueNpe() {
    new PromiseImpl<>(null, eventSink, resolvedState);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplEventSinkNpe() {
    new PromiseImpl<>(cue, null, resolvedState);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplStateNpe() {
    new PromiseImpl<>(cue, eventSink, null);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplCallbackNpe() {
    promise.then((Callback) null);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplNullCallbackNpe() {
    promise.then((NullCallback) null);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplVoidCallbackNpe() {
    promise.then((VoidCallback) null);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplNullVoidCallbackNpe() {
    promise.then((NullVoidCallback) null);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplErrbackNpe() {
    promise.fail((Errback) null);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplVoidErrbackNpe() {
    promise.fail((VoidErrback) null);
  }

  @Test(expected = NullPointerException.class)
  public void testPromiseImplAlwaysCallbackNpe() {
    promise.always(null);
  }
}

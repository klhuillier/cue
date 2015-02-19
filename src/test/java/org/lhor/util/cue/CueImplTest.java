package org.lhor.util.cue;


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class CueImplTest {
  private static final ForkJoinPool executors = new ForkJoinPool(50);
  private static final Injector injector = Guice.createInjector(new CueModule(executors));
  private static final Cue cue = injector.getInstance(Cue.class);

  @Test
  public void testWhenFulfilledDone() {
    Promise<String> promise = cue.when("fulfilled");
    Assert.assertEquals("fulfilled", promise.done());
  }

  @Test(expected = RejectedException.class)
  public void testWhenRejectedDone() {
    Promise<Object> promise = cue.reject(new Exception());
    promise.done();
  }

  @Test
  public void testResolvedThenCallback() {
    String expectBase = "expected";
    Integer number = 123;
    String expected = expectBase + number;
    Deferred<Integer> deferred = cue.defer();
    Promise<String> result = deferred.promise().then(i -> expectBase + i);
    deferred.resolve(number);
    Assert.assertEquals(expected, result.done());
  }

  @Test
  public void testResolvedLongCallbackChain() {
    String expectBase = "expected";
    Integer[] numbers = {1, 2, 3};
    String[] letters = {"a", "b", "c"};
    Deferred<String> deferred = cue.defer();
    Promise<String> result = deferred.promise()
        .then(s -> s + numbers[0])
        .then(s -> s + numbers[1])
        .then(s -> s + numbers[2])
        .then(s -> s + letters[0])
        .then(s -> s + letters[1])
        .then(s -> s + letters[2]);
    deferred.resolve(expectBase);
    Assert.assertEquals("expected123abc", result.done());
  }

  @Test
  public void testResolvedNullCallback() {
    String expectBase = "expected";
    String expected = expectBase + "123";
    Deferred<String> deferred = cue.defer();
    Promise<String> result = deferred.promise()
          .then(() -> expectBase + "123");
    deferred.resolve("asdf");
    Assert.assertEquals(expected, result.done());
  }

  @Test
  public void testResolvedVoidCallback() {
    String expected = "expected";
    String[] received = new String[1];
    Deferred<String> deferred = cue.defer();
    Promise<String> result = deferred.promise()
          .then(s -> { received[0] = s; });
    deferred.resolve(expected);
    Assert.assertEquals(expected, result.done());
    Assert.assertEquals(expected, received[0]);
  }

  @Test
  // Checks every type of callback/errback in a chain
  public void testResolvedMixedChain() {
    String expectBase = "expected";
    Integer[] numbers = {1, 2, 3};
    AtomicBoolean nvcCalled = new AtomicBoolean(false);
    AtomicReference<String> vcCalled = new AtomicReference<>();
    AtomicBoolean veCalled = new AtomicBoolean(false);
    AtomicBoolean nvcAlways = new AtomicBoolean(false);
    Deferred<String> deferred = cue.defer();
    Promise<String> result = deferred.promise()
          .then(() -> expectBase)
          .then(s -> s + numbers[0])
          .then(() -> {
            nvcCalled.set(true);
          })
          .then(s -> s + numbers[1])
          .then(s -> {
            vcCalled.set(s);
          })
          .fail(ex -> {
            veCalled.set(true);
          })
          .fail(ex -> "" + numbers[0])
          .always(() -> nvcAlways.set(true))
          .then(s -> s + numbers[2]);

    deferred.resolve("asdf");
    result.done();

    Assert.assertTrue(nvcCalled.get());
    Assert.assertEquals("expected12", vcCalled.get());
    Assert.assertFalse(veCalled.get());
    Assert.assertTrue(nvcAlways.get());
    Assert.assertEquals("expected123", result.done());
  }

  @Test
  public void testRejectedErrback() {
    String expected = "expected";
    Deferred<String> deferred = cue.defer();
    Promise<String> result = deferred.promise()
            .then((Callback) s -> { throw new NullPointerException(); })
            .fail((Errback) ex -> expected);

    deferred.resolve(null);
    Assert.assertEquals(expected, result.done());
  }

  @Test
  public void testRejectedVoidErrback() {
    String expected = "expected";
    AtomicReference<String> message = new AtomicReference<>();

    Deferred<String> deferred = cue.defer();
    Promise<String> result = deferred.promise()
        .then(s -> s)
        .fail(ex -> { message.set(ex.getMessage()); });

    deferred.reject(new Exception(expected));

    try {
      result.done();
      Assert.fail();
    } catch (RejectedException e) {
      Assert.assertEquals(expected, e.getCause().getMessage());
      Assert.assertEquals(expected, message.get());
    }
  }

  @Test
  public void testMultipleCallbacks() {
    String expected = "expected";

    Deferred<String> deferred = cue.defer();
    Promise<String> promise = deferred.promise();

    Promise<String> p2 = promise.then((Callback) s -> {
      throw new IllegalArgumentException();
    });
    Promise<String> p3 = promise.then(s -> s);
    Promise<String> result = promise.then(s -> expected);
    deferred.resolve("asdf");

    // Make sure the other two have had a chance to go
    try { p2.done(); } catch (RejectedException e) {}
    p3.done();

    // Now check the expected result
    Assert.assertEquals(expected, result.done());
  }

  @Test
  public void testMultipleNullCallbacks() {
    String expected = "expected";
    String unexpected = "asdf";

    Deferred<String> deferred = cue.defer();
    Promise<String> promise = deferred.promise();

    Promise<String> p2 = promise.then((NullCallback) () -> {
      throw new IllegalArgumentException();
    });
    Promise<String> p3 = promise.then(() -> unexpected);
    Promise<String> result = promise.then(() -> expected);
    deferred.resolve(unexpected);

    // Make sure the other two have had a chance to go
    try { p2.done(); } catch (RejectedException e) {}
    p3.done();

    // Now check the expected result
    Assert.assertEquals(expected, result.done());
  }

  @Test
  public void testThrowingCallbackReplacementErrback() {
    String expected = "expected";
    String unexpected = "asdf";

    Deferred<String> deferred = cue.defer();
    Promise<String> result = deferred.promise()
            .then((Callback) s -> { throw new IllegalArgumentException(); })
            .then((Callback) s -> unexpected)
            .fail((Errback) ex -> expected);

    deferred.resolve(unexpected);

    // Now check the expected result
    Assert.assertEquals(expected, result.done());
  }

  // This test serves two purposes:
  // 1. It tests that the ordering of resolution operates in the expected way
  // 2. It tests that a ridiculous number of threads can still resolve
  // (Note that if the number of the parked threads is the thread pool maximum,
  // no new threads will be created which can complete.)
  @Test
  public void testFlurryOfResolves() {
    String expected = "expected";
    String unexpected = "asdf";
    CountDownLatch latch = new CountDownLatch(1);

    Deferred<String> deferred1 = cue.defer();
    Deferred<String> deferred2 = cue.defer();
    Promise<String> promise1 = deferred1.promise();
    Promise<String> promise2 = deferred2.promise();

    for (int i = 0; i < 20; i++) {
      promise1.then(s -> { latch.await(); deferred2.resolve(s); });
      // Stick the correct result in the middle
      if (i == 10) {
        promise1.then(s -> {
          deferred2.resolve(expected);
          latch.countDown();
        });
      }
    }

    deferred1.resolve(unexpected);
    Assert.assertEquals(expected, promise2.done());
  }

  @Test
  public void testFlurryOfRejects() {
    Exception expected = new IllegalArgumentException();
    Exception unexpected = new NullPointerException();
    CountDownLatch latch = new CountDownLatch(1);

    Deferred<String> deferred1 = cue.defer();
    Deferred<String> deferred2 = cue.defer();
    Promise<String> promise1 = deferred1.promise();
    Promise<String> promise2 = deferred2.promise();

    for (int i = 0; i < 20; i++) {
      promise1.then(() -> {
        latch.await();
        deferred2.reject(unexpected);
      });

      if (i == 10) {
        promise1.then(() -> {
          deferred2.reject(expected);
          latch.countDown();
        });
      }
    }

    deferred1.resolve("");
    try {
      promise2.done();
      Assert.fail();
    } catch (Exception e) {
      Assert.assertSame(expected, e.getCause());
    }
  }

  @Test
  public void testFlurryOfRejectsAndResolves() {
    String expected = "expected";
    String unexpected = "asdf";
    CountDownLatch latch = new CountDownLatch(1);

    Deferred<String> deferred1 = cue.defer();
    Deferred<String> deferred2 = cue.defer();
    Promise<String> promise1 = deferred1.promise();
    Promise<String> promise2 = deferred2.promise();

    for (int i = 0; i < 20; i++) {
      if (i % 2 == 0) {
        promise1.then(() -> {
          latch.await();
          deferred2.reject(null);
        });
      } else {
        promise1.then(() -> {
          latch.await();
          deferred2.resolve(unexpected);
        });
      }

      if (i == 10) {
        promise1.then(() -> {
          deferred2.resolve(expected);
          latch.countDown();
        });
      }
    }

    deferred1.resolve(unexpected);
    Assert.assertEquals(expected, promise2.done());
  }

  @Test
  public void testNullReject() {
    Deferred<String> deferred = cue.defer();
    deferred.reject();
    try {
      deferred.promise().done();
    } catch (Exception e) {
      Assert.assertTrue(e instanceof RejectedException);
      Assert.assertNull(e.getCause());
    }
  }

  @Test
  public void testInterruptedWhileWaitingResolve() {
    Deferred<String> deferred = cue.defer();
    Promise<String> promise = deferred.promise();

    Thread.currentThread().interrupt();
    try {
      promise.done();
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e.getCause() instanceof InterruptedException);
    }
  }

  // These interrupt tests are difficult because the callbacks are
  // generally not invoked until the state is resolved. What can be
  // done is wire up a fake ResolvedState that always throws
  // InterruptedExceptions
  private final static class InterruptedResolvedState<T> extends MockResolvedState<T> {
    @Override
    public T get() throws Exception {
      throw new InterruptedException();
    }

    @Override
    public T getValue() throws InterruptedException {
      throw new InterruptedException();
    }

    @Override
    public Exception getReason() throws InterruptedException {
      throw new InterruptedException();
    }
  }

  @Test
  public void testCallbackInterrupted() {
    MockEventSink eventSink = new MockEventSink();
    PromiseImpl<String> promise = new PromiseImpl<>(cue, eventSink, new InterruptedResolvedState<>());
    AtomicBoolean called = new AtomicBoolean(false);
    Promise<String> result = promise.then(s -> {
      called.set(true);
      return s;
    });
    eventSink.drainRunnables().forEach(r -> r.run());
    try {
      result.done();
      Assert.fail();
    } catch (RejectedException e) {
      Assert.assertFalse(called.get());
      Assert.assertTrue(e.getReason() instanceof InterruptedException);
    }
  }

  @Test
  public void testNullCallbackInterrupted() {
    MockEventSink eventSink = new MockEventSink();
    PromiseImpl<String> promise = new PromiseImpl<>(cue, eventSink, new InterruptedResolvedState<>());
    AtomicBoolean called = new AtomicBoolean(false);
    Promise<String> result = promise.then(() -> {
      called.set(true);
      return "";
    });
    eventSink.drainRunnables().forEach(r -> r.run());
    try {
      result.done();
      Assert.fail();
    } catch (RejectedException e) {
      Assert.assertFalse(called.get());
      Assert.assertTrue(e.getReason() instanceof InterruptedException);
    }
  }

  @Test
  public void testVoidCallbackInterrupted() {
    MockEventSink eventSink = new MockEventSink();
    PromiseImpl<String> promise = new PromiseImpl<>(cue, eventSink, new InterruptedResolvedState<>());
    AtomicBoolean called = new AtomicBoolean(false);
    Promise<String> result = promise.then(s -> {
      called.set(true);
    });
    eventSink.drainRunnables().forEach(r -> r.run());
    try {
      result.done();
      Assert.fail();
    } catch (RejectedException e) {
      Assert.assertFalse(called.get());
      Assert.assertTrue(e.getReason() instanceof InterruptedException);
    }
  }

  @Test
  public void testNullVoidCallbackInterrupted() {
    MockEventSink eventSink = new MockEventSink();
    PromiseImpl<String> promise = new PromiseImpl<>(cue, eventSink, new InterruptedResolvedState<>());
    AtomicBoolean called = new AtomicBoolean(false);
    Promise<String> result = promise.then(() -> {
      called.set(true);
    });
    eventSink.drainRunnables().forEach(r -> r.run());
    try {
      result.done();
      Assert.fail();
    } catch (RejectedException e) {
      Assert.assertFalse(called.get());
      Assert.assertTrue(e.getReason() instanceof InterruptedException);
    }
  }

  @Test
  public void testErrbackInterrupted() {
    MockEventSink eventSink = new MockEventSink();
    PromiseImpl<String> promise = new PromiseImpl<>(cue, eventSink, new InterruptedResolvedState<>());
    AtomicBoolean called = new AtomicBoolean(false);
    Promise<String> result = promise.fail(ex -> {
      called.set(true);
      return "";
    });
    eventSink.drainRunnables().forEach(r -> r.run());
    try {
      result.done();
      Assert.fail();
    } catch (RejectedException e) {
      Assert.assertFalse(called.get());
      Assert.assertTrue(e.getReason() instanceof InterruptedException);
    }
  }

  @Test
  public void testVoidErrbackInterrupted() {
    MockEventSink eventSink = new MockEventSink();
    PromiseImpl<String> promise = new PromiseImpl<>(cue, eventSink, new InterruptedResolvedState<>());
    AtomicBoolean called = new AtomicBoolean(false);
    Promise<String> result = promise.fail(ex -> {
      called.set(true);
    });
    eventSink.drainRunnables().forEach(r -> r.run());
    try {
      result.done();
      Assert.fail();
    } catch (RejectedException e) {
      Assert.assertFalse(called.get());
      Assert.assertTrue(e.getReason() instanceof InterruptedException);
    }
  }

  @Test
  public void testAlwaysCallbackInterrupted() {
    MockEventSink eventSink = new MockEventSink();
    PromiseImpl<String> promise = new PromiseImpl<>(cue, eventSink, new InterruptedResolvedState<>());
    AtomicBoolean called = new AtomicBoolean(false);
    Promise<String> result = promise.always(() -> {
      called.set(true);
    });
    eventSink.drainRunnables().forEach(r -> r.run());
    try {
      result.done();
      Assert.fail();
    } catch (RejectedException e) {
      Assert.assertTrue(called.get());
      Assert.assertTrue(e.getReason() instanceof InterruptedException);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResolveFromOwnPromise() {
    Deferred<String> deferred = cue.defer();
    deferred.resolveFrom(deferred.promise());
  }

  @Test
  public void testWhenFutureFulfilled() {
    String expected = "expected";
    CountDownLatch latch = new CountDownLatch(1);
    Future<String> future = executors.submit(() -> {
      latch.await();
      return expected;
    });
    Promise<String> result = cue.whenFuture(future);
    latch.countDown();
    String actual = result.then((String s) -> s.toUpperCase()).done();
    Assert.assertEquals("EXPECTED", actual);
  }

  @Test
  public void testWhenFutureCancelled() {
    String expected = "expected";
    CountDownLatch latch = new CountDownLatch(1);
    Future<String> future = executors.submit(() -> {
      latch.await();
      return expected;
    });
    Promise<String> result = cue.whenFuture(future);
    future.cancel(true);
    try {
      result.done();
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e.getCause() instanceof CancellationException);
    }
  }

  @Test
  public void testAllPromisesResolved() {
    String[] expected = {"a", "b", "c"};
    ArrayList<Deferred<String>> deferreds = new ArrayList<>(3);
    ArrayList<Promise<String>> promises = new ArrayList<>(3);
    Arrays.stream(expected).forEach(str -> {
      Deferred<String> deferred = cue.defer();
      promises.add(deferred.promise());
      deferreds.add(deferred);
    });
    Promise<List<String>> result = cue.all(promises);
    for (int i = 0; i < expected.length; i++) {
      deferreds.get(i).resolve(expected[i]);
    }
    List<String> actual = result.done();
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals(expected[i], actual.get(i));
    }
  }

  @Test(expected = RejectedException.class)
  public void testAllPromisesRejected() {
    ArrayList<Deferred<String>> deferreds = new ArrayList<>(3);
    ArrayList<Promise<String>> promises = new ArrayList<>(3);
    for (int i = 0; i < 3; i++) {
      deferreds.add(cue.defer());
      promises.add(deferreds.get(i).promise());
    }
    Promise<List<String>> result = cue.all(promises);
    deferreds.get(0).resolve("a");
    deferreds.get(1).resolve("b");
    deferreds.get(2).reject();
    result.done();
  }

  @Test
  public void testAllFuturesResolved() {
    ArrayList<Future<String>> futures = new ArrayList<>(3);
    CountDownLatch latch = new CountDownLatch(1);
    futures.add(executors.submit(() -> { latch.await(); return "a"; }));
    futures.add(executors.submit(() -> { latch.await(); return "b"; }));
    futures.add(executors.submit(() -> {
      latch.await();
      return "c";
    }));
    Promise<List<String>> result = cue.allFutures(futures);
    latch.countDown();
    List<String> actual = result.done();
    Assert.assertEquals("a", actual.get(0));
    Assert.assertEquals("b", actual.get(1));
    Assert.assertEquals("c", actual.get(2));
  }

  @Test(expected = RejectedException.class)
  public void testAllFuturesRejected() {
    ArrayList<Future<String>> futures = new ArrayList<>(3);
    CountDownLatch latch = new CountDownLatch(1);
    futures.add(executors.submit(() -> { latch.await(); return "a"; }));
    futures.add(executors.submit(() -> { latch.await(); return "b"; }));
    futures.add(executors.submit(() -> { latch.await(); return "c"; }));
    futures.get(2).cancel(true);
    Promise<List<String>> result = cue.allFutures(futures);
    latch.countDown();
    result.done();
  }
}

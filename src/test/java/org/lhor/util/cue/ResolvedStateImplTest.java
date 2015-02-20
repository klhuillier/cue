/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;


public final class ResolvedStateImplTest {
  private final ForkJoinPool executors = new ForkJoinPool(50);
  private final ResolvedStateImpl<String> state = new ResolvedStateImpl<>();

  @Test
  public void testFulfills() throws Exception {
    String expected = "expected";
    String unexpected = "asdf";
    CountDownLatch latch = new CountDownLatch(1);

    for (int i = 0; i < 50; i++) {
      if (i == 25) {
        executors.submit(() -> {
          state.offerFulfillment(expected);
          latch.countDown();
        });
      } else {
        executors.submit(() -> {
          latch.await();
          state.offerFulfillment(unexpected);
          return null;
        });
      }
    }

    Assert.assertEquals(expected, state.get());
    Assert.assertEquals(expected, state.getValue());
    Assert.assertTrue(state.isResolved());
    Assert.assertTrue(state.isFulfilled());
    Assert.assertFalse(state.isRejected());
    Assert.assertNull(state.getReason());
  }

  @Test
  public void testFulfillsAndRejects() throws Exception {
    String expected = "expected";
    String unexpected = "asdf";
    Exception reason = new Exception();
    CountDownLatch latch = new CountDownLatch(1);

    for (int i = 0; i < 50; i++) {
      if (i == 25) {
        executors.submit(() -> {
          state.offerFulfillment(expected);
          latch.countDown();
        });
      } else if (i % 2 == 0) {
        executors.submit(() -> {
          latch.await();
          state.offerFulfillment(unexpected);
          return null;
        });
      } else {
        executors.submit(() -> {
          latch.await();
          state.offerRejection(reason);
          return null;
        });
      }
    }

    Assert.assertEquals(expected, state.get());
    Assert.assertEquals(expected, state.getValue());
    Assert.assertTrue(state.isResolved());
    Assert.assertTrue(state.isFulfilled());
    Assert.assertFalse(state.isRejected());
    Assert.assertNull(state.getReason());
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testRejectsAndFulfills() throws Exception {
    Exception expected = new ArrayIndexOutOfBoundsException();
    String unexpected = "asdf";
    Exception reason = new Exception();
    CountDownLatch latch = new CountDownLatch(1);

    for (int i = 0; i < 50; i++) {
      if (i == 25) {
        executors.submit(() -> {
          state.offerRejection(expected);
          latch.countDown();
        });
      } else if (i % 2 == 0) {
        executors.submit(() -> {
          latch.await();
          state.offerFulfillment(unexpected);
          return null;
        });
      } else {
        executors.submit(() -> {
          latch.await();
          state.offerRejection(reason);
          return null;
        });
      }
    }

    Assert.assertNull(state.getValue());
    Assert.assertTrue(state.isResolved());
    Assert.assertFalse(state.isFulfilled());
    Assert.assertTrue(state.isRejected());
    Assert.assertSame(expected, state.getReason());
    // Throw the expected AIOOBE
    state.get();
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testRejects() throws Exception {
    Exception expected = new ArrayIndexOutOfBoundsException();
    Exception unexpected = new Exception();
    CountDownLatch latch = new CountDownLatch(1);

    for (int i = 0; i < 50; i++) {
      if (i == 25) {
        executors.submit(() -> {
          state.offerRejection(expected);
          latch.countDown();
        });
      } else {
        executors.submit(() -> {
          latch.await();
          state.offerRejection(unexpected);
          return null;
        });
      }
    }

    Assert.assertNull(state.getValue());
    Assert.assertTrue(state.isResolved());
    Assert.assertFalse(state.isFulfilled());
    Assert.assertTrue(state.isRejected());
    Assert.assertSame(expected, state.getReason());
    // Throw the expected AIOOBE
    state.get();
  }
}

/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


import junit.framework.Assert;
import org.junit.Test;
import java.util.concurrent.ForkJoinPool;


// Simple tests to make sure the CueFactory appears to be wiring everything up
// correctly.
public class CueFactoryTest {
  private final ForkJoinPool executorService = ForkJoinPool.commonPool();
  private final CueFactory cueFactory = new CueFactory(executorService);
  private final Cue cue = cueFactory.get();

  @Test
  public void testSimpleChain() {
    String expected = "expected";
    Deferred<String> deferred = cue.defer();
    Promise<String> promise = deferred.promise()
            .then(() -> expected)
            .fail(ex -> {});
    deferred.resolve("asdf");
    Assert.assertEquals(expected, promise.done());
  }
}

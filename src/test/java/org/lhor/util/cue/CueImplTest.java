package org.lhor.util.cue;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CueImplTest {
  private Cue cue;
  private ExecutorService executors;

  @BeforeClass
  public void beforeClass() {
    executors = Executors.newFixedThreadPool(8);
    cue = Cue.newCue();
  }

  @AfterClass
  public void afterClass() {
    executors.shutdown();
  }

  @Test
  public void testWhenFulfilledThen() {
    cue.when("fulfilled");
  }
}

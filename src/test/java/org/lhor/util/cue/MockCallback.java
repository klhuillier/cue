package org.lhor.util.cue;


final class MockCallback<I, O> implements Callback<I, O> {
  private I calledWith;
  private final O returned;
  private final Exception thrown;

  public MockCallback(O returned, Exception thrown) {
    this.returned = returned;
    this.thrown = thrown;
  }

  public MockCallback(O returned) {
    this(returned, null);
  }

  public MockCallback(Exception thrown) {
    this(null, thrown);
  }

  @Override
  public O call(I i) throws Exception {
    calledWith = i;
    if (returned == null) {
      throw thrown;
    }
    return returned;
  }

  public I getCalledWith() {
    return calledWith;
  }
}

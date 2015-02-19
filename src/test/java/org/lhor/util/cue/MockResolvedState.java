package org.lhor.util.cue;


class MockResolvedState<T> implements ResolvedState<T> {
  private T value;
  private Exception reason;

  @Override
  public boolean isResolved() {
    return value != null || reason != null;
  }

  @Override
  public boolean isFulfilled() {
    return value != null;
  }

  @Override
  public boolean isRejected() {
    return reason != null;
  }

  @Override
  public T get() throws Exception {
    if (reason != null) {
      throw reason;
    }
    return value;
  }

  @Override
  public T getValue() throws InterruptedException {
    return value;
  }

  @Override
  public Exception getReason() throws InterruptedException {
    return reason;
  }
}

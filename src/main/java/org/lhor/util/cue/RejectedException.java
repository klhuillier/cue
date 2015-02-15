package org.lhor.util.cue;


public final class RejectedException extends RuntimeException {
  public RejectedException(Throwable cause) {
    super(cause);
  }

  protected static RejectedException wrap(Throwable cause) {
    if (cause instanceof RejectedException) {
      return (RejectedException) cause;
    }
    return new RejectedException(cause);
  }
}

package org.lhor.util.cue;


/**
 * An error handler which does not affect the resolved state of the
 * subsequent Promise in the chain.
 */
@FunctionalInterface
public interface VoidErrback {
  public static final VoidErrback NOOP = (ex) -> {};

  void call(Exception err) throws Exception;
}

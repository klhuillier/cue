package org.lhor.util.cue;


/**
 * A nullary callback which receives no parameters and returns no value.
 * <p>
 * A type of callback which does not care about the current resolved state
 * and it should not affect the resolved state of any subsequent Promises in
 * the chain.
 * </p>
 */
@FunctionalInterface
public interface NullVoidCallback {
  public static final NullVoidCallback NOOP = () -> {};

  void call() throws Exception;
}

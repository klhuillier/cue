package org.lhor.util.cue;


/**
 * A callback which always returns no value.
 * <p>
 * This should be used when it requires the current resolved value, but it is
 * intended that the result of the callback should not affect the resolved state
 * of the subsequent Promise in the chain.
 * </p>
 *
 * @param <I>
 */
@FunctionalInterface
public interface VoidCallback<I>  {
  public static final VoidCallback NOOP = i -> {};

  /**
   * @param value
   * @throws Exception
   */
  void call(I value) throws Exception;
}

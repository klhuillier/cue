package org.lhor.util.cue;


/**
 * A callback which receives a value but produces no value.
 *
 * @param <I>
 */
@FunctionalInterface
public interface VoidCallback<I>  {
  /**
   * @param value nullable fulfillment value
   * @throws Exception
   */
  void call(I value) throws Exception;
}

package org.lhor.util.cue;


/**
 * A callback which receives a value and produces a new value.
 *
 * @param <I> input argument
 * @param <O> return value
 */
@FunctionalInterface
public interface Callback<I, O> {
  O call(I i) throws Exception;
}

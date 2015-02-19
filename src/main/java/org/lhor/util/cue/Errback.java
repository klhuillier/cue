package org.lhor.util.cue;


/**
 * A callback which receives an Exception and produces a value.

 * @param <O> the return type for a possible replacement value on error
 */
@FunctionalInterface
public interface Errback<O> {
  O call(Exception err) throws Exception;
}

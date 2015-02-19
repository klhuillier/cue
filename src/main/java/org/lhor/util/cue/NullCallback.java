package org.lhor.util.cue;


/**
 * A callback which produces a value.
 *
 * @param <O>
 */
@FunctionalInterface
public interface NullCallback<O> {
  O call() throws Exception;
}

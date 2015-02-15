package org.lhor.util.cue;


/**
 * A callback which produces a value that the subsequent Promise in the chain
 * will be resolved with.
 *
 * @param <O>
 */
@FunctionalInterface
public interface NullCallback<O> {
  public static final NullCallback NOOP = () -> null;

  O call() throws Exception;
}

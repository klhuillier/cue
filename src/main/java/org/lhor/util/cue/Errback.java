package org.lhor.util.cue;


/**
 * An error handler which returns a value for the subsequent Promise in
 * the chain.
 *
 * @param <O>
 */
@FunctionalInterface
public interface Errback<O> {
  public static final Errback NOOP = (ex) -> null;

  O call(Exception err) throws Exception;
}

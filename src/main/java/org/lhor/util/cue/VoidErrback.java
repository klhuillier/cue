package org.lhor.util.cue;


/**
 * A callback which receives an Exception and does not produce a value.
 */
@FunctionalInterface
public interface VoidErrback {
  void call(Exception err) throws Exception;
}

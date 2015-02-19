package org.lhor.util.cue;


/**
 * A callback which receives no parameters and returns no value.
 */
@FunctionalInterface
public interface NullVoidCallback {
  void call() throws Exception;
}

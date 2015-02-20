/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


/**
 * A callback which receives an Exception and does not produce a value.
 */
@FunctionalInterface
public interface VoidErrback {
  void call(Exception err) throws Exception;
}

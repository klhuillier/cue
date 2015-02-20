/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


/**
 * A callback which receives no parameters and returns no value.
 */
@FunctionalInterface
public interface NullVoidCallback {
  void call() throws Exception;
}

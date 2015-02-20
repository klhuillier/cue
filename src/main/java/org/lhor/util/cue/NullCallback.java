/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


/**
 * A callback which produces a value.
 *
 * @param <O> return value
 */
@FunctionalInterface
public interface NullCallback<O> {
  O call() throws Exception;
}

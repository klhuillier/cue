/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


/**
 * A callback which receives an Exception and produces a value.

 * @param <O> the return type for a possible replacement value on error
 */
@FunctionalInterface
public interface Errback<O> {
  O call(Exception err) throws Exception;
}

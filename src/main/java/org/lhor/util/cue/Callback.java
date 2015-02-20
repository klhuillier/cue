/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


/**
 * A callback which receives a value and produces a new value.
 *
 * @param <I> input argument
 * @param <O> return value
 */
@FunctionalInterface
public interface Callback<I, O> {
  O call(I i) throws Exception;
}

/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


/**
 * A callback which receives a value but produces no value.
 *
 * @param <I> fulfillment value type
 */
@FunctionalInterface
public interface VoidCallback<I>  {
  void call(I value) throws Exception;
}

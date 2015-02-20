/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

/**
 * An implementation of the <a href="https://promisesaplus.com/">Promise/A+</a>
 * specification in Java, and inspired by (and named for)
 * <a href="https://github.com/kriskowal/q/wiki/API-Reference">the Q library</a>.
 * <p>
 * Instances of Cue are served up by Google Guice by including
 * {@link org.lhor.util.cue.CueModule} in your injector's configuration, or by
 * using the plain Java factory {@link org.lhor.util.cue.CueFactory}.
 * </p>
 * <p>
 * Users of the Cue library will need Java 8 or later because the API makes
 * for heavy use of Java 8's lambda features. (Adapting it to earlier versions
 * of Java would make the required syntax far too verbose to make it worth
 * using, so there is no attempt made to support older versions.)
 * </p>
 */
package org.lhor.util.cue;

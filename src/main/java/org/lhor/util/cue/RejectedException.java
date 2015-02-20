/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


import net.jcip.annotations.Immutable;


/**
 * A RejectedException is thrown when a Deferred is rejected or a callback
 * throws an exception. For this reason, it almost always wraps an
 * exception (the reason) and should be constructed with
 * {@link RejectedException#wrap(Exception)}. Note that only Exceptions
 * can be wrapped and not any Throwable. Errors should generally not be
 * caught and the Cue module does not catch any.
 */
@Immutable
public final class RejectedException extends RuntimeException {
  private final Exception reason;

  private RejectedException(Exception reason) {
    super(reason);
    this.reason = reason;
  }

  public Exception getReason() {
    return reason;
  }

  static RejectedException wrap(Exception cause) {
    if (cause instanceof RejectedException) {
      return (RejectedException) cause;
    }
    return new RejectedException(cause);
  }
}

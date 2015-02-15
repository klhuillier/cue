package org.lhor.util.cue;


/**
 * Provided to a Promise's methods for handling a <i>successful</i> resolution.
 * <p>
 * Because some methods are unable to alter any state, including by return value, they
 * simply use NullCallback. Callback is only for successful resolution with .then().
 * </p>
 * <p>
 * If <code>null</code> is returned, the following Promise will be resolved with
 * the current value (which may, itself, be null).
 * </p>
 * <p>
 * If the return value contains a Promise, the invoking method will block until the Promise
 * has been resolved.
 * </p>
 * <p>
 * If the callback throws an exception, a Promise's then method will returned a
 * rejected promise instead.
 * </p>
 * <p>
 * Neither throwing an exception nor returning a value will alter the current Promise's
 * resolution state. Invoking then() on the same Promise with the same callback will
 * always invoke the callback with the same value.
 * </p>
 *
 * @param <I> input argument
 * @param <O> return value
 */
@FunctionalInterface
public interface Callback<I, O> {
  public static final Callback NOOP = i -> null;

  O call(I i) throws Exception;
}

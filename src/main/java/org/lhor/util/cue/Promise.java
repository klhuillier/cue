/*
 * Copyright (c) 2015, Kevin L'Huillier <klhuillier@gmail.com>
 *
 * Released under the zlib license. See LICENSE or
 * http://spdx.org/licenses/Zlib for the full license text.
 */

package org.lhor.util.cue;


/**
 * Awaits the result of a Deferred's execution and allows chained callbacks to
 * handle the results or a blocking done method to simply get the result.
 * <p>
 * Promise chaining allows for the result of one asynchronous task to be used
 * as the provided value for a subsequent asynchronous task.
 * </p>
 * <pre>
 * List&lt;Record&gt; records = promise
 * .then(user -&gt; lookupRecords(user))
 * .then(resultSet -&gt; convertToList(resultSet))
 * .fail(ex -&gt; log.log(Level.WARNING, "Failed to get user's records", ex))
 * .always(() -&gt; cleanup())
 * .done();
 * </pre>
 * <p>
 * Because JavaScript is single-threaded, a blocking .done() method is not feasible
 * in the original q library. However, for simplicity, it has been added to this
 * implementation. <code>result = promise.done();</code> will either return the
 * resolved value of this promise or throw a RejectedException if the promise was
 * rejected. The <code>then</code> and <code>fail</code> methods all execute
 * asynchronously as the initial promise is resolved and each subsequent promise in
 * the chain is resolved. The result of the <code>done</code> method is the final
 * resolved state from the chain.
 * </p>
 * <p>
 * Due to type strictness, automatic unwrapping of promises is not feasible in
 * this implementation. If a Callback were to accept a user ID and return
 * Promise&lt;User&gt;, its parameterized type would need to be
 * <code>Callback&lt;Long, Promise&lt;User&gt;&gt;</code> meaning the Promise's
 * <code>then</code> methods would need to return an instance of type
 * Promise&lt;Promise&lt;User&gt;&gt; and not Promise&lt;User&gt;.
 * Unfortunately, this limitation cannot easily be resolved, but it shouldn't be
 * as much of a problem to manually unwrap with the <code>done</code> method.
 * </p>
 * <p>
 * Also, due to the strictness of the Java language, it is necessary to use multiple
 * types of callbacks and errbacks. Because of this, overloads would become far too
 * cumbersome, such as <code>then(Callback)</code>, <code>then(NullCallback)</code>,
 * <code>then(Errback)</code>, <code>then(Callback, Errback)</code>,
 * <code>then(NullCallback, Errback)</code>, etc. In this implementation,
 * <code>then</code> is used <i>only</i> for successful resolutions, and
 * <code>fail</code> is required to use an Errback or VoidErrback.
 * </p>
 * <p>
 * Exceptions thrown by <code>done</code> are wrapped in a
 * {@link RejectedException}. The causal exception can be retrieved from
 * {@link Exception#getCause()}. The errbacks will be given the causal exception
 * instead of a RejectedException, unless the original Exception is a
 * RejectedException. If a callback or errback throws a RejectedException,
 * the exception will not be wrapped again and its <code>getCause()</code>
 * method may return <code>null</code>.
 * </p>
 *
 * @param <T> the type of value the promise is expected to be fulfilled with
 */
public interface Promise<T> {
  /**
   * Executes the callback if the promise was fulfilled providing it the value
   * the Promise was fulfilled with.
   * <p>
   * If this Promise is fulfilled, the callback's returned value will be used to
   * fulfill the Promise this method returns. If the callback throws an uncaught
   * Exception, the returned promise will be rejected with that Exception.
   * </p>
   * <p>
   * If this Promise is rejected, the callback will not be called and the returned
   * Promise will be rejected with the same Exception as the reason.
   * </p>
   *
   * @param callback non-null
   * @param <O> the type of the value returned by the callback
   * @return a Promise which will be resolved after the callback is completed
   *   or skipped
   */
  <O> Promise<O> then(Callback<T, O> callback);

  /**
   * Executes the callback if the promise was fulfilled providing it the value
   * the promise was fulfilled with.
   * <p>
   * Because the callback produces no return value, the returned Promise will
   * be fulfilled in exactly the same state as the current Promise after the
   * callback returns normally. If the callback throws an Exception, the returned
   * Promise will be rejected with that Exception.
   * </p>
   * <p>
   * If this Promise is rejected, the callback will not be called and the returned
   * Promise will be rejected with the same Exception as the reason.
   * </p>
   *
   * @param callback non-null
   * @return a Promise which will be resolved after the callback is completed
   *   or skipped
   */
  Promise<T> then(VoidCallback<T> callback);

  /**
   * Executes the callback if the promise was fulfilled and using the returned value
   * to fulfill the returned Promise.
   * <p>
   * If the callback returns normally, its returned value will be used to fulfill the
   * Promise returned by this method. If the callback throws an Exception, the
   * returned Promise will be rejected with that Exception.
   * </p>
   * <p>
   * If this Promise is rejected, the callback will not be called and the returned
   * Promise will be rejected with the same Exception as the reason.
   * </p>
   *
   * @param callback non-null
   * @param <O> the type of the value returned by the callback
   * @return a Promise which will be resolved after the callback is completed
   *   or skipped
   */
  <O> Promise<O> then(NullCallback<O> callback);

  /**
   * Executes the callback if this Promise was fulfilled, returning a Promise that
   * will be resolved the same as this Promise, unless an exception is thrown.
   * <p>
   * If the callback returns normally, the Promise returned by this method will
   * be fulfilled with the same value as this Promise was fulfilled with. If the
   * callback throws an Exception, the returned Promise will be rejected with
   * that Exception.
   * </p>
   * <p>
   * If this Promise is rejected, the callback will not be called and the returned
   * Promise will be rejected with the same Exception as the reason.
   * </p>
   *
   * @param callback non-null
   * @return a Promise which will be resolved after the callback is completed
   *   or skipped
   */
  Promise<T> then(NullVoidCallback callback);

  /**
   * Executes the errback if this Promise was rejected, resolving the returned
   * Promise with a replacement value or a new rejection reason.
   * <p>
   * If this Promise is rejected, the errback will be called with the Exception
   * that caused the rejection. If the errback returns a value, its value will be
   * used to fulfill the Promise this method returns. If the errback throws an
   * Exception, the returned Promise will be rejected with that Exception.
   * </p>
   * <p>
   * Because the errback only executes during an error condition, the returned
   * Promise will have the same fulfilled type as this Promise. The errback is
   * able to provide a replacement value in the event of an error (e.g., a
   * default value), but it cannot change the type the Promise can be fulfilled
   * with.
   * </p>
   * <p>
   * If this Promise was fulfilled, the errback will not be called and the
   * returned Promise will be fulfilled with the same value as this Promise.
   * </p>
   *
   * @param errback non-null
   * @return a Promise which will be resolved after the errback is completed
   *   or skipped
   */
  Promise<T> fail(Errback<T> errback);

  /**
   * Executes the errback if the Promise was rejected, returning a Promise that
   * will be resolved the same as this Promise unless an exception is thrown.
   * <p>
   * If this Promise is rejected, the errback will be called with the Exception
   * that caused the rejection. If the errback returns normally, the Promise
   * returned by this method will be rejected with the same Exception as this
   * Promise. If the errback throws an Exception, the returned Promise will be
   * rejected with that Exception.
   * </p>
   * <p>
   * If this Promise was fulfilled, the errback will not be called and the
   * returned Promise will be fulfilled with the same value as this Promise.
   * </p>
   *
   * @param errback non-null
   * @return a Promise which will be resolved after the errback is completed
   *   or skipped
   */
  Promise<T> fail(VoidErrback errback);

  /**
   * Triggers the callback regardless of whether this Promise was fulfilled
   * or rejected, returning a Promise that will always be resolved the same
   * way regardless of whether the callback throws an exception.
   * <p>
   * The resolved/rejected status of the returned Promise will be the same as
   * the current promise. The returned Promise will be resolved or rejected
   * after the callback completes. If the callback throws an Exception, that
   * Exception will be ignored.
   * </p>
   * <p>
   * This should only be used for code which would be appropriate in a
   * <code>finally</code> block, e.g., resource cleanup.
   * </p>
   *
   * @param callback non-null
   * @return a Promise which will be resolved after the callback is completed
   *   containing exactly the same resolved state as the current Promise
   */
  Promise<T> always(NullVoidCallback callback);

  /**
   * Ends a Promise chain and returns the final value or throws a
   * {@link RejectedException} if the Promise is rejected.
   * <p>
   * A <code>done</code> method should be used to terminate a promise chain to
   * allow exceptions to propagate.
   * </p>
   *
   * @return value of the Promise
   * @throws RejectedException if the Promise is rejected
   */
  T done();
}

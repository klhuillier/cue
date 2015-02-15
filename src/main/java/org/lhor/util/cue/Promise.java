package org.lhor.util.cue;


/**
 * Awaits the result of a Deferred's execution and allows chained callbacks to
 * handle the results or a blocking get method to simply get the result.
 * <p>
 * Promise chaining allows for the result of one asynchronous task to be used
 * as the provided value for a subsequent asynchronous task.
 * </p>
 * <pre>
 * promise
 * .then(result1 -> processResult(result1))
 * .then(result2 -> processResult(result2))
 * .done();
 * </pre>
 * <p>
 * Because JavaScript is single-threaded, a blocking .get() method is not feasible
 * in the original q library. However, for simplicity, it has been added to this
 * implementation. <code>result = promise.get();</code> will either return the
 * resolved value of this promise or throw a RejectedException if the promise was
 * rejected.
 * </p>
 * <p>
 * Due to type strictness, automatic unwrapping of promises is not feasible in
 * this implementation. If a Callback were to return Promise&lt;User&gt;, its
 * parameterized type would need to be Callback&lt;Object, Promise&lt;User&gt;&gt;
 * meaning the promise's <code>then</code> methods would need to return an instance
 * of type Promise&lt;Promise&lt;User&gt;&gt; and not Promise&lt;User&gt;.
 * Unfortunately, this limitation cannot easily be resolved, but it shouldn't be
 * as much of a problem to manually unwrap with the <code>get</code> method.
 * </p>
 * <p>
 * Also, due to the strictness of the Java language, it is necessary to use multiple
 * types of callbacks and errbacks. Because of this, overloads would become far too
 * cumbersome, such as <code>then()</code>, <code>then(callback)</code>,
 * <code>then(nullCallback)</code>, <code>then(nullReturnCallback)</code>,
 * <code>then(errback)</code>, <code>then(callback, errback)</code>. In this implementation,
 * <code>then</code> is used <i>only</i> for successful resolutions, and <code>fail</code>
 * is required to use an Errback or VoidErrback.
 * </p>
 * <p>
 * Exceptions thrown by <code>done</code> methods are wrapped in a {@link RejectedException}.
 * The causal exception can be retrieved from {@link Exception#getCause()}.
 * If a callback or errback throws a RejectedException, the exception will not be
 * wrapped again.
 * </p>
 *
 * @param <T> the type of value the promise is expected to be resolved with
 */
public interface Promise<T> {
  /**
   * Executes the callback if the promise was fulfilled providing it the value
   * the promise was fulfilled with.
   * <p>
   * If this promise is fulfilled, the callback may produce a new value which
   * will be the resolved value of the promise that is returned. If the callback
   * throws an uncaught exception, the returned promise will be rejected with
   * that exception.
   * </p>
   * <p>
   * The promise may return itself if either the callback returns the same value
   * or the promise is rejected.
   * </p>
   *
   * @param callback
   * @param <O> the type of the value returned by the callback
   * @return
   */
  <O> Promise<O> then(Callback<T, O> callback);

  /**
   * Executes the callback if the promise was fulfilled providing it the value
   * the promise was fulfilled with.
   * <p>
   * Because the callback produces no return value, the returned Promise will
   * be resolved in exactly the same state as the current Promise, <i>unless</i>
   * an Exception is thrown, in which case the returned Promise will be rejected
   * with the thrown Exception as the RejectedException's reason.
   * </p>
   *
   * @param callback
   * @return
   */
  Promise<T> then(VoidCallback<T> callback);

  /**
   * Executes the callback if the promise was fulfilled and using the returned value
   * to resolve the returned Promise.
   * <p>
   * If the callback throws an exception, the returned Promise will be rejected with
   * the thrown Exception as the RejectedException's reason.
   * </p>
   *
   * @param callback
   * @param <O>
   * @return
   */
  <O> Promise<O> then(NullCallback<O> callback);

  /**
   * Executes the callback if the promise was fulfilled.
   * <p>
   * Because the callback produces no return value, the returned Promise will
   * be resolved in exactly the same state as the current Promise, <i>unless</i>
   * an Exception is thrown, in which case the returned Promise will be rejected
   * with the thrown Exception as the RejectedException's reason.
   * </p>
   *
   * @param callback
   * @return
   */
  Promise<T> then(NullVoidCallback callback);

  /**
   * Executes the errback if the promise was rejected and returns a promise with
   * the replacement value.
   * <p>
   * Because the current Promise may be fulfilled, this is only used to
   * set a value (e.g., a fallback or default) in the case of an error. In the
   * case where the current Promise is successfully resolved, the errback is not
   * called, and the returned Promise will retain the same resolved state as the
   * current one.
   * </p>
   *
   * @param errback
   * @return
   */
  Promise<T> fail(Errback<T> errback);

  /**
   * Executes the errback if the promise was rejected and returns a promise with
   * the same resolved/rejected value.
   * <p>
   * The promise may return itself.
   * </p>
   *
   * @param errback
   * @return
   */
  Promise<T> fail(VoidErrback errback);

  /**
   * Triggers the callback regardless of whether the promise was fulfilled
   * or rejected.
   * <p>
   * The resolved/rejected status of the returned promise will be the same as
   * the current promise, and the promise may return itself.
   * </p>
   * <p>
   * It should be emphasized that the returned Promise will have exactly the same
   * resolution state as the current Promise. <strong>Any exceptions thrown by the
   * callback will be ignored.</strong> This should only be used for code which
   * would be appropriate in a <code>finally</code> block, e.g., resource cleanup.
   * </p>
   *
   * @param callback
   * @return
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
   * @return final value of the Promise
   * @throws RejectedException if the Promise is rejected
   */
  T done();

  /**
   * Executes the callback if the promise was fulfilled and throws a RejectedException
   * if the callback had an uncaught exception or the promise was rejected.
   * <p>
   * This returns the final value of the chain when the entire chain has finished,
   * including the invocation of the callback.
   * </p>
   * <p>
   * A <code>done</code> method should be used to terminate a promise chain to
   * allow exceptions to propagate.
   * </p>
   *
   * @param callback a non-null callback to be invoked when the Promise chain is fulfilled
   * @return final value of the Promise
   * @throws RejectedException if the Promise is rejected
   */
  T done(NullVoidCallback callback);

  /**
   * Executes the errback if the promise was rejected and throws a RejectedException
   * if the errback had an uncaught exception or the promise was rejected.
   * <p>
   * A <code>done</code> method should be used to terminate a promise chain to
   * allow exceptions to propagate.
   * </p>
   *
   * @param errback a non-null callback to be invoked when the Promise chain is rejected
   * @return final value of the Promise
   * @throws RejectedException if the Promise is rejected
   */
  T done(VoidErrback errback);

  /**
   * Executes the callback if the promise was fulfilled or errback if the promise
   * was rejected and throws a RejectedException if the callback/errback had an uncaught
   * exception.
   * <p>
   * A <code>done</code> method should be used to terminate a promise chain to
   * allow exceptions to propagate.
   * </p>
   *
   * @param callback a non-null callback to be invoked when the Promise chain is fulfilled
   * @param errback a non-null callback to be invoked when the Promise chain is rejected
   * @return final value of the Promise
   * @throws RejectedException if the Promise is rejected
   */
  T done(NullVoidCallback callback, VoidErrback errback);
}

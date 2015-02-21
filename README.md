# Cue

Cue is a Java 8 library for asynchronous work, providing an interface for a producer and allowing consumers to register callback chains.

As those familiar with the JavaScript library [q](https://github.com/kriskowal/q) may quickly notice, this project was inspired by the q API. It operates a bit differently due to differences in the languages.

# Requirements

Cue is made with as few runtime dependencies as possible. You will need:
* Java 8
* [javax.inject](https://code.google.com/p/atinject/)
* (optional) [Guice](https://github.com/google/guice/)

# Getting Started

Once you have it included in your project, there are two ways to start using it. Both will require you to provide a thread pool, some form of [ExecutorService](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html). Using Guice, add `new CueModule(executorService)` to your Injector's list of modules. Without Guice, you can create a new `Provider<Cue>` with `new CueFactory(executorService)`.

Once you have a Cue instance, you can begin by invoking `cue.defer()` to create a Deferred<T>. A producer will fulfill or reject this Deferred<T>. The consumer will be interested in the Promise<T> associated with the Deferred<T> which can be retrieved with `deferred.promise()`.

# Producers

Producers are the thread(s) which generate values for their consumers to work with. The main interface for a producer is Deferred<T> which can be fulfilled with an object of type T.

Consumer threads will remain in waiting until the Deferred has been resolved (either fulfilled or rejected), so most producers should have a catch block in case there was a problem producing the value: `catch (Exception e) { deferred.reject(e); }` It is generally not advised to catch all Throwable types because Errors tend to be thrown during irrecoverable and unpredictable states. (The reject method only allows Throwables of type Exception for this reason.)

To ensure even Errors will unblock the consumers, you can add an additional finally block: `catch (Exception e) { deferred.reject(e); } finally { deferred.reject(); }`. If the deferred is resolved or rejected with a regular Exception, the finally block's code will have no effect because a resolved Promise's state can not be altered.

However, in the event of an Error, it is probably better to allow it to terminate the application, and your application's cleanup should shutdown all the threads in the thread pool, possibly with a call to `executorService.shutdownNow()` if the threads are not daemon threads. (ForkJoinPool uses daemon threads, but pools created from Executors use user threads by default.)

# Consumers

The Promise<T> interface provided to consumers is entirely passive and simply awaits for a value to become available or a rejection to occur. The associated Promise<T> is returned from `deferred.promise()`. All Promise<T> instances produced from the same Deferred<T> will have identical behavior (and are likely to be the same instance, but this is not guaranteed) and once the Deferred<T> is resolved, all Promise<T> instances will have exactly the same resolved state: either the identical value object or the identical exception. The referent object will always be the same, but if the value object or exception are mutable, callbacks may see (depending on concurrency) changes made by other threads. It is therefore recommended that the objects be treated as immutable within the callbacks and use caution otherwise, because the ordering of callback execution and memory propagation between caches is not guaranteed.

The consumers' interface is Promise<T>. There are four method names with several overloads:
* then - callbacks are invoked when the Promise is resolved successfully, and they may return a new value in the chain or allow the current value to resolve the next Promise
* fail - callbacks (or errbacks) are invoked when the Promise was rejected, and may offer a replacement value of the same type as the current Promise to recover from an error
* always - callbacks are invoked whenever the Promise is resolved regardless of how it was resolved and their returns will not affect the resolution of the next Promise
* done - no callback, this method will return the value of the Promise or throw a RejectedException if the Promise was rejected, blocking the invoking thread until the final Promise is resolved

`then`, `fail`, and `always` all produce a new Promise instance, either of type T or O depending on whether the callback has a return type O. void callbacks will return a new Promise of the same type the registering method was called on.

Two types of `then` callbacks and one type of `fail` callback return a value. The returned Promise will be resolved when the callbacks return the new value. The void callbacks, which do not return new values, will resolve the returned Promise with the same value as the previous Promise. If any of the callbacks throw an exception, the returned Promise will instead be rejected. (Except for always callbacks: the Promise returned by always will always have exactly the same resolution state as the current Promise and will be resolved when the callback returns normally or abnormally.) `null` is an acceptable value for resolving a promise and will resolve the next Promise with null rather than carrying on the current value.

Fail callbacks will be provided with the rejection reason, i.e., the Exception provided to `deferred.reject(e)` or the Exception thrown by a callback. The Errback callback has an opportunity to provide a replacement value for the following Promise if some sort of default value is a preferred option.

The done method will always wrap the rejection reason in a RejectedException.

Note that all the methods for registering callbacks will not block, they immediately return a Promise which will be resolved when the callback has finished executing. The only method which will block is `done`, which is also the only method which will throw an exception if the chain has encountered an error. For this reason, it is recommended that `done` be invoked on the final Promise in any chain. It does not necessarily need to terminate the chain's declaration and may be invoked later, but it should be invoked at some point unless there is a reason for ignoring the final result.

Only the `done` method rethrows an Exception, but all of the `then` and `fail` methods will pass an Exception along so it can be propagated. The only method which swallows Exceptions is `always`. For this reason, `always` is only intended for a Promise form of a finally block. Execute cleanup code and enter a log message, but it is strongly suggested that your `always` callback not have any code which can fail with an uncaught Exception. It is for this reason that the Runnable interface is used as it does not allow any checked exceptions.

A simple example in code:

```
Promise<Cart> promise = asyncGetUser(userId)
.then(user -> { appCtrl.setUser(user); return getCart(user); })
.then(cart -> { appCtrl.setCart(cart); })
.fail(ex -> logError(ex))
.always(() -> closeResources())
.done();
```

A consumer may wish to make use of the Cue instance to group Promises together. If it has produced a large list of Promise chains, they can all be treated as a single Promise with `cue.all(List<Promise<T>>)`. This will produce a Promise<List<T>> containing a list of all Promises' values in the same order. Calling `done` on the aggregated Promise will wait for every Promise in the list to complete.

# The Cue interface

The thread creating the Deferred<T> from `cue.defer()` can hand off the Deferred<T> to a producer thread and the Promise<T> to a consumer thread. The producer will only have methods to resolve the Promise and the consumer will only have methods to handle the resolution.

Occasionally you may find you have a value already prepared, perhaps it was cached, but the consumer still expects the value to be provided at some point in the future via the Promise interface. `cue.when(T)` will produce a Promise<T> that is immediately resolved with the given value.

You may also wish to adapt from an interface that provides a Future<T> and use it with an interface that expects a Promise<T>. The method `cue.whenFuture(Future<T>)` will produce a Promise<T> that will become resolved when a value is available. Note that this will use a thread in the thread pool until `future.get()` returns. You may wish to avoid this if your common thread pool has a low number of maximum threads. Either create a separate instance of Cue with `new CueFactory(futuresThreadPool).get()` or start up a thread outside the pool:

```
Deferred<T> deferred = cue.defer();
new Thread(() -> {
  try {
    deferred.resolve(future.get());
  } catch (Exception e) {
    deferred.reject(e);
  }
}).start();
return deferred.promise();
```

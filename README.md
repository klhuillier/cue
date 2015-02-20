# cue

Cue is a Java 8 library for asynchronous work, providing an interface for a producer and allowing consumers to register callback chains.

As those familiar with the JavaScript library [q](https://github.com/kriskowal/q) may quickly notice, this project was inspired by the q API. It operates a bit differently due to differences in the languages.

# Requirements

Cue is made with as few runtime dependencies as possible. You will need:
* Java 8
* [javax.inject](https://code.google.com/p/atinject/)
* (optional) [Guice](https://github.com/google/guice/)

# Getting Started

Once you have it included in your project, there are two ways to start using it. Both will require you to provide a thread pool, some form of [ExecutorService](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html). Using Guice, add `new CueModule(executorService)` to your Injector's list of modules. Without Guice, you can create a new `Provider<Cue>` with `new CueFactory(executorService)` or simply get the instance from `cueFactory.get()`.

# Producers

Producers are the thread(s) which are generating values for their consumers to work with. The main interface for a producer is Deferred<T> which can be fulfilled with an object of type T. Note that consumer threads will remain in waiting until the Deferred has been resolved (either fulfilled or rejected), so most producers should have a catch block in case there was a problem producing the value: `catch (Exception e) { deferred.reject(e); }`

The thread creating the Deferred<T> can hand off a Promise<T> to a consumer thread. The Promise<T> interface is entirely passive and simply awaits for a value to become available. The associated Promise<T> is returned from `deferred.promise()`. All Promise<T> instances produced from the same Deferred<T> will have identical behavior (and are likely to be the same instance, but this is not guaranteed) and once the Deferred<T> is resolved, all Promise<T> instances will have exactly the same resolved state. (Either the identical value object or the identical exception.)

# Consumers

The consumers' interface is Promise<T>. There are four method names with several overloads:
* then - callbacks are invoked when the Promise is resolved successfully
* fail - callbacks (or errbacks) are invoked when the Promise was rejected
* always - callbacks are invoked whenever the Promise is resolved regardless of how it was resolved
* done - no callback, this method will return the value of the Promise or throw a RejectedException if the Promise was rejected

`then`, `fail`, and `always` all produce a new Promise instance, either of type T or O depending on whether the callback has a return type O. void callbacks will return a new Promise of the same type the registering method was called on.

Two types of `then` callbacks and one type of `fail` callback return a value. The returned Promise will be resolved when the callbacks return the new value. The void callbacks, which do not return new values, will resolve the returned Promise with the same value as the previous Promise. If any of the callbacks throw an exception, the returned Promise will instead be rejected. (Except for always callbacks: the Promise returned by always will always have exactly the same resolution state as the current Promise and will be resolved when the callback returns normally or abnormally.)

Fail callbacks will be provided with the rejection reason, i.e., the Exception provided to `deferred.reject(e)` or the Exception thrown by a callback.

The done method will always wrap the rejection reason in a RejectedException.

A simple example in code:

```
Promise<Cart> promise = asyncGetUser(userId)
.then(user -> { appCtrl.setUser(user); return getCart(user); })
.then(cart -> { appCtrl.setCart(cart); })
.fail(ex -> logError(ex))
.always(() -> closeResources())
.done();
```

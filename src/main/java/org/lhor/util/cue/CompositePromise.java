package org.lhor.util.cue;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


final class CompositePromise<T> implements Promise<List<T>> {
  private final List<Promise<T>> promises;
  private final ArrayList<T> values;
  private final AtomicReference<RejectedException> err = new AtomicReference<>(null);

  protected CompositePromise(List<Promise<T>> promises) {
    this.promises = new ArrayList<>(promises);
    this.values = new ArrayList<>(promises.size());
  }

  @Override
  public Promise<List<T>> then(NullVoidCallback callback) {
    try {
      resolveAll();
      callback.call();
    } catch (Exception e) {
      err.compareAndSet(null, RejectedException.wrap(e));
      return new PromiseImpl<>(new ResolvedState<>(err.get()));
    }
    return new PromiseImpl<>(new ResolvedState<>(values));
  }

  @Override
  public <O> Promise<O> then(NullCallback<O> callback) {
    try {
      resolveAll();
      O result = callback.call();
      return new PromiseImpl<>(new ResolvedState<>(result));
    } catch (Exception e) {
      err.compareAndSet(null, RejectedException.wrap(e));
      return new PromiseImpl<>(new ResolvedState<>(err.get()));
    }
  }

  @Override
  public Promise<List<T>> then(VoidCallback<List<T>> callback) {
    try {
      resolveAll();
      callback.call(values);
    } catch (Exception e) {
      err.compareAndSet(null, RejectedException.wrap(e));
      return new PromiseImpl<>(new ResolvedState<>(err.get()));
    }
    return new PromiseImpl<>(new ResolvedState<>(values));
  }

  @Override
  public <O> Promise<O> then(Callback<List<T>, O> callback) {
    try {
      resolveAll();
      O result = callback.call(values);
      return new PromiseImpl<>(new ResolvedState<>(result));
    } catch (Exception e) {
      err.compareAndSet(null, RejectedException.wrap(e));
      return new PromiseImpl<>(new ResolvedState<>(err.get()));
    }
  }

  @Override
  public Promise<List<T>> fail(Errback<List<T>> errback) {
    try {
      resolveAll();
      if (err.get() != null) {
        List<T> result = errback.call(err.get());
        return new PromiseImpl<>(new ResolvedState<>(result));
      }
      return this;
    } catch (Exception e) {
      err.compareAndSet(null, RejectedException.wrap(e));
      return new PromiseImpl<>(new ResolvedState<>(err.get()));
    }
  }

  @Override
  public Promise<List<T>> fail(VoidErrback errback) {
    try {
      resolveAll();
      if (err.get() != null) {
        errback.call(err.get());
      }
      return this;
    } catch (Exception e) {
      err.compareAndSet(null, RejectedException.wrap(e));
      return new PromiseImpl<>(new ResolvedState<>(err.get()));
    }
  }

  @Override
  public Promise<List<T>> always(NullVoidCallback callback) {
    try {
      resolveAll();
      callback.call();
      return this;
    } catch (Exception e) {
      err.compareAndSet(null, RejectedException.wrap(e));
      return new PromiseImpl<>(new ResolvedState<>(err.get()));
    }
  }

  @Override
  public List<T> done() {
    try {
      resolveAll();
      return values;
    } catch (Exception e) {
      throw RejectedException.wrap(e);
    }
  }

  @Override
  public List<T> done(NullVoidCallback callback) {
    try {
      resolveAll();
      callback.call();
      return values;
    } catch (Exception e) {
      throw RejectedException.wrap(e);
    }
  }

  @Override
  public List<T> done(VoidErrback errback) {
    try {
      resolveAll();
      if (err.get() != null) {
        errback.call(err.get());
      }
      return values;
    } catch (Exception e) {
      throw RejectedException.wrap(e);
    }
  }

  @Override
  public List<T> done(NullVoidCallback callback, VoidErrback errback) {
    try {
      resolveAll();
      if (err.get() != null) {
        errback.call(err.get());
      } else {
        callback.call();
      }
      return values;
    } catch (Exception e) {
      throw RejectedException.wrap(e);
    }
  }

  protected void resolveAll() {
    if (err.get() != null) {
      throw err.get();
    }
    try {
      for (Promise<T> promise : promises) {
        values.add(promise.done());
      }
    } catch (Exception e) {
      err.compareAndSet(null, RejectedException.wrap(e));
      throw err.get();
    }
  }
}

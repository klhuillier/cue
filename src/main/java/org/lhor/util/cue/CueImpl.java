package org.lhor.util.cue;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;


final class CueImpl implements Cue {
  @Override
  public <T> Deferred<T> defer() {
    return new DeferredImpl<>();
  }

  @Override
  public <T> Promise<List<T>> all(List<Promise<T>> promises) {
    return new CompositePromise<T>(promises);
  }

  @Override
  public <T> Promise<List<T>> all(Promise<T> promise, Promise<T>... promises) {
    ArrayList<Promise<T>> list = new ArrayList<>(promises.length + 1);
    list.add(promise);
    Arrays.stream(promises).forEach(list::add);
    return all(list);
  }

  @Override
  public <T> Promise<List<T>> allFutures(List<Future<T>> futures) {
    ArrayList<Promise<T>> promises = new ArrayList<>(futures.size());
    futures.forEach(f -> promises.add(whenFuture(f)));
    return all(promises);
  }

  @Override
  public <T> Promise<List<T>> allFutures(Future<T> future, Future<T>... futures) {
    ArrayList<Promise<T>> promises = new ArrayList<>(futures.length + 1);
    promises.add(new FuturePromise<T>(future));
    Arrays.stream(futures).forEach(f -> promises.add(whenFuture(f)));
    return all(promises);
  }

  @Override
  public <T> Promise<T> whenFuture(Future<T> future) {
    return new FuturePromise<>(future);
  }

  @Override
  public <T> Promise<T> when(T value) {
    return new PromiseImpl<>(new ResolvedState<>(value));
  }

  @Override
  public <T> Promise<T> reject(Exception ex) {
    return new PromiseImpl<>(new ResolvedState<>(ex));
  }
}

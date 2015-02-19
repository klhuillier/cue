package org.lhor.util.cue;


import com.google.inject.Inject;
import com.google.inject.Provider;
import net.jcip.annotations.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


@Immutable
final class CueImpl implements Cue {
  private final ExecutorService executorService;
  private final Provider<Deferred> deferredProvider;

  @Inject
  public CueImpl(@CueExecutors ExecutorService executorService, Provider<Deferred> deferredProvider) {
    if (executorService == null) {
      throw new NullPointerException("executorService");
    } else if (deferredProvider == null) {
      throw new NullPointerException("deferredProvider");
    }
    this.executorService = executorService;
    this.deferredProvider = deferredProvider;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Deferred<T> defer() {
    // unchecked cast, but because it is a new Deferred instance this is safe
    return (Deferred<T>) deferredProvider.get();
  }

  @Override
  public <T> Promise<List<T>> all(List<Promise<T>> promises) {
    if (promises == null) {
      throw new NullPointerException("promises");
    }

    Deferred<List<T>> deferred = defer();
    executorService.submit(() -> {
      ArrayList<T> result = new ArrayList<>(promises.size());
      try {
        promises.forEach(p -> result.add(p.done()));
        deferred.resolve(Collections.unmodifiableList(result));
      } catch (RejectedException e) {
        deferred.reject(e.getReason());
      } catch (Exception e) {
        deferred.reject(e);
      }
    });
    return deferred.promise();
  }

  @Override
  public <T> Promise<List<T>> allFutures(List<Future<T>> futures) {
    if (futures == null) {
      throw new NullPointerException("futures");
    }

    Deferred<List<T>> deferred = defer();
    ArrayList<Future<T>> copyOfFutures = new ArrayList<>(futures);
    executorService.submit(() -> {
      try {
        ArrayList<T> values = new ArrayList<>(copyOfFutures.size());
        for (Future<T> future : copyOfFutures) {
          values.add(future.get());
        }
        deferred.resolve(Collections.unmodifiableList(values));
      } catch (Exception e) {
        deferred.reject(e);
      }
    });
    return deferred.promise();
  }

  @Override
  public <T> Promise<T> whenFuture(Future<T> future) {
    if (future == null) {
      throw new NullPointerException("future");
    }

    Deferred<T> deferred = defer();
    executorService.submit(() -> {
      try {
        T value = future.get();
        deferred.resolve(value);
      } catch (Exception e) {
        deferred.reject(e);
      }
    });
    return deferred.promise();
  }

  @Override
  public <T> Promise<T> when(T value) {
    Deferred<T> deferred = defer();
    deferred.resolve(value);
    return deferred.promise();
  }

  @Override
  public <T> Promise<T> reject(Exception ex) {
    Deferred<T> deferred = defer();
    deferred.reject(ex);
    return deferred.promise();
  }
}

package org.lhor.util.cue;


import net.jcip.annotations.Immutable;


@Immutable
final class PromiseImpl<T> implements Promise<T> {
  private final ResolvedState<T> state;

  public PromiseImpl(ResolvedState<T> state) {
    this.state = state;
  }

  @Override
  public <O> Promise<O> then(Callback<T, O> callback) {
    state.parkUntilResolved();
    if (state.isRejected()) {
      return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
    }
    try {
      O value = callback.call(state.get());
      return new PromiseImpl<>(new ResolvedState<>(value));
    } catch (Exception e) {
      state.offerRejection(e);
    }
    return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
  }

  @Override
  public Promise<T> then(NullVoidCallback callback) {
    state.parkUntilResolved();
    if (state.isRejected()) {
      return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
    }
    try {
      state.get();
      callback.call();
      return this;
    } catch (Exception e) {
      state.offerRejection(e);
    }
    return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
  }

  @Override
  public <O> Promise<O> then(NullCallback<O> callback) {
    state.parkUntilResolved();
    if (state.isRejected()) {
      return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
    }
    try {
      O value = callback.call();
      return new PromiseImpl<>(new ResolvedState<>(value));
    } catch (Exception e) {
      state.offerRejection(e);
    }
    return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
  }

  @Override
  public Promise<T> then(VoidCallback<T> callback) {
    state.parkUntilResolved();
    if (state.isRejected()) {
      return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
    }
    try {
      callback.call(state.get());
      return this;
    } catch (Exception e) {
      state.offerRejection(e);
    }
    return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
  }

  @Override
  public Promise<T> fail(VoidErrback errback) {
    try {
      state.parkUntilResolved();
      if (state.isRejected()) {
        errback.call(state.getReason());
      }
      return this;
    } catch (Exception e) {
      state.offerRejection(e);
    }
    return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
  }

  @Override
  public Promise<T> fail(Errback<T> errback) {
    try {
      state.parkUntilResolved();
      if (state.isRejected()) {
        T value = errback.call(state.getReason());
        return new PromiseImpl<>(new ResolvedState<>(value));
      }
      return this;
    } catch (Exception e) {
      state.offerRejection(e);
    }
    return new PromiseImpl<>(new ResolvedState<>(state.getReason()));
  }

  @Override
  public Promise<T> always(NullVoidCallback callback) {
    state.parkUntilResolved();
    try {
      callback.call();
    } catch (Exception e) {
      // Exceptions in always do not affect the returned Promise's state
    }
    return this;
  }

  @Override
  public T done() {
    return state.get();
  }

  @Override
  public T done(NullVoidCallback callback) {
    try {
      T result = state.get();
      callback.call();
      return result;
    } catch (Exception e) {
      throw RejectedException.wrap(e);
    }
  }

  @Override
  public T done(VoidErrback errback) {
    try {
      state.parkUntilResolved();
      if (state.isRejected()) {
        errback.call(state.getReason());
      }
      return state.get();
    } catch (Exception e) {
      throw RejectedException.wrap(e);
    }
  }

  @Override
  public T done(NullVoidCallback callback, VoidErrback errback) {
    try {
      state.parkUntilResolved();
      if (state.isFulfilled()) {
        callback.call();
      } else if (state.isRejected()) {
        errback.call(state.getReason());
      }
      return state.get();
    } catch (Exception e) {
      throw RejectedException.wrap(e);
    }
  }
}

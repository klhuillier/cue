package org.lhor.util.cue;


import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import net.jcip.annotations.Immutable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutorService;


@Immutable
public final class CueModule extends AbstractModule {
  private final ExecutorService executorService;

  public CueModule(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  protected void configure() {
    bind(Cue.class).to(CueImpl.class).in(Singleton.class);
  }
}

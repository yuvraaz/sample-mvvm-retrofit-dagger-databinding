package es.amarinag.mvvm_databinging_dagger2.domain.usecase;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import retrofit2.adapter.rxjava.Result;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by AMarinaG on 28/04/2016.
 */
public abstract class BaseUseCase {
  protected Executor executor;

  public BaseUseCase(Executor executor) {
    this.executor = executor;
  }

  public <T> Observable.Transformer<T, T> applySchedulers() {
    return observable -> observable.subscribeOn(Schedulers.from(executor))
        .observeOn(AndroidSchedulers.mainThread());
  }
  @NonNull
  public static <T> Observable.Transformer<Result<T>, T> transformResult() {

    return responseObservable -> responseObservable.flatMap((Func1<Result<T>, Observable<T>>) result -> {
      if (!result.isError() && result.response().isSuccessful()) {
        return Observable.just(result.response().body());
      }

      if (result.isError()) {

        if (result.error() instanceof SocketTimeoutException) {
          // TODO handle exception
        } else if (result.error() instanceof IOException) {
          if (result.error() instanceof java.net.ConnectException) {
            // TODO handle exception
          } else if (result.error() instanceof SocketTimeoutException) {
            // TODO handle exception
          } else {
            // TODO handle exception
          }
        } else {
          // TODO handle exception
        }

        // TODO change to handled exception
        return Observable.error(result.error());
      }
      return Observable.error(result.error());
    });
  }


  public static Observable<?> getRetryObservable(Observable<? extends Throwable> observable, int count) {
    return observable.zipWith(Observable.range(1, count), (throwable, integer) -> {
      if (integer < count) {
        Timber.e("error, trying again...", throwable);
        return Observable.timer(1, TimeUnit.SECONDS);
      }
      Timber.e("error, finish tries...", throwable);
      return Observable.error(new IllegalStateException());
    });
  }
}

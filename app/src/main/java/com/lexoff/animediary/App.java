package com.lexoff.animediary;

import android.app.Application;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;

import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

public class App extends Application {
    private static App app;

    private static Client client;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        getClient();

        configureRxJavaErrorHandler();
    }

    public Client getClient() {
        if (client==null) {
            client = Client.init(null);
        }
        return client;
    }

    public static App getApp() {
        return app;
    }

    private void configureRxJavaErrorHandler() {
        // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) {
                if (throwable instanceof UndeliverableException) {
                    // As UndeliverableException is a wrapper, get the cause of it to get the "real" exception
                    throwable = throwable.getCause();
                }

                final List<Throwable> errors;
                if (throwable instanceof CompositeException) {
                    errors = ((CompositeException) throwable).getExceptions();
                } else {
                    errors = Collections.singletonList(throwable);
                }

                for (final Throwable error : errors) {
                    if (isThrowableIgnored(error)) return;

                    reportException(error);
                    return;
                }

            }

            private boolean isThrowableIgnored(@NonNull final Throwable throwable) {
                return hasAssignableCause(throwable,
                        new Class[]{IOException.class, SocketException.class, SocketTimeoutException.class, // network api cancellation
                                InterruptedException.class, InterruptedIOException.class}); // blocking code disposed
            }

            private boolean hasAssignableCause(Throwable throwable, Class[] classes){
                if (throwable==null) return false;

                for (Class clazz : classes){
                    if (clazz.isAssignableFrom(throwable.getClass())) return true;
                }

                Throwable cause=throwable.getCause();
                if (!throwable.equals(cause)){
                    return hasAssignableCause(cause, classes);
                }

                return false;
            }

            private void reportException(@NonNull final Throwable throwable) {
                // Throw uncaught exception that will trigger the report system
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), throwable);
            }
        });
    }

}

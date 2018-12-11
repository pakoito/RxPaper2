/*
 * The MIT License (MIT)
 * Copyright (c) 2017 pakoito & 2015 César Ferreira
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit  persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.pacoworks.rxpaper2;

import android.content.Context;
import android.util.Pair;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import io.paperdb.Book;
import io.paperdb.Paper;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Adapter class with a new interface to perform PaperDB operations.
 * 
 * @author pakoito
 */
public class RxPaperBook {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    final Book book;

    final Scheduler scheduler;

    final Subject<Pair<String, ?>> updates = PublishSubject.<Pair<String, ?>> create().toSerialized();

    private RxPaperBook(Scheduler scheduler) {
        this.scheduler = scheduler;
        book = Paper.book();
    }

    private RxPaperBook(String customBook, Scheduler scheduler) {
        this.scheduler = scheduler;
        book = Paper.book(customBook);
    }

    private RxPaperBook(Scheduler scheduler, String path) {
        this.scheduler = scheduler;
        book = Paper.bookOn(path);
    }

    private RxPaperBook(String path, String customBook, Scheduler scheduler) {
        this.scheduler = scheduler;
        book = Paper.bookOn(path, customBook);
    }

    /**
     * Initializes the underlying {@link Paper} database.
     * <p/>
     * This operation is required only once, but can be called multiple times safely.
     * 
     * @param context application context
     */
    public static void init(Context context) {
        if (INITIALIZED.compareAndSet(false, true)) {
            Paper.init(context.getApplicationContext());
        }
    }

    private static void assertInitialized() {
        if (!INITIALIZED.get()) {
            throw new IllegalStateException(
                    "RxPaper not initialized. Call RxPaper#init(Context) once");
        }
    }

    /**
     * Open the main {@link Book} running its operations on {@link Schedulers#io()}.
     * <p/>
     * Requires calling {@link RxPaperBook#init(Context)} at least once beforehand.
     * 
     * @return new RxPaperBook
     */
    public static RxPaperBook with() {
        assertInitialized();
        return new RxPaperBook(Schedulers.io());
    }

    /**
     * Open a custom {@link Book} running its operations on {@link Schedulers#io()}.
     * <p/>
     * Requires calling {@link RxPaperBook#init(Context)} at least once beforehand.
     * 
     * @param customBook book name
     * @return new RxPaperBook
     */
    public static RxPaperBook with(String customBook) {
        assertInitialized();
        return new RxPaperBook(customBook, Schedulers.io());
    }

    /**
     * Open the main {@link Book} running its operations on a provided scheduler.
     * <p/>
     * Requires calling {@link RxPaperBook#init(Context)} at least once beforehand.
     * 
     * @param scheduler scheduler where operations will be run
     * @return new RxPaperBook
     */
    public static RxPaperBook with(Scheduler scheduler) {
        assertInitialized();
        return new RxPaperBook(scheduler);
    }

    /**
     * Open a custom {@link Book} running its operations on a provided scheduler.
     * <p/>
     * Requires calling {@link RxPaperBook#init(Context)} at least once beforehand.
     * 
     * @param customBook book name
     * @param scheduler scheduler where operations will be run
     * @return new RxPaperBook
     */
    public static RxPaperBook with(String customBook, Scheduler scheduler) {
        assertInitialized();
        return new RxPaperBook(customBook, scheduler);
    }

    /**
     * Open a custom {@link Book} with custom storage location path running its operations on
     * {@link Schedulers#io()}.
     * <p/>
     * Requires calling {@link RxPaperBook#init(Context)} at least once beforehand.
     *
     * @param path storage location name
     * @return new RxPaperBook
     */
    public static RxPaperBook withPath(String path) {
        assertInitialized();
        return new RxPaperBook(Schedulers.io(), path);
    }

    /**
     * Open a custom {@link Book} with custom storage location path running its operations on a
     * provided scheduler.
     * <p/>
     * Requires calling {@link RxPaperBook#init(Context)} at least once beforehand.
     *
     * @param path storage location
     * @param scheduler scheduler where operations will be run
     * @return new RxPaperBook
     */
    public static RxPaperBook withPath(String path, Scheduler scheduler) {
        assertInitialized();
        return new RxPaperBook(scheduler, path);
    }

    /**
     * Open a custom {@link Book} with custom storage location path running its operations on
     * {@link Schedulers#io()}.
     * <p/>
     * Requires calling {@link RxPaperBook#init(Context)} at least once beforehand.
     *
     * @param path storage location
     * @param customBook book name
     * @return new RxPaperBook
     */
    public static RxPaperBook withPath(String path, String customBook) {
        assertInitialized();
        return new RxPaperBook(path, customBook, Schedulers.io());
    }

    /**
     * Open a custom {@link Book} with custom storage location path running its operations on a
     * provided scheduler.
     * <p/>
     * Requires calling {@link RxPaperBook#init(Context)} at least once beforehand.
     *
     * @param path storage location
     * @param scheduler scheduler where operations will be run
     * @param customBook book name
     * @return new RxPaperBook
     */
    public static RxPaperBook withPath(String path, String customBook, Scheduler scheduler) {
        assertInitialized();
        return new RxPaperBook(path, customBook, scheduler);
    }

    /**
     * Saves most types of POJOs or collections in {@link Book} storage.
     * <p/>
     * To deserialize correctly it is recommended to have an all-args constructor, but other types
     * may be available.
     *
     * @param key object key is used as part of object's file name
     * @param value object to save, must have no-arg constructor, can't be null.
     * @return this Book instance
     */
    public <T> Completable write(final String key, final T value) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                book.write(key, value);
            }
        })
        // FIXME in RxJava1 the error would be propagated to updates.
        // In RxJava2 the error happens on the Completable this method returns.
        // This andThen block reproduces the behavior in RxJava1.
        .andThen(Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                try {
                    updates.onNext(Pair.create(key, value));
                } catch (Throwable t) {
                    updates.onError(t);
                }
            }
        })).subscribeOn(scheduler);
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their default
     * values.
     *
     * @param key object key to read
     * @param defaultValue value to be returned if key doesn't exist
     * @return the saved object instance or defaultValue
     */
    public <T> Single<T> read(final String key, final T defaultValue) {
        return Single.fromCallable(new Callable<T>() {
            @Override
            public T call() {
                return book.read(key, defaultValue);
            }
        }).subscribeOn(scheduler);
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their default
     * values.
     *
     * @param key object key to read
     * @return the saved object instance
     */
    public <T> Single<T> read(final String key) {
        return Single.fromCallable(new Callable<T>() {
            @Override
            public T call() {
                final T read = book.read(key);
                if (null == read) {
                    throw new IllegalArgumentException("Key " + key + " not found");
                }
                return read;
            }
        }).subscribeOn(scheduler);
    }

    /**
     * Delete saved object for given key if it is exist.
     */
    public Completable delete(final String key) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                book.delete(key);
            }
        }).subscribeOn(scheduler);
    }

    /**
     * Check if an object with the given key is saved in Book storage.
     *
     * @param key object key
     * @return true if object with given key exists in Book storage, false otherwise
     * @deprecated As of PaperDB release 2.6, replaced by {@link #contains(String)}}
     */
    public Single<Boolean> exists(final String key) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return book.exist(key);
            }
        }).subscribeOn(scheduler);
    }

    /**
     * Returns all keys for objects in {@link Book}.
     *
     * @return all keys
     */
    public Single<List<String>> keys() {
        return Single.fromCallable(new Callable<List<String>>() {
            @Override
            public List<String> call() {
                return book.getAllKeys();
            }
        }).subscribeOn(scheduler);
    }

    /**
     * Destroys all data saved in {@link Book}.
     */
    public Completable destroy() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                book.destroy();
            }
        }).subscribeOn(scheduler);
    }

    /**
     * Naive update subscription for saved objects. Subscription is filtered by key and type.
     *
     * @param key object key
     * @param backPressureStrategy how the backpressure is handled downstream
     * @return hot observable
     */
    public <T> Flowable<T> observe(final String key, final Class<T> clazz, BackpressureStrategy backPressureStrategy) {
        return updates.toFlowable(backPressureStrategy)
                .filter(new Predicate<Pair<String, ?>>() {
                    @Override
                    public boolean test(Pair<String, ?> stringPair) {
                        return stringPair.first.equals(key);
                    }
                }).map(new Function<Pair<String, ?>, Object>() {
                    @Override
                    public Object apply(Pair<String, ?> stringPair) {
                        return stringPair.second;
                    }
                }).ofType(clazz);
    }

    /**
     * Naive update subscription for saved objects.
     * <p/>
     * This method will return all objects for a key casted unsafely, and throw
     * {@link ClassCastException} if types do not match. For a safely checked and filtered version
     * use {@link this#observe(String, Class, BackpressureStrategy)}.
     *
     * @param key object key
     * @param backPressureStrategy how the backpressure is handled downstream
     * @return hot observable
     */
    @SuppressWarnings("unchecked")
    public <T> Flowable<T> observeUnsafe(final String key, BackpressureStrategy backPressureStrategy) {
        return updates.toFlowable(backPressureStrategy)
                .filter(new Predicate<Pair<String, ?>>() {
                    @Override
                    public boolean test(Pair<String, ?> stringPair) {
                        return stringPair.first.equals(key);
                    }
                }).map(new Function<Pair<String, ?>, T>() {
                    @Override
                    public T apply(Pair<String, ?> stringPair) {
                        return (T) stringPair.second;
                    }
                });
    }

    /**
     * Checks whether the current book contains the key given
     *
     * @param key the key to look up
     * @return true is the book contains a value for the given key
     */
    public Single<Boolean> contains(final String key) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return book.contains(key);
            }
        }).subscribeOn(scheduler);
    }

    /**
     * Returns the path of the current book
     *
     * @return the path to the book
     */
    public Single<String> getPath() {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                return book.getPath();
            }
        }).subscribeOn(scheduler);
    }

    /**
     * Returns the path of the data stored at the key passed as a parameter.
     * The returned path does not exist if the method has been called prior
     * saving data for the given key.
     *
     * @param key the key to look up
     * @return the path to the value stored at the key
     */
    public Single<String> getPath(final String key) {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                return book.getPath(key);
            }
        }).subscribeOn(scheduler);
    }
}

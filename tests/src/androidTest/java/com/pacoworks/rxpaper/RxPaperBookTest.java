
package com.pacoworks.rxpaper;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pacoworks.rxpaper.sample.MainActivity;
import com.pacoworks.rxpaper.sample.model.ComplexObject;
import com.pacoworks.rxpaper.sample.model.ImmutableObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Completable;
import rx.Subscriber;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(AndroidJUnit4.class)
public class RxPaperBookTest {
    @Rule
    public final ActivityTestRule<MainActivity> activity = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void setUp() throws Exception {
        RxPaperBook.init(activity.getActivity().getApplicationContext());
        RxPaperBook.with("WRITE").destroy().subscribe();
        RxPaperBook.with("WRITE_ERROR").destroy().subscribe();
        RxPaperBook.with("READ").destroy().subscribe();
        RxPaperBook.with("READ_WITH_DEFAULT").destroy().subscribe();
        RxPaperBook.with("DELETE").destroy().subscribe();
        RxPaperBook.with("EXISTS").destroy().subscribe();
        RxPaperBook.with("KEYS").destroy().subscribe();
        RxPaperBook.with("DESTROY").destroy().subscribe();
        RxPaperBook.with("UPDATES_UNCH").destroy().subscribe();
        RxPaperBook.with("UPDATES_CH").destroy().subscribe();
    }

    @Test
    public void testWrite() throws Exception {
        RxPaperBook book = RxPaperBook.with("WRITE", Schedulers.immediate());
        final String key = "hello";
        final Completable write = book.write(key, ComplexObject.random());
        Assert.assertFalse(book.book.exist(key));
        final TestSubscriber<Object> testSubscriber = TestSubscriber.create();
        write.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        Assert.assertTrue(book.book.exist(key));
    }

    @Test
    public void testReadError() throws Exception {
    }

    @Test
    public void testRead() throws Exception {
        RxPaperBook book = RxPaperBook.with("READ", Schedulers.immediate());
        final String key = "hello";
        final ComplexObject value = ComplexObject.random();
        book.write(key, value).subscribe();
        final TestSubscriber<ComplexObject> testSubscriber = TestSubscriber.create();
        book.<ComplexObject> read(key).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValues(value);
        // notFoundSubscriber
        final TestSubscriber<ComplexObject> notFoundSubscriber = TestSubscriber.create();
        String noKey = ":(";
        book.<ComplexObject> read(noKey).subscribe(notFoundSubscriber);
        notFoundSubscriber.awaitTerminalEvent();
        notFoundSubscriber.assertError(IllegalArgumentException.class);
        // incorrectTypeSubscriber
        book.<Integer> read(key).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Assert.fail();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                Assert.fail();
            }
        });
        // immutable objects
        book.write(key, new ImmutableObject(key)).subscribe();
        final TestSubscriber<ImmutableObject> immutableReadSubscriber = TestSubscriber.create();
        book.<ImmutableObject> read(key).subscribe(immutableReadSubscriber);
        immutableReadSubscriber.awaitTerminalEvent();
        immutableReadSubscriber.assertNoErrors();
        immutableReadSubscriber.assertCompleted();
        Assert.assertNotEquals(null, immutableReadSubscriber.getOnNextEvents().get(0).getValue());
    }

    @Test
    public void testReadWithDefault() throws Exception {
        RxPaperBook book = RxPaperBook.with("READ_WITH_DEFAULT", Schedulers.immediate());
        final String key = "hello";
        final ComplexObject value = ComplexObject.random();
        book.write(key, value).subscribe();
        final TestSubscriber<ComplexObject> testSubscriber = TestSubscriber.create();
        book.<ComplexObject> read(key).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValues(value);
        // notFoundSubscriber
        final TestSubscriber<ComplexObject> notFoundSubscriber = TestSubscriber.create();
        String noKey = ":(";
        final ComplexObject defaultValue = ComplexObject.random();
        book.read(noKey, defaultValue).subscribe(notFoundSubscriber);
        notFoundSubscriber.awaitTerminalEvent();
        notFoundSubscriber.assertNoErrors();
        notFoundSubscriber.assertValueCount(1);
        notFoundSubscriber.assertValues(defaultValue);
        // incorrectTypeSubscriber
        book.<Integer> read(key).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Assert.fail("Expected ClassCastException");
            }

            @Override
            public void onError(Throwable e) {
                if (!(e instanceof ClassCastException)) {
                    Assert.fail(e.getMessage());
                }
            }

            @Override
            public void onNext(Integer integer) {
                Assert.fail("Expected ClassCastException");
            }
        });
    }

    @Test
    public void testDelete() throws Exception {
        RxPaperBook book = RxPaperBook.with("DELETE", Schedulers.immediate());
        final String key = "hello";
        final TestSubscriber<ComplexObject> errorSubscriber = TestSubscriber.create();
        book.delete(key).subscribe(errorSubscriber);
        errorSubscriber.awaitTerminalEvent();
        errorSubscriber.assertCompleted();
        errorSubscriber.assertNoErrors();
        book.write(key, ComplexObject.random()).subscribe();
        final TestSubscriber<ComplexObject> testSubscriber = TestSubscriber.create();
        book.<ComplexObject> delete(key).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        Assert.assertFalse(book.book.exist(key));
    }

    @Test
    public void testExists() throws Exception {
        RxPaperBook book = RxPaperBook.with("EXISTS", Schedulers.immediate());
        final String key = "hello";
        book.write(key, ComplexObject.random()).subscribe();
        final TestSubscriber<Boolean> foundSubscriber = TestSubscriber.create();
        book.exists(key).subscribe(foundSubscriber);
        foundSubscriber.awaitTerminalEvent();
        foundSubscriber.assertNoErrors();
        foundSubscriber.assertValueCount(1);
        foundSubscriber.assertValues(true);
        // notFoundSubscriber
        final TestSubscriber<Boolean> notFoundSubscriber = TestSubscriber.create();
        String noKey = ":(";
        book.exists(noKey).subscribe(notFoundSubscriber);
        notFoundSubscriber.awaitTerminalEvent();
        notFoundSubscriber.assertCompleted();
        notFoundSubscriber.assertValueCount(1);
        notFoundSubscriber.assertValues(false);
    }

    @Test
    public void testGetAllKeys() throws Exception {
        RxPaperBook book = RxPaperBook.with("KEYS", Schedulers.immediate());
        final String key = "hello";
        final String key2 = "you";
        final ComplexObject value = ComplexObject.random();
        book.write(key, value).subscribe();
        book.write(key2, value).subscribe();
        final TestSubscriber<List<String>> foundSubscriber = TestSubscriber.create();
        book.keys().subscribe(foundSubscriber);
        foundSubscriber.awaitTerminalEvent();
        foundSubscriber.assertNoErrors();
        foundSubscriber.assertValueCount(1);
        final List<List<String>> onNextEvents = foundSubscriber.getOnNextEvents();
        foundSubscriber.assertValueCount(1);
        Assert.assertEquals(book.book.getAllKeys(), onNextEvents.get(0));
    }

    @Test
    public void testDestroy() throws Exception {
        RxPaperBook book = RxPaperBook.with("DESTROY", Schedulers.immediate());
        final String key = "hello";
        final String key2 = "you";
        final ComplexObject value = ComplexObject.random();
        book.write(key, value).subscribe();
        book.write(key2, value).subscribe();
        final TestSubscriber<Void> destroySubscriber = TestSubscriber.create();
        book.destroy().subscribe(destroySubscriber);
        destroySubscriber.awaitTerminalEvent();
        destroySubscriber.assertCompleted();
        destroySubscriber.assertNoErrors();
        destroySubscriber.assertValueCount(0);
        Assert.assertFalse(book.book.exist(key));
        Assert.assertFalse(book.book.exist(key2));
    }

    @Test
    public void testUpdatesUnchecked() throws Exception {
        RxPaperBook book = RxPaperBook.with("UPDATES_UNCH", Schedulers.immediate());
        final String key = "hello";
        final ComplexObject value = ComplexObject.random();
        final TestSubscriber<ComplexObject> updatesSubscriber = TestSubscriber.create();
        book.<ComplexObject> observeUnsafe(key).subscribe(updatesSubscriber);
        updatesSubscriber.assertValueCount(0);
        book.write(key, value).subscribe();
        updatesSubscriber.assertValueCount(1);
        updatesSubscriber.assertValue(value);
        final ComplexObject newValue = ComplexObject.random();
        book.write(key, newValue).subscribe();
        updatesSubscriber.assertValueCount(2);
        updatesSubscriber.assertValues(value, newValue);
        // Error value
        final int wrongValue = 3;
        book.<ComplexObject> observeUnsafe(key).subscribe(new Subscriber<ComplexObject>() {
            @Override
            public void onCompleted() {
                Assert.fail("Expected ClassCastException");
            }

            @Override
            public void onError(Throwable e) {
                if (!(e instanceof ClassCastException)) {
                    Assert.fail(e.getMessage());
                }
            }

            @Override
            public void onNext(ComplexObject complexObject) {
                Assert.fail("Expected ClassCastException");
            }
        });
        book.write(key, wrongValue).subscribe();
    }

    @Test
    public void testUpdatesChecked() throws Exception {
        RxPaperBook book = RxPaperBook.with("UPDATES_CH", Schedulers.immediate());
        final String key = "hello";
        final ComplexObject value = ComplexObject.random();
        final TestSubscriber<ComplexObject> updatesSubscriber = TestSubscriber.create();
        book.observe(key, ComplexObject.class).subscribe(updatesSubscriber);
        updatesSubscriber.assertValueCount(0);
        book.write(key, value).subscribe();
        updatesSubscriber.assertValueCount(1);
        updatesSubscriber.assertValues(value);
        final ComplexObject newValue = ComplexObject.random();
        book.write(key, newValue).subscribe();
        updatesSubscriber.assertValueCount(2);
        updatesSubscriber.assertValues(value, newValue);
        // Error value
        final int wrongValue = 3;
        book.write(key, wrongValue).subscribe();
        updatesSubscriber.assertValueCount(2);
        updatesSubscriber.assertValues(value, newValue);
        updatesSubscriber.assertNoErrors();
    }
}

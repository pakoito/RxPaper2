# RxPaper2

RxPaper is a [RxJava](https://github.com/ReactiveX/RxJava) wrapper for the cool [Paper](https://github.com/pilgr/Paper) library. It's a clean rewrite of the original [RxPaper](http://www.github.com/cesarferreira/rxpaper) library by [César Ferreira](http://www.github.com/cesarferreira).

![Paper icon](https://raw.githubusercontent.com/pilgr/Paper/master/paper_icon.png)

For the RxJava1 version, please go to [RxPaper](https://www.github.com/pakoito/RxPaper).

## Rationale

Sometimes you need storage for arbitrary objects on disk, but do not want to store them in a relational database with all the associated problems: writing ORMs, composing the queries, updating scripts. For this purpose NoSQL data storages were created: schemaless document repositories where to store arbitrary data that's not structured.

RxPaper allows access to Paper, which is a NoSQL data storage for Android that allows you to save arbitrary objects in system files called Books. Serialization and deserialization is done using efficient [Kryo](https://github.com/EsotericSoftware/kryo).

The Paper/Kryo combination supports some partial data structure changes. Check [Paper's README](https://github.com/pilgr/Paper#handle-data-structure-changes) for more information.

## Usage

### Object model handling

RxPaper is subject to the same restrictions as the current version of Paper when the library was last updated. As of Paper 1.5, the library can work with empty constructors and all-arg constructors. Some other combinations would need to be tested by the library user.

I personally recommend using immutable objects, as it makes data handling way simpler on both sides. An immutable object has an all-args constructor, doesn't allow any null fields, and keeps all fields public and final. Like any other dto in Java, it is recommended to implement your own version of `equals`, `hashCode` and `toString`.

As of Paper 1.5 you can also add your own serializers by calling `Paper.addSerializer()`. Partial structure changes are supported too, as described on [Paper's README](https://github.com/pilgr/Paper#handle-data-structure-changes).

### Threading

All operations are run on the [Scheduler](https://github.com/Froussios/Intro-To-RxJava/blob/master/Part%204%20-%20Concurrency/1.%20Scheduling%20and%20threading.md#schedulers) provided on the constructor, or `Schedulers.io()` by default. When subscribing to them, specially if using the data to be applied to UI; it's recommended to use the operator `observeOn(Scheduler)` to see the changes on any desired thread, i.e. Android's main thread.

### Initialization

Before the library is usable it requires initializing the underlying Paper library. You only have to initialize RxPaper by calling:

```java
RxPaperBook.init(context);
```

### Working on a book

RxPaper works on books, and each is a folder on the system. A book is only opened and closed on an operation, but you can check the [Paper](https://github.com/pilgr/Paper) repository for specifics. To make sure no operations are done on the main thread, any operations done on a book can be executed on one [Scheduler](https://github.com/Froussios/Intro-To-RxJava/blob/master/Part%204%20-%20Concurrency/1.%20Scheduling%20and%20threading.md#schedulers) provided in the constructor. To create an instance of RxPaper the library provides several flavours.

```java
RxPaperBook.with();
```

Works with the default book, and executes operations on [`Schedulers.io()`](https://github.com/Froussios/Intro-To-RxJava/blob/master/Part%204%20-%20Concurrency/1.%20Scheduling%20and%20threading.md#schedulers).

```java
RxPaperBook.with(Schedulers.newThread());
```

Works with the default book, and executes operations on any provided scheduler.

```java
RxPaperBook.with("my_book_name");
```

Works with a custom book with the provided id/name, and executes operations on [`Schedulers.io()`](https://github.com/Froussios/Intro-To-RxJava/blob/master/Part%204%20-%20Concurrency/1.%20Scheduling%20and%20threading.md#schedulers).

```java
RxPaperBook.with("my_book_name", Schedulers.newThread());
```

Works with a custom book with the provided id/name, and executes operations on any provided scheduler.

```java
RxPaperBook.withPath(myPath);
RxPaperBook.withPath(myPath, scheduler);
RxPaperBook.withPath(myPath, "my_book_name");
```

Works with a custom storage location.

### Writing a value

Write is a `Completable` operation, a subset of `Observable<T>` without a return value, just success/error. Completables can be converted back to Observables by using the operator `toObservable()`.

```java
RxPaperBook book = RxPaperBook.with("my-book");
Completable write = book.write(key, value);
// Because RxJava is lazily evaluated, the operation is not executed until the Observable is subscribed.
write.subscribe(new Completable.CompletableSubscriber() {
            @Override
            public void onCompleted() {
                // Operation suceeded
            }

            @Override
            public void onError(Throwable e) {
                // Operation failed
            }

            @Override
            public void onSubscribe(Subscription d) {
                // Called once on creation
            }
        });
```

Every key written is stored as a file on the system under the folder specified by the book.

### Reading a value

Reading is a `Single<T>` operation, a subset of `Observable<T>` that returns just a single element and then completes. Singles can be converted back to Observables by using the operator `toObservable()`. Reading comes in two flavours:

```java
Single<ComplexObject> read = book.read(key);
read.subscribe(new SingleSubscriber<ComplexObject>() {
            @Override
            public void onSuccess(ComplexObject value) {
                // Operation succeeded and returned a value
            }

            @Override
            public void onError(Throwable error) {
                // Operation failed
            }
        });

ComplexObject defaultValue = new ComplexObject();
Single<ComplexObject> readOrDefault = book.read(key, defaultValue);
readOrDefault.subscribe(new SingleSubscriber<ComplexObject>() {
            @Override
            public void onSuccess(ComplexObject value) {
                // Operation succeeded and returned a value
            }

            @Override
            public void onError(Throwable error) {
                // Operation failed
            }
        });
```

`read(key)` fails with `IllegalArgumentException` if the key is not found. `read(key, defaultValue)` returns a default value if the key is not found.

If the subscriber is not of the same type as the value stored expect a `ClassCastException`.

Make sure to read the rules on [how object models are handled](https://github.com/pakoito/RxPaper#object-model-handling) on the section above.

#### Observing changes on a key

All write operations are naively forwarded into a `PublishSubject<?>` by default, which makes it possible to observe all changes for a specific key. Observing is a `Flowable<T>` operation that never completes.

```java
Flowable<ComplexObject> observe = book.observe(key, ComplexObject.class);
observe.subscribe(new Subscriber() { /* ... */ });
```

Observe filters on both the key and the type. Another version of observe that filters only on key and casts any values unsafely is provided under the name `observeUnsafe()`. It's recommended to use it with strict care.

#### Contains

Contains is a `Single<Boolean>` operation that returns true if the key is on the current book, or false otherwise.

```java
Single<Boolean> contains = book.contains(key);
contains.subscribe(new SingleSubscriber<Boolean>() { /* ... */ });
```

#### Delete

Delete is a `Completable` operation. Deletes data stored for a key on the current book. It will still succeed even if the key is not found.

```java
Completable delete = book.delete(key);
delete.subscribe(new Completable.CompletableSubscriber() { /* ... */ });
```

#### Keys

Keys is a `Single<List<String>>` operation that returns a list of all keys stored on the current book.

```java
Single<List<String>> keys = book.keys();
exists.subscribe(new SingleSubscriber<List<String>>() { /* ... */ });
```

#### GetPath

Returns the path to the current book. Note that the path will not exist until any value is saved in the book. 

```java
Single<String> path = book.getPath();
path.subscribe(new SingleSubscriber<String>() { /* ... */ });
```

Returns the path to the key in the current book. Note that the path will not exist until a value is saved for that key. 

```java
Single<String> pathKey = book.getPath("my_key");
pathKey.subscribe(new SingleSubscriber<String>() { /* ... */ });
```

#### Destroy

Destroy is a `Completable` operation that deletes all keys and values on the current book.

```java
Completable destroy = book.destroy();
destroy.subscribe(new Completable.CompletableSubscriber() { /* ... */ });
```

## Distribution

Add as a dependency to your `build.gradle`

```groovy
    repositories {
        ...
        maven { url "https://jitpack.io" }
        ...
    }
    
    dependencies {
        ...
        compile 'com.github.pakoito:RxPaper2:1.4.0'
        ...
    }
```

or to your `pom.xml`

```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    
    <dependency>
        <groupId>com.github.pakoito</groupId>
        <artifactId>RxPaper2</artifactId>
        <version>1.3.0</version>
    </dependency>
```

## License

Copyright (c) 2019 pakoito & 2015 César Ferreira

The MIT License (MIT)

See LICENSE.md

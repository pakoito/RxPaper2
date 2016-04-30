#RxPaper

RxPaper is an [RxJava](https://github.com/ReactiveX/RxJava) wrapper for the cool [Paper](https://github.com/pilgr/Paper) library. It's a clean rewrite of the original [RxPaper](http://www.github.com/cesarferreira/rxpaper) library by [César Ferreira](http://www.github.com/cesarferreira).

![Paper icon](https://raw.githubusercontent.com/pilgr/Paper/master/paper_icon.png)

##Rationale

Sometimes you need storage for arbitrary objects on disc, but do not want to store them in a relational database with all the associated problems: writing ORMs, composing the queries, updating scripts. For this purpose NoSQL data storages were created: schemaless document repositories where to store arbitrary data that's not structured.

RxPaper allows access to Paper, which is a NoSQL data storage for Android that allows you to save arbitrary objects in system files called Books. Serialization and deserialization is done using efficient [Kryo](https://github.com/EsotericSoftware/kryo).

The Paper/Kryo combination supports some partial data structure changes. Chech [Paper's README](https://github.com/pilgr/Paper#handle-data-structure-changes) for more information.

##Usage

###Object model handling

RxPaper is subject to the same restrictions as the current version of Paper when the library was last updated. As of Paper 1.5, the library can work with empty constructors and all-arg constructors. Some other combinations would need to be tested by the library user.

I personally recommend using immutable objects, as it makes data handling way simpler on both sides. An immutable object has an all-args constructor, doesn't allow any null fields, and keeps all fields public and final. Like any other dto in Java, it is recommended to implement your own version of `equals`, `hashCode` and `toString`.

As of Paper 1.5 you can also add your own serializers by calling `Paper.addSerializer()`. Partial structure changes are supported too, as described on [Paper's README]((https://github.com/pilgr/Paper#handle-data-structure-changes)).

###Initialization

Before the library is usable it requires initializing the underlying Paper library. You only have to initialize RxPaper by calling:

```java
RxPaper.init(context);
```

###Working on a book

RxPaper works on books, and each is a file on the system. A book is only open and closed on an operation, but you can check Paper repository for specifics. To make sure no operations are done on the main thread, any operations done on a book can be executed on one Scheduler provided in the constructor. To create an instance of RxPaper the library provides several flavours.

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

###Writing a value

Write is a `Completable` operation, a subset of `Observable<T>` without a return value, just success/error.

```java
RxPaperBook defaultBook = RxPaper.with();
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

###Reading a value

Reading is a `Single<T>` operation, a subset of `Observable<T>` that returns just a single element and then completes. 

Reading comes in two flavours:

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

Single<ComplexObject> readOrDefault = book.read(key, new ComplexObject());
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

`read(key)` fails with `IllegalArgumentException` if the key is not found. `read(key, defaultValue)` returns a defualt value if the key is not found.

If the subscriber is not of the same type as the value stores, expect a `ClassCastException`.

Make sure to read the rules on how object models are handler on the section above.

####Observing changes on a key

All write operations are naively forwarded into a `PublishSubject<?>` by default, which makes it possible to observe all changes for a specific key.Observing is an `Observable<T>` operation that never completes.

```java
Observable<ComplexObject> observe = book.observe(key, ComplexObject.class);
observe.subscribe(new Subscriber() { /* ... */ });
```

Observe filters on both the key and the type. Another version of observe that filters only on key and casts any values unsafely is provided under the name `observeUnsafe()`. It's recommended to use it with strict care.

####Exists

Exists is a `Single<Boolean>` operation that returns true if the key is on the current book, or false otherwise.

```java
Single<Boolean> exists = book.exists(key);
exists.subscribe(new SingleSubscriber<Boolean>() { /* ... */ });
```

####Delete

Delete is a `Completable` operation. Deletes data stored for a key on the current book. Will still succeed even if the key is not found.

```java
Completable delete = book.delete(key);
delete.subscribe(new Completable.CompletableSubscriber() { /* ... */ });
```

####Keys

Keys is a `Single<List<String>>` operation that returns a list of all keys stored on the current book.

```java
Single<List<String>> keys = book.keys();
exists.subscribe(new SingleSubscriber<List<String>>() { /* ... */ });
```

####Destroy

Destroy is a `Completable` operation that deletes all keys and values on the current book.

```java
Completable destroy = book.destroy();
destroy.subscribe(new Completable.CompletableSubscriber() { /* ... */ });
```

##Distribution

Add as a dependency to your `build.gradle`

    repositories {
        ...
        maven { url "https://jitpack.io" }
        ...
    }
    
    dependencies {
        ...
        compile 'com.github.pakoito:RxPaper:1.0.+'
        ...
    }

or to your `pom.xml`

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    
    <dependency>
        <groupId>com.github.pakoito</groupId>
        <artifactId>RxPaper</artifactId>
        <version>1.0.0</version>
    </dependency>

##License

Copyright (c) pakoito 2016

The Apache Software License, Version 2.0

See LICENSE.md
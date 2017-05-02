# JavaPromise
JavaPromise is a lightweight Promise library based on the [A+ promise spec](https://promisesaplus.com/) and
inspired by [PromiseKit](https://github.com/mxcl/PromiseKit)
and [Bluebird](https://github.com/petkaantonov/bluebird/).
All modern day data-driven apps deal with asynchronous programming, this causes challenges and can lead to
[callback hell](http://callbackhell.com/). The use of
[anonymous inner classes](https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html)  for callbacks
in Java can make code even more unorganized and hard to follow. Using promises
to handle asynchronous processes can help you make your code more concise, organized and efficient by offering a
cleaner API and eliminating the need for duplicating error handling logic.

```java
makeAsyncRequest()
    .then(data -> {
        return transformData(data);
    })
    .<CustomViewModel>then(transformedData -> {
         // transformedData is an instanceof CustomViewModel
        mTextView.setText(transformedData.title);
    })
    .error(error -> {
        // handle failure of either async request or transformData
    })
```

# Getting Started
## Installation

#### For projects using gradle:
Add the following to your project `build.gradle` file:
```groovy
allprojects {
    repositories {
        jcenter()
        maven {
            url  "http://dl.bintray.com/nascent/Maven"
        }
    }
}
```

Then add this to your module's `build.gradle` file:
```groovy
dependencies {
    compile 'com.javapromise.promise:javapromiseandroid:0.0.4'
}
```

Sync your gradle project and your good to go!

# Basic Usage

You can create a promise by wrapping an existing asynchronous process
```java
public Promise<String> getGooglePage() {
    RequestQueue queue = Volley.newRequestQueue(this);

    return new Promise(deferral -> {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
            "http://www.google.com", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Resolve with the response
                deferral.resolve(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // fail with exception
                deferral.reject(new Exception(error.message));
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    })
}
getGooglePage()
    .then(response -> {
        // this will execute when the initial promise is resolved
        // value in this case will be strongly typed as a string
        assert(value instanceof String) // true
        myTextView.setText(Html.fromHtml(response));
    }).error(error -> {
        // this will execute if the inital promise is rejected
    });

```

There are convenience methods for creating rejected and resolved Promises:
```java
Promise.resolve(value);
Promise.reject(value);
```

### Chaining promises

Chaining promises can be accomplished by either returning a value or promise in a `then` block.
Unfortunately due to limitations of generics in Java you are required to provide the type info for any
chained `then` blocks after the first.
```java
somePromise
    .then(v1 -> {
        // v1 will be strongly typed
        return Integer.valueOf(50);
    })
    .<Integer>then(v2 -> {
        // v2 will be 50, as of right now the type of the value in the
        // 2nd chained then needs to be explicit.
        return someMethodThatReturnsStringPromise(v2);
    })
    .<String>then(v3 -> {
        // this block will execute once the promise returned above is resolved
        assert(v3 instanceof String)
    })
    .error(error -> {
        // this error block will be executed if any of the above promises is rejected
        // unless one of those rejections is already handled by a .error block and that
        // exception is not re-thrown
    })
    .always(() -> {
        // this block will be executed regardless of resolution/rejection
    });
```

If an exception is thrown in a `then` block, the return value is a rejected Promise.
```java
promise.then(value -> {
    throw new RunTimeException("Unexpected");
}).then(v2 -> {
    // this block won't execute
}).error(error -> {
    // this block will execute where error is the exception thrown above
    // Note: you are able to re-throw this exception if you do not want it
    // to be consumed by this error block
});
```

### Recovering rejected promises

Rejected promises can be 'recovered' in a error block that will return a resolved promise.
```java
Promise.<String>reject(new Exception("Failure"))
    .error((error, recovery) -> {
        // invoking recovery.recover with a value matching the original type specified
        // will result in a resolved promise
        recovery.recover(String.valueOf("Recovered"));
    })
    .<String>then(value -> {
        assert(value.equals("Recovered"));
    })
```

### Waiting on multiple promises

You can wait on multiple promises by providing an arraylist or array of Promises that represent the same value type to `Promise.when`.
```java
Integer[] initialValues = { 0, 1, 2, 3 };

ArrayList<Promise<Integer>> promises = new ArrayList<>();
for (Integer num : initialValues) {
    promises.add(createPromise(num));
}

Promise.when(promises)
    .then(values -> {
        // where values is an arraylist of Integers matching the order of
        // the promises arraylist
    });
```

If you need to wait on a list of promises that represent varying types (value list will not be strongly typed) you can use `Promise.all`.
```java
Promise[] list = {
    Promise.resolve(String.valueOf("Test")),
    Promise.resolve(Integer.valueOf(20)),
    Promise.resolve(new ArrayList()),
    Promise.resolve(someCustomObject)
};

Promise.all(list)
    .then(valueList -> {

        // valueList will be an ArrayList of values matching the order
        // of the promises in the input list
        // they have all be of type Object
        return null;
    });
```


You can wait on up to three promises of differing types using `Promise.when` in combination with `VariadicPromiseValue`.
Using this strategy *will* give you strongly typed results.
```java
String firstVal = "First value";
Integer secondVal = new Integer(50);

Promise<VariadicPromiseValue<String, Integer, Void>> promise = Promise.when(
    createAsyncPromise(firstVal),
    createAsyncPromise(secondVal)
);
promise
    .then(values -> {
        values.getFirst() // "First value"
        values.getSecond() // 50
        values.getThird() // null
    })
```

# Contributing

Run
```bash
$ scripts/setup.sh
```
To set up pre-commit symlinks. Pre-commit hooks will run unit tests and increment the version number by `0.0.1`.
Running `git commit` with the `--no-verify` will skip the pre-commit hooks.

##### To Upload new version to Bintray
```sh
$ ./gradlew install
$ ./gradlew bintrayUpload
```


##### Original bintray deployment setup:
https://antoniocappiello.com/2015/11/11/publish-your-library-to-jcenter-in-3-steps/
- add username and API key to local.properties file


##### TODO in project:
- README:
  - install instructions without gradle
- COMING SOON Features:
  - tap

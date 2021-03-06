[![GitHub tag](https://img.shields.io/github/tag/mvysny/vaadin-on-kotlin.svg)](https://github.com/mvysny/vaadin-on-kotlin/tags)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.vaadinonkotlin/vok-rest-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/eu.vaadinonkotlin/vok-rest-client)

# VoK REST Client Support

This module serves two purposes:

* It makes it easy to consume REST endpoints exported by another server and process/show them in your VoK server.
* It makes it easy to test your VoK REST server endpoints.

We're using the [okhttp](http://square.github.io/okhttp/)
library, and you can optionally use the [Retrofit](https://square.github.io/retrofit/)
support though this is not recommended: it is based on annotation magic which tends
to fail with mysterious ways.

> Note: If you wish to _expose_ your objects from your app, rather than _consume_ objects from some other service, please see the [vok-rest](../vok-rest) module.

## Adding REST Client To Your App

Include dependency on this module to your app; just add the following Gradle dependency to your `build.gradle`:

```groovy
dependencies {
    compile "com.github.vok:vok-rest-client:x.y.z"
}
```

> Note: to obtain the newest version see above for the most recent tag

Now you can write the REST client. The recommended method is to use `okhttp` directly; you can also use `Retrofit` to generate REST clients out of
your annotated interfaces but that's not really recommended.

### Using `okhttp`

You simply use the `OkHttpClient` to make HTTP calls. See [OkHttp home page](http://square.github.io/okhttp/) for documentation on the API.
VoK introduces the `exec` method which helps tremendously with synchronous calls. It fails automatically when the response is not in 200..299.

See the example code for more details:

```kotlin
// Demoes direct access via okhttp
class PersonRestClient(val baseUrl: String) {
    init {
        require(!baseUrl.endsWith("/")) { "$baseUrl must not end with a slash" }
    }
    private val client: OkHttpClient = RetrofitClientVokPlugin.okHttpClient!!
    fun helloWorld(): String {
        val request = Request.Builder().url("$baseUrl/helloworld").build()
        return client.exec(request) { response -> response.string() }
    }
    fun getAll(): List<Person> {
        val request = Request.Builder().url(baseUrl).build()
        return client.exec(request) { response -> response.jsonArray(Person::class.java) }
    }
}
val client = PersonRestClient("http://localhost:8080/rest/person")
println(client.getAll())
```

The `RetrofitClientVokPlugin.okHttpClient` is constructed automatically by the
VOK module loading mechanism in `RetrofitClientVokPlugin.init()`. Alternatively, if you
need to customize/configure the `OkHttpClient` instance, you can simply assign your
own `OkHttpClient` instance to `RetrofitClientVokPlugin.okHttpClient` before
the VoK is initialized. Your instance will not be overwritten by `init()`.

### Polling CRUD Endpoint For Data

You can use the `CrudClient` class to access CRUD REST endpoints:

```kotlin
val crud = CrudClient("http://localhost:8080/rest/person/", Person::class.java)
println(crud.getAll())
```

The CRUD client supports filtering, paging and sorting. It also implements the `DataLoader` interface
which is very easy to turn into Vaadin 8 or Vaadin 10's `DataProvider` which can
then be fed directly to the Vaadin `Grid` component, making it easy to display
CRUD contents in a tabular fashion:

```kotlin
val crud = CrudClient("http://localhost:8080/rest/person/", Person::class.java)
val dp = DataLoaderAdapter(Person::class.java, crud, { it.id!! }).withConfigurableFilter2()
grid.dataProvider = dp
```

Just add dependency on either [vok-util-vaadin8](../vok-util-vaadin8) or
[vok-util-vaadin10](../vok-util-vaadin10) to get access to the `DataLoaderAdapter` class.
Please read more about this in the [Accessing NoSQL or REST data sources](http://www.vaadinonkotlin.eu/nosql_rest_datasources.html).

The CRUD Client expects the CRUD endpoint to expose data in the following fashion:

* `GET /rest/users` returns all users
* `GET /rest/users?select=count` returns a single number - the count of all users. This is only necessary for `getCount()`
or if you plan to use this client as a backend for Vaadin Grid.
* `GET /rest/users/22` returns one users
* `POST /rest/users` will create an user
* `PATCH /rest/users/22` will update an user
* `DELETE /rest/users/22` will delete an user

Paging/sorting/filtering is supported: the following query parameters will simply be added to the "get all" URL request:

* `limit` and `offset` for result paging. Both must be 0 or greater. The server may impose max value limit on the `limit` parameter.
* `sort_by=-last_modified,+email,first_name` - a list of sorting clauses. The server may restrict sorting by only a selected subset of properties.
* The filters are simply converted to query parameters, for example `age=81`. `OpFilter`s are also supported: the value will be prefixed with a special operator prefix:
`eq:`, `lt:`, `lte:`, `gt:`, `gte:`, `ilike:`, `like:`, `isnull:`, `isnotnull:`, for example `age=lt:25`. A full example is `name=ilike:martin&age=lte:70&age=gte:20&birthdate=isnull:&grade=5`.
OR filters are not supported - passing `OrFilter` will cause `getAll()` to throw `IllegalArgumentException`.

All column names are expected to be Kotlin property name of the bean.

### Retrofit

Retrofit uses okhttp under the belt but allows you to create client out of annotated interfaces. Might save you a few keystrokes, but makes it
impossible to debug. This way is therefore not recommended.

For precise instructions on how to construct REST client interfaces for Retrofit please visit the [Retrofit page](https://square.github.io/retrofit/).

Example:
```kotlin
interface PersonRestClient2 {
    @GET("helloworld")
    @Throws(IOException::class)
    fun helloWorld(): String

    @GET(".")
    @Throws(IOException::class)
    fun getAll(): List<Person>
}

val client = createRetrofit("http://localhost:8080/rest/person/").create(PersonRestClient2::class.java)
println(client.getAll())
```

Retrofit is automatically configured by `vok-rest-client` to properly fail on result code other than 200..299; it is configured to properly
handle any type of values. Retrofit doesn't support functions returning `Unit` or `void` - just make the function return `Unit?`.

## Using `vok-rest-client` For Testing

If you use `vok-rest-client` from within of your VoK app then VoK will take care of properly
initializing and destroying of this module. However, if you plan to use this module for testing purposes, it's important to properly initialize it
and destroy it after all of your tests are done.

You need to do one of these:

* Call `VaadinOnKotlin.init()` before all tests and `VaadinOnKotlin.destroy()` after all tests. That will
  also properly initialize and destroy the `vok-rest-client` module. In the example below, this is
  done via the call to `usingApp()` function, which in turn calls `Bootstrap().contextInitialized(null)`
  and `Bootstrap().contextDestroyed(null)`.
* Or you need to init the module manually: `RetrofitClientVokPlugin().init()` and `RetrofitClientVokPlugin().destroy()`

Otherwise the OkHttpClient won't get initialized and the test will fail with NPE.

Example test:

```kotlin
class PersonRestTest : DynaTest({
    lateinit var javalin: Javalin
    beforeGroup {
        javalin = Javalin.create().disableStartupBanner()
        javalin.configureRest().start(9876)
    }
    afterGroup { javalin.stop() }

    usingApp()  // to bootstrap the app to have access to the database.

    lateinit var client: PersonRestClient
    beforeEach { client = PersonRestClient("http://localhost:9876/rest/") }

    test("hello world") {
        expect("Hello World") { client.helloWorld() }
    }

    test("get all users") {
        expectList() { client.getAll() }
        val p = Person(name = "Duke Leto Atreides", age = 45, dateOfBirth = LocalDate.of(1980, 5, 1), maritalStatus = MaritalStatus.Single, alive = false)
        p.save()
        val all = client.getAll()
        p.created = all[0].created
        expectList(p) { all }
    }
})
```

In order to start Javalin with Jetty, you also need to add Jetty to your test classpath:

```groovy
dependencies {
    testCompile("org.eclipse.jetty.websocket:websocket-server:9.4.12.v20180830")
}
```

## Customizing JSON mapping

Gson by default only export non-transient fields. It only exports actual Java fields, or only Kotlin properties that are backed by actual fields;
it ignores computed Kotlin properties such as `val reviews: List<Review> get() = Review.findAll()`.
To reconfigure Gson, just set a new instance to `RetrofitClientVokPlugin.gson`. To reconfigure `OkHttpClient` just set `RetrofitClientVokPlugin.okHttpClient`
before calling `VaadinOnKotlin.init()`.

Please see [Gson User Guide](https://github.com/google/gson/blob/master/UserGuide.md) for more details.

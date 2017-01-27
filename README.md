# GoDaddy Logger

[![Build Status](https://travis-ci.org/godaddy/godaddy-logger.svg?branch=master)](https://travis-ci.org/godaddy/godaddy-logger)
[![latest version](https://img.shields.io/maven-central/v/com.godaddy/logging.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.godaddy%22%20AND%20a%3A%22logging%22)

The GoDaddy Logger is a wrapping of the [SLF4J Logger](http://www.slf4j.org/manual.html), which includes various features built on top of the SLF4J Logger to allow for a better and easier logging experience.

Instead of worrying about formatting your logs, the GoDaddy Logger will handle the formatting of your logs for you in a clean efficient way, with the use of [reflectasm](https://github.com/EsotericSoftware/reflectasm). An example of how this works can be seen with the added `with` functionality:

```
Logger logger = LoggerFactory.getLogger(LoggerTest.class);
Car car = new Car("911", 2015, "Porsche", 70000.00, Country.GERMANY, new Engine("V12"));
logger.with(car).info("Logging Car");
```
Produces the following Log Statement:
```
14:31:03.943 [main] INFO  com.godaddy.logger.LoggerTest - Logging Car; cost=70000.0; country=GERMANY; engine.name="V12"; make="Porsche"; model="911"; year=2015; test="HI"
```
Notice the use of the with functionality: logger.**with(car)**.info("Logging Car");

## What's included in this library?

 - [org.slf4j.slf4j-api](http://www.slf4j.org/) - 1.7.10
 - [com.google.guava.guava](https://code.google.com/p/guava-libraries/) - 18.0
 - [com.esotericsoftware.reflectasm](https://github.com/EsotericSoftware/reflectasm) - 1.10.0

## How To Use
If you are using maven first add a `godaddy.logging.version` property to your POM with the current version of the logging library (which is can be found in the shield above, `1.2.1` for example):
```
<properties>
    <godaddy.logging.version>INSERT VERSION HERE</godaddy.logging.version>
</properties>
```

Then add the `com.goodaddy.logging` library as a dependency:

```
<dependency>
	<groupId>com.godaddy</groupId>
	<artifactId>logging</artifactId>
	<version>${godaddy.logging.version}</version>
</dependency>
```
A basic Logger, with all of the default settings can be created as follows:
```
Logger logger = LoggerFactory.getLogger(SomeClass.class);
```
Where `SomeClass.class` is the name of the class where the logger is instantiated.

## Logging Configs
The GoDaddy logger can be configured in various ways.

 - **Recursive Level**: This defines the number of inner class levels to be logged. By default the logger will go 5 levels deep.
 - **Method Prefixes**: This contains a set of method prefixes to include. By default this set contains "get" and "is". Any method containing a prefix defined in Method Prefixes will have its return value outputted in the logs. Method prefixes can be added to the defaults.
 - **Excludes Prefixes**: This contains a set of field prefixes to exclude. By default this set contains val$ and this$. Any field containing the a prefix defined in Exclude Prefixes will be ignored in the logs. Exclude Prefixes can be added to the defaults.
 - **Custom Mapper**: The Custom Mapper provides the ability to define a specific mapping function (returning a string) for a class. By default, `UUID.class` is contained in this custom mapper and is set to map to its `toString` function. Custom mappings that are added to this custom mapper will be used in the logs.
 - **Message Builder Function**: The Message Builder Function provides the ability to provide your own `MessageBuilder` rather than using the default `LoggerMessageBuilder`. If you would like to format your logs using a custom `MessageBuilder` rather than using the default, the support is here.
 - **Exception mapper function**: The exception mapper function provides the ability to translate any `Throwable` that might occur at runtime from the logger to inspect exceptions and return a string for that field based on the exception.
 - **Hash Processor**: Processor used to hash data which has been marked to be hashed via [LoggingScope](#loggingScope). By default the MD5HashProcessor is used which uses Guava's MD5 hashing algorithm to hash data. MD5 is not cryptographically secure, but it is extremely fast. For a more robust encryption you can use your own HashProcessor.
 - **Logger**: This defines the Logger Implementation. It allows the use of a custom logger implementation. By default the LoggerImpl is used.
 - **Collection Filter**: Allows the ability to filter collections. By default, collections are filtered to only log 50 entries.

A logger can be instantiated with a set of LoggingConfigs as follows (If logging configs aren't passed, the set of default logging configs is used):
```
Logger logger = LoggerFactory.getLogger(SomeClass.class, loggingConfigs);
```
Where `loggingConfigs` is an instance of `LoggingConfigs`.

Below is an example of a Logger being created with the use of LoggingConfigs. In this example the Logging Configs contain a custom mapper which defines a mapping between the Car class and its toString method. As seen in the example at the top of the Readme, the Car has the exact same values, however this time we have a custom mapper tied to the car class.

The following is the implementation of the Car classes toString function:
```
public String toString() {
        return "My car is a " + year + " " + make + " " + model + ". It cost me $" + cost + ". I bought it in " + country +
               ". It has " + (getEngine() != null ? "a " + getEngine().getName() : "no") + " Engine.";
    }
```
To create the Logger with the LoggingConfigs containing the Custom Mapping of the Car class to it's toString function can be seen below:
```
 HashMap<Class<?>, Function<Object, String>> customMappers = new HashMap<>();
 customMappers.put(Car.class, Object::toString);

 LoggingConfigs configs = LoggingConfigs.builder().customMapper(customMappers).build();

 Logger logger = LoggerFactory.getLogger(LoggerTest.class, configs);

 Car car = new Car("911", 2015, "Porsche", 70000.00, Country.GERMANY, new Engine("V12"));
 
 logger.with(car).info("Logging Car");
```
This produces the following Log Statement:
```
15:22:07.439 [main] INFO  com.godaddy.logger.LoggerTest - TEST; My car is a 2015 Porsche 911. It cost me $70000.0. I bought it in GERMANY. It has a V12 Engine.
```
Notice how it's different from the log statement in the first example. Whenever an instance of the Car object is logged, it's toString function result is now what is logged.

## <a name="loggingScope">Logging Scope</a>
The GoDaddy Logger provides annotation based logging scope support, allowing you the ability to skip fields/methods from being logged with the use of annotations. There is also support to hash values that are to be logged. Some values you may want to hash include passwords, credit card information, etc.

#### Logging Scope Options:
 - **LOG**: Value is to be logged. This is the default value, If no annotation is provided, LOG is used as the logging scope.
 - **SKIP**: Value is to be skipped from the logs.
 - **HASH**: Value is to hashed in the logs.
 
#### An Example of an Annotated class being logged can be seen below:

```
@Data
public class AnnotatedObject {
    private String notAnnotated;

    @LoggingScope(scope = Scope.SKIP)
    private String annotatedLogSkip;

    public String getNotAnnotatedMethod() {
        return "Not Annotated";
    }

    @LoggingScope(scope = Scope.SKIP)
    public String getAnnotatedLogSkipMethod() {
        return "Annotated";
    }

    @LoggingScope(scope = Scope.HASH)
    public String getCreditCardNumber() {
        return "1234-5678-9123-4567";
    }
}
```

As you can see the AnnotatedObject has made use of the LoggingScope annotation. The annotatedLogSkip field will be ignore from the logs as it has a defined LoggingScope value of SKIP. The getAnnotatedLogSkipMethod method result will also be ignored from the logs. The getCreditCardNumber method result will be hashed and logged as it's logging scope value was set to HASH.

```
AnnotatedObject annotatedPojo = new AnnotatedObject();
annotatedPojo.setAnnotatedLogSkip("SKIP ME");
annotatedPojo.setNotAnnotated("NOT ANNOTATED");

logger.with(annotatedPojo).info("Annotation Logging");
```

When the above code block is run, the following will be output in the logs as expected:

```
15:50:44.667 [main] INFO  com.godaddy.logging.LoggerTest - Annotation Logging; creditCardNumber="5d4e923fe014cb34f4c7ed17b82d6c58"; notAnnotated="NOT ANNOTATED"; notAnnotatedMethod="Not Annotated"
```

##Structured JSON Logging with Logstash
There is support for JSON structure logging with Logstash. To use JSON structured logging your logging configs need to be configured:
`LoggingConfigs configs = LoggingConfigs.getCurrent().useJson();`
The with functionality now appends a LogstashMarker to the logstatement instead of just appending a String at the end of your log message. You need to setup Logstash or some other appender to handle these markers. You can check out LogstashTest.java and logstash-test.xml to see an example of how this is done.



##Logger Bindings
The GoDaddy Logger does not provide any specific log binding. A log binding must be configured to make use of this logger. Log4j and logback are both widely used log bindings which can be used.

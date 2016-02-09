/**
 * Copyright (c) 2015 GoDaddy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.godaddy.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.godaddy.logging.messagebuilders.providers.StringMessageBuilderProvider;
import com.godaddy.logging.models.*;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MarkerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LoggerTest {
    private Logger logger;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    private ch.qos.logback.classic.Logger testLogger =
            (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

    @Before
    public void setUp() {
        testLogger.addAppender(mockAppender);

        LoggingConfigs loggingConfigs = LoggingConfigs.builder().messageBuilderFunction(new StringMessageBuilderProvider()).build();

        logger = LoggerFactory.getLogger(LoggerTest.class, loggingConfigs);

    }

    @Test
    public void testUUID() {
        UUID uuid = UUID.randomUUID();

        logger.with(uuid).info("test");

        assertEquals(getLoggingEvent().getFormattedMessage(), "test; " + uuid.toString());
    }

    @Test
    public void test_unnamed_values_and_message_counters() {
        Object toLog = new Object() {
            String horse = "NEIGH";
            String _message = "message2";
            String _unnamed_values = "_unnamed_values1";
            Object a = new Object() {
                String _message = "a.message";
            };
        };

        logger.with(toLog).info("YO");

        assertEquals(getLoggingEvent().getFormattedMessage(), "YO; _message2=\"message2\"; _unnamed_values1=\"_unnamed_values1\"; a._message=\"a.message\"; " +
                                                              "horse=\"NEIGH\"");
    }

    @Test
    public void test_cycles() {
        CycleObject cycleObject = new CycleObject();

        cycleObject.text = "test_cycles";

        cycleObject.cycle = cycleObject;

        logger.with(cycleObject).info("Cycles");

        assertEquals(getLoggingEvent().getFormattedMessage(), "Cycles; CycleObject.text=\"test_cycles\"");
    }

    @Test
    public void test_overrides() {
        LoggingConfigs originalLogConfigs = LoggingConfigs.builder().recursiveLevel(6).build();
        originalLogConfigs.withOverride(URI.class, URI::toString);

        LoggingConfigs copyLogConfigs = new LoggingConfigs(originalLogConfigs).withRecursiveLevel(2);
        copyLogConfigs.withOverride(Car.class, Car::toString);

        assertTrue(copyLogConfigs.getCustomMapper().size() > originalLogConfigs.getCustomMapper().size());
        assertTrue(originalLogConfigs.getRecursiveLevel().equals(6));
        assertTrue(copyLogConfigs.getRecursiveLevel().equals(2));
    }

    @Test
    public void test_key_value() {
        UUID uuid = UUID.randomUUID();

        Logger with = logger.with("uuid", uuid)
                            .with("foo", "bar");

        with.info("test");

        assertEquals(getLoggingEvent().getFormattedMessage(), "test; foo=\"bar\"; uuid=\"" + uuid.toString() + "\"");
    }

    @Test
    public void test_custom_mapper() {
        Logger customLogger = LoggerFactory.getLogger(LoggerTest.class,
                                                      LoggingConfigs.builder().customMapper(new HashMap<Class<?>, Function<Object, String>>() {{
                                                          put(Car.class, Object::toString);
                                                      }}).build());

        Car car = new Car("911", 2015, "Porsche", 70000.00, Country.GERMANY, new Engine("V12"));

        String expected = "TEST; My car is a 2015 Porsche 911. It cost me $70000.0. I bought it in GERMANY. It has a V12 Engine.";

        customLogger.with(car).info("TEST");

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_custom_global_mapper() throws URISyntaxException {
        Logger customLogger = LoggerFactory.getLogger(LoggerTest.class);

        LoggingConfigs.getCurrent().withOverride(URI.class, URI::toString);

        customLogger.with(new URI("http://foo/bar"))
                    .info("uri test");

        String expected = "uri test; http://foo/bar";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_double_brace() {
        logger.with(new LogMessage() {
            {
                put("key", "value");
                put("otherKey", "otherValue");
            }
        }).info("test");

        assertEquals(getLoggingEvent().getFormattedMessage(), "test; key=\"value\"; otherKey=\"otherValue\"");
    }

    @Test
    public void test_double_brace_with_nested_object() {
        logger.with(new LogMessage() {{
            put("key", "value");
            put("otherKey", new Engine("V8"));
        }}).info("test");

        assertEquals(getLoggingEvent().getFormattedMessage(), "test; key=\"value\"; otherKey.Engine.name=\"V8\"");
    }

    @Test
    public void test_recursive_object_logging() {

        Person person = new Person("bob", 25, false, Lists.newArrayList(new Car("A4", 2010, "Audi", 45000.20, Country.GERMANY, new Engine("V6")),
                                                                        new Car("Element", 2012, "Honda", 25000.50, Country.JAPAN, new Engine("V6"))), new Car("Mustang",
                                                                                                                                                               2011,
                                                                                                                                                               "Ford",
                                                                                                                                                               55000.20,
                                                                                                                                                               Country.USA,
                                                                                                                                                               new Engine(
                                                                                                                                                                       "V8")));

        String expected = "TEST; Person.myCar.country=USA; Person.myCar.cost=55000.2; Person.myCar.test=\"HI\"; Person.myCar.engine.name=\"V8\"; Person.myCar" +
                          ".year=2011; Person.myCar.model=\"Mustang\"; Person.myCar.make=\"Ford\"; Person.cars=[{country=GERMANY, cost=45000.2, test=HI, " +
                          "engine={name=V6}, year=2010, model=A4, make=Audi}, {country=JAPAN, cost=25000.5, test=HI, engine={name=V6}, year=2012, model=Element, " +
                          "make=Honda}]; Person.name=\"bob\"; Person.retired=false; Person.age=25";

        logger.with(person).info("TEST");

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_closed_over_object_exclues_lifted_values() {
        IntStream.of(0, 0).limit(1).forEach(i -> {
            final String foo = "foo";

            logger.with(new Object() {
                long bar = foo.length();
            }).info("Test");
        });

        assertEquals(getLoggingEvent().getFormattedMessage(), "Test; bar=3");
    }

    @Test
    public void test_empty_object() {
        logger.with(new EmptyObject()).info("TEST");

        assertEquals(getLoggingEvent().getFormattedMessage(), "TEST");
    }

    @Test
    public void test_log_integer() {
        logger.with(123).info("TEST");

        String expected = "TEST; 123";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_log_string() {
        logger.with("String Test").info("TEST");

        String expected = "TEST; String Test";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_two_withs() {
        logger.with("one", "data1")
              .with("two", "data2")
              .info("test");

        assertEquals(getLoggingEvent().getFormattedMessage(), "test; one=\"data1\"; two=\"data2\"");
    }

    @Test
    public void test_paralallel_logger() {
        int logNum = 1000;

        IntStream.range(0, logNum)
                 .parallel()
                 .forEach(i -> {
                     logger.with("data", i)
                           .info("test" + i);
                 });


        final List<LoggingEvent> loggingEvents = getLoggingEvents();

        loggingEvents.sort((a, b) -> a.getMessage().compareTo(b.getMessage()));

        final Set<String> collect = loggingEvents.stream().map(i -> i.getFormattedMessage()).collect(toSet());

        for (int i = 0; i < logNum; i++) {
            assertTrue(collect.contains(String.format("test%s; data=%s", String.valueOf(i), String.valueOf(i))));
        }
    }

    @Test
    public void closed_logger() {
        Logger with = logger.with("capture", "data");

        with.with("test", "test").info("test");

        with.with("test2", "test2").info("test2");

        assertEquals(getLoggingEvents().get(1).getFormattedMessage(), "test2; capture=\"data\"; test2=\"test2\"");
    }

    @Test
    public void test_error_with_exception_and_format() {
        logger.with("HI").error(new Exception("Exception!!"), "foo {} {}", "1", "2");

        String expected = "foo 1 2; HI";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_markers() {
        logger.success("Foo!");

        assertEquals(getLoggingEvent().getMarker(), MarkerFactory.getMarker("SUCCESS"));
    }

    @Test
    public void test_with_null_msg() {
        logger.info(null);

        assertEquals(getLoggingEvent().getFormattedMessage(), "null");
    }

    @Test
    public void test_with_null() {
        logger.with(null).info("TEST");

        assertEquals(getLoggingEvent().getFormattedMessage(), "TEST");
    }

    @Test
    public void test_exception_mapper() {
        logger.with(new GetterThrowsError()).info("test");

        assertEquals(getLoggingEvent().getFormattedMessage(), "test; GetterThrowsError.text=\"<An error occurred logging!>\"");
    }

    @Test
    public void test_cannot_override_log_message_type() {
        Logger customLogger = LoggerFactory.getLogger(LoggerTest.class,
                                                      LoggingConfigs.builder().customMapper(new HashMap<Class<?>, Function<Object, String>>() {{
                                                          put(Map.class, Object::toString);
                                                      }}).build());

        customLogger.with("foo", "bar").info("test");

        assertEquals(getLoggingEvent().getFormattedMessage(), "test; foo=\"bar\"");
    }

    @Test
    public void test_custom_exception_mapper() {
        Logger customLogger = LoggerFactory.getLogger(LoggerTest.class,
                                                      LoggingConfigs.builder().build().withExceptionTranslator(i -> "<<err>>"));

        customLogger.with(new GetterThrowsError()).info("test");

        assertEquals(getLoggingEvent().getFormattedMessage(), "test; GetterThrowsError.text=\"<<err>>\"");
    }

    @Test
    public void test_with_null_string_int_field_object() {
        Person person = new Person("bob", 25, false, Lists.newArrayList(new Car("A4", 2010, "Audi", 45000.20, Country.GERMANY, new Engine("V6")),
                                                                        new Car("Element", 2012, "Honda", 25000.50, Country.JAPAN, new Engine("V6"))), new Car("Mustang",
                                                                                                                                                               2011,
                                                                                                                                                               "Ford",
                                                                                                                                                               55000.20,
                                                                                                                                                               Country.USA,
                                                                                                                                                               new Engine(
                                                                                                                                                                       "V8")));

        logger.with("My String").with(person).with(1).with(null).with(new EmptyObject()).with("Another String")
              .with(new Object() {
                  int year = 2015;
                  String month = "January";
                  Country country = Country.USA;
              }).info("TEST");

        String expected =
                "TEST; Another String; 1; My String; Person.myCar.country=USA; Person.myCar.cost=55000.2; Person.myCar.test=\"HI\"; Person.myCar.engine.name=\"V8\"; " +
                "Person.myCar.year=2011; Person.myCar.model=\"Mustang\"; Person.myCar.make=\"Ford\"; Person.cars=[{country=GERMANY, cost=45000.2, test=HI, " +
                "engine={name=V6}, year=2010, model=A4, make=Audi}, {country=JAPAN, cost=25000.5, test=HI, engine={name=V6}, year=2012, model=Element, make=Honda}]; " +
                "Person.name=\"bob\"; Person.retired=false; Person.age=25; country=USA; month=\"January\"; year=2015";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_fields() {

        logger.with(new Object() {
            String name = "Brendan";
            Integer age = 27;
            Car car = new Car("Element", 2012, "Honda", 25000.50, Country.JAPAN, new Engine("V6"));
        }).info("TEST");

        String expected = "TEST; age=27; car.country=JAPAN; car.cost=25000.5; car.test=\"HI\"; car.engine.name=\"V6\"; car.year=2012; car.model=\"Element\"; car" +
                          ".make=\"Honda\"; name=\"Brendan\"";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_object_without_get_or_is_getters() {
        DifferentGetters diffGetters = new DifferentGetters("Some String", 30, Country.CANADA);

        logger.with(diffGetters).info("TEST");

        String expected = "TEST; DifferentGetters.get=\"getTest\"; DifferentGetters.is=\"isTest\"";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_custom_mapping_inheritance() {
        Logger customLogger = LoggerFactory.getLogger(LoggerTest.class,
                                                      LoggingConfigs.builder().build().withOverride(CustomMapping.class, CustomMapping::testString));


        customLogger.with(new CustomMappingImpl()).info("TEST");

        String expected = "TEST; Testing Custom Mapping Inheritance.";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_with_null_inner_object() {
        Car car = new Car("911", 2015, "Porsche", 70000.00, Country.GERMANY, null);

        logger.with(car).info("TEST");

        String expected = "TEST; Car.country=GERMANY; Car.cost=70000.0; Car.test=\"HI\"; Car.engine=<null>; Car.year=2015; Car.model=\"911\"; Car.make=\"Porsche\"";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_annotation_logging() {
        AnnotatedObject annotatedObject = new AnnotatedObject();
        annotatedObject.setAnnotatedLogSkip("SKIP ME");
        annotatedObject.setNotAnnotated("NOT ANNOTATED");

        logger.with(annotatedObject).info("Annotation Logging");

        String expected = "Annotation Logging; AnnotatedObject.creditCardNumber=\"5d4e923fe014cb34f4c7ed17b82d6c58\"; AnnotatedObject.notAnnotated=\"NOT ANNOTATED\"; " +
                          "AnnotatedObject.notAnnotatedMethod=\"Not Annotated\"";

        assertEquals(getLoggingEvent().getFormattedMessage(), expected);
    }

    @Test
    public void test_with_null_value() {
        logger.with("test", null).info("Testing Null");

        assertEquals(getLoggingEvent().getFormattedMessage(), "Testing Null; test=<null>");
    }

    @Test
    public void test_log_array() {
        int[] array = new int[]{ 1, 2, 3 };

        logger.with("array", array).info("Logging Array");

        assertEquals(getLoggingEvent().getFormattedMessage(), "Logging Array; array=[1, 2, 3]");
    }

    @Test
    public void test_collection_filter() {
        Logger customLogger = LoggerFactory.getLogger(LoggerTest.class,
                                                      LoggingConfigs.builder().messageBuilderFunction(new StringMessageBuilderProvider()).build().withCollectionFilter(
                                                              collection -> (Collection) collection.stream().limit(1).collect(toList())
                                                      ));

        List<Integer> nums = Lists.newArrayList(1, 2);

        logger.with("nonFilteredList", nums).info("nonFilteredList");

        assertEquals(getLoggingEvent().getFormattedMessage(), "nonFilteredList; nonFilteredList=[1, 2]");

        customLogger.with("filteredList", nums).info("filteredList");

        assertEquals(getLoggingEvent().getFormattedMessage(), "filteredList; filteredList=[1]");
    }

    private LoggingEvent getLoggingEvent() {
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        return captorLoggingEvent.getValue();
    }

    private List<LoggingEvent> getLoggingEvents() {
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        return captorLoggingEvent.getAllValues();
    }

    @After
    public void cleanUp() {
        testLogger.detachAndStopAllAppenders();
    }
}

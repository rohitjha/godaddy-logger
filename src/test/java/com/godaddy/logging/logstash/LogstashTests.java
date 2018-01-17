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

package com.godaddy.logging.logstash;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.godaddy.logging.CommonKeys;
import com.godaddy.logging.LogMessage;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.godaddy.logging.LoggingConfigs;
import com.godaddy.logging.Slf4WrapperLogger;
import com.godaddy.logging.logger.MarkerAppendingLogger;
import com.godaddy.logging.messagebuilders.providers.LogstashMessageBuilderProvider;
import com.godaddy.logging.models.Car;
import com.godaddy.logging.models.Country;
import com.godaddy.logging.models.CycleObject;
import com.godaddy.logging.models.EmptyObject;
import com.godaddy.logging.models.Engine;
import com.godaddy.logging.models.GetterThrowsError;
import com.godaddy.logging.models.Person;
import com.google.common.collect.Lists;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import net.logstash.logback.marker.MapEntriesAppendingMarker;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class LogstashTests {

    private ch.qos.logback.classic.Logger testLogger =
            (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(LogstashTests.class);

    private final ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) testLogger.getAppender("listAppender");

    private final JsonFactory jsonFactory = new MappingJsonFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

    private Logger logger;

    private StringWriter writer;

    private JsonGenerator generator;

    @Before
    public void setup() throws IOException {
        listAppender.list.clear();

        LoggingConfigs configs = LoggingConfigs.getCurrent().useJson();

        logger = LoggerFactory.getLogger(LogstashTests.class, configs);

        writer = new StringWriter();
        generator = jsonFactory.createGenerator(writer);
        generator.useDefaultPrettyPrinter();
        generator.writeStartObject();
    }

    @Test
    public void test_logback_json_log() throws IOException {
        Object toLog = new Object() {
            String horse = "NEIGH";
            Object inner = new Object() {
                String car = "VROOM";
            };
        };

        logger.with(toLog).info("YO");

        Map<String, Object> output = getJson();

        assertEquals(output.get("horse"), "NEIGH");
        assertEquals(((LinkedHashMap) output.get("inner")).get("car"), "VROOM");
        assertEquals(output.get("level"), "INFO");
        assertEquals(output.get("customMessage"), "YO");
    }

    @Test
    public void testUUID() throws IOException {
        UUID uuid = UUID.randomUUID();

        logger.with(uuid).info("test");

        Map<String, Object> json = getJson();

        assertEquals(json.get(CommonKeys.UNNAMED_VALUES_KEY), Lists.newArrayList(uuid.toString()));
        assertEquals(json.get("customMessage"), "test");
    }

    @Test
    public void test_cycles() throws IOException {
        CycleObject cycleObject = new CycleObject();

        cycleObject.text = "test_cycles";

        cycleObject.cycle = cycleObject;

        logger.with(cycleObject).info("Cycles");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"CycleObject\" : {\n" +
                                   "    \"text\" : \"test_cycles\"\n" +
                                   "  }\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "Cycles");
    }

    @Test
    public void test_key_value() throws IOException {
        UUID uuid = UUID.randomUUID();

        Logger with = logger.with("uuid", uuid)
                            .with("foo", "bar");

        with.info("test");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"uuid\" : \"" + uuid + "\",\n" +
                                   "  \"foo\" : \"bar\"\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "test");
    }

    @Test
    public void test_custom_mapper() throws IOException {
        Logger customLogger = logger = LoggerFactory.getLogger(LogstashTests.class,
                                                               LoggingConfigs.builder()
                                                                             .customMapper(new HashMap<Class<?>, Function<Object, String>>() {{
                                                                                 put(Car.class, Object::toString);
                                                                             }})
                                                                             .logger((clazz, configs) -> new MarkerAppendingLogger(new Slf4WrapperLogger(org.slf4j.LoggerFactory.getLogger(clazz)), configs))
                                                                             .messageBuilderFunction(new LogstashMessageBuilderProvider()).build());

        Car car = new Car("911", 2015, "Porsche", 70000.00, Country.GERMANY, new Engine("V12"));

        customLogger.with(car).info("TEST");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"_unnamed_values\" : [ \"My car is a 2015 Porsche 911. It cost me $70000.0. I bought it in GERMANY. It has a V12 Engine.\" ]\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_double_brace() throws IOException {
        logger.with(new LogMessage() {
            {
                put("key", "value");
                put("otherKey", "otherValue");
            }
        }).info("test");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"key\" : \"value\",\n" +
                                   "  \"otherKey\" : \"otherValue\"\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "test");
    }

    @Test
    public void test_double_brace_with_nested_object() throws IOException {
        logger.with(new LogMessage() {{
            put("key", "value");
            put("otherKey", new Engine("V8"));
        }}).info("test");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"key\" : \"value\",\n" +
                                   "  \"otherKey\" : {\n" +
                                   "    \"Engine\" : {\n" +
                                   "      \"name\" : \"V8\"\n" +
                                   "    }\n" +
                                   "  }\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "test");
    }

    @Test
    public void test_recursive_object_logging() throws IOException {

        Person person = new Person("bob", 25, false, Lists.newArrayList(new Car("A4", 2010, "Audi", 45000.20, Country.GERMANY, new Engine("V6")),
                                                                        new Car("Element", 2012, "Honda", 25000.50, Country.JAPAN, new Engine("V6"))), new Car("Mustang",
                                                                                                                                                               2011,
                                                                                                                                                               "Ford",
                                                                                                                                                               55000.20,
                                                                                                                                                               Country.USA,
                                                                                                                                                               new Engine(
                                                                                                                                                                       "V8")));

        String expected = "{\n" +
                          "  \"Person\" : {\n" +
                          "    \"myCar\" : {\n" +
                          "      \"country\" : \"USA\",\n" +
                          "      \"cost\" : 55000.2,\n" +
                          "      \"test\" : \"HI\",\n" +
                          "      \"engine\" : {\n" +
                          "        \"name\" : \"V8\"\n" +
                          "      },\n" +
                          "      \"year\" : 2011,\n" +
                          "      \"model\" : \"Mustang\",\n" +
                          "      \"make\" : \"Ford\"\n" +
                          "    },\n" +
                          "    \"cars\" : [ {\n" +
                          "      \"country\" : \"GERMANY\",\n" +
                          "      \"cost\" : 45000.2,\n" +
                          "      \"test\" : \"HI\",\n" +
                          "      \"engine\" : {\n" +
                          "        \"name\" : \"V6\"\n" +
                          "      },\n" +
                          "      \"year\" : 2010,\n" +
                          "      \"model\" : \"A4\",\n" +
                          "      \"make\" : \"Audi\"\n" +
                          "    }, {\n" +
                          "      \"country\" : \"JAPAN\",\n" +
                          "      \"cost\" : 25000.5,\n" +
                          "      \"test\" : \"HI\",\n" +
                          "      \"engine\" : {\n" +
                          "        \"name\" : \"V6\"\n" +
                          "      },\n" +
                          "      \"year\" : 2012,\n" +
                          "      \"model\" : \"Element\",\n" +
                          "      \"make\" : \"Honda\"\n" +
                          "    } ],\n" +
                          "    \"name\" : \"bob\",\n" +
                          "    \"retired\" : false,\n" +
                          "    \"age\" : 25\n" +
                          "  }\n" +
                          "}";

        logger.with(person).info("TEST");

        assertEquals(getMarkers(), expected);

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_closed_over_object_exclues_lifted_values() throws IOException {
        IntStream.of(0, 0).limit(1).forEach(i -> {
            final String foo = "foo";

            logger.with(new Object() {
                long bar = foo.length();
            }).info("Test");
        });

        assertEquals(getMarkers(), "{\n" +
                                   "  \"bar\" : 3\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "Test");
    }

    @Test
    public void test_empty_object() throws IOException {
        logger.with(new EmptyObject()).info("TEST");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"EmptyObject\" : { }\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_log_integer() throws IOException {
        logger.with(123).info("TEST");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"_unnamed_values\" : [ 123 ]\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_log_string() throws IOException {
        logger.with("String Test").info("TEST");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"_unnamed_values\" : [ \"String Test\" ]\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_two_withs() throws IOException {
        logger.with("one", "data1")
              .with("two", "data2")
              .info("test");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"two\" : \"data2\",\n" +
                                   "  \"one\" : \"data1\"\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "test");
    }

    @Test
    public void test_error_with_exception_and_format() throws IOException {
        logger.with("HI").error(new Exception("Exception!!"), "foo {} {}", "1", "2");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"_unnamed_values\" : [ \"HI\" ]\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "foo 1 2");
    }

    @Test
    public void test_with_null() throws IOException {
        logger.with(null).info("TEST");

        assertEquals(getMarkers(), "{ }");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_exception_mapper() throws IOException {
        logger.with(new GetterThrowsError()).info("test");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"GetterThrowsError\" : {\n" +
                                   "    \"text\" : \"<An error occurred logging!>\"\n" +
                                   "  }\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "test");
    }

    @Test
    public void test_fields() throws IOException {

        logger.with(new Object() {
            String name = "Brendan";
            Integer age = 27;
            Car car = new Car("Element", 2012, "Honda", 25000.50, Country.JAPAN, new Engine("V6"));
        }).info("TEST");

        String expected = "{\n" +
                          "  \"name\" : \"Brendan\",\n" +
                          "  \"car\" : {\n" +
                          "    \"country\" : \"JAPAN\",\n" +
                          "    \"cost\" : 25000.5,\n" +
                          "    \"test\" : \"HI\",\n" +
                          "    \"engine\" : {\n" +
                          "      \"name\" : \"V6\"\n" +
                          "    },\n" +
                          "    \"year\" : 2012,\n" +
                          "    \"model\" : \"Element\",\n" +
                          "    \"make\" : \"Honda\"\n" +
                          "  },\n" +
                          "  \"age\" : 27\n" +
                          "}";

        assertEquals(getMarkers(), expected);

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_with_null_inner_object() throws IOException {
        Car car = new Car("911", 2015, "Porsche", 70000.00, Country.GERMANY, null);

        logger.with(car).info("TEST");

        String expected = "{\n" +
                          "  \"Car\" : {\n" +
                          "    \"country\" : \"GERMANY\",\n" +
                          "    \"cost\" : 70000.0,\n" +
                          "    \"test\" : \"HI\",\n" +
                          "    \"engine\" : null,\n" +
                          "    \"year\" : 2015,\n" +
                          "    \"model\" : \"911\",\n" +
                          "    \"make\" : \"Porsche\"\n" +
                          "  }\n" +
                          "}";

        assertEquals(getMarkers(), expected);

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_collection() throws IOException {
        logger.with("data", Arrays.asList("foo", "bar"))
              .info("TEST");

        String expected = "{\n" +
                          "  \"data\" : [ \"foo\", \"bar\" ]\n" +
                          "}";

        assertEquals(getMarkers(), expected);

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_array() throws IOException {
        int[] array = new int[]{1, 2, 3};

        logger.with("data", array).info("TEST");

        String expected = "{\n" +
                          "  \"data\" : [ 1, 2, 3 ]\n" +
                          "}";

        assertEquals(getMarkers(), expected);

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "TEST");
    }

    @Test
    public void test_map() throws IOException {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("Test", "Logstash");
        testMap.put("Engine", new Engine("V8"));
        testMap.put("Car", new Car("A4", 2010, "Audi", 45000.20, Country.GERMANY, new Engine("V6")));

        logger.with(testMap).info("Testing");

        assertEquals(getMarkers(), "{\n" +
                                   "  \"_unnamed_values\" : [ {\n" +
                                   "    \"Test\" : \"Logstash\",\n" +
                                   "    \"Car\" : {\n" +
                                   "      \"Car\" : {\n" +
                                   "        \"country\" : \"GERMANY\",\n" +
                                   "        \"cost\" : 45000.2,\n" +
                                   "        \"test\" : \"HI\",\n" +
                                   "        \"engine\" : {\n" +
                                   "          \"name\" : \"V6\"\n" +
                                   "        },\n" +
                                   "        \"year\" : 2010,\n" +
                                   "        \"model\" : \"A4\",\n" +
                                   "        \"make\" : \"Audi\"\n" +
                                   "      }\n" +
                                   "    },\n" +
                                   "    \"Engine\" : {\n" +
                                   "      \"Engine\" : {\n" +
                                   "        \"name\" : \"V8\"\n" +
                                   "      }\n" +
                                   "    }\n" +
                                   "  } ]\n" +
                                   "}");

        Map<String, Object> json = getJson();
        assertEquals(json.get("customMessage"), "Testing");
    }

    private Map<String, Object> getJson() throws IOException {
        OutputStreamAppender<ILoggingEvent> appender = (OutputStreamAppender<ILoggingEvent>) testLogger.getAppender("loggingEventCompositeJsonEncoderAppender");
        LoggingEventCompositeJsonEncoder encoder = (LoggingEventCompositeJsonEncoder) appender.getEncoder();

        String encodedValue = new String(encoder.encode(listAppender.list.get(0)), "UTF-8");

        return jsonFactory.createParser(encodedValue).readValueAs(new TypeReference<Map<String, Object>>() {});
    }

    private String getMarkers() throws IOException {
        MapEntriesAppendingMarker marker = (MapEntriesAppendingMarker) listAppender.list.get(0).getMarker();
        marker.writeTo(generator);
        generator.writeEndObject();
        generator.flush();

        return writer.toString();
    }

}

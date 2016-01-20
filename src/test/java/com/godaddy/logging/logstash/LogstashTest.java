package com.godaddy.logging.logstash;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.godaddy.logging.LoggingConfigs;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LogstashTest {

    private ch.qos.logback.classic.Logger testLogger =
            (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(LogstashTest.class);

    private final ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) testLogger.getAppender("listAppender");

    private final JsonFactory jsonFactory = new MappingJsonFactory();

    @Before
    public void setup() {
        listAppender.list.clear();
    }

    @Test
    public void test_logback_json_log() throws IOException {
        Object toLog = new Object() {
            String horse = "NEIGH";
            Object inner = new Object() {
                String car = "VROOM";
            };
        };

        LoggingConfigs configs = LoggingConfigs.getCurrent().useJson();

        Logger logger = LoggerFactory.getLogger(LogstashTest.class, configs);

        logger.with(toLog).info("YO");

        Map<String, Object> output = getOutput("loggingEventCompositeJsonEncoderAppender");

        assertEquals(output.get("horse"), "NEIGH");
        assertEquals(((LinkedHashMap) getOutput("loggingEventCompositeJsonEncoderAppender").get("inner")).get("car"), "VROOM");
        assertEquals(output.get("message"), "YO");
        assertEquals(output.get("level"), "INFO");
    }

    private Map<String, Object> getOutput(String appenderName) throws IOException {
        OutputStreamAppender<ILoggingEvent> appender = (OutputStreamAppender<ILoggingEvent>) testLogger.getAppender(appenderName);
        LoggingEventCompositeJsonEncoder encoder = (LoggingEventCompositeJsonEncoder) appender.getEncoder();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encoder.init(outputStream);
        encoder.doEncode(listAppender.list.get(0));

        return parseJson(outputStream.toString("UTF-8"));
    }

    private Map<String, Object> parseJson(final String text) throws IOException {
        return jsonFactory.createParser(text).readValueAs(new TypeReference<Map<String, Object>>() {
        });
    }

}

package com.godaddy.logging.logstash;

import com.godaddy.logging.InitialLogContext;
import com.godaddy.logging.LogContext;
import com.godaddy.logging.RunningLogContext;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ContextUtils {
    public static RunningLogContext<List<Map<String, Object>>> initialToRunning(final LogContext<List<Map<String, Object>>> previous) {
        if (previous instanceof InitialLogContext) {
            final String logMessage = ((InitialLogContext) previous).getLogMessage();

            Map<String, Object> initialMap = new HashMap<>();
            initialMap.put("message", logMessage);

            return new RunningLogContext<>(Lists.newArrayList(initialMap));

        }

        else {
            return ((RunningLogContext<List<Map<String, Object>>>) previous);
        }
    }
}

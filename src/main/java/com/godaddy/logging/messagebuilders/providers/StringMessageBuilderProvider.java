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

package com.godaddy.logging.messagebuilders.providers;

import com.godaddy.logging.CommonKeys;
import com.godaddy.logging.LogContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StringMessageBuilderProvider extends JsonMessageBuilderProvider {

    @Override
    public Object formatPayload(final LogContext<List<Map<String, Object>>> runningLogContext) {
        return new StringMessageFormatter(runningLogContext).getFormattedPayload();
    }

    class StringMessageFormatter {
        private final LogContext<List<Map<String, Object>>> runningLogContext;

        private final StringBuilder messageBuilder = new StringBuilder();

        private static final String SEPARATOR = "; ";

        public StringMessageFormatter(final LogContext<List<Map<String, Object>>> runningLogContext){
            this.runningLogContext = runningLogContext;
        }

        public Object getFormattedPayload() {
            // Clear out the message builder.
            messageBuilder.setLength(0);

            // Leveraging TreeMap so that log data is consistently sorted.
            Map<String, Object> jsonMap = new TreeMap<>();

            jsonMap.putAll(getContextMap(runningLogContext));

            addLogMessageToFormattedString(jsonMap);

            buildFormattedContext(0, jsonMap, "");

            trimLastSeparator();

            return messageBuilder.toString();
        }

        private void trimLastSeparator() {
            messageBuilder.delete(messageBuilder.length() - SEPARATOR.length(), messageBuilder.length());
        }

        private void addLogMessageToFormattedString(Map<String, Object> jsonMap) {
            if (jsonMap.containsKey(CommonKeys.LOG_MESSAGE_KEY)) {
                messageBuilder.append(jsonMap.get(CommonKeys.LOG_MESSAGE_KEY) + SEPARATOR);
            }

            if (jsonMap.containsKey(CommonKeys.UNNAMED_VALUES_KEY)) {
                List<Object> context = (List<Object>) jsonMap.get(CommonKeys.UNNAMED_VALUES_KEY);
                for (Object ctx : context) {
                    messageBuilder.append(ctx).append(SEPARATOR);
                }
            }
        }

        private void buildFormattedContext(int currentLevel, Map<String, Object> jsonMap, String prefix) {
            for (String key : jsonMap.keySet()) {
                if (jsonMap.get(key) instanceof HashMap) {
                    String recursivePrefix = prefix.equals("") ? key + "." : prefix + key + ".";
                    buildFormattedContext(currentLevel + 1, (Map<String, Object>) jsonMap.get(key), recursivePrefix);
                }
                else if (!prefix.isEmpty() || (!key.equals(CommonKeys.LOG_MESSAGE_KEY) && !key.equals(CommonKeys.UNNAMED_VALUES_KEY))) {
                    messageBuilder.append(prefix).append(key).append("=");

                    Object value = jsonMap.get(key);

                    if (value == null) {
                        messageBuilder.append("<null>");
                    }
                    else if (value instanceof String) {
                        messageBuilder.append("\"" + value + "\"");
                    }
                    else {
                        messageBuilder.append(value);
                    }

                    messageBuilder.append(SEPARATOR);
                }
            }
        }
    }
}

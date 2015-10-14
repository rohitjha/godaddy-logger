/**
 *
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
 *
 */

package com.godaddy.logging;

import com.google.common.primitives.Primitives;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class StringMessageBuilder extends LoggerMessageBuilder<String> {
    private static final String SEPARATOR = "; ";

    public StringMessageBuilder(final LoggingConfigs configs) {
        super(configs);

        messageBuilder = new StringBuilder();
    }

    protected StringBuilder messageBuilder;

    private RunningLogContext<String> initialToRunning(final LogContext<String> previous){
        if (previous instanceof InitialLogContext) {
            return new RunningLogContext<> (((InitialLogContext) previous).getLogMessage());
        }

        else {
            return ((RunningLogContext<String>) previous);
        }
    }

    @Override
    public RunningLogContext<String> buildMessage(final LogContext<String> previous, final Object currentObject) {

        if (currentObject == null) {
            return initialToRunning(previous);
        }

        if (previous != null) {
            String lastData = initialToRunning(previous).getData();

            messageBuilder.append(lastData)
                          .append(SEPARATOR);
        }

        try {
            buildMessage(currentObject, new ArrayList<>(), "", 0);

            trimLastSeparator();
        }
        catch (Throwable t) {
            messageBuilder.append(configs.getExceptionTranslator().translate(t));
        }

        return new RunningLogContext<>(messageBuilder.toString());
    }

    private void trimLastSeparator() {
        messageBuilder.delete(messageBuilder.length() - SEPARATOR.length(), messageBuilder.length());
    }

    protected void processNull(String currentField) {
        messageBuilder.append(currentField)
                      .append(!currentField.isEmpty() ? "=<null>" : "")
                      .append(SEPARATOR);
    }

    protected void processLogMessage(LogMessage logMessage) {
        final Map<String, Object> map = logMessage;

        map.keySet()
           .stream()
           .forEach(key -> {
               final RunningLogContext<String> runningLogContext = new StringMessageBuilder(configs).buildMessage(null, map.get(key));

               messageBuilder.append(String.format("%s=%s", key, runningLogContext == null ? "<null>" : runningLogContext.getData()))
                             .append(SEPARATOR);
           });
    }

    protected void processCollection(String currentField, Collection collection) {
        messageBuilder.append(currentField)
                      .append(!currentField.isEmpty() ? ".size=" : "")
                      .append(collection.size())
                      .append(SEPARATOR);
    }

    protected void processArray(String currentField, Object array) {
        messageBuilder.append(currentField)
                      .append(!currentField.isEmpty() ? ".size=" : "")
                      .append(Array.getLength(array))
                      .append(SEPARATOR);

    }

    protected void processMap(String currentField, Map map) {
        messageBuilder.append(currentField)
                      .append(!currentField.isEmpty() ? ".size=" : "")
                      .append(map.size())
                      .append(SEPARATOR);
    }

    protected void processString(String currentField, String str) {
        messageBuilder.append(currentField)
                      .append(!currentField.isEmpty() ? "=\"" + str + "\"" : str)
                      .append(SEPARATOR);
    }

    protected void processPrimitive(String currentField, Object obj) {
        messageBuilder.append(currentField)
                      .append(!currentField.isEmpty() ? "=" : "")
                      .append(obj)
                      .append(SEPARATOR);
    }

    protected void processEnum(String currentField, Object obj) {
        messageBuilder.append(currentField)
                      .append(!currentField.isEmpty() ? "=" : "")
                      .append(obj)
                      .append(SEPARATOR);
    }

    @Override protected void processCustomImpl(String currentField, final String message) {
        messageBuilder.append(currentField)
                      .append(!currentField.isEmpty() ? "=" : "")
                      .append(message)
                      .append(SEPARATOR);

    }
}

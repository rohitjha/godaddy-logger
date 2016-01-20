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

import com.godaddy.logging.LogContext;
import com.godaddy.logging.LogMessage;
import com.godaddy.logging.LoggerMessageBuilder;
import com.godaddy.logging.LoggingConfigs;
import com.godaddy.logging.RunningLogContext;
import com.google.common.base.Strings;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static java.util.stream.Collectors.toList;

/**
 * The logstash marker formatting uses json to serialize its objects, but that doesn't give us any control over annotation
 * scopes like hash or skip, as well as the ability to do custom mapping. The serializer also uses reflection
 * and we use bytecode asm to reflect on an object. This way we send an already formatted hashmap to the
 * serializer in the form of a logstash marker. At that point iterating over the hashmap is cheap and much faster.
 */
public class LogstashMessageBuilder extends LoggerMessageBuilder<List<Map<String, Object>>> {

    private final Stack<Map<String, Object>> messageBuilderStack = new Stack<>();

    public LogstashMessageBuilder(final LoggingConfigs configs) {
        super(configs);
        messageBuilderStack.push(new HashMap<>());
    }

    @Override
    public RunningLogContext<List<Map<String, Object>>> buildMessage(final LogContext<List<Map<String, Object>>> previous, final Object currentObject) {
        return buildMessage(previous, currentObject, "");
    }

    private RunningLogContext<List<Map<String, Object>>> buildMessage(final LogContext<List<Map<String, Object>>> previous, final Object currentObject, String key) {
        if (currentObject == null) {
            return ContextUtils.initialToRunning(previous);
        }

        buildMessage(currentObject, new ArrayList<>(), key, 0);

        RunningLogContext<List<Map<String, Object>>> nextContext = ContextUtils.initialToRunning(previous);

        if (nextContext == null || nextContext.getData() == null) {
            nextContext = new RunningLogContext<>(new ArrayList<>());
        }

        nextContext.getData().add(messageBuilderStack.peek());

        return nextContext;
    }

    @Override protected void processNull(String currentField) {
        messageBuilderStack.peek().put(currentField, null);
    }

    @Override protected void processLogMessage(final LogMessage logMessage) {
        logMessage.keySet().stream().forEach(key -> {
            LogstashMessageBuilder logstashMessageBuilder = new LogstashMessageBuilder(configs);

            List<Map<String, Object>> data = logstashMessageBuilder.buildMessage(null, logMessage.get(key)).getData();

            Object process = process(data);

            messageBuilderStack.peek().put(key, process);
        });
    }

    /**
     * Unbox the map into more useful forms. For example, if the size of the list is only 1 lets unbox the map itself
     *
     * If the list is maps of all size 1 with anonymous keys, thats a list and we should flatten it
     * @param data
     * @return
     */
    private Object process(List<Map<String, Object>> data) {
        if (data.stream().allMatch(i -> i.size() == 1 && i.keySet().toArray()[0] == "")) {
            List<Object> collect = data.stream()
                                       .flatMap(i -> i.values().stream())
                                       .collect(toList());

            if(collect.size() == 1){
                return collect.get(0);
            }

            return collect;
        }

        if (data.size() == 1) {
            return data.get(0);
        }

        return data;
    }

    @Override protected void processCollection(String currentField, Collection collection) {

        List<Object> items = Arrays.stream(collection.toArray())
                                   .flatMap(i -> {
                                       LogstashMessageBuilder logstashMessageBuilder = new LogstashMessageBuilder(configs);

                                       List<Map<String, Object>> data = logstashMessageBuilder.buildMessage(null, i).getData();

                                       return data.stream();
                                   })
                                   .flatMap(i -> i.values().stream())
                                   .collect(toList());

        messageBuilderStack.peek().put(currentField, items);
    }

    @Override protected void processArray(final String currentField, final Object array) {
        int length = Array.getLength(array);

        List<Object> items = new ArrayList<>();

        for(int i = 0; i < length; i++) {
            LogstashMessageBuilder logstashMessageBuilder = new LogstashMessageBuilder(configs);

            List<Map<String, Object>> data = logstashMessageBuilder.buildMessage(null, Array.get(array, i)).getData();

            data.forEach(y -> items.addAll(y.values()));
        }

        messageBuilderStack.peek().put(currentField, items);
    }

    @Override protected void processMap(String currentField, Map map) {
        Map<String, Object> builtMap = new HashMap<>();

        for(Object key : map.keySet()){
            List<Map<String, Object>> data = new LogstashMessageBuilder(configs).buildMessage(null, map.get(key)).getData();

            builtMap.put(key.toString(), process(data));
        }

        messageBuilderStack.peek().put(currentField, builtMap);
    }

    @Override protected void processString(String currentField, String str) {
        messageBuilderStack.peek().put(currentField, str);
    }

    @Override protected void processPrimitive(String currentField, Object obj) {
        messageBuilderStack.peek().put(currentField, obj);
    }

    @Override protected void processEnum(String currentField, Object obj) {
        messageBuilderStack.peek().put(currentField, obj);
    }

    @Override protected boolean processObject(Object obj, List<String> path, String currentField, Integer recursiveLevel) {
        messageBuilderStack.push(new HashMap<>());

        if (super.processObject(obj, path, currentField, recursiveLevel)) {
            final Map<String, Object> pop = messageBuilderStack.pop();

            if (Strings.isNullOrEmpty(currentField)) {
                currentField = obj.getClass().getSimpleName();
            }

            if (currentField.equals("")) {
                messageBuilderStack.peek().putAll(pop);
            }
            else {
                messageBuilderStack.peek().put(currentField, pop);
            }
        }
        /**
         * If the object wasn't processed (cycle detected), the empty map created must be popped off the stack.
         */
        else {
            messageBuilderStack.pop();
        }

        return true;
    }

    @Override protected void processCustomImpl(final String currentField, final String message) {
        messageBuilderStack.peek().put(currentField, message);
    }

    @Override protected String formatMethod(List<String> path, String currentField) {
        currentField = trimMethodOfPrefix(currentField);

        path.add(currentField);

        return currentField;
    }

    @Override protected String formatField(String currentField, String fieldName) {
        return fieldName;
    }

}

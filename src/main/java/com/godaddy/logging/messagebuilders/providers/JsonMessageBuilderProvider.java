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
import com.godaddy.logging.LoggingConfigs;
import com.godaddy.logging.MessageBuilder;
import com.godaddy.logging.MessageBuilderProvider;
import com.godaddy.logging.RunningLogContext;
import com.godaddy.logging.messagebuilders.JsonContextUtils;
import com.godaddy.logging.messagebuilders.JsonMessageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class FoldedLogData {
    List<Map<String, Object>> unnamed = new ArrayList<>();

    List<Map<String, Object>> named = new ArrayList<>();
}

public abstract class JsonMessageBuilderProvider implements MessageBuilderProvider<List<Map<String, Object>>> {
    @Override public MessageBuilder<List<Map<String, Object>>> getBuilder(final LoggingConfigs configs) {
        return new JsonMessageBuilder(configs);
    }

    protected Map<String, Object> getContextMap(final LogContext<List<Map<String, Object>>> context) {
        /**
         * Take all the log contexts that were aggregated by the message builder
         * and format them into a hash map.
         *
         * Take care to process duplicate keys at the top level maps (which could be a result of
         * multiple anonymous 'with' keywords of objects, and assign them a unique identity
         *
         * Any keys that dont' have any text will get put into a 'context' array flattened.
         */

        final RunningLogContext<List<Map<String, Object>>> runningLogContext = JsonContextUtils.initialToRunning(context);

        FoldedLogData data = buildLogData(runningLogContext);

        HashMap<String, Object> json = new HashMap<>();

        data.named.forEach(json::putAll);

        if(data.unnamed.size() > 0) {
            json.put(CommonKeys.UNNAMED_VALUES_KEY, data.unnamed.stream().flatMap(i -> i.values().stream()).collect(toList()));
        }

        return json;
    }

    private FoldedLogData buildLogData(final RunningLogContext<List<Map<String, Object>>> runningLogContext) {
        FoldedLogData data = new FoldedLogData();

        Map<String, List<String>> keyNames = runningLogContext.getData()
                                                              .stream()
                                                              .flatMap(i -> i.keySet().stream())
                                                              .collect(Collectors.groupingBy(i -> i));

        Map<String, Integer> keyCollisionIdentifier = new HashMap<>();

        runningLogContext.getData()
                         .stream()
                         .forEach(withObject -> {

                             appendUnnamedContext(withObject, data);

                             appendNamedContext(withObject, data, keyNames, keyCollisionIdentifier);
                         });

        return data;
    }

    private void appendUnnamedContext(Map<String, Object> logContext, FoldedLogData data) {
        HashMap<String, Object> unnamed = new HashMap<>();

        logContext.keySet()
                  .stream()
                  .filter(key -> key.equals(""))
                  .forEach(key -> unnamed.put(key, logContext.get(key)));

        if(unnamed.size() > 0) {
            data.unnamed.add(unnamed);
        }
    }

    private void appendNamedContext(Map<String, Object> logContext, FoldedLogData data, Map<String, List<String>> keyNames, Map<String, Integer> keyCollisionIdentifier) {
        HashMap<String, Object> named = new HashMap<>();

        logContext.keySet()
                  .stream()
                  .filter(key -> !key.equals(""))
                  .forEach(key -> {
                      String name = key;
                      /**
                       * _unnamed_values is a reserved key for values with no key.
                       * If a named key with the value of _unnamed_values is received, it will be appended to.
                       */
                      if (keyNames.get(key).size() > 1 || key.equals(CommonKeys.UNNAMED_VALUES_KEY)) {
                          if (!keyCollisionIdentifier.containsKey(key)) {
                              keyCollisionIdentifier.put(key, 1);
                          }

                          Integer identity = keyCollisionIdentifier.get(key);

                          name = identity == 1 && !key.equals(CommonKeys.UNNAMED_VALUES_KEY) ? name : (name += identity);

                          keyCollisionIdentifier.put(key, ++identity);
                      }

                      named.put(name, logContext.get(key));
                  });

        if (named.size() > 0) {
            data.named.add(named);
        }
    }

}

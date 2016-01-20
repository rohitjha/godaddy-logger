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
import com.godaddy.logging.LoggingConfigs;
import com.godaddy.logging.MessageBuilder;
import com.godaddy.logging.MessageBuilderProvider;
import com.godaddy.logging.RunningLogContext;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static net.logstash.logback.marker.Markers.appendEntries;

class FoldedLogData {
    List<Map<String, Object>> unnamed = new ArrayList<>();

    List<Map<String, Object>> named = new ArrayList<>();
}


public class LogstashMessageBuilderProvider implements MessageBuilderProvider<List<Map<String, Object>>> {

    @Override public MessageBuilder<List<Map<String, Object>>> getBuilder(final LoggingConfigs configs) {
        return new LogstashMessageBuilder(configs);
    }

    @Override public Marker formatPayload(final LogContext<List<Map<String, Object>>> context) {

        /**
         * Take all the log contexts that were aggregated by the message builder
         * and format them into a hash map.
         *
         * Take care to process duplicate keys at the top level maps (which could be a result of
         * multiple anonymous 'with' keywords of objects, and assign them a unique identity
         *
         * Any keys that dont' have any text will get put into a 'context' array flattened.
         */

        final RunningLogContext<List<Map<String, Object>>> runningLogContext = ContextUtils.initialToRunning(context);

        FoldedLogData data = buildLogData(runningLogContext);

        HashMap<String, Object> json = new HashMap<>();

        data.named.forEach(json::putAll);

        if(data.unnamed.size() > 0) {
            json.put("context", data.unnamed.stream().flatMap(i -> i.values().stream()).collect(toList()));
        }

        return appendEntries(json);
    }

    private FoldedLogData buildLogData(final RunningLogContext<List<Map<String, Object>>> runningLogContext) {
        FoldedLogData data = new FoldedLogData();

        Map<String, List<String>> keyNames = runningLogContext.getData()
                                                              .stream()
                                                              .flatMap(i -> i.keySet().stream())
                                                              .collect(Collectors.groupingBy(i -> i));


        Map<String, Integer> keyCollisionIdentifer = new HashMap<>();

        runningLogContext.getData()
                         .stream()
                         .forEach(withObject -> {
                             HashMap<String, Object> unnamed = new HashMap<>();

                             withObject.keySet()
                                       .stream()
                                       .filter(key -> key.equals(""))
                                       .forEach(key -> unnamed.put(key, withObject.get(key)));

                             HashMap<String, Object> named = new HashMap<>();

                             withObject.keySet()
                                       .stream()
                                       .filter(key -> !key.equals(""))
                                       .forEach(key -> {
                                           String name = key;
                                           if(keyNames.get(key).size() > 1){
                                               if(!keyCollisionIdentifer.containsKey(key)){
                                                   keyCollisionIdentifer.put(key, 1);
                                               }

                                               Integer identity = keyCollisionIdentifer.get(key);

                                               name += identity;

                                               keyCollisionIdentifer.put(key, ++identity);
                                           }

                                           named.put(name, withObject.get(key));
                                       });


                             if(named.size() > 0) {
                                 data.named.add(named);
                             }

                             if(unnamed.size() > 0) {
                                 data.unnamed.add(unnamed);
                             }
                         });

        return data;
    }
}

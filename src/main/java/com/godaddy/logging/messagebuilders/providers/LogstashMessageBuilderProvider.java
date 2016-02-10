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
import org.slf4j.Marker;

import java.util.List;
import java.util.Map;

import static net.logstash.logback.marker.Markers.appendEntries;


public class LogstashMessageBuilderProvider extends JsonMessageBuilderProvider {

    @Override public Marker formatPayload(final LogContext<List<Map<String, Object>>> context) {
        final Map<String, Object> contextMap = getContextMap(context);
        // Removing the _message key so that the log message does not appear twice.
        contextMap.remove(CommonKeys.LOG_MESSAGE_KEY);

        return appendEntries(contextMap);
    }

}

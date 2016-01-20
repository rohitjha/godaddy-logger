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

import com.godaddy.logging.InitialLogContext;
import com.godaddy.logging.LogContext;
import com.godaddy.logging.LogMessage;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerImpl;
import com.godaddy.logging.LoggingConfigs;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public class LogstashLoggerImpl extends LoggerImpl {
    private final Logger root;
    private final LoggingConfigs configs;

    public LogstashLoggerImpl(Logger root, LoggingConfigs configs) {
        super(root, configs);
        this.root = root;
        this.configs = configs;
    }

    @Override
    public Logger with(Object obj) {
        return new LogstashAnnotatingLogger(root, this, obj, configs);
    }

    protected LogContext getMessage(LogContext<?> runningLogContext) {
        return runningLogContext;
    }

    @Override
    public Logger with(final String key, final Object value) {
        LogMessage logMessage = new LogMessage();

        logMessage.put(key, value);

        return new LogstashAnnotatingLogger(root, this, logMessage, configs);
    }

    private LogContext getMessage(String msg) {
        return getMessage(new InitialLogContext(msg));
    }

    private Marker formatMessage(LogContext msg) {
        return (Marker) configs.getMessageBuilderFunction().formatPayload(msg);
    }

    @Override public void info(final String msg) {
        root.info(formatMessage(getMessage(msg)), msg);
    }

    @Override
    public void trace(String msg) {
        root.trace(formatMessage(getMessage(msg)), msg);
    }

    @Override
    public void trace(String format, Object arg) {
        root.trace(formatMessage(getMessage(format)), format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        root.trace(formatMessage(getMessage(format)), format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        root.trace(formatMessage(getMessage(format)), format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        root.trace(formatMessage(getMessage(msg)), msg, t);
    }

    @Override
    public void trace(Marker marker, String msg) {
        marker.add(formatMessage(getMessage(msg)));
        root.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        marker.add(formatMessage(getMessage(format)));
        root.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        marker.add(formatMessage(getMessage(format)));
        root.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        marker.add(formatMessage(getMessage(format)));
        root.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        marker.add(formatMessage(getMessage(msg)));
        root.trace(marker, msg, t);
    }

    @Override
    public void debug(String msg) {
        root.debug(formatMessage(getMessage(msg)), msg);
    }

    @Override
    public void debug(String format, Object arg) {
        root.debug(formatMessage(getMessage(format)), format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        root.debug(formatMessage(getMessage(format)), format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        root.debug(formatMessage(getMessage(format)), format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        root.debug(formatMessage(getMessage(msg)), msg, t);
    }

    @Override
    public void debug(Marker marker, String msg) {
        marker.add(formatMessage(getMessage(msg)));
        root.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        marker.add(formatMessage(getMessage(format)));
        root.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        marker.add(formatMessage(getMessage(format)));
        root.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        marker.add(formatMessage(getMessage(format)));
        root.debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        marker.add(formatMessage(getMessage(msg)));
        root.debug(marker, msg, t);
    }

    @Override
    public void info(String format, Object arg) {
        root.info(formatMessage(getMessage(format)), format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        root.info(formatMessage(getMessage(format)), format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        root.info(formatMessage(getMessage(format)), format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        root.info(formatMessage(getMessage(msg)), msg, t);
    }

    @Override
    public void info(Marker marker, String msg) {
        marker.add(formatMessage(getMessage(msg)));
        root.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        marker.add(formatMessage(getMessage(format)));
        root.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        marker.add(formatMessage(getMessage(format)));
        root.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        marker.add(formatMessage(getMessage(format)));
        root.info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        marker.add(formatMessage(getMessage(msg)));
        root.info(marker, msg, t);
    }

    @Override
    public void warn(String msg) {
        root.warn(formatMessage(getMessage(msg)), msg);
    }

    @Override
    public void warn(String format, Object arg) {
        root.warn(formatMessage(getMessage(format)), format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        root.warn(formatMessage(getMessage(format)), format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        root.warn(formatMessage(getMessage(format)), format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        root.warn(formatMessage(getMessage(msg)), msg, t);
    }

    @Override
    public void warn(Marker marker, String msg) {
        marker.add(formatMessage(getMessage(msg)));
        root.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        marker.add(formatMessage(getMessage(format)));
        root.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        marker.add(formatMessage(getMessage(format)));
        root.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        marker.add(formatMessage(getMessage(format)));
        root.warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        marker.add(formatMessage(getMessage(msg)));
        root.warn(marker, msg, t);
    }

    @Override
    public void error(String msg) {
        root.error(formatMessage(getMessage(msg)), msg);
    }

    @Override
    public void error(String format, Object arg) {
        root.error(formatMessage(getMessage(format)), format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        root.error(formatMessage(getMessage(format)), format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        root.error(formatMessage(getMessage(format)), format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        root.error(formatMessage(getMessage(msg)), msg, t);
    }

    @Override
    public void error(Marker marker, String msg) {
        marker.add(formatMessage(getMessage(msg)));
        root.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        marker.add(formatMessage(getMessage(format)));
        root.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        marker.add(formatMessage(getMessage(format)));
        root.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        marker.add(formatMessage(getMessage(format)));
        root.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        marker.add(formatMessage(getMessage(msg)));
        root.error(marker, msg, t);
    }

    @Override
    public void error(Throwable t, String format, Object... args) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        root.error(formatMessage(getMessage(ft.getMessage())), ft.getMessage(), t);
    }

    @Override
    public void warn(final Throwable t, final String format, final Object... args) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        root.warn(formatMessage(getMessage(ft.getMessage())), ft.getMessage(), t);
    }

    @Override public void success(final String format, final Object... args) {
        successMarker.add(formatMessage(getMessage(format)));
        info(successMarker, format, args);
    }

    @Override public void dashboard(final String format, final Object... args) {
        successMarker.add(formatMessage(getMessage(format)));
        info(dashboardMarker, format, args);
    }
}

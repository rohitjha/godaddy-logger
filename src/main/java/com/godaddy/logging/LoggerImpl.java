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

package com.godaddy.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public class LoggerImpl implements Logger {

    protected Logger root;

    protected LoggingConfigs configs;

    protected final Marker successMarker = MarkerFactory.getMarker("SUCCESS");

    protected final Marker dashboardMarker = MarkerFactory.getMarker("DASHBOARD");

    public LoggerImpl(Logger root, LoggingConfigs configs) {
        this.root = root;
        this.configs = configs;
    }

    @Override
    public Logger with(Object obj) {
        return new AnnotatingLogger(root, this, obj, configs);
    }

    @Override
    public Logger with(final String key, final Object value) {
        LogMessage logMessage = new LogMessage();

        logMessage.put(key, value);

        return new AnnotatingLogger(root, this, logMessage, configs);
    }

    protected LogContext getMessage(LogContext<?> runningLogContext) {
        return runningLogContext;
    }

    private LogContext getMessage(String msg) {
        return getMessage(new InitialLogContext(msg));
    }

    private String formatMessage(LogContext msg) {
        Object formattedPayload = configs.getMessageBuilderFunction().formatPayload(msg);

        return formattedPayload == null ? null : formattedPayload.toString();
    }

    @Override
    public void info(String msg) {
        root.info(formatMessage(getMessage(msg)));
    }

    @Override
    public String getName() {
        return root.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return root.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        root.trace(formatMessage(getMessage(msg)));
    }

    @Override
    public void trace(String format, Object arg) {
        root.trace(formatMessage(getMessage(format)), arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        root.trace(formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        root.trace(formatMessage(getMessage(format)), arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        root.trace(formatMessage(getMessage(msg)), t);
    }

    @Override public boolean isTraceEnabled(final Marker marker) {
        return root.isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String msg) {
        root.trace(marker, formatMessage(getMessage(msg)));
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        root.trace(marker, formatMessage(getMessage(format)), arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        root.trace(marker, formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        root.trace(marker, formatMessage(getMessage(format)), argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        root.trace(marker, formatMessage(getMessage(msg)), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return root.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        root.debug(formatMessage(getMessage(msg)));
    }

    @Override
    public void debug(String format, Object arg) {
        root.debug(formatMessage(getMessage(format)), arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        root.debug(formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        root.debug(formatMessage(getMessage(format)), arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        root.debug(formatMessage(getMessage(msg)), t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return root.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        root.debug(marker, formatMessage(getMessage(msg)));
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        root.debug(marker, formatMessage(getMessage(format)), arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        root.debug(marker, formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        root.debug(marker, formatMessage(getMessage(format)), arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        root.debug(marker, formatMessage(getMessage(msg)), t);
    }

    @Override
    public boolean isInfoEnabled() {
        return root.isInfoEnabled();
    }

    @Override
    public void info(String format, Object arg) {
        root.info(formatMessage(getMessage(format)), arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        root.info(formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {

        root.info(formatMessage(getMessage(format)), arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        root.info(formatMessage(getMessage(msg)), t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return root.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        root.info(marker, formatMessage(getMessage(msg)));
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        root.info(marker, formatMessage(getMessage(format)), arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        root.info(marker, formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        root.info(marker, formatMessage(getMessage(format)), arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        root.info(marker, formatMessage(getMessage(msg)), t);
    }

    @Override
    public boolean isWarnEnabled() {
        return root.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        root.warn(formatMessage(getMessage(msg)));
    }

    @Override
    public void warn(String format, Object arg) {
        root.warn(formatMessage(getMessage(format)), arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        root.warn(formatMessage(getMessage(format)), arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        root.warn(formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        root.warn(formatMessage(getMessage(msg)), t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return root.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        root.warn(marker, formatMessage(getMessage(msg)));
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        root.warn(marker, formatMessage(getMessage(format)), arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        root.warn(marker, formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        root.warn(marker, formatMessage(getMessage(format)), arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        root.warn(marker, formatMessage(getMessage(msg)), t);
    }

    @Override
    public boolean isErrorEnabled() {
        return root.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        root.error(formatMessage(getMessage(msg)));
    }

    @Override
    public void error(String format, Object arg) {
        root.error(formatMessage(getMessage(format)), arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        root.error(formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        root.error(formatMessage(getMessage(format)), arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        root.error(formatMessage(getMessage(msg)), t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return root.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        root.error(marker, formatMessage(getMessage(msg)));
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        root.error(marker, formatMessage(getMessage(format)), arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        root.error(marker, formatMessage(getMessage(format)), arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        root.error(marker, formatMessage(getMessage(format)), arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        root.error(marker, formatMessage(getMessage(msg)), t);
    }

    @Override
    public void error(Throwable t, String format, Object... args) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        root.error(formatMessage(getMessage(ft.getMessage())), t);
    }

    @Override
    public void warn(final Throwable t, final String format, final Object... args) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        root.warn(formatMessage(getMessage(ft.getMessage())), t);
    }

    @Override public void success(final String format, final Object... args) {
        info(successMarker, format, args);
    }

    @Override public void dashboard(final String format, final Object... args) {
        info(dashboardMarker, format, args);
    }
}

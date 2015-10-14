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

import lombok.experimental.Delegate;

/**
 * This is the root sl4j wrapper class and provides no extension functionality.
 * It's used to delegate to the main root sl4jf logger
 */
public class Slf4WrapperLogger implements Logger {

    @Delegate
    private final org.slf4j.Logger logger;

    public Slf4WrapperLogger(final org.slf4j.Logger logger) {

        this.logger = logger;
    }

    @Override public Logger with(final Object obj) {
        return null;
    }

    @Override public void error(final Throwable t, final String format, final Object... args) {

    }

    @Override public void warn(final Throwable t, final String format, final Object... args) {

    }

    @Override public Logger with(final String key, final Object value) {
        return null;
    }

    @Override public void success(final String format, final Object... args) {

    }

    @Override public void dashboard(final String format, final Object... args) {

    }
}

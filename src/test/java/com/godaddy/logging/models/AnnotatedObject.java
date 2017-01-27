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

package com.godaddy.logging.models;

import com.godaddy.logging.LoggingScope;
import com.godaddy.logging.Scope;

public class AnnotatedObject {

    public String noAnnotationField = "NoAnnotationField";

    private final String noAnnotationProperty = "NoAnnotationProperty";

    public String getNoAnnotationProperty() {
        return noAnnotationProperty;
    }

    @LoggingScope(scope = Scope.HASH)
    public String hashAnnotationField = "HashAnnotationField";

    @LoggingScope(scope = Scope.HASH)
    private final String hashAnnotationProperty = "HashAnnotationProperty";

    public String getHashAnnotationProperty() {
        return hashAnnotationProperty;
    }

    @LoggingScope(scope = Scope.HASH)
    public String hashAnnotationMethod() {
        return "HashAnnotationMethod";
    }

    @LoggingScope(scope = Scope.SKIP)
    public String skipAnnotationField = "SkipAnnotationField";

    @LoggingScope(scope = Scope.SKIP)
    private final String skipAnnotationProperty = "SkipAnnotationProperty";

    public String getSkipAnnotationProperty() {
        return skipAnnotationProperty;
    }

    @LoggingScope(scope = Scope.SKIP)
    public String skipAnnotationMethod() {
        return "SkipAnnotationMethod";
    }
}

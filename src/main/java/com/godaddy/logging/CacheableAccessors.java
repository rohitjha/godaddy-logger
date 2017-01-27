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

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores a method sorted cache as well as a field sorted cache. These caches are needed because when reflect asm
 * gathers methods and/or fields it is not done in a sorted order, it is done in a random order. Therefore the order of methods/fields
 * will be different each time the JVM is started back up. For consistent logging purposes we want these methods and fields to be
 * in the same order.
 */
public class CacheableAccessors {
    private static ConcurrentHashMap<Class<?>, LogCache[]> _methodSortCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Class<?>, LogCache[]> _fieldSortCache = new ConcurrentHashMap<>();

    private CacheableAccessors() { }

    /**
     * Builds the method sorted cache. Sorts the methods alphabetically by name.
     */
    private static void buildMethodCache(Class<?> clazz, MethodAccess methodAccess) {

        if(!_methodSortCache.containsKey(clazz)) {
            LogCache[] sortedLogCache = new LogCache[methodAccess.getMethodNames().length];

            String[] sortedMethodNames = Arrays.copyOf(methodAccess.getMethodNames(), sortedLogCache.length);

            Arrays.sort(sortedMethodNames);

            for(int i = 0; i < sortedLogCache.length; i++) {
                sortedLogCache[i] = new LogCache(methodAccess.getIndex(sortedMethodNames[i]),
                                                 getMethodLogScope(clazz, sortedMethodNames[i]));
            }

            _methodSortCache.put(clazz, sortedLogCache);
        }

    }

    /**
     * Builds the field sorted cache. Sorts the fields alphabetically by name.
     */
    private static void buildFieldCache(Class<?> clazz, FieldAccess fieldAccess) {

        if(!_fieldSortCache.contains(clazz)) {
            LogCache[] sortedLogCache = new LogCache[fieldAccess.getFieldCount()];

            String[] sortedFieldNames = Arrays.copyOf(fieldAccess.getFieldNames(), sortedLogCache.length);

            Arrays.sort(sortedFieldNames);

            for(int i = 0; i < sortedLogCache.length; i++) {
                sortedLogCache[i] = new LogCache(fieldAccess.getIndex(sortedFieldNames[i]),
                                                 getLoggingScope(clazz, sortedFieldNames[i]));
            }

            _fieldSortCache.put(clazz, sortedLogCache);
        }

    }

    public static LogCache[] getMethodIndexes(Class<?> clazz, MethodAccess methodAccess) {
        buildMethodCache(clazz, methodAccess);

        return _methodSortCache.get(clazz);
    }

    public static LogCache[] getFieldIndexes(Class<?> clazz, FieldAccess fieldAccess) {
        buildFieldCache(clazz, fieldAccess);

        return _fieldSortCache.get(clazz);
    }

    /**
     * Gets the LoggingScope for a method. If the method does not have a LoggingScope annotation defined,
     * this function will check if the method is a getter for a field. If the method is a getter for a field,
     * that fields LoggingScope value will be returned. Else Scope.LOG is returned.
     */
    private static Scope getMethodLogScope(Class<?> clazz, String methodName) {
        try {
            LoggingScope loggingScope = clazz.getDeclaredMethod(methodName).getAnnotation(LoggingScope.class);

            if (loggingScope != null) {
                return loggingScope.scope();
            }
        }
        catch(NoSuchMethodException e) {
            // thrown if the method does not exist for the class.
        }

        return getScopeForMethodField(clazz, methodName);
    }

    /**
     * If the method is a getter for a field, that fields LoggingScope value will be returned.
     * If the method is not a getter for a field, Scope.LOG is returned.
     */
    private static Scope getScopeForMethodField(Class<?> clazz, String methodName) {

        try {
            for(PropertyDescriptor propertyDescriptor: Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (methodName.equals(propertyDescriptor.getReadMethod().getName())) {
                    return getLoggingScope(clazz, propertyDescriptor.getName());
                }
            }
        }
        catch (IntrospectionException e) {

        }
        return Scope.LOG;
    }

    private static Scope getLoggingScope(Class<?> clazz, String fieldName) {
        try {
            LoggingScope loggingScope = clazz.getDeclaredField(fieldName).getAnnotation(LoggingScope.class);

            return loggingScope == null ? Scope.LOG : loggingScope.scope();
        }
        catch (NoSuchFieldException e) {
            return Scope.LOG;
        }
    }
}

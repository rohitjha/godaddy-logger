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

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public abstract class LoggerMessageBuilder<T> implements MessageBuilder<T> {

    private List<Object> processedObjects = new LinkedList<>();

    protected LoggingConfigs configs;

    protected Integer currentRecursiveLevel = 0;

    public LoggerMessageBuilder(LoggingConfigs configs) {
        this.configs = configs;
    }

    public LoggerMessageBuilder(LoggingConfigs configs, Integer currentRecursiveLevel) {
        this.configs = configs;
        this.currentRecursiveLevel = currentRecursiveLevel;
    }

    @Override
    public abstract RunningLogContext<T> buildMessage(final LogContext<T> previous, final Object currentObject);

    protected void buildMessage(Object obj, List<String> path, String currentField) {
        if (currentRecursiveLevel > configs.getRecursiveLevel()) {
            return;
        }

        /**
         * If the custom mapper contains a key that is assignable from obj.getClass() then the function related to the value of the
         * custom mappers key is applied and appended to the builder.
         */
        if (processedCustom(obj, currentField)) {
            return;
        }

        /** If the object is null "=<null>" is appended to show that the object was null in the logs. */
        if (obj == null) {
            processNull(currentField);
        }
        else if (obj instanceof LogMessage) {
            processLogMessage((LogMessage) obj);
        }
        /** If the object is an instance of collection, only the size of the collection is logged. */
        else if (obj instanceof Collection<?>) {
            processCollection(currentField, (Collection) obj);
        }
        else if (obj.getClass().isArray()) {
            processArray(currentField, obj);
        }
        else if (obj instanceof Map) {
            processMap(currentField, (Map) obj);
        }
        /** If the object is an instance of String, the String is wrapped in quotes. */
        else if (obj instanceof String) {
            processString(currentField, (String) obj);
        }
        else if (Primitives.isWrapperType(obj.getClass())) {
            processPrimitive(currentField, obj);
        }
        else if (obj instanceof Enum) {
            processEnum(currentField, obj);
        }
        else {
            processObject(obj, path, currentField);
        }
    }

    protected abstract void processNull(String currentField);

    protected abstract void processLogMessage(LogMessage logMessage);

    protected abstract void processCollection(String currentField, Collection collection);

    protected abstract void processArray(String currentField, Object array);

    protected abstract void processMap(String currentField, Map map);

    protected abstract void processString(String currentField, String str);

    protected abstract void processPrimitive(String currentField, Object obj);

    protected abstract void processEnum(String currentField, Object obj);

    protected boolean processObject(Object obj, List<String> path, String currentField) {
        if (cyclesDetected(obj)) {
            return false;
        }

        markObjectAsProcessed(obj);

        currentRecursiveLevel++;

        recurseThroughObject(obj, path, currentField);

        return true;
    }

    private void markObjectAsProcessed(final Object obj) {
        processedObjects.add(obj);
    }

    private boolean cyclesDetected(final Object obj) {
        for (Object cached : processedObjects) {
            // use reference equality
            if (cached == obj) {
                return true;
            }
        }

        return false;
    }

    protected boolean processedCustom(Object obj, String currentField) {
        if (configs.getCustomMapper() == null || obj == null || obj instanceof LogMessage) {
            return false;
        }

        final Optional<Class<?>> customMap = configs.getCustomMapper()
                                                    .keySet()
                                                    .stream()
                                                    .filter(i -> i.isAssignableFrom(obj.getClass()))
                                                    .findFirst();

        if (!customMap.isPresent()) {
            return false;
        }

        processCustomImpl(currentField, configs.getCustomMapper()
                                               .get(customMap.get())
                                               .apply(obj));

        return true;
    }

    protected abstract void processCustomImpl(String currentField, String message);

    protected String trimMethodOfPrefix(String methodName) {
        for (String startsWith : configs.getMethodPrefixes()) {
            if (methodName.startsWith(startsWith)) {
                return methodName.length() == startsWith.length() ?
                       methodName
                                                                  :
                       Character.toLowerCase(methodName.charAt(startsWith.length())) +
                       methodName.substring(startsWith.length() + 1);
            }
        }

        return null;
    }

    protected String formatMethod(List<String> path, String currentField) {
        currentField = trimMethodOfPrefix(currentField);

        StringBuilder pathBuilder = new StringBuilder();

        path.stream().forEach(p -> pathBuilder.append(p).append("."));

        pathBuilder.append(currentField);

        path.add(currentField);

        return pathBuilder.toString();
    }

    /**
     * Retrieves the objects methods and fields. Recurses through the objects methods.
     *
     * @param obj            - Current object being recursed through.
     * @param currentField         - Recursive prefix.
     * @param recursiveLevel - Current level in recursion. maximum amount of recursion levels is defined by
     *                       RECURSIVE_LEVEL.
     */
    private void recurseThroughObject(Object obj, List<String> path, String currentField) {

        MethodAccess methodAccess = MethodAccess.get(obj.getClass());

        for (LogCache logCache : CacheableAccessors.getMethodIndexes(obj.getClass(), methodAccess)) {

            if (canLogMethod(logCache, methodAccess)) {

                List<String> recursivePath = Lists.newArrayList(path);

                Object logResult;

                try {
                    logResult = methodAccess.invoke(obj, logCache.getIndex());
                }
                catch(IllegalAccessError er) {
                    logResult = "<Illegal Method Access Error>";
                }
                catch (Throwable t) {
                    logResult = configs.getExceptionTranslator().translate(t);
                }

                try {
                    buildMessage(getLogMessage(logCache, logResult), recursivePath,
                                 formatMethod(recursivePath, methodAccess.getMethodNames()[logCache.getIndex()]));
                }
                catch (Throwable t) {
                    // result is ignored, but can be captured for debugging since we've already tried to catch
                    // and build
                    configs.getExceptionTranslator().translate(t);
                }
            }
        }

        FieldAccess fieldAccess = FieldAccess.get(obj.getClass());

        for (LogCache logCache : CacheableAccessors.getFieldIndexes(obj.getClass(), fieldAccess)) {
            String fieldName = "???";

            try {
                if (Scope.SKIP == logCache.getLogScope()) {
                    continue;
                }

                fieldName = fieldAccess.getFieldNames()[logCache.getIndex()];

                List<String> recursivePath = Lists.newArrayList(path);
                recursivePath.add(fieldName);

                if (!configs.getExcludesPrefixes().stream().anyMatch(fieldName::startsWith)) {
                    buildMessage(getLogMessage(logCache, fieldAccess.get(obj, logCache.getIndex())), recursivePath,
                                 formatField(currentField, fieldName));
                }
            }
            catch (Throwable t) {
                String fieldError = configs.getExceptionTranslator().translate(t);

                buildMessage(getLogMessage(logCache, fieldError), path,
                             formatField(currentField, fieldName));
            }
        }

    }

    protected String formatField(String currentField, String fieldName) {
        return currentField.isEmpty() ? fieldName : currentField + "." + fieldName;
    }

    private boolean canLogMethod(LogCache logCache, MethodAccess methodAccess) {
        boolean logScopeSkip = Scope.SKIP == logCache.getLogScope();

        boolean returnTypeNotVoid = methodAccess.getReturnTypes()[logCache.getIndex()] != void.class;

        boolean methodHasNoParameters = methodAccess.getParameterTypes()[logCache.getIndex()].length == 0;

        boolean methodStartsWithDefinedPrefix = trimMethodOfPrefix(methodAccess.getMethodNames()[logCache.getIndex()]) != null;

        return !logScopeSkip && returnTypeNotVoid && methodHasNoParameters && methodStartsWithDefinedPrefix;
    }

    private Object getLogMessage(LogCache logCache, Object object) {
        if (logCache.getLogScope() == Scope.HASH) {
            try {
                return configs.getHashProcessor().process(object);
            }
            catch (Throwable t) {
                return configs.getExceptionTranslator().translate(t);
            }
        }
        return object;
    }
}

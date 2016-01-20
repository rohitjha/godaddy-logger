package com.godaddy.logging.logstash;

import com.godaddy.logging.LogContext;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggingConfigs;
import com.godaddy.logging.MessageBuilder;

public class LogstashAnnotatingLogger extends LogstashLoggerImpl {

    private final Logger root;
    private final LogstashLoggerImpl parent;
    private Object obj;
    private final LoggingConfigs configs;

    public LogstashAnnotatingLogger(Logger root, LogstashLoggerImpl parent, Object obj, final LoggingConfigs configs) {
        super(root, configs);
        this.root = root;
        this.parent = parent;

        this.obj = obj;
        this.configs = configs;


    }

    @Override
    public LogContext<?> getMessage(LogContext<?> previous) {
        MessageBuilder messageBuilder = configs.getMessageBuilderFunction().getBuilder(configs);

        return parent.getMessage(messageBuilder.buildMessage(previous, obj));
    }

}

package org.apereo.cas.util;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link LoggingUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
public class LoggingUtils {

    private static final int CHAR_REPEAT_ACCOUNT = 60;

    private static final String LOGGER_NAME_PROTOCOL_MESSAGE = "PROTOCOL_MESSAGE";

    /**
     * Protocol message.
     *
     * @param title   the title
     * @param context the context
     */
    public static void protocolMessage(final String title,
                                       final Map<String, Object> context) {
        protocolMessage(title, context, StringUtils.EMPTY);
    }

    /**
     * Protocol message.
     *
     * @param title   the title
     * @param context the context
     * @param message the message
     */
    public static void protocolMessage(final String title,
                                       final Map<String, Object> context,
                                       final Object message) {
        if (isProtocolMessageLoggerEnabled()) {
            val builder = new StringBuilder();
            builder.append('\n');
            builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
            builder.append(String.format("\n%s\n", title));
            builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
            builder.append('\n');
            context.forEach((key, value) -> {
                val toLog = value.toString();
                if (StringUtils.isNotBlank(toLog)) {
                    builder.append(String.format("%s: %s\n", key, toLog));
                }
            });
            if (!context.isEmpty()) {
                builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
                builder.append('\n');
            }
            if (message != null && StringUtils.isNotBlank(message.toString())) {
                builder.append(String.format("%s\n", message));
                builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
            }
            LoggerFactory.getLogger(LOGGER_NAME_PROTOCOL_MESSAGE).info(builder.toString());
        }
    }

    public static boolean isProtocolMessageLoggerEnabled() {
        return LoggerFactory.getLogger(LOGGER_NAME_PROTOCOL_MESSAGE).isInfoEnabled();
    }

    /**
     * Error.
     *
     * @param logger the logger
     * @param msg    the msg
     */
    public static void error(final Logger logger, final String msg) {
        logger.error(msg);
    }

    /**
     * Error.
     *
     * @param logger    the logger
     * @param msg       the msg
     * @param throwable the throwable
     */
    public static void error(final Logger logger, final String msg, final Throwable throwable) {
        FunctionUtils.doIf(logger.isDebugEnabled(),
                unused -> logger.error(msg, throwable),
                unused -> logger.error(summarizeStackTrace(msg, throwable)))
            .accept(throwable);
    }

    /**
     * Log Error.
     *
     * @param logger    the logger
     * @param throwable the throwable
     */
    public static void error(final Logger logger, final Throwable throwable) {
        error(logger, getMessage(throwable), throwable);
    }

    /**
     * Log warning.
     *
     * @param logger    the logger
     * @param throwable the throwable
     */
    public static void warn(final Logger logger, final Throwable throwable) {
        warn(logger, getMessage(throwable), throwable);
    }

    /**
     * Log warning.
     *
     * @param logger    the logger
     * @param message   the message
     * @param throwable the throwable
     */
    public static void warn(final Logger logger, final String message, final Throwable throwable) {
        FunctionUtils.doIf(logger.isDebugEnabled(),
                unused -> logger.warn(message, throwable),
                unused -> logger.warn(summarizeStackTrace(message, throwable)))
            .accept(throwable);
    }

    private static String summarizeStackTrace(final String message, final Throwable throwable) {
        val builder = new StringBuilder(message).append('\n');
        Arrays.stream(throwable.getStackTrace()).limit(3).forEach(trace -> {
            val error = String.format("\t%s:%s:%s%n", trace.getFileName(), trace.getMethodName(), trace.getLineNumber());
            builder.append(error);
        });
        return builder.toString();
    }

    /**
     * Get first non-null exception message, and return class name if all messages null.
     *
     * @param throwable Top level throwable
     * @return String containing first non-null exception message, or Throwable simple class name
     */
    static String getMessage(final Throwable throwable) {
        if (StringUtils.isEmpty(throwable.getMessage())) {
            val message = ExceptionUtils.getThrowableList(throwable)
                .stream().map(Throwable::getMessage).filter(Objects::nonNull).findFirst();
            if (message.isPresent()) {
                return message.get();
            }
        }
        return StringUtils.defaultIfEmpty(throwable.getMessage(), throwable.getClass().getSimpleName());
    }

}

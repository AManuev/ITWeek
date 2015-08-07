package com.jcr.sling.junit.transientRepository;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface FeedGeneration {
    /**
     * Possible feed generation states.
     */
    enum FeedGenerationState {

        /**
         * The running.
         */
        RUNNING,

        /**
         * The failed.
         */
        FAILED,

        /**
         * The succeeded.
         */
        SUCCEEDED
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the state.
     *
     * @return the state
     */
    FeedGeneration.FeedGenerationState getState();

    /**
     * Checks if is error.
     *
     * @return true, if is error
     */
    boolean isFailed();

    /**
     * Checks if is completed.
     *
     * @return true, if is completed
     */
    boolean isCompleted();

    /**
     * Checks if is stopped.
     *
     * @return true, if is stopped
     */
    boolean isStopped();

    /**
     * Gets the error.
     *
     * @return the error
     */
    Throwable getError();

    /**
     * Stop.
     *
     * @return true, if successful
     */
    boolean stop();

    /**
     * Gets the feed generation result.
     *
     * @return the file
     */
    File getResult();

    /**
     * Gets the feed generation result.
     *
     * @param timeout  the timeout
     * @param timeUnit the time unit
     * @return the file
     */
    File getResult(long timeout, TimeUnit timeUnit);

    /**
     * Convert to future.
     *
     * @return the listenable future
     */
    ListenableFuture<File> convertToFuture();

    /**
     * Gets the property names.
     *
     * @return the property names
     */
    Collection<String> getPropertyNames();

    /**
     * Sets the property.
     *
     * @param property the property
     * @param value    the value
     */
    void setProperty(String property, Object value);

    /**
     * Gets the property.
     *
     * @param property the property
     * @return the property
     */
    Object getProperty(String property);

    /**
     * Gets the property and convert it to provided type.
     *
     * @param <T>      the generic type
     * @param property the property
     * @param type     the type
     * @return the property
     */
    <T> T getProperty(String property, Class<T> type);

    /**
     * Gets the property.
     *
     * @param <T>          the generic type
     * @param property     the property
     * @param defaultValue the default value
     * @return the property
     */
    <T> T getProperty(String property, T defaultValue);

    /**
     * Log message.
     *
     * @param message the message
     * @param args    the args
     */
    void logMessage(String message, Object... args);

    /**
     * Gets immutable copy of log messages.
     *
     * @return the messages
     */
    Collection<String> getMessages();

    /**
     * Gets the generation time. If completed get difference between complete and start time, otherwise between current time and start time
     *
     * @return the generation time
     */
    long getGenerationTime();
}

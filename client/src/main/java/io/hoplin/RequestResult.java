package io.hoplin;

/**
 * Represents the result of a the request
 */
public class RequestResult {

    // The reply value to the client
    private final Object reply;
    // The exception hit during the execution of the request (or null if there was no exception).
    private final Exception exception;

    public RequestResult(Object reply, Exception exception) {
        this.reply = reply;
        this.exception = exception;
    }
}

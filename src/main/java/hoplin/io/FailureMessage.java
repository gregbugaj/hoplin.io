package hoplin.io;

import java.util.Objects;

/**
 * Message representing a failure that will be submitted to DLQ
 */
public class FailureMessage
{
    private String payload;

    public FailureMessage()
    {
        // serialization
    }

    public FailureMessage(final String payload)
    {
        this.payload = Objects.requireNonNull(payload);
    }

}

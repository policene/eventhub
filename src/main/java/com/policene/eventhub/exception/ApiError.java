package com.policene.eventhub.exception;

import java.time.Instant;

public record ApiError(
        String error,
        int status,
        Instant timeStamp
) {
}

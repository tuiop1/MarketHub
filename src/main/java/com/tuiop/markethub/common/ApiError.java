package com.tuiop.markethub.common;

import java.time.Instant;

//common error dto response

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}

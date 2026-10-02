package org.datamate.collaboration.chat.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Inbound Query for fetching paginated messages within a thread.
 * Strictly adheres to CQRS naming conventions (Query for read-only data fetch).
 */
public record GetMessagesQuery(
        @Min(value = 1, message = "Page number must be at least 1")
        int page,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size must not exceed 100")
        int size
) {
    public GetMessagesQuery {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;
    }

    public GetMessagesQuery() {
        this(1, 20);
    }
}
package sn.symmetry.cadoobi.domain.enums;

public enum PayoutStatus {
    PENDING,      // Created but not yet executed
    PROCESSING,   // Executed and being processed by operator
    COMPLETED,    // Successfully completed
    FAILED        // Failed or rejected
}

package sn.symmetry.cadoobi.domain.enums;

public enum NotificationEventType {
    // Payment events
    PAYMENT_INITIATED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    PAYMENT_CANCELLED,
    PAYMENT_EXPIRED,
    PAYMENT_STATUS_UPDATE,

    // Legacy events
    CARD_REDEEMED,
    CASHIN_COMPLETED,
    CARD_EXPIRED
}

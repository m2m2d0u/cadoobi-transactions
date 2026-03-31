package sn.symmetry.cadoobi.domain.enums;

public enum LedgerEntryType {
    /** PSP settled a PAYIN: balance += netAmount */
    PAYIN_SETTLEMENT,

    /** Payout initiated: lockedBalance += (amount + merchantFee) */
    PAYOUT_LOCK,

    /** Payout failed/cancelled: lockedBalance -= (amount + merchantFee) */
    PAYOUT_RELEASE,

    /** Payout completed — cash going out to PSP: balance -= amount, lockedBalance -= (amount + merchantFee) */
    PAYOUT_SETTLEMENT,

    /** Payout completed — Cadoobi revenue sidecar: balance -= merchantFee (audit trail) */
    PAYOUT_FEE,

    /** Manual balance correction by an admin */
    MANUAL_ADJUSTMENT
}

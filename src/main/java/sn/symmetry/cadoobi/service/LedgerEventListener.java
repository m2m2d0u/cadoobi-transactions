package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;
import sn.symmetry.cadoobi.event.PaymentCompletedEvent;
import sn.symmetry.cadoobi.event.PayoutStatusChangedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class LedgerEventListener {

    private final LedgerService ledgerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Handling PaymentCompletedEvent for payment {}",
            event.getPaymentTransaction().getReference());
        ledgerService.postPayinSettlement(event.getPaymentTransaction());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPayoutStatusChanged(PayoutStatusChangedEvent event) {
        PayoutStatus newStatus = event.getNewStatus();
        log.info("Handling PayoutStatusChangedEvent: payout={}, status={}",
            event.getPayoutTransaction().getId(), newStatus);

        switch (newStatus) {
            case COMPLETED -> ledgerService.settleCompletedPayout(event.getPayoutTransaction());
            case FAILED -> ledgerService.releasePayoutLock(event.getPayoutTransaction());
            default -> log.debug("No ledger action for payout status: {}", newStatus);
        }
    }
}

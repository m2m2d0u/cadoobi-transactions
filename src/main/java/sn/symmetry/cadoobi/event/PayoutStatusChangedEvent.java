package sn.symmetry.cadoobi.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import sn.symmetry.cadoobi.domain.entity.PayoutTransaction;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;

@Getter
public class PayoutStatusChangedEvent extends ApplicationEvent {

    private final PayoutTransaction payoutTransaction;
    private final PayoutStatus oldStatus;
    private final PayoutStatus newStatus;

    public PayoutStatusChangedEvent(Object source, PayoutTransaction payoutTransaction,
                                    PayoutStatus oldStatus, PayoutStatus newStatus) {
        super(source);
        this.payoutTransaction = payoutTransaction;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}

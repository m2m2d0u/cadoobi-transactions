package sn.symmetry.cadoobi.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import sn.symmetry.cadoobi.domain.entity.PaymentTransaction;

@Getter
public class PaymentCompletedEvent extends ApplicationEvent {

    private final PaymentTransaction paymentTransaction;

    public PaymentCompletedEvent(Object source, PaymentTransaction paymentTransaction) {
        super(source);
        this.paymentTransaction = paymentTransaction;
    }
}

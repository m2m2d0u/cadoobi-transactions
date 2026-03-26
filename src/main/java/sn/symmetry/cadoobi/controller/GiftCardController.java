package sn.symmetry.cadoobi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.GiftCardBalanceResponse;
import sn.symmetry.cadoobi.dto.RedeemGiftCardRequest;
import sn.symmetry.cadoobi.dto.RedemptionResponse;
import sn.symmetry.cadoobi.service.GiftCardService;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Slf4j
public class GiftCardController {

    private final GiftCardService giftCardService;

    @GetMapping("/{cardCode}/balance")
    public ResponseEntity<GiftCardBalanceResponse> getCardBalance(@PathVariable String cardCode) {
        log.info("Fetching gift card balance: cardCode={}", cardCode);

        GiftCardBalanceResponse response = giftCardService.getGiftCardBalance(cardCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cardCode}/redeem")
    public ResponseEntity<RedemptionResponse> redeemGiftCard(
        @PathVariable String cardCode,
        @Valid @RequestBody RedeemGiftCardRequest request
    ) {
        log.info("Redeeming gift card: cardCode={}, merchant={}, amount={}",
            cardCode, request.getMerchantId(), request.getAmountToRedeem());

        RedemptionResponse response = giftCardService.redeemGiftCard(cardCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

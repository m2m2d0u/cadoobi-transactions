package sn.symmetry.cadoobi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.ControllerApiResponse;
import sn.symmetry.cadoobi.dto.GiftCardBalanceResponse;
import sn.symmetry.cadoobi.dto.RedeemGiftCardRequest;
import sn.symmetry.cadoobi.dto.RedemptionResponse;
import sn.symmetry.cadoobi.service.GiftCardService;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gift Cards", description = "Gift card management endpoints for checking balances and redeeming cards")
public class GiftCardController {

    private final GiftCardService giftCardService;

    @GetMapping("/{cardCode}/balance")
    @Operation(
        summary = "Get gift card balance",
        description = "Retrieves the current balance and status information for a gift card"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Gift card balance retrieved successfully",
            content = @Content(schema = @Schema(implementation = GiftCardBalanceResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Gift card not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid card code format",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<GiftCardBalanceResponse>> getCardBalance(
        @Parameter(description = "Unique gift card code identifier", required = true, example = "ABC123DEF456")
        @PathVariable String cardCode
    ) {
        log.info("Fetching gift card balance: cardCode={}", cardCode);

        GiftCardBalanceResponse balance = giftCardService.getGiftCardBalance(cardCode);

        ControllerApiResponse<GiftCardBalanceResponse> response = ControllerApiResponse.success(
            balance,
            "Gift card balance retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cardCode}/redeem")
    @Operation(
        summary = "Redeem gift card",
        description = "Processes a gift card redemption transaction for a specified amount"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Gift card redeemed successfully",
            content = @Content(schema = @Schema(implementation = RedemptionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid redemption request (insufficient balance, invalid amount, etc.)",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Gift card not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Gift card is expired or already fully redeemed",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<RedemptionResponse>> redeemGiftCard(
        @Parameter(description = "Unique gift card code identifier", required = true, example = "ABC123DEF456")
        @PathVariable String cardCode,
        @Valid @RequestBody RedeemGiftCardRequest request
    ) {
        log.info("Redeeming gift card: cardCode={}, merchant={}, amount={}",
            cardCode, request.getMerchantId(), request.getAmountToRedeem());

        RedemptionResponse redemption = giftCardService.redeemGiftCard(cardCode, request);

        ControllerApiResponse<RedemptionResponse> response = ControllerApiResponse.created(
            redemption,
            "Gift card redeemed successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

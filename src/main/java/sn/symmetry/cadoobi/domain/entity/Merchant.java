package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.MerchantStatus;

@Entity
@Table(name = "merchants", indexes = {
    @Index(name = "idx_merchant_code", columnList = "code"),
    @Index(name = "idx_merchant_status", columnList = "status"),
    @Index(name = "idx_merchant_country", columnList = "country")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant extends BaseEntity {

    // ── Cadoobi BO fields ─────────────────────────────────────────────────────

    /** Short business code, e.g. "SNM12345". Used as idempotency key toward Symmetry. */
    @Column(name = "code", length = 10, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "phone", length = 15, nullable = false)
    private String phone;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "country", length = 2, nullable = false)
    private String country;

    @Column(name = "rccm", length = 100)
    private String rccm;

    @Column(name = "ninea", length = 50)
    private String ninea;

    // ── Owner / Manager ───────────────────────────────────────────────────────

    @Column(name = "owner_full_name", length = 150)
    private String ownerFullName;

    @Column(name = "owner_email", length = 100)
    private String ownerEmail;

    @Column(name = "owner_phone", length = 15)
    private String ownerPhone;

    @Column(name = "owner_cni", length = 50)
    private String ownerCni;

    /** System user who manages this merchant account */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // ── Compensation account ──────────────────────────────────────────────────

    @Embedded
    private CompensationAccount compensationAccount;

    // ── Symmetry fields (set after onboarding call) ───────────────────────────

    @Column(name = "symmetry_merchant_id", unique = true, length = 50)
    private String symmetryMerchantId;

    @Column(name = "agency_code", length = 50)
    private String agencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MerchantStatus status;
}

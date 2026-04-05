package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * System parameter entity for storing configuration key-value pairs
 */
@Entity
@Table(name = "parameters", indexes = {
    @Index(name = "idx_parameter_key", columnList = "param_key", unique = true),
    @Index(name = "idx_parameter_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parameter extends BaseEntity {

    /**
     * Unique parameter key
     */
    @Column(name = "param_key", nullable = false, unique = true, length = 100)
    private String key;

    /**
     * Parameter value
     */
    @Column(name = "param_value", columnDefinition = "TEXT")
    private String value;

    /**
     * Parameter category for grouping (e.g., "SYSTEM", "PAYMENT", "NOTIFICATION")
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * Parameter description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Whether the parameter is active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether the parameter is a system parameter (cannot be deleted)
     */
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;
}

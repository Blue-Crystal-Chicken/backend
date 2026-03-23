package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an order in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "user", "orderProducts" })
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Order_id")
    @NotBlank(message = "L'ID ordine è obbligatorio")
    private String orderId;

    @Column(name = "Code")
    private String code;

    @Column(name = "Service_type")
    @NotBlank(message = "Il tipo di servizio è obbligatorio")
    private String serviceType;

    @Column(name = "Order_type")
    private String orderType;

    @Column(name = "Table_number")
    private String tableNumber;

    @Column(name = "Payment_type")
    private String paymentType;

    @Column(name = "Payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "Total_at")
    private BigDecimal totalAt;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "User_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Location_id")
    private LocationEntity location;

    // Bidirectional with OrderProduct
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

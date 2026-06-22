package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.order;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.location.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.user.UserEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.OrderProduct;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Represents an order in the Blue Crystal Chicken system.
 */
@Entity
@Table(name = "ORDERS")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "user", "orderProducts" })
public class OrderEntity extends AuditingField {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "User_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Location_id")
    private LocationEntity location;

    // Bidirectional with OrderProduct
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderProduct> orderProducts;
}

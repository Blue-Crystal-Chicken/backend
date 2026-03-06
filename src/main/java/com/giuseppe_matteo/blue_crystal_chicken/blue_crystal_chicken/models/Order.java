package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an order in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "ORDERS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Order_id")
    private String orderId;

    @Column(name = "Code")
    private String code;

    @Column(name = "Service_type")
    private String serviceType;

    @Column(name = "Order_type")
    private String orderType;

    @Column(name = "Table_number")
    private String tableNumber;

    @Column(name = "Payment_type")
    private String paymentType;

    @Column(name = "Payment_amount")
    private Double paymentAmount;

    @Column(name = "Total_at")
    private Double totalAt;

    @CreationTimestamp
    @Column(name = "Created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "User_id")
    private User user;

    // Bidirectional with OrderProduct
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;
}
package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.OrderProductKey;

import jakarta.persistence.*;
import lombok.*;

/**
 * Junction entity for Order and Product with additional attributes.
 */
@Entity
@Table(name = "Order_Products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct {

    @EmbeddedId
    private OrderProductKey id;

    @Column(name = "Additions")
    private Double additions;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Special_note")
    private String specialNote;

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "Order_id")
    private OrderPOJO order;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "Product_id")
    private ProductPOJO product;
}
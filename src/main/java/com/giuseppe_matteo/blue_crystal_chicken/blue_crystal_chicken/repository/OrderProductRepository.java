package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.OrderProductKey;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, OrderProductKey> {

    List<OrderProduct> findById_OrderId(Long orderId);

    List<OrderProduct> findById_ProductId(Long productId);

    // Prodotti con note speciali in un ordine
    List<OrderProduct> findById_OrderIdAndSpecialNoteIsNotNull(Long orderId);

    // Quante volte è stato ordinato un prodotto (per statistiche)
    @Query("SELECT SUM(op.quantity) FROM OrderProduct op WHERE op.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    boolean existsById_OrderIdAndId_ProductId(Long orderId, Long productId);

    void deleteById_OrderIdAndId_ProductId(Long orderId, Long productId);

}

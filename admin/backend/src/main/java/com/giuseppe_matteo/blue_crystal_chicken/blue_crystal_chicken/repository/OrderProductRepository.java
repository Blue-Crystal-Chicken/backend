package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    List<OrderProduct> findByOrder_Id(Long orderId);

    List<OrderProduct> findByProduct_Id(Long productId);

    List<OrderProduct> findByOffer_Id(Long offerId);

    // Prodotti con note speciali in un ordine
    List<OrderProduct> findByOrder_IdAndSpecialNoteIsNotNull(Long orderId);

    // Quante volte è stato ordinato un prodotto (per statistiche)
    @Query("SELECT SUM(op.quantity) FROM OrderProduct op WHERE op.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    boolean existsByOrder_IdAndProduct_Id(Long orderId, Long productId);

    boolean existsByOrder_IdAndOffer_Id(Long orderId, Long offerId);

    void deleteByOrder_IdAndProduct_Id(Long orderId, Long productId);
}

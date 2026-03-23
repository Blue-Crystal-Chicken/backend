package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
 
    Optional<OrderEntity> findByOrderId(String orderId);
 
    boolean existsByOrderId(String orderId);
 
    List<OrderEntity> findByUserId(Long userId);
 
    List<OrderEntity> findByServiceType(String serviceType);
 
    List<OrderEntity> findByOrderType(String orderType);
 
    List<OrderEntity> findByPaymentType(String paymentType);
 
    List<OrderEntity> findByTableNumber(String tableNumber);
 
    // Ordini in un intervallo di tempo
    List<OrderEntity> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
 
    // Ordini di un utente in un intervallo di tempo
    List<OrderEntity> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);
 
    // Totale incassato in un intervallo di tempo
    @Query("SELECT SUM(o.totalAt) FROM OrderEntity o WHERE o.createdAt BETWEEN :from AND :to")
    BigDecimal sumTotalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
 
    // Ordini sopra un certo importo
    List<OrderEntity> findByTotalAtGreaterThanEqual(BigDecimal minTotal);
}
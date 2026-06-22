package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.OfferProductKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferProductRepository extends JpaRepository<OfferProduct, OfferProductKey> {

    List<OfferProduct> findById_OfferId(Long offerId);

    List<OfferProduct> findById_ProductId(Long productId);

    boolean existsById_OfferIdAndId_ProductId(Long offerId, Long productId);

    void deleteById_OfferIdAndId_ProductId(Long offerId, Long productId);
}

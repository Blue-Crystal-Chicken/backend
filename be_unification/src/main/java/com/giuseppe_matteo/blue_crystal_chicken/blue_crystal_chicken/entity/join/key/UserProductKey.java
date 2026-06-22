package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.key;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProductKey implements Serializable {

    @Column(name = "User_id")
    private Long userId;

    @Column(name = "Product_id")
    private Long productId;
}

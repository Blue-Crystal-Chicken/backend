package com.r2u.notification.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class AuditingField {

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(updatable = false)
    private Long createdBy;

    @Column
    private Long updatedBy;

    @Column
    private Boolean deleted;

}
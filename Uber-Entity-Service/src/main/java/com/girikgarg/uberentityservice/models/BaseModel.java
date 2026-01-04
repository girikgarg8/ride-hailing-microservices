package com.girikgarg.uberentityservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
@Setter
public abstract class BaseModel {
    @Id // this annotation makes the id property a primary key of our table
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Identity means auto_increment
    protected Long id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    // @Temporal tells JPA how to map Date to database:
    // - DATE: stores only date (2026-01-04)
    // - TIME: stores only time (14:30:45)
    // - TIMESTAMP: stores date + time + milliseconds (2026-01-04 14:30:45.123)
    @CreatedDate // Auto-populated by Spring Data JPA when entity is first created
    protected Date createdAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate // Auto-updated by Spring Data JPA whenever entity is modified
    protected Date updatedAt;
}

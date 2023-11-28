package com.github.uziskull.restdbservice.data.dao;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "device")
@Data
public class DeviceDAO {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String brand;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant creationTimestamp;
}

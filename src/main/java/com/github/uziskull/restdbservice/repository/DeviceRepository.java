package com.github.uziskull.restdbservice.repository;

import com.github.uziskull.restdbservice.model.dao.DeviceDAO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceDAO, UUID> {
    Page<DeviceDAO> findByBrand(String brand, Pageable pageable);
}

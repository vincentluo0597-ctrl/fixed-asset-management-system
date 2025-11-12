package com.example.gdzc.repository;

import com.example.gdzc.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByName(String name);
    List<Location> findByParentId(Long parentId);
    boolean existsByParentId(Long parentId);
    List<Location> findByNameContainingIgnoreCase(String name);
}

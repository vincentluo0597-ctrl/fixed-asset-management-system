package com.example.gdzc.repository;

import com.example.gdzc.domain.FaultCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FaultCaseRepository extends JpaRepository<FaultCase, Long>, JpaSpecificationExecutor<FaultCase> {
}
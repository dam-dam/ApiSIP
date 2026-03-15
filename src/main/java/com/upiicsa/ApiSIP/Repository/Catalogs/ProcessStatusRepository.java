package com.upiicsa.ApiSIP.Repository.Catalogs;

import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessStatusRepository extends JpaRepository<ProcessStatus, Integer> {
    Optional<ProcessStatus> findByDescription(String description);
}

package com.upiicsa.ApiSIP.Repository.Document_Process;

import com.upiicsa.ApiSIP.Model.Catalogs.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentStatusRepository extends JpaRepository<DocumentStatus, Integer> {

    Optional<DocumentStatus> findByDescription(String description);
}

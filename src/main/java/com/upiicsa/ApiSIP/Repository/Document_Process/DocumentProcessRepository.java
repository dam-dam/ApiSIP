package com.upiicsa.ApiSIP.Repository.Document_Process;

import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentProcess;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentProcessId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentProcessRepository extends JpaRepository<DocumentProcess, DocumentProcessId> {

    @Query("SELECT dp.documentType FROM DocumentProcess dp WHERE dp.processStatus = :state")
    List<DocumentType> findDocumentTypesByProcessState(@Param("state") ProcessStatus state);
}

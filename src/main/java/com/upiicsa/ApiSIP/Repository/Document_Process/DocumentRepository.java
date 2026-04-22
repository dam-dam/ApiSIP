package com.upiicsa.ApiSIP.Repository.Document_Process;

import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.UserSIP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

    @Query("SELECT d FROM Document d WHERE d.user = :user AND d.documentType.description = :typeCode ORDER BY d.id DESC")
    List<Document> findAllByUserAndTypeCode(@Param("user") UserSIP user, @Param("typeCode") String typeCode);

    Optional<Document> findByStudentProcessAndDocumentTypeAndCancellationDateIsNull
            (StudentProcess process, DocumentType type);

    @Query("SELECT d FROM Document d JOIN DocumentProcess dp ON d.documentType.id = dp.documentType.id " +
            "WHERE dp.processStatus = :status AND d.studentProcess.id = :idProcess AND d.cancellationDate IS NULL")
    List<Document> findByProcessAndCancelDateIsNull(@Param("status") ProcessStatus status,
                                                    @Param("idProcess")  Integer idProcess);

    Integer countByStudentProcessAndDocumentType(StudentProcess process, DocumentType type);

    @Query("SELECT d FROM Document d WHERE d.studentProcess = :process AND d.cancellationDate IS NULL " +
            "ORDER BY d.uploadDate DESC")
    List<Document> findActiveDocumentsOrdered(@Param("process") StudentProcess process);
}

package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Model.Catalogs.DocumentStatus;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentRepository;
import com.upiicsa.ApiSIP.Service.Infrastructure.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentPersistenceService {

    private final DocumentRepository documentRepository;
    private final DocumentUtilsService utilsService;
    private final FileStorageService fileStorage;

    public DocumentPersistenceService(DocumentRepository documentRepository, DocumentUtilsService utilsService,
                                      FileStorageService fileStorage) {
        this.documentRepository = documentRepository;
        this.utilsService = utilsService;
        this.fileStorage = fileStorage;
    }

    @Transactional(readOnly = true)
    public Document findDocByProcessAndType(StudentProcess process, String typeName){
        DocumentType type = utilsService.findTypeByDescription(typeName);

        return documentRepository.findByStudentProcessAndDocumentTypeAndCancellationDateIsNull
                (process, type).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Document> findByProcessAndStatus(StudentProcess process){
        return documentRepository.findByProcessAndCancelDateIsNull(
                process.getProcessStatus(), process.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<Document> findActiveDocumentsOrdered(StudentProcess process) {
        return documentRepository.findActiveDocumentsOrdered(process);
    }

    @Transactional
    public void createDocument(StudentProcess process, DocumentType type, MultipartFile file){
        DocumentStatus docStatus = utilsService.findStatusByDescription("PENDIENTE");

        Document newDocument = Document.builder()
                .studentProcess(process).user(process.getStudent())
                .uploadDate(LocalDateTime.now()).URL(fileStorage.store(file, process, type))
                .documentType(type).documentStatus(docStatus)
                .build();
        documentRepository.save(newDocument);
    }

    @Transactional
    public void createLetter(UserSIP user, StudentProcess process, MultipartFile file){
        DocumentStatus docStatus = utilsService.findStatusByDescription("CORRECTO");
        DocumentType docType = utilsService.findTypeByDescription("CARTA_PRESENTACION");

        Document newDocument = Document.builder()
                .studentProcess(process)
                .user(user)
                .uploadDate(LocalDateTime.now())
                .URL(fileStorage.store(file, process.getStudent().getEnrollment()))
                .documentType(docType)
                .documentStatus(docStatus)
                .build();
        documentRepository.save(newDocument);
    }

    @Transactional
    public void updateDocument(Document doc, MultipartFile file) {
        doc.setUploadDate(LocalDateTime.now());
        documentRepository.save(doc);
        fileStorage.store(file, doc);
    }

    @Transactional
    public void cancelDocument(Document doc) {
        doc.setCancellationDate(LocalDateTime.now());
        documentRepository.save(doc);
    }

    @Transactional
    public void changeStatus(Document doc, DocumentStatus status){
        doc.setDocumentStatus(status);
        documentRepository.save(doc);
    }
}

package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Dto.Document.DocumentStatusDto;
import com.upiicsa.ApiSIP.Exception.BusinessException;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentStatus;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentReview;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.Enum.ErrorCode;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentRepository;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import com.upiicsa.ApiSIP.Service.Infrastructure.FileStorageService;
import com.upiicsa.ApiSIP.Utils.DocumentNamingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentUtilsService utilsService;
    private final StudentProcessService processService;
    private final DocumentNamingUtils documentNaming;
    private final FileStorageService fileStorage;

    public DocumentService(DocumentRepository documentRepository,UserRepository userRepository,
                           DocumentUtilsService utilsService, StudentProcessService processService,
                           DocumentNamingUtils documentNaming, FileStorageService fileStorage) {

        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.utilsService = utilsService;
        this.processService = processService;
        this.documentNaming = documentNaming;
        this.fileStorage = fileStorage;
    }

    @Transactional(readOnly = true)
    public Optional<Document> findDocByProcessAndType(StudentProcess process, String typeName){
        DocumentType type = utilsService.getTypeByDescription(typeName);

        return documentRepository.findByStudentProcessAndDocumentTypeAndCancellationDateIsNull
                (process, type);
    }

    @Transactional(readOnly = true)
    public List<Document> findByProcessAndStatus(ProcessStatus status){
        return documentRepository.findByProcessAndCancelDateIsNull(status);
    }

    @Transactional
    public void saveDoc(MultipartFile file, String typeName, Integer userId) {
        StudentProcess process = processService.getByStudentId(userId);
        DocumentType type = utilsService.getTypeByDescription(typeName);

        Optional<Document> document = findDocByProcessAndType(process, typeName);

        if(document.isPresent()){
            Document currentDoc = document.get();

            switch (currentDoc.getDocumentStatus().getDescription()) {
                case "CORRECTO": throw new BusinessException(ErrorCode.DOCUMENT_ALREADY_APPROVED);
                case "INCORRECTO": cancelledAndCreated(currentDoc, type, file, userId);
                    break;
                case "PENDIENTE": updateDoc(currentDoc, typeName, file);
                    break;
                default: throw new BusinessException(ErrorCode.INTERNAL_ERROR);
            }
        } else {
            createNewDocument(userId, type, file);
        }
        if(process.getProcessStatus().getId() != 2){
            processService.updateStatus(process);
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentStatusDto> getDocuments(Integer userId, String processStatus) {
        StudentProcess process = processService.getByStudentId(userId);
        List<DocumentType> requiredTypes = utilsService.getRequiredTypesStatus(processStatus);

        List<Document> activeDocuments = documentRepository.findActiveDocumentsOrdered(process);

        Map<DocumentType, Document> activeDocsMap = activeDocuments.stream()
                .collect(Collectors.toMap(
                        Document::getDocumentType,
                        doc -> doc,
                        (doc1, doc2) -> getHighestPriorityDoc(doc1, doc2)
                ));

        return requiredTypes.stream().map(type -> {
            Document targetDoc = activeDocsMap.get(type);

            if (targetDoc == null) {
                return new DocumentStatusDto(type.getDescription(), "SIN_CARGA", null, "",
                        "", null);
            }

            DocumentReview docReview = utilsService.getReviewByDocument(targetDoc);
            String comment = (docReview != null && docReview.getComment() != null) ? docReview.getComment() : "";

            return new DocumentStatusDto(type.getDescription(), targetDoc.getDocumentStatus().getDescription(),
                    targetDoc.getURL(), comment, "/view-document/" + targetDoc.getURL(),
                    targetDoc.getUploadDate()
            );
        }).collect(Collectors.toList());
    }

    private Document getHighestPriorityDoc(Document doc1, Document doc2) {
        String status1 = doc1.getDocumentStatus().getDescription();
        String status2 = doc2.getDocumentStatus().getDescription();

        if (status1.equals("CORRECTO") || status2.equals("CORRECTO")) {
            return status1.equals("CORRECTO") ? doc1 : doc2;
        }
        if (status1.equals("PENDIENTE") || status2.equals("PENDIENTE")) {
            return status1.equals("PENDIENTE") ? doc1 : doc2;
        }
        return doc1.getUploadDate().isAfter(doc2.getUploadDate()) ? doc1 : doc2;
    }

    @Transactional
    public void createNewDocument(Integer userId, DocumentType type, MultipartFile file){
        UserSIP user = userRepository.findById(userId).orElse(null);
        StudentProcess process = processService.getByStudentId(userId);

        String finalName = documentNaming.generateVersionedName(process, type);
        DocumentStatus docStatus = utilsService.getStatusByDescription("PENDIENTE");

        Document newDocument = Document.builder()
                .studentProcess(process)
                .user(user)
                .uploadDate(LocalDateTime.now())
                .URL(finalName)
                .documentType(type)
                .documentStatus(docStatus)
                .build();

        documentRepository.save(newDocument);
        fileStorage.store(file, finalName);
    }

    @Transactional
    public void changeStatus(DocumentStatus docStatus, Document doc){

        doc.setDocumentStatus(docStatus);
        documentRepository.save(doc);
    }

    @Transactional
    public void updateDoc(Document currentDoc, String typeName, MultipartFile file) {
        if(!currentDoc.getDocumentType().getDescription().equals(typeName)){
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
        currentDoc.setUploadDate(LocalDateTime.now());
        documentRepository.save(currentDoc);
        fileStorage.store(file, currentDoc.getURL());
    }

    @Transactional
    public void cancelledAndCreated(Document currentDoc, DocumentType type,  MultipartFile file, Integer userId) {
        currentDoc.setCancellationDate(LocalDateTime.now());
        documentRepository.save(currentDoc);
        createNewDocument(userId, type, file);
    }
}

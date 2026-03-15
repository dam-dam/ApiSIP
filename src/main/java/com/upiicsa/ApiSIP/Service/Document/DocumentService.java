package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Dto.Document.DocumentStatusDto;
import com.upiicsa.ApiSIP.Exception.ValidationException;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentStatus;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentReview;
import com.upiicsa.ApiSIP.Model.Enum.StateProcessEnum;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentReviewRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentStatusRepository;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import com.upiicsa.ApiSIP.Service.Infrastructure.FileStorageService;
import com.upiicsa.ApiSIP.Service.StudentProcessService;
import com.upiicsa.ApiSIP.Utils.DocumentNamingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private DocumentRepository documentRepository;
    private DocumentReviewRepository reviewRepository;
    private DocumentStatusRepository documentStatusRepository;
    private UserRepository userRepository;
    private DocumentTypeService docTypeService;
    private StudentProcessService processService;
    private DocumentNamingUtils documentNaming;
    private FileStorageService fileStorage;

    public DocumentService(DocumentRepository documentRepository, DocumentReviewRepository reviewRepository,
                           DocumentStatusRepository documentStatusRepository,UserRepository userRepository,
                           DocumentTypeService typeService, StudentProcessService processService,
                           DocumentNamingUtils documentNaming, FileStorageService fileStorage) {
        this.documentRepository = documentRepository;
        this.reviewRepository = reviewRepository;
        this.documentStatusRepository = documentStatusRepository;
        this.userRepository = userRepository;
        this.docTypeService = typeService;
        this.processService = processService;
        this.documentNaming = documentNaming;
        this.fileStorage = fileStorage;
    }

    @Transactional
    public void saveDoc(MultipartFile file, String typeName, Integer userId) {
        StudentProcess process = processService.getByStudentId(userId);
        DocumentType type = docTypeService.getByDescription(typeName);

        Optional<Document> document = documentRepository.findByStudentProcessAndDocumentTypeAndCancellationDateIsNull
                (process, type);

        if(document.isPresent()){
            Document currentDoc = document.get();

            switch (currentDoc.getDocumentStatus().getDescription()) {
                case "APROBADO": throw new ValidationException("Este documento ya fue aprobado y no puede modificarse.");
                case "CANCELADO": cancelledAndCreated(currentDoc, type, file, userId);
                break;
                case "PENDIENTE": updateDoc(currentDoc, typeName, file);
                break;
            }
        } else {
            createNewDocument(userId, type, file);
        }
        if(process.getProcessStatus().getId() != 2){
            processService.updateProcessStatus(process, StateProcessEnum.INITIAL_DOC);
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentStatusDto> getDocuments(Integer userId) {
        StudentProcess process = processService.getByStudentId(userId);
        List<DocumentType> requiredTypes = docTypeService.getRequiredTypes(process);

        return requiredTypes.stream().map(type -> {

            Optional<Document> docOpt = documentRepository
                    .findByStudentProcessAndDocumentTypeAndCancellationDateIsNull(process, type);

            if (docOpt.isPresent()) {
                Document doc = docOpt.get();

                DocumentReview review = reviewRepository.findByDocument(doc);
                if (review != null) {
                    return new DocumentStatusDto(type.getDescription(),
                            doc.getDocumentStatus().getDescription(),
                            doc.getURL(), review.getComment(), "/view-document/" + doc.getURL(),
                            doc.getUploadDate()
                    );
                }
                return new DocumentStatusDto(type.getDescription(), doc.getDocumentStatus().getDescription(),
                        doc.getURL(), "", "", doc.getUploadDate());
            }
            return new DocumentStatusDto(type.getDescription(), "PENDING", null,
                    "", "", null);

        }).collect(Collectors.toList());
    }

    @Transactional
    public void createNewDocument(Integer userId, DocumentType type, MultipartFile file){
        UserSIP user = userRepository.findById(userId).orElse(null);
        StudentProcess process = processService.getByStudentId(userId);

        String finalName = documentNaming.generateVersionedName(process, type);
        DocumentStatus docStatus = documentStatusRepository.findByDescription("PENDIENTE")
                .orElse(null);

        Document newDocument = Document.builder()
                .studentProcess(process)
                .user(user)
                .UploadDate(LocalDateTime.now())
                .URL(finalName)
                .documentType(type)
                .documentStatus(docStatus)
                .build();

        documentRepository.save(newDocument);
        fileStorage.store(file, finalName);
    }

    @Transactional
    public void updateDoc(Document currentDoc, String typeName, MultipartFile file) {
        if(!currentDoc.getDocumentType().getDescription().equals(typeName)){
            throw new ValidationException("Type for document not coincided.");
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

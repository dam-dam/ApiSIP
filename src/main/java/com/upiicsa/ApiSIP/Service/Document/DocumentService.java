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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentUtilsService utilsService;
    private final StudentProcessService processService;
    private final FileStorageService fileStorage;

    public DocumentService(DocumentRepository documentRepository,UserRepository userRepository,
                           DocumentUtilsService utilsService, StudentProcessService processService,
                           FileStorageService fileStorage) {

        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.utilsService = utilsService;
        this.processService = processService;
        this.fileStorage = fileStorage;
    }

    @Transactional(readOnly = true)
    public Document findDocByProcessAndType(StudentProcess process, String typeName){
        DocumentType type = utilsService.findTypeByDescription(typeName);

      /*  return documentRepository.findByStudentProcessAndDocumentTypeAndCancellationDateIsNull
                (process, type).orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "Recurso: Documento"));*/
        // Quitamos el .orElseThrow y ponemos .orElse(null)
        return documentRepository.findByStudentProcessAndDocumentTypeAndCancellationDateIsNull
                (process, type).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Document> findByProcessAndStatus(ProcessStatus status){
        return documentRepository.findByProcessAndCancelDateIsNull(status);
    }

    @Transactional
    public void saveDoc(MultipartFile file, String typeName, Integer userId) {
        StudentProcess process = processService.findByStudentId(userId);
        DocumentType type = utilsService.findTypeByDescription(typeName);

        Document doc = findDocByProcessAndType(process, typeName);

        if(doc != null){

            switch (doc.getDocumentStatus().getDescription()) {
                case "CORRECTO": throw new BusinessException(ErrorCode.DOCUMENT_ALREADY_APPROVED);
                case "INCORRECTO": cancelledAndCreated(doc, type, file, process);
                    break;
                case "PENDIENTE": updateDoc(doc, typeName, file);
                    break;
                default: throw new BusinessException(ErrorCode.INTERNAL_ERROR);
            }
        } else {
            createNewDocument(process, type, file);
        }
        if(process.getProcessStatus().getId() != 2){
            processService.updateStatus(process);
        }
    }

    @Transactional(readOnly = true)
    public DocumentStatusDto getLetter(Integer userId){
        StudentProcess process = processService.findByStudentId(userId);
        Document doc = findDocByProcessAndType(process, "CARTA_PRESENTACION");

        return new DocumentStatusDto(doc.getDocumentType().getDescription(), doc.getDocumentStatus().getDescription(),
                doc.getURL(), " ", "/view-document" + doc.getURL(), doc.getUploadDate());
    }
    @Transactional
    public void saveLetter(MultipartFile file, String enrollment, Integer userId) {
        UserSIP user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        createNewDocument(user, enrollment, file);
    }

    @Transactional(readOnly = true)
    public List<DocumentStatusDto> getDocuments(Integer userId, String processStatus) {
        StudentProcess process = processService.findByStudentId(userId);
        List<DocumentType> requiredTypes = utilsService.findRequiredTypesStatus(processStatus);

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
    public void createNewDocument(StudentProcess process, DocumentType type, MultipartFile file){
        DocumentStatus docStatus = utilsService.findStatusByDescription("PENDIENTE");

        Document newDocument = Document.builder()
                .studentProcess(process).user(process.getStudent())
                .uploadDate(LocalDateTime.now()).URL(fileStorage.store(file, process, type))
                .documentType(type).documentStatus(docStatus)
                .build();
        documentRepository.save(newDocument);
    }

    @Transactional
    public void createNewDocument(UserSIP user, String enrollment, MultipartFile file){
        DocumentStatus docStatus = utilsService.findStatusByDescription("CORRECTO");
        DocumentType docType = utilsService.findTypeByDescription("CARTA_PRESENTACION");

        Document newDocument = Document.builder()
                .studentProcess(null).user(user)
                .uploadDate(LocalDateTime.now()).URL(fileStorage.store(file, enrollment))
                .documentType(docType).documentStatus(docStatus)
                .build();
        documentRepository.save(newDocument);
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
        fileStorage.store(file, currentDoc);
    }

    @Transactional
    public void cancelledAndCreated(Document currentDoc, DocumentType type,  MultipartFile file,
                                    StudentProcess process) {
        currentDoc.setCancellationDate(LocalDateTime.now());
        documentRepository.save(currentDoc);
        createNewDocument(process, type, file);
    }
}

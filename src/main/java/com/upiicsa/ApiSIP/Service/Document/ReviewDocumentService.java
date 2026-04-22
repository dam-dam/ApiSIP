package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Dto.Document.ReviewDocumentDto;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentReview;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentStatus;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentReviewRepository;
import com.upiicsa.ApiSIP.Service.StudentService;
import com.upiicsa.ApiSIP.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ReviewDocumentService {

    public final DocumentReviewRepository documentReviewRepository;
    public final UserService userService;
    public final StudentService studentService;
    public final StudentProcessService processService;
    public final DocumentPersistenceService documentPersistence;
    public final DocumentUtilsService utilsService;

    public ReviewDocumentService(DocumentReviewRepository documentReviewRepository, UserService userService,
                    StudentService studentService, StudentProcessService processService,
                    DocumentPersistenceService documentPersistence, DocumentUtilsService utilsService) {
        this.documentReviewRepository = documentReviewRepository;
        this.userService = userService;
        this.studentService = studentService;
        this.processService = processService;
        this.documentPersistence = documentPersistence;
        this.utilsService = utilsService;
    }

    @Transactional
    public void save(Document document, UserSIP user, Boolean approved, String comment) {
        String statusDescription = approved ? "CORRECTO" : "INCORRECTO";

        DocumentStatus newStatus = utilsService.findStatusByDescription(statusDescription);
        boolean alreadyExists = documentReviewRepository.existsById(document.getId());

        if (!alreadyExists) {
            documentPersistence.changeStatus(document, newStatus);

            DocumentReview newReview = DocumentReview.builder()
                    .document(document)
                    .user(user)
                    .approved(approved)
                    .comment(comment)
                    .reviewDate(LocalDateTime.now())
                    .build();
            documentReviewRepository.save(newReview);

            log.info("Operador ID [{}] reviso el documento ID [{}] (Tipo: {}) como [{}] - comentario: '{}'",
                    user.getId(), document.getId(), document.getDocumentType().getDescription(),
                    document.getDocumentStatus().getDescription(), comment);
        }
    }

    @Transactional
    public void performReview(String enrollment, List<ReviewDocumentDto> reviewsDto, Integer userId) {
        StudentProcess process = processService.findByEnrollment(enrollment);
        UserSIP user = userService.getUserById(userId);

        log.info("Operador ID [{}] inicio revisión de documentos para la matrícula [{}]", userId, enrollment);

        for (ReviewDocumentDto dto : reviewsDto) {
            Document doc = documentPersistence.findDocByProcessAndType(process, dto.typeName());

            if(doc != null) {
                save(doc, user, dto.approved(), dto.comment());
            }
        }
        processService.validateUpdateStatus(process, documentPersistence.findByProcessAndStatus(process));
        log.info("Operador ID [{}] finalizo la revisión para la matrícula [{}]", userId, enrollment);
    }
}

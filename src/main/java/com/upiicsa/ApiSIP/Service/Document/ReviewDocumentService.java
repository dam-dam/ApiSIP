package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentReview;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentStatus;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewDocumentService {

    public final DocumentReviewRepository documentReviewRepository;
    public final DocumentRepository documentRepository;
    public final DocumentUtilsService utilsService;

    public ReviewDocumentService(DocumentReviewRepository documentReviewRepository,
                DocumentRepository documentRepository, DocumentUtilsService utilsService) {
        this.documentReviewRepository = documentReviewRepository;
        this.documentRepository = documentRepository;
        this.utilsService = utilsService;
    }

    @Transactional
    public void save(Document document, UserSIP user, Boolean approved, String comment) {
        String statusDescription = approved ? "CORRECTO" : "INCORRECTO";

        DocumentStatus newStatus = utilsService.getStatusByDescription(statusDescription);
        boolean alreadyExists = documentReviewRepository.existsById(document.getId());

        if (!alreadyExists) {
            document.setDocumentStatus(newStatus);
            documentRepository.save(document);

            DocumentReview newReview = DocumentReview.builder()
                    .document(document)
                    .user(user)
                    .approved(approved)
                    .comment(comment)
                    .reviewDate(LocalDateTime.now())
                    .build();

            documentReviewRepository.save(newReview);
        }
    }
}

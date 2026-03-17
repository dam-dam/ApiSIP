package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentReview;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewDocumentService {

    public final DocumentReviewRepository documentReviewRepository;

    public ReviewDocumentService(DocumentReviewRepository documentReviewRepository) {
        this.documentReviewRepository = documentReviewRepository;
    }

    @Transactional
    public void save(Document document, UserSIP user, Boolean approved, String comment) {

        DocumentReview review = DocumentReview.builder()
                .idDocument(document.getId())
                .document(document)
                .user(user)
                .reviewDate(LocalDateTime.now())
                .approved(approved)
                .comment(comment).build();

        documentReviewRepository.save(review);
    }
}

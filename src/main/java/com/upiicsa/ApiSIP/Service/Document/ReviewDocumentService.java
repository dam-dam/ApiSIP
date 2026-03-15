package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Model.Document_Process.DocumentReview;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewDocumentService {

    public final DocumentReviewRepository documentReviewRepository;

    public ReviewDocumentService(DocumentReviewRepository documentReviewRepository) {
        this.documentReviewRepository = documentReviewRepository;
    }

    @Transactional
    public void save(DocumentReview documentReview) {
        documentReviewRepository.save(documentReview);
    }
}

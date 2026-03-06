package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Model.Document_Process.ReviewDocument;
import com.upiicsa.ApiSIP.Repository.Document_Process.ReviewDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewDocumentService {

    public final ReviewDocumentRepository reviewDocumentRepository;

    public ReviewDocumentService(ReviewDocumentRepository reviewDocumentRepository) {
        this.reviewDocumentRepository = reviewDocumentRepository;
    }

    @Transactional
    public void save(ReviewDocument reviewDocument) {
        reviewDocumentRepository.save(reviewDocument);
    }
}

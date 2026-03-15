package com.upiicsa.ApiSIP.Repository.Document_Process;

import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentReviewRepository extends JpaRepository<DocumentReview, Integer> {

    DocumentReview findByDocument(Document document);
}

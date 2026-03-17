package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Model.Catalogs.DocumentStatus;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentReview;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentProcessRepository;
import com.upiicsa.ApiSIP.Repository.Catalogs.DocumentTypeRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentReviewRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentUtilsService {

    private DocumentTypeRepository typeRepository;
    private DocumentProcessRepository docProcessRepository;
    private DocumentStatusRepository statusRepository;
    private DocumentReviewRepository reviewRepository;

    public List<DocumentType> getRequiredTypesByProcess(StudentProcess process){
        return docProcessRepository.findDocumentTypesByProcessState(process.getProcessStatus());
    }

    public DocumentType getTypeByDescription(String typeName){
        return typeRepository.findByDescription(typeName).orElse(null);
    }

    public DocumentStatus getStatusByDescription(String description){
        return statusRepository.findByDescription(description).orElse(null);
    }

    public DocumentReview getReviewByDescription(Document doc){
        return reviewRepository.findByDocument(doc);
    }
}

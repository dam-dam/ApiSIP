package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Dto.Document.DocumentStatusDto;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentStatus;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.DocumentReview;
import com.upiicsa.ApiSIP.Repository.Catalogs.ProcessStatusRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentProcessRepository;
import com.upiicsa.ApiSIP.Repository.Catalogs.DocumentTypeRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentReviewRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentStatusRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentUtilsService {

    private DocumentTypeRepository typeRepository;
    private DocumentProcessRepository docProcessRepository;
    private ProcessStatusRepository processStatusRepository;
    private DocumentStatusRepository statusRepository;
    private DocumentReviewRepository reviewRepository;

    public DocumentUtilsService(DocumentTypeRepository typeRepository, DocumentProcessRepository docProcessRepository,
                                ProcessStatusRepository processStatusRepository, DocumentStatusRepository statusRepository,
                                DocumentReviewRepository reviewRepository) {
        this.typeRepository = typeRepository;
        this.docProcessRepository = docProcessRepository;
        this.processStatusRepository = processStatusRepository;
        this.statusRepository = statusRepository;
        this.reviewRepository = reviewRepository;

    }

    public List<DocumentType> getRequiredTypesStatus(String processStatus) {
        ProcessStatus status = processStatusRepository.findByDescription(processStatus)
                .orElseThrow(() -> new RuntimeException("Process Status Not Found"));

        return docProcessRepository.findDocumentTypesByProcessStatus(status);
    }

    public DocumentType getTypeByDescription(String typeName){
        return typeRepository.findByDescription(typeName).orElse(null);
    }

    public DocumentStatus getStatusByDescription(String description){
        return statusRepository.findByDescription(description).orElse(null);
    }

    public List<DocumentStatus> getListOfStatus(List<String> statusList){
        List<DocumentStatus> docStatusList = new ArrayList<>();
        for(String status : statusList){
            docStatusList.add(statusRepository.findByDescription(status).orElse(null));
        }
        return docStatusList;
    }

    public DocumentReview getReviewByDocument(Document doc){
        return reviewRepository.findByDocument(doc);
    }

    public DocumentStatusDto generateDto(DocumentType type, Document doc, DocumentReview docReview){

        if(docReview == null && doc == null){
            return new DocumentStatusDto(type.getDescription(), "SIN_CARGA", null,
                    "", "", null);
        } else if(docReview == null && doc != null){
            return new DocumentStatusDto(type.getDescription(), doc.getDocumentStatus().getDescription(),
                    doc.getURL(), "", "/view-document/" + doc.getURL(),
                    doc.getUploadDate());
        } else if(docReview != null && doc != null){
            return new DocumentStatusDto(type.getDescription(), doc.getDocumentStatus().getDescription(),
                    doc.getURL(), docReview.getComment(), "/view-document/" + doc.getURL(),
                    doc.getUploadDate());
        }else {
            throw new RuntimeException("Entities Not Found");
        }
    }
}

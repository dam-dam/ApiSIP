package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentProcessRepository;
import com.upiicsa.ApiSIP.Repository.Catalogs.DocumentTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentTypeService {

    private DocumentTypeRepository typeRepository;
    private DocumentProcessRepository docProcessRepository;

    public DocumentTypeService (DocumentTypeRepository typeRepository,
                                DocumentProcessRepository docProcessRepository) {
        this.typeRepository = typeRepository;
        this.docProcessRepository = docProcessRepository;
    }

    public List<DocumentType> getRequiredTypes(StudentProcess process){
        return docProcessRepository.findDocumentTypesByProcessState(process.getProcessStatus());
    }

    public DocumentType getByDescription(String typeName){
        return typeRepository.findByDescription(typeName).orElse(null);
    }

}

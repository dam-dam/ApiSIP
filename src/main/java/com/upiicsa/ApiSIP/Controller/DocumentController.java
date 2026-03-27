package com.upiicsa.ApiSIP.Controller;

import com.upiicsa.ApiSIP.Dto.Document.DocumentStatusDto;
import com.upiicsa.ApiSIP.Service.Document.DocumentService;
import com.upiicsa.ApiSIP.Utils.AuthHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/my-status")
    @PreAuthorize("hasAnyRole('ALUMNO')")
    public ResponseEntity<List<DocumentStatusDto>> getMyStatus(@RequestParam String processStatus) {

        return ResponseEntity.ok(documentService.getDocuments(getUserId(), processStatus));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ALUMNO')")
    public ResponseEntity<String> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type){

        documentService.saveDoc(file, type, getUserId());
        return ResponseEntity.ok().body("Uploaded successfully");
    }

    private Integer getUserId(){
        return AuthHelper.getAuthenticatedUserId();
    }
}

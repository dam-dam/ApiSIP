package com.upiicsa.ApiSIP.Controller;

import com.upiicsa.ApiSIP.Dto.Document.DocumentStatusDto;
import com.upiicsa.ApiSIP.Dto.Document.ReviewDocumentDto;
import com.upiicsa.ApiSIP.Service.Document.DocumentService;
import com.upiicsa.ApiSIP.Service.Document.ReviewDocumentService;
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
    private ReviewDocumentService reviewService;

    public DocumentController(DocumentService documentService,  ReviewDocumentService reviewService) {
        this.documentService = documentService;
        this.reviewService = reviewService;
    }

    @GetMapping("/my-status")
    @PreAuthorize("hasAnyRole('ALUMNO')")
    public ResponseEntity<List<DocumentStatusDto>> getMyStatus(@RequestParam String processStatus) {

        return ResponseEntity.ok(documentService.getDocuments(getUserId(), processStatus));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ALUMNO')")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file,
            @RequestParam("type") String type){

        documentService.saveDoc(file, type, getUserId());
        return ResponseEntity.ok().body("Uploaded successfully");
    }

    @PostMapping("/uploadLetter")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<String> uploadDocumentLetter(@RequestParam("file") MultipartFile file,
                                  @RequestParam("enrollment") String enrollment){
        documentService.saveLetter(file, enrollment, getUserId());
        return ResponseEntity.ok().body("Uploaded successfully");
    }

    @PostMapping("/review")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<Boolean> reviewedDocument(@RequestParam String enrollment,
                                                    @RequestBody List<ReviewDocumentDto> reviewsDto) {
        Integer userId = AuthHelper.getAuthenticatedUserId();
        reviewService.performReview(enrollment, reviewsDto, userId);

        return ResponseEntity.ok(true);
    }

    private Integer getUserId(){
        return AuthHelper.getAuthenticatedUserId();
    }
}

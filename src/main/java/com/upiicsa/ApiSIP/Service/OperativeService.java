package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.Data.DashboardStatsDto;
import com.upiicsa.ApiSIP.Dto.Document.DocumentStatusDto;
import com.upiicsa.ApiSIP.Dto.Document.ReviewDocumentDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentReviewDto;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.StudentRepository;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import com.upiicsa.ApiSIP.Service.Document.DocumentService;
import com.upiicsa.ApiSIP.Service.Document.ReviewDocumentService;
import com.upiicsa.ApiSIP.Service.Document.StudentProcessService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OperativeService {

    private StudentRepository studentRepository;
    private StudentProcessService processService;
    private UserRepository userRepository;
    private DocumentService documentService;
    private ReviewDocumentService reviewService;

    public OperativeService(StudentRepository studentRepository, StudentProcessService processService,
                            UserRepository userRepository, DocumentService documentService,
                            ReviewDocumentService reviewService) {
        this.studentRepository = studentRepository;
        this.processService = processService;
        this.userRepository = userRepository;
        this.documentService = documentService;
        this.reviewService = reviewService;
    }

    public DashboardStatsDto getStats(String careerAcronym, String planCode) {
        Page<Student> filteredStudents = studentRepository.findFiltered("", careerAcronym, planCode, Pageable.unpaged());
        List<Student> students = filteredStudents.getContent();

        Map<String, Long> counts = students.stream()
                .map(student -> processService.findByStudentId(student.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.groupingBy(
                        (StudentProcess process) -> process.getProcessStatus().getDescription(),
                        Collectors.counting()
                ));

        return new DashboardStatsDto(
                (int) filteredStudents.getTotalElements(),
                counts.getOrDefault("Registrado", 0L).intValue(),
                counts.getOrDefault("Doc Inicial", 0L).intValue(),
                counts.getOrDefault("Carta Aceptacion", 0L).intValue(),
                counts.getOrDefault("Doc Final", 0L).intValue()
        );
    }

    public StudentReviewDto getReview(String enrollment, String processStatus){
        Student student = studentRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        String semester;
        if(student.isGraduate()){
            semester = "PASANTE";
        }else {
            semester =  student.getSemester().getDescription();
        }

        String fullName =  student.getName() + " " + student.getFLastName() + " " + student.getMLastName();
        List<DocumentStatusDto> documents = documentService.getDocuments(student.getId(), processStatus);

        return new StudentReviewDto(fullName, student.getEnrollment(), student.getOffer().getCareer().getName(),
                semester, student.getOffer().getSyllabus().code, documents);
    }

    @Transactional
    public void performReview(String enrollment, List<ReviewDocumentDto> reviewsDto, Integer userId) {
        StudentProcess process = processService.findByEnrollment(enrollment)
                .orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado"));
        UserSIP user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        for (ReviewDocumentDto dto : reviewsDto) {
            Document doc = documentService.getDocByProcessAndType(process, dto.typeName())
                    .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado: " + dto.typeName()));

            reviewService.save(doc, user, dto.approved(), dto.comment());
        }
        processService.validateUpdateStatus(process, documentService.findByProcessAndStatus(process.getProcessStatus()));
    }
}

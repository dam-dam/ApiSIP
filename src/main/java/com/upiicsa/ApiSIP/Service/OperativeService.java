package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.DashboardStatsDto;
import com.upiicsa.ApiSIP.Dto.Document.DocumentStatusDto;
import com.upiicsa.ApiSIP.Dto.ReviewDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentReviewDto;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.Document_Process.StudentProcessRepository;
import com.upiicsa.ApiSIP.Repository.StudentRepository;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import com.upiicsa.ApiSIP.Service.Document.DocumentService;
import com.upiicsa.ApiSIP.Service.Document.ReviewDocumentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OperativeService {

    private StudentRepository studentRepository;
    private StudentProcessRepository processRepository;
    private UserRepository userRepository;
    private DocumentService documentService;
    private ReviewDocumentService reviewService;

    public OperativeService(StudentRepository studentRepository, StudentProcessRepository processRepository,
                            UserRepository userRepository, DocumentService documentService,
                            ReviewDocumentService reviewService) {
        this.studentRepository = studentRepository;
        this.processRepository = processRepository;
        this.userRepository = userRepository;
        this.documentService = documentService;
        this.reviewService = reviewService;
    }

    public DashboardStatsDto getStats(String careerAcronym){
        List<Student> students;

        if(careerAcronym.equals("all")){
            students = studentRepository.findAll();
        }else {
            students = studentRepository.findAllByCareerAcronym(careerAcronym);
        }

        Map<String, Long> counts = students.stream()
                .map(student -> processRepository.findByStudentIdAndReasonLeavingIsNull
                        (student.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        process -> process.getProcessStatus().getDescription(),
                        Collectors.counting()
                ));

        return new DashboardStatsDto(
                students.size(),
                counts.getOrDefault("Registrado", 0L).intValue(),
                counts.getOrDefault("Doc Inicial", 0L).intValue(),
                counts.getOrDefault("Carta Aceptacion", 0L).intValue(),
                counts.getOrDefault("Doc Final", 0L).intValue()
        );
    }

    public StudentReviewDto getReview(String enrollment){
        Student student = studentRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        String semester;
        if(student.isGraduate()){
            semester = "PASANTE";
        }else {
            semester =  student.getSemester().getDescription();
        }

        String fullName =  student.getName() + " " + student.getFLastName() + " " + student.getMLastName();
        List<DocumentStatusDto> documents = documentService.getDocuments(student.getId());

        return new StudentReviewDto(fullName, student.getEnrollment(), student.getOffer().getCareer().getName(),
                semester, student.getOffer().getSyllabus().code, documents);
    }

    @Transactional
    public void performReview(String enrollment, List<ReviewDto> reviewsDto, Integer userId) {
        StudentProcess process = processRepository.findByStudentEnrollmentAndReasonLeavingIsNull(enrollment)
                .orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado"));
        UserSIP user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        for (ReviewDto dto : reviewsDto) {
            Document doc = documentService.getDocByProcessAndDocumentType(process, dto.typeName())
                    .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado: " + dto.typeName()));

            reviewService.save(doc, user, dto.approved(), dto.comment());
        }
    }
}

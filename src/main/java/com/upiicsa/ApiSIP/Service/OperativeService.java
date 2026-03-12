package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.DashboardStatsDto;
import com.upiicsa.ApiSIP.Dto.Document.DocumentStatusDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentReviewDto;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Repository.Document_Process.StudentProcessRepository;
import com.upiicsa.ApiSIP.Repository.StudentRepository;
import com.upiicsa.ApiSIP.Service.Document.DocumentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OperativeService {

    private StudentRepository studentRepository;
    private StudentProcessRepository processRepository;
    private DocumentService documentService;

    public OperativeService(StudentRepository studentRepository, StudentProcessRepository processRepository,
                            DocumentService documentService) {
        this.studentRepository = studentRepository;
        this.processRepository = processRepository;
        this.documentService = documentService;
    }

    public DashboardStatsDto getStats(String careerAcronym){
        List<Student> students;

        if(careerAcronym.equals("all")){
            students = studentRepository.findAll();
        }else {
            students = studentRepository.findAllByCareerAcronym(careerAcronym);
        }

        Map<String, Long> counts = students.stream()
                .map(student -> processRepository.findByActiveIsTrueAndStudentId(student.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        process -> process.getProcessState().getDescription(),
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
}

package com.upiicsa.ApiSIP.Controller;

import com.upiicsa.ApiSIP.Dto.DashboardStatsDto;
import com.upiicsa.ApiSIP.Dto.ReviewDto;
import com.upiicsa.ApiSIP.Dto.Student.ResponseStudentDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentReviewDto;
import com.upiicsa.ApiSIP.Service.OperativeService;
import com.upiicsa.ApiSIP.Service.StudentService;
import com.upiicsa.ApiSIP.Utils.AuthHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operatives")
public class OperativeController {

    private StudentService studentService;
    private OperativeService operativeService;

    public OperativeController(StudentService studentService, OperativeService operativeService) {
        this.studentService = studentService;
        this.operativeService = operativeService;
    }

    //lo comento dam por si no jalaba el que el puso
    /*@GetMapping("/get-allStudents")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<Page<ResponseStudentDto>> getAllStudents(Pageable pageable) {
        Page<ResponseStudentDto> students = studentService.getStudents(pageable);

        return ResponseEntity.ok(students);
    }*/
    /*
    @GetMapping("/get-allStudents")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<Page<ResponseStudentDto>> getAllStudents(
            Pageable pageable,
            @RequestParam(required = false) String search
    ) {

        // Ahora llamamos al nuevo método del Service que acepta el 'search'
        Page<ResponseStudentDto> students = studentService.getAllStudents(search, pageable);

        return ResponseEntity.ok(students);
    }*/
    @GetMapping("/get-allStudents")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<Page<ResponseStudentDto>> getAllStudents(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String career,
            @RequestParam(defaultValue = "all") String plan) {

        return ResponseEntity.ok(studentService.getAllStudents(search, career, plan, pageable));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<DashboardStatsDto> getStats(
            @RequestParam(defaultValue = "all") String careerAcronym,
            @RequestParam(defaultValue = "all") String planCode) {// agregado por dam


        return ResponseEntity.ok(operativeService.getStats(careerAcronym, planCode));
    }

    @GetMapping("/student-review")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StudentReviewDto> getStudentReview(@RequestParam String enrollment) {

        return ResponseEntity.ok(operativeService.getReview(enrollment));
    }

    @PostMapping("/review-document")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<Boolean> reviewedDocument(@RequestParam String enrollment,
                                                    @RequestBody List<ReviewDto> reviewDto) {
        Integer userId = AuthHelper.getAuthenticatedUserId();
        operativeService.performReview(enrollment, reviewDto, userId);

        return ResponseEntity.ok(true);
    }

}

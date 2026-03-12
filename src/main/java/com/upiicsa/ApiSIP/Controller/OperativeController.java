package com.upiicsa.ApiSIP.Controller;

import com.upiicsa.ApiSIP.Dto.DashboardStatsDto;
import com.upiicsa.ApiSIP.Dto.Student.ResponseStudentDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentReviewDto;
import com.upiicsa.ApiSIP.Service.OperativeService;
import com.upiicsa.ApiSIP.Service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operatives")
public class OperativeController {

    private StudentService studentService;
    private OperativeService operativeService;

    public OperativeController(StudentService studentService, OperativeService operativeService) {
        this.studentService = studentService;
        this.operativeService = operativeService;
    }

    @RequestMapping("/get-allStudents")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<Page<ResponseStudentDto>> getAllStudents(Pageable pageable) {
        Page<ResponseStudentDto> students = studentService.getStudents(pageable);

        return ResponseEntity.ok(students);
    }

    @RequestMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<DashboardStatsDto> getStats(@RequestParam String careerAcronym) {
        return ResponseEntity.ok(operativeService.getStats(careerAcronym));
    }

    @RequestMapping("/student-review")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StudentReviewDto> getStudentReview(@RequestParam String enrollment) {

        return ResponseEntity.ok(operativeService.getReview(enrollment));
    }

}

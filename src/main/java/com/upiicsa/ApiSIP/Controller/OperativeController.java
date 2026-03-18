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

@RestController
@RequestMapping("/operatives")
public class OperativeController {

    private StudentService studentService;
    private OperativeService operativeService;

    public OperativeController(StudentService studentService, OperativeService operativeService) {
        this.studentService = studentService;
        this.operativeService = operativeService;
    }

    @GetMapping("/get-allStudents")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<Page<ResponseStudentDto>> getAllStudents(Pageable pageable) {
        Page<ResponseStudentDto> students = studentService.getStudents(pageable);

        return ResponseEntity.ok(students);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<DashboardStatsDto> getStats(@RequestParam String careerAcronym) {
        return ResponseEntity.ok(operativeService.getStats(careerAcronym));
    }

    @GetMapping("/student-review")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StudentReviewDto> getStudentReview(@RequestParam String enrollment) {

        return ResponseEntity.ok(operativeService.getReview(enrollment));
    }

    @PostMapping("/reviewDocument")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<Boolean> reviewedDocument(@RequestParam String enrollment, @RequestBody ReviewDto reviewDto) {
        Integer userId = AuthHelper.getAuthenticatedUserId();
        operativeService.performReview(enrollment, reviewDto, userId);

        return ResponseEntity.ok(true);
    }

}

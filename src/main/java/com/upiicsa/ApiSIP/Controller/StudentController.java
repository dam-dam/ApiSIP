package com.upiicsa.ApiSIP.Controller;

import com.upiicsa.ApiSIP.Dto.Data.DashboardStatsDto;
import com.upiicsa.ApiSIP.Dto.Data.ProcessProgressDto;
import com.upiicsa.ApiSIP.Dto.Student.ResponseStudentDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentRegistrationDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentReviewDto;
import com.upiicsa.ApiSIP.Dto.User.DataDto;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Service.Document.StudentProcessService;
import com.upiicsa.ApiSIP.Service.StudentService;
import com.upiicsa.ApiSIP.Utils.AuthHelper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/students")
public class StudentController {

    private StudentService studentService;
    private StudentProcessService processService;

    public StudentController(StudentService studentService, StudentProcessService processService) {
        this.studentService = studentService;
        this.processService = processService;
    }

    @GetMapping("/filtered")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<Page<ResponseStudentDto>> getAllStudents(Pageable pageable,
              @RequestParam(required = false) String search, @RequestParam(defaultValue = "all") String career,
              @RequestParam(defaultValue = "all") String plan) {

        Page<ResponseStudentDto> students = studentService.getStudentsByFilters(search, career, plan, pageable);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/data")
    @PreAuthorize("hasAnyRole('ALUMNO')")
    public ResponseEntity<DataDto> getStudentData(){
        Integer studentId = AuthHelper.getAuthenticatedUserId();

        return ResponseEntity.ok(studentService.getDataForStudent(studentId));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid StudentRegistrationDto registrationDto) {

        Student student = studentService.registerStudent(registrationDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(student.getId()).toUri();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/process-status")
    @PreAuthorize("hasAnyRole('ALUMNO')")
    public ResponseEntity<List<ProcessProgressDto>> getProcessStatus() {
        return ResponseEntity.ok(processService.
                getProcessHistory(AuthHelper.getAuthenticatedUserId()));
    }

    @GetMapping("/toReview")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<StudentReviewDto> getStudentReview(
            @RequestParam String enrollment,
            @RequestParam String processStatus) {

        log.info("Operador ID [{}] consultó la informacion de la matrícula [{}] para revisión en estado [{}]",
                AuthHelper.getAuthenticatedUserId(), enrollment, processStatus);

        return ResponseEntity.ok(studentService.dataToReview(enrollment, processStatus));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<DashboardStatsDto> getStats(
            @RequestParam(defaultValue = "all") String careerAcronym,
            @RequestParam(defaultValue = "all") String planCode) {

        return ResponseEntity.ok(processService.getStats(careerAcronym, planCode));
    }
}

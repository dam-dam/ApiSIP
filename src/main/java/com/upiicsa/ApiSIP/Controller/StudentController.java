package com.upiicsa.ApiSIP.Controller;

import com.upiicsa.ApiSIP.Dto.Data.ProcessProgressDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentRegistrationDto;
import com.upiicsa.ApiSIP.Dto.User.DataDto;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Service.Document.StudentProcessService;
import com.upiicsa.ApiSIP.Service.StudentService;
import com.upiicsa.ApiSIP.Utils.AuthHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private StudentService studentService;
    private StudentProcessService studentProcessService;

    public StudentController(StudentService studentService, StudentProcessService studentProcessService) {
        this.studentService = studentService;
        this.studentProcessService = studentProcessService;
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
        return ResponseEntity.ok(studentProcessService.
                getProcessHistory(AuthHelper.getAuthenticatedUserId()));
    }
}

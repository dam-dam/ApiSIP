package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.DashboardStatsDto;
import com.upiicsa.ApiSIP.Dto.Student.ResponseStudentDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentRegistrationDto;
import com.upiicsa.ApiSIP.Exception.ValidationException;
import com.upiicsa.ApiSIP.Model.Address;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Repository.*;
import com.upiicsa.ApiSIP.Service.Auth.EmailVerificationService;
import com.upiicsa.ApiSIP.Service.Document.StudentProcessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StudentService {

    private StudentRepository studentRepository;
    private PasswordEncoder passwordEncoder;
    private EmailVerificationService verificationService;
    private CatalogsService catalogsService;
    private StudentProcessService processService;


    public StudentService (StudentRepository studentRepository, PasswordEncoder passwordEncoder,
                           EmailVerificationService verificationService, CatalogsService catalogsService,
                           StudentProcessService processService) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.catalogsService = catalogsService;
        this.processService = processService;
    }

    public Optional<Student> getStudentById(Integer id) {
        return studentRepository.findById(id);
    }

    public void setAddress(Student student, Address address) {
        student.setAddress(address);
        studentRepository.save(student);
    }

    public Page<ResponseStudentDto> getStudents(Pageable pageable) {
        Page<Student> studentPage = studentRepository.findAll(pageable);

        return studentPage.map(student -> new ResponseStudentDto(
                student.getName(), student.getFLastName(), student.getMLastName(),
                student.getEnrollment(), student.getOffer()
        ));
    }

    @Transactional
    public Student registerStudent(StudentRegistrationDto registrationDto) {
        if (!registrationDto.password().equals(registrationDto.confirmPassword()))
            throw new ValidationException("Invalid password");

        Student newStudent = Student.builder()
                .email(registrationDto.email())
                .password(passwordEncoder.encode(registrationDto.password()))
                .fLastName(registrationDto.fLastName()).mLastName(registrationDto.mLastName())
                .name(registrationDto.name())
                .enabled(false).registrationDate(LocalDateTime.now())
                .userType(catalogsService.getType("ALUMNO"))
                .status(catalogsService.getStatus("ACTIVO"))
                .enrollment(registrationDto.enrollment()).phone(registrationDto.phone())
                .semester(catalogsService.getSemester(registrationDto.semester(), registrationDto.graduated()))
                .graduate(registrationDto.graduated())
                .offer(catalogsService.getOffer(registrationDto.schoolName(), registrationDto.acronymCareer(),
                        registrationDto.syllabusCode())).build();

        studentRepository.save(newStudent);
        verificationService.createAndSendConfirmationCode(newStudent);
        processService.setFirstState(newStudent);

        return newStudent;
    }

    // lo hizo daaaam, es para buscar en la bd, aver si jala
    /*public Page<ResponseStudentDto> getAllStudents(String search, Pageable pageable) {
        Page<Student> students;

        // LÓGICA: Si el buscador trae texto, filtramos. Si no, traemos todo.
        if (search != null && !search.trim().isEmpty()) {
            students = studentRepository.findAllWithSearch(search, pageable);
        } else {
            students = studentRepository.findAll(pageable);
        }

        // Usamos la misma lógica de conversión que ya tienes arriba
        return students.map(student -> new ResponseStudentDto(
                student.getName(),
                student.getFLastName(),
                student.getMLastName(),
                student.getEnrollment(),
                student.getOffer()
        ));
    }*/

    public Page<ResponseStudentDto> getAllStudents(String search, String career, String plan, Pageable pageable) {
        // IMPORTANTE: Que el nombre y los parámetros coincidan con el Controller
        Page<Student> students = studentRepository.findFiltered(search, career, plan, pageable);

        return students.map(student -> new ResponseStudentDto(
                student.getName(),
                student.getFLastName(),
                student.getMLastName(),
                student.getEnrollment(),
                student.getOffer()
        ));
    }

}

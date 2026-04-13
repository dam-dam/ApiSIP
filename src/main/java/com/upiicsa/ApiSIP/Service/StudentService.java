package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.Document.DocumentStatusDto;
import com.upiicsa.ApiSIP.Dto.Student.InfoInstitutionalDto;
import com.upiicsa.ApiSIP.Dto.Student.ResponseStudentDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentRegistrationDto;
import com.upiicsa.ApiSIP.Dto.Student.StudentReviewDto;
import com.upiicsa.ApiSIP.Dto.User.DataDto;
import com.upiicsa.ApiSIP.Exception.BusinessException;
import com.upiicsa.ApiSIP.Model.Address;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.Enum.ErrorCode;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Repository.*;
import com.upiicsa.ApiSIP.Service.Auth.EmailVerificationService;
import com.upiicsa.ApiSIP.Service.Document.DocumentService;
import com.upiicsa.ApiSIP.Service.Document.StudentProcessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentService {

    private StudentRepository studentRepository;
    private PasswordEncoder passwordEncoder;
    private EmailVerificationService verificationService;
    private CatalogsService catalogsService;
    private StudentProcessService processService;
    private DocumentService documentService;

    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder,
                          EmailVerificationService verificationService, CatalogsService catalogsService,
                          StudentProcessService processService,  DocumentService documentService) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.catalogsService = catalogsService;
        this.processService = processService;
        this.documentService = documentService;
    }

    @Transactional(readOnly = true)
    public Student getStudentById(Integer id) {
        return studentRepository.findById(id).
                orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Student getStudentByEnrollment(String enrollment) {
        return studentRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Recurso: Alumno con boleta: " + enrollment));
    }

    @Transactional(readOnly = true)
    public DataDto getDataForStudent(Integer id) {
        Student student = getStudentById(id);
        StudentProcess process = processService.findByStudentId(student.getId());

        return new DataDto(student.getName(), student.getFLastName(), student.getMLastName(),
                student.getEmail(), new InfoInstitutionalDto(student, process));
    }

    @Transactional(readOnly = true)
    public Page<ResponseStudentDto> getStudentsByFilters(String search, String career, String plan, Pageable pageable) {
        Page<Student> students = studentRepository.findFiltered(search, career, plan, pageable);

        return students.map(student -> {

            String status = processService.findByStudentId(student.getId()).getProcessStatus().getDescription();

            return new ResponseStudentDto(
                    student.getName(),
                    student.getFLastName(),
                    student.getMLastName(),
                    student.getEnrollment(),
                    student.getOffer().getSyllabus().getCode(),
                    status
            );
        });
    }

    @Transactional
    public void setAddress(Student student, Address address) {
        student.setAddress(address);
        studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public StudentReviewDto dataToReview(String enrollment, String processStatus){
        Student student = this.getStudentByEnrollment(enrollment);

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
    public Student registerStudent(StudentRegistrationDto registrationDto) {
        if (!registrationDto.password().equals(registrationDto.confirmPassword()))
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);

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
}

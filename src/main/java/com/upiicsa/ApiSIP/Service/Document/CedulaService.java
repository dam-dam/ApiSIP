package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Dto.Cedula.AddressDto;
import com.upiicsa.ApiSIP.Dto.Cedula.CedulaDto;
import com.upiicsa.ApiSIP.Dto.Cedula.CompanyDto;
import com.upiicsa.ApiSIP.Model.Address;
import com.upiicsa.ApiSIP.Model.Company;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Repository.*;
import com.upiicsa.ApiSIP.Repository.Document_Process.StudentProcessRepository;
import com.upiicsa.ApiSIP.Service.AddressService;
import com.upiicsa.ApiSIP.Service.CompanyService;
import com.upiicsa.ApiSIP.Service.Infrastructure.PdfService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CedulaService {

    private StudentRepository studentRepository;
    private StudentProcessRepository processRepository;
    private AddressService addressService;
    private CompanyService companyService;
    private PdfService pdfService;

    public CedulaService(StudentRepository sRepository, StudentProcessRepository pRepository,
                         AddressService addressService, CompanyService companyService, PdfService pdfService) {
        this.studentRepository = sRepository;
        this.processRepository = pRepository;
        this.addressService = addressService;
        this.companyService = companyService;
        this.pdfService = pdfService;
    }

    public CedulaDto getAllData(Integer studentId){
        Student student =  studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        AddressDto studentAddress = null;
        CompanyDto company = null;
        AddressDto companyAddress = null;

        if(student.getAddress()!=null){
            studentAddress = new AddressDto(student.getAddress());
        }

        StudentProcess studentProcess = processRepository.findByStudentIdAndReasonLeavingIsNull(student.getId())
                .orElseThrow(() -> new EntityNotFoundException("StudentProcess not found"));

        if(studentProcess.getCompany()!=null){
            company = new CompanyDto(studentProcess.getCompany());
            companyAddress =  new AddressDto(studentProcess.getCompany().getAddress());
        }

        return new CedulaDto(studentAddress, company, companyAddress);
    }

    public String generateCedula(Integer studentId, CedulaDto cedulaDto) {

        Address studentAddress;
        Address companyAddress;
        Company company;

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        if(student.getAddress() == null){
            studentAddress = addressService.createAddress(cedulaDto.studentAddress());

            student.setAddress(studentAddress);
            studentRepository.save(student);
        }else {
            studentAddress = addressService.updateAddress(student.getAddress().getId(), cedulaDto.studentAddress());
        }

        StudentProcess studentProcess = processRepository.findByStudentIdAndReasonLeavingIsNull(student.getId())
                .orElseThrow(() -> new EntityNotFoundException("StudentProcess not found"));

        if(studentProcess.getCompany() == null){

            companyAddress = addressService.createAddress(cedulaDto.companyAddress());
            company = companyService.createCompany(cedulaDto.companyInfo(), companyAddress);
            studentProcess.setCompany(company);
            processRepository.save(studentProcess);

        }else {

            company = studentProcess.getCompany();
            companyAddress = addressService.updateAddress(company.getAddress().getId(), cedulaDto.companyAddress());
            company = companyService.updateCompany(company.getId(), cedulaDto.companyInfo(), companyAddress);
        }
        pdfService.stampTextOnPdf(student, company);

        return "";
    }


    public ResponseEntity<Resource> getPdfResponseEntity(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        Resource resource = pdfService.loadCedulaAsResource(student.getEnrollment());

        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

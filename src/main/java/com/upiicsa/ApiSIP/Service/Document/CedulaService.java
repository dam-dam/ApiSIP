package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Dto.Cedula.AddressDto;
import com.upiicsa.ApiSIP.Dto.Cedula.CedulaDto;
import com.upiicsa.ApiSIP.Dto.Cedula.CompanyDto;
import com.upiicsa.ApiSIP.Exception.ResourceNotFoundException;
import com.upiicsa.ApiSIP.Model.Address;
import com.upiicsa.ApiSIP.Model.Company;
import com.upiicsa.ApiSIP.Model.Enum.CoordsEnum;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Repository.*;
import com.upiicsa.ApiSIP.Repository.Document_Process.StudentProcessRepository;
import com.upiicsa.ApiSIP.Service.AddressService;
import com.upiicsa.ApiSIP.Service.CompanyService;
import com.upiicsa.ApiSIP.Service.Infrastructure.PdfService;
import com.upiicsa.ApiSIP.Service.StudentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Service
public class CedulaService {

    private StudentService studentService;
    private StudentProcessRepository processRepository;
    private AddressService addressService;
    private CompanyService companyService;
    private PdfService pdfService;

    public CedulaService(StudentService studentService, StudentProcessRepository pRepository,
                         AddressService addressService, CompanyService companyService, PdfService pdfService) {
        this.studentService = studentService;
        this.processRepository = pRepository;
        this.addressService = addressService;
        this.companyService = companyService;
        this.pdfService = pdfService;
    }

    public CedulaDto getAllData(Integer studentId){
        Student student = requestStudent(studentId);

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
        Student student = requestStudent(studentId);

        Address studentAddress;
        if(student.getAddress() == null){
            studentAddress = addressService.createAddress(cedulaDto.studentAddress());
            studentService.setAddress(student, studentAddress);
        } else {
            studentAddress = addressService.updateAddress(student.getAddress().getId(), cedulaDto.studentAddress());
        }

        StudentProcess studentProcess = processRepository.findByStudentIdAndReasonLeavingIsNull(student.getId())
                .orElseThrow(() -> new EntityNotFoundException("StudentProcess not found"));

        Company company;
        Address companyAddress;
        if(studentProcess.getCompany() == null){
            companyAddress = addressService.createAddress(cedulaDto.companyAddress());
            company = companyService.createCompany(cedulaDto.companyInfo(), companyAddress);
            studentProcess.setCompany(company);
            processRepository.save(studentProcess);
        } else {
            company = studentProcess.getCompany();
            companyAddress = addressService.updateAddress(company.getAddress().getId(), cedulaDto.companyAddress());
            company = companyService.updateCompany(company.getId(), cedulaDto.companyInfo(), companyAddress);
        }

        Map<CoordsEnum, String> pdfData = new EnumMap<>(CoordsEnum.class);

        // Datos del Alumno
        pdfData.put(CoordsEnum.NOMBRE, student.getName());
        pdfData.put(CoordsEnum.PATERNO, student.getFLastName());
        pdfData.put(CoordsEnum.MATERNO, student.getMLastName());
        pdfData.put(CoordsEnum.BOLETA, student.getEnrollment());
        pdfData.put(CoordsEnum.CARRERA, student.getOffer().getCareer().getName());
        pdfData.put(CoordsEnum.PLAN_EST, student.getOffer().getSyllabus().getCode());
        pdfData.put(CoordsEnum.CORREO_ALUMNO, student.getEmail());
        pdfData.put(CoordsEnum.TELEFONO_ALUMNO, student.getPhone());

        // Dirección del Alumno
        pdfData.put(CoordsEnum.CALLE, studentAddress.getStreet());
        pdfData.put(CoordsEnum.NUMERO, studentAddress.getNumber());
        pdfData.put(CoordsEnum.COLONIA, studentAddress.getNeighborhood());
        pdfData.put(CoordsEnum.CODIGO_P, studentAddress.getZipCode());
        pdfData.put(CoordsEnum.ENTIDAD, studentAddress.getState().getName());

        // Datos de la Empresa
        pdfData.put(CoordsEnum.RAZON_SOCIAL, company.getName());
        pdfData.put(CoordsEnum.TELEFONO_EMPRESA, company.getPhone());
        pdfData.put(CoordsEnum.EXTESION, company.getExtension());
        pdfData.put(CoordsEnum.FAX, company.getFax());
        pdfData.put(CoordsEnum.CORREO_EMPRESA, company.getEmail());

        String fullCompanyAddress = String.format("%s %s %s %s %s",
                companyAddress.getStreet(), companyAddress.getNumber(),
                companyAddress.getNeighborhood(), companyAddress.getZipCode(),
                companyAddress.getState().getName());
        pdfData.put(CoordsEnum.DOMICILIO, fullCompanyAddress);

        pdfData.put(CoordsEnum.RESPONSABLE, company.getSupervisorGrade() + " " + company.getSupervisor());
        pdfData.put(CoordsEnum.PUESTO_RESPONSABLE, company.getPositionSupervisor());
        pdfData.put(CoordsEnum.PUESTO_ALUMNO, company.getPositionStudent());
        pdfData.put(CoordsEnum.DIRIGE_CARTA, "A quien corresponda");

        String careerAcronym = student.getOffer().getCareer().getAcronym();
        if ("ING_INFORMATICA".equals(careerAcronym) || "CIE_INFORMATICA".equals(careerAcronym)) {
            pdfData.put(CoordsEnum.PRACTICAS, "X");
        } else if (Set.of("ING_TRANSPORTE", "ING_FERROVIARIA", "ING_INDUSTRIAL", "ADM_INDUSTRIAL", "ING_SISA")
                .contains(careerAcronym)) {
            pdfData.put(CoordsEnum.ESTANCIAS, "X");
        }

        if (student.isGraduate()) {
            pdfData.put(CoordsEnum.PASANTE, "X");
        } else {
            switch (student.getSemester().getDescription()) {
                case "SEXTO" -> pdfData.put(CoordsEnum.SEMESTRE_6, "X");
                case "SEPTIMO" -> pdfData.put(CoordsEnum.SEMESTRE_7, "X");
                case "OCTAVO" -> pdfData.put(CoordsEnum.SEMESTRE_8, "X");
                case "NOVENO" -> pdfData.put(CoordsEnum.SEMESTRE_9, "X");
            }
        }

        if ("PUBLICO".equalsIgnoreCase(company.getSector())) {
            pdfData.put(CoordsEnum.SECTOR_PUBLICO, "X");
        } else if ("PRIVADO".equalsIgnoreCase(company.getSector())) {
            pdfData.put(CoordsEnum.SECTOR_PRIVADO, "X");
        }

        String outputFileName = "cedula_" + student.getEnrollment() + ".pdf";
        pdfService.stampTextOnPdf(pdfData, outputFileName);

        return outputFileName;
    }

    public Resource getPdfResponseEntity(Integer studentId) {
        Student student = requestStudent(studentId);
        Resource resource = pdfService.loadCedulaAsResource(student.getEnrollment());

        if (resource == null) {
            throw new ResourceNotFoundException("PDF not found");
        }

        return resource;
    }

    public Student requestStudent(Integer studentId){
        return studentService.getStudentById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
    }
}

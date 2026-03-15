package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.Catalogs.*;
import com.upiicsa.ApiSIP.Dto.Student.StudentRegistrationDto;
import com.upiicsa.ApiSIP.Exception.ResourceNotFoundException;
import com.upiicsa.ApiSIP.Model.Catalogs.Career;
import com.upiicsa.ApiSIP.Model.Catalogs.School;
import com.upiicsa.ApiSIP.Model.Catalogs.Semester;
import com.upiicsa.ApiSIP.Model.Catalogs.Status;
import com.upiicsa.ApiSIP.Model.Offer;
import com.upiicsa.ApiSIP.Model.UserType;
import com.upiicsa.ApiSIP.Repository.*;
import com.upiicsa.ApiSIP.Repository.Catalogs.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogsService {

    private SemesterRepository semesterRepository;
    private OfferRepository offerRepository;
    private SchoolRepository schoolRepository;
    private CareerRepository careerRepository;
    private UserTypeRepository typeRepository;
    private StatusRepository statusRepository;
    private StateRepository stateRepository;

    public CatalogsService(SemesterRepository semesterRepository, OfferRepository offerRepository,
                           SchoolRepository schoolRepository, CareerRepository careerRepository,
                           UserTypeRepository typeRepository, StatusRepository statusRepository,
                           StateRepository stateRepository) {
        this.semesterRepository = semesterRepository;
        this.offerRepository = offerRepository;
        this.schoolRepository = schoolRepository;
        this.careerRepository = careerRepository;
        this.typeRepository = typeRepository;
        this.statusRepository = statusRepository;
        this.stateRepository = stateRepository;
    }

    //School
    public List<SchoolDto> getSchools() {
        return offerRepository.findAllSchools().stream()
                .map(school -> new SchoolDto(school))
                .toList();
    }
    public School getSchool(String schoolAcronym){
        return schoolRepository.findSchoolByAcronym(schoolAcronym)
                .orElseThrow(()-> new EntityNotFoundException("School with acronym " + schoolAcronym + " not found"));
    }

    //Career
    public List<CareerDto> getCareers(String schoolAcronym) {
        return offerRepository.findBySchool(getSchool(schoolAcronym)).stream()
                .map(career -> new CareerDto(career))
                .toList();
    }

    public Career getCareer(String careerAcronym) {
        return careerRepository.findCareerByAcronym(careerAcronym)
                .orElseThrow(() -> new EntityNotFoundException("Career with acronym" + careerAcronym + " not found"));
    }
    //Syllabus
    public List<SyllabusDto> getSyllabuses(String schoolAcronym, String careerAcronym) {
        return offerRepository.findBySchoolAndCareer(getSchool(schoolAcronym), getCareer(careerAcronym))
                .stream()
                .map(syllabus -> new SyllabusDto(syllabus))
                .toList();
    }
    //Offer
    public Offer getOffer(StudentRegistrationDto  registrationDto) {
        return offerRepository.findByCompositeKeys(
                        registrationDto.schoolName(), registrationDto.acronymCareer(), registrationDto.syllabusCode())
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    }
    //Semester
    public Semester getSemester(StudentRegistrationDto registrationDto) {
        Semester semester = null;

        if(!registrationDto.graduated()) {
            semester = semesterRepository.findByDescription(registrationDto.semester())
                    .orElseThrow(() -> new ResourceNotFoundException("Semester not found"));
        }
        return semester;
    }

    public List<SemesterDto> getSemesters() {
        return semesterRepository.findAll().stream()
                .map(sem -> new SemesterDto(sem))
                .toList();
    }
    //UserType
    public UserType getType(String type) {
        return typeRepository.findByDescription(type)
                .orElseThrow(() -> new ResourceNotFoundException("UserType not found"));
    }
    //Status
    public Status getStatus(String status) {
        return statusRepository.findByDescription(status)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
    }
    //State
    public List<StateDto> getStates() {
        return stateRepository.findAll().stream()
                .map(state -> new StateDto(state))
                .toList();
    }
}

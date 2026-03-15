package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.ProfileDto;
import com.upiicsa.ApiSIP.Dto.Student.InfoInstitutionalDto;
import com.upiicsa.ApiSIP.Dto.UserNameDto;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.Document_Process.StudentProcessRepository;
import com.upiicsa.ApiSIP.Repository.StudentRepository;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private UserRepository userRepository;
    private StudentRepository studentRepository;
    private StudentProcessRepository processRepository;

    public  UserService(UserRepository userRepository, StudentRepository studentRepository,
                        StudentProcessRepository processRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.processRepository = processRepository;
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfile(Integer id) {
        Student student = studentRepository.findById(id).orElse(null);
        UserSIP user =  userRepository.findById(id).orElse(null);

        ProfileDto profileDto;

        if(student == null) {
            profileDto = new ProfileDto(user.getName(), user.getFLastName(), user.getMLastName(), user.getEmail(),
                    null, null);
        }else{
            StudentProcess process= processRepository.getById(id);
            String semester = student.isGraduate()
                    ? "Pasante"
                    : student.getSemester().getDescription();
            InfoInstitutionalDto institutionalDto =  new InfoInstitutionalDto(student.getEnrollment(),
                    student.getOffer().getCareer().getName(), student.getOffer().getSyllabus().getCode(),
                    semester, process.getProcessStatus().getDescription());

            profileDto = new ProfileDto(user.getName(), user.getFLastName(), user.getMLastName(), user.getEmail(),
                    student.getPhone(), institutionalDto);
        }
        return profileDto;
    }

    @Transactional(readOnly = true)
    public UserNameDto getName(Integer id) {
        UserSIP user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String name  = user.getName();
        String splitName;
        int space =  name.indexOf(" ");

        if(space != -1) {
            splitName =  name.substring(0, space);
        }else {
            splitName = name;
        }

        return new UserNameDto(splitName, user.getFLastName());
    }
}

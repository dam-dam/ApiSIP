package com.upiicsa.ApiSIP.Dto.Student;

import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.Student;

public record InfoInstitutionalDto(
        String phone,
        String enrollment,
        String career,
        String syllabus,
        String semester,
        String processStatus
) {
    public InfoInstitutionalDto(Student s, StudentProcess p){
        this(s.getPhone(), s.getEnrollment(), s.getOffer().getCareer().getName(), s.getOffer().getSyllabus().code,
                s.getSemester() == null ? "--" : s.getSemester().getDescription(),
                p.getProcessStatus().getDescription());
    }
}

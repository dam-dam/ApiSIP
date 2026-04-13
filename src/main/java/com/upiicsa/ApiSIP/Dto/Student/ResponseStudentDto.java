package com.upiicsa.ApiSIP.Dto.Student;

public record ResponseStudentDto(
    String name,
    String fLastName,
    String mLastName,
    String enrollment,
    String syllabusCode,
    String processStatus
) {
}
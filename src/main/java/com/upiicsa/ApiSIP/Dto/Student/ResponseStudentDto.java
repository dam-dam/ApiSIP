package com.upiicsa.ApiSIP.Dto.Student;

import com.upiicsa.ApiSIP.Model.Offer;

public record ResponseStudentDto(
    String name,
    String fLastName,
    String mLastName,
    String enrollment,
    Offer offer
) {
}
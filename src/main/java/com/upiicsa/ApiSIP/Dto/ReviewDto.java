package com.upiicsa.ApiSIP.Dto;

public record ReviewDto(
        String studentEnrollment,
        String typeName,
        String status,
        String comment
) {
}

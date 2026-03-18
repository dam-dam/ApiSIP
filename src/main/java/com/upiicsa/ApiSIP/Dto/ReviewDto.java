package com.upiicsa.ApiSIP.Dto;

public record ReviewDto(
        String typeName,
        Boolean approved,
        String comment
) {
}

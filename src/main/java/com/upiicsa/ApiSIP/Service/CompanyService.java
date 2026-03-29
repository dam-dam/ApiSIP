package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.Cedula.CompanyDto;
import com.upiicsa.ApiSIP.Exception.BusinessException;
import com.upiicsa.ApiSIP.Model.Address;
import com.upiicsa.ApiSIP.Model.Company;
import com.upiicsa.ApiSIP.Model.Enum.ErrorCode;
import com.upiicsa.ApiSIP.Repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Company createCompany(CompanyDto companyDto, Address address) {
        Company company = Company.builder()
                .name(companyDto.name()).email(companyDto.email())
                .sector(companyDto.sector()).phone(companyDto.phone())
                .extension(companyDto.extension()).fax(companyDto.fax())
                .supervisor(companyDto.supervisor()).supervisorGrade(companyDto.supervisorGrade())
                .positionSupervisor(companyDto.positionSupervisor()).positionStudent(companyDto.positionStudent())
                .address(address)
                .build();

        return companyRepository.save(company);
    }

    @Transactional
    public Company updateCompany(Integer companyId, CompanyDto companyDto, Address address) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Recurso: Compañia"));

        if(!companyDto.name().equals(company.getName())) {
            company.setName(companyDto.name());
        }
        if(!companyDto.email().equals(company.getEmail())) {
            company.setEmail(companyDto.email());
        }
        if(!companyDto.sector().equals(company.getSector())) {
            company.setSector(companyDto.sector());
        }
        if(!companyDto.phone().equals(company.getPhone())) {
            company.setPhone(companyDto.phone());
        }
        if(!companyDto.extension().equals(company.getExtension())) {
            company.setExtension(company.getExtension());
        }
        if(!companyDto.fax().equals(company.getFax())) {
            company.setFax(company.getFax());
        }
        if(!companyDto.supervisor().equals(company.getSupervisor())) {
            company.setSupervisor(company.getSupervisor());
        }
        if(!companyDto.supervisorGrade().equals(company.getSupervisorGrade())) {
            company.setSupervisorGrade(company.getSupervisorGrade());
        }
        if(!companyDto.positionSupervisor().equals(company.getPositionSupervisor())) {
            company.setPositionSupervisor(company.getPositionSupervisor());
        }
        if(!companyDto.positionStudent().equals(company.getPositionStudent())) {
            company.setPositionStudent(company.getPositionStudent());
        }
        company.setAddress(address);
        return companyRepository.save(company);
    }
}

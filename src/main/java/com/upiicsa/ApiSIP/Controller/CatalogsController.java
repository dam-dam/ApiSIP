package com.upiicsa.ApiSIP.Controller;

import com.upiicsa.ApiSIP.Dto.Catalogs.*;
import com.upiicsa.ApiSIP.Service.CatalogsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalogs")
public class CatalogsController {

    private CatalogsService catalogsService;

    public CatalogsController(CatalogsService catalogsService) {
        this.catalogsService = catalogsService;
    }

    @GetMapping("/schools")
    public ResponseEntity<List<SchoolDto>> schools() {
        return ResponseEntity.ok(catalogsService.getSchools());
    }

    @GetMapping("/careers")
    public ResponseEntity<List<CareerDto>> careersBySchool(@RequestParam String SchoolName) {
        return ResponseEntity.ok(catalogsService.getCareers(SchoolName));
    }

    @GetMapping("/syllabus")
    public ResponseEntity<List<SyllabusDto>> syllabusesByCareerAndSchool(@RequestParam String schoolAcronym,
                                                                         @RequestParam String careerAcronym) {
        return ResponseEntity.ok(catalogsService.getSyllabusesByCareerAndSchool(schoolAcronym, careerAcronym));
    }

    @GetMapping("allSyllabus")
    public ResponseEntity<List<SyllabusDto>> syllabusesBySchool(@RequestParam String schoolAcronym) {
        return ResponseEntity.ok(catalogsService.getSyllabusesBySchool(schoolAcronym));
    }

    @GetMapping("/semesters")
    public ResponseEntity<List<SemesterDto>> semesters() {
        return ResponseEntity.ok(catalogsService.getSemesters());
    }

    @GetMapping("/states")
    public ResponseEntity<List<StateDto>> states(){
        return ResponseEntity.ok(catalogsService.getStates());
    }
}

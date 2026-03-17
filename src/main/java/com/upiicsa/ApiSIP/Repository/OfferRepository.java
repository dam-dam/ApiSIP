package com.upiicsa.ApiSIP.Repository;

import com.upiicsa.ApiSIP.Model.Catalogs.Career;
import com.upiicsa.ApiSIP.Model.Catalogs.School;
import com.upiicsa.ApiSIP.Model.Catalogs.Syllabus;
import com.upiicsa.ApiSIP.Model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Integer> {

    @Query("SELECT o FROM Offer o " +
            "WHERE o.school.acronym = :schoolAcronym " +
            "AND o.career.acronym = :careerAcronym " +
            "AND o.syllabus.code = :syllabusCode")
    Optional<Offer> findByCompositeKeys(
            @Param("schoolAcronym") String school,
            @Param("careerAcronym") String career,
            @Param("syllabusCode") String syllabus
    );

    @Query("SELECT DISTINCT o.career FROM Offer o WHERE o.school = :school")
    List<Career> findBySchool(@Param("school") School school);

    @Query("SELECT DISTINCT o.syllabus FROM Offer o WHERE o.school = :school AND o.career = :career")
    List<Syllabus> findBySchoolAndCareer(@Param("school") School school, @Param("career") Career career);
}

package com.upiicsa.ApiSIP.Repository;

import com.upiicsa.ApiSIP.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

    Optional<Student> findById(int id);

    Optional<Student> findByEnrollment(String enrollment);

    @Query("SELECT s FROM Student s JOIN s.offer o JOIN o.career c WHERE c.acronym = :acronym")
    List<Student> findAllByCareerAcronym(@Param("acronym") String acronym);

    //lo inseto dam, aver si jala
    @Query("SELECT s FROM Student s " +
            "LEFT JOIN s.offer o " +
            "LEFT JOIN o.career c " +
            "LEFT JOIN o.syllabus sy " +
            "WHERE (:career = 'all' OR (c IS NOT NULL AND c.acronym = :career)) " +
            "AND (:plan = 'all' OR (sy IS NOT NULL AND sy.code = :plan)) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "    LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "    LOWER(s.fLastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "    LOWER(s.mLastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "    s.enrollment LIKE CONCAT('%', :search, '%'))")
    Page<Student> findFiltered(
            @Param("search") String search,
            @Param("career") String career,
            @Param("plan") String plan,
            Pageable pageable);
}

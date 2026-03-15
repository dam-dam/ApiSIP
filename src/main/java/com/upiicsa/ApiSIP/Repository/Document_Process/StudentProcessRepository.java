package com.upiicsa.ApiSIP.Repository.Document_Process;

import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProcessRepository extends JpaRepository<StudentProcess, Integer> {

    Optional<StudentProcess> findByStudentIdAndReasonLeavingIsNull(Integer studentId);

    Optional<StudentProcess> findByStudentEnrollmentAndReasonLeavingIsNull(String enrollment);
}

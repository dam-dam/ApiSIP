package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Dto.Data.DashboardStatsDto;
import com.upiicsa.ApiSIP.Dto.Data.ProcessProgressDto;
import com.upiicsa.ApiSIP.Exception.BusinessException;
import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import com.upiicsa.ApiSIP.Model.Document_Process.Document;
import com.upiicsa.ApiSIP.Model.Enum.ErrorCode;
import com.upiicsa.ApiSIP.Model.Enum.StateProcessEnum;
import com.upiicsa.ApiSIP.Model.History;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Repository.Catalogs.ProcessStatusRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.DocumentProcessRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.StudentProcessRepository;
import com.upiicsa.ApiSIP.Repository.StudentRepository;
import com.upiicsa.ApiSIP.Service.HistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StudentProcessService {

    private StudentProcessRepository processRepository;
    private DocumentProcessRepository docProcessRepository;
    private ProcessStatusRepository processStatusRepository;
    private StudentRepository studentRepository;
    private HistoryService historyService;

    public StudentProcessService(StudentProcessRepository processRepository, DocumentProcessRepository docProcessRepository,
                                 ProcessStatusRepository processStatusRepository, StudentRepository studentRepository,
                                 HistoryService historyService) {
        this.processRepository = processRepository;
        this.docProcessRepository = docProcessRepository;
        this.processStatusRepository = processStatusRepository;
        this.studentRepository = studentRepository;
        this.historyService = historyService;
    }

    @Transactional(readOnly = true)
    public StudentProcess findByStudentId(Integer userId) {
        return processRepository.findByStudentIdAndReasonLeavingIsNull(userId)
                .orElseThrow(()->new BusinessException(ErrorCode.PROCESS_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public StudentProcess findByEnrollment(String enrollment) {
        return processRepository.findByStudentEnrollmentAndReasonLeavingIsNull(enrollment)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROCESS_NOT_FOUND));
    }

    @Transactional
    public void saveProcess(StudentProcess studentProcess) {
        processRepository.save(studentProcess);
    }

    @Transactional
    public void setFirstState(Student student) {
        ProcessStatus status = processStatusRepository.findByDescription(StateProcessEnum.REGISTERED.getName())
                        .orElseThrow(()-> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        StudentProcess firstProcess = StudentProcess.builder()
                .startDate(LocalDateTime.now())
                .student(student)
                .processStatus(status)
                .build();

        processRepository.save(firstProcess);
        log.info("Sistema ejecuto Primer estado para el Proceso ID[{}]", firstProcess.getId());
    }

    @Transactional
    public void updateStatus(StudentProcess process) {
        StateProcessEnum currentState = StateProcessEnum
                .fromName(process.getProcessStatus().getDescription());

        ProcessStatus newStatus = processStatusRepository.findByDescription(
                StateProcessEnum.fromId(currentState.getNextId()).getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        historyService.saveHistory(process, process.getProcessStatus(), newStatus);
        process.setProcessStatus(newStatus);
        processRepository.save(process);
        log.info("Sistema ejecuto cambio del Estado de Proceso ID[{}] de [{}] a [{}]", process.getId(),
                currentState.getName(), newStatus.getDescription());
    }

    @Transactional
    public void validateUpdateStatus(StudentProcess process, List<Document> docs) {
        log.info("Validando la actualizacion del Estado de Proceso ID[{}]...", process.getId());
        int countedTrue = 0;
        int numberOfNeed = docProcessRepository.countByProcessStatus(process.getProcessStatus());

        for (Document doc : docs) {
            if(doc.getDocumentStatus().getDescription().equals("CORRECTO")){
                countedTrue++;
            }
        }
        if(!docs.isEmpty() && docs.size() == numberOfNeed){
            if(countedTrue == docs.size()){
                updateStatus(process);
                log.info("Cambio de para el Proceso ID[{}] Valido",  process.getId());
            }else {
                log.info("Cambio de para el Proceso ID[{}] Invalido",  process.getId());
            }
        }else {
            log.info("Fallo, info: -Number need [{}] -Docs Size [{}]", numberOfNeed, docs.size());
        }
    }

    @Transactional(readOnly = true)
    public List<ProcessProgressDto> getProcessHistory(Integer userId) {
        StudentProcess process = findByStudentId(userId);
        int currentStageId = process.getProcessStatus().getId();
        List<History> historyList = historyService.getHistoriesByProcess(process);

        List<ProcessProgressDto> progress = new ArrayList<>();

        for(StateProcessEnum state : StateProcessEnum.values()) {
            if(state == StateProcessEnum.CANCELLATION) continue;

            int stageId = state.getId();
            String stageName = state.getName();
            String date = "-";

            if(stageId == 1 && currentStageId > 1) {
                date = process.getStartDate().toLocalDate().toString();
            } else if (stageId > 1 && stageId <= currentStageId) {
                date = historyList.stream()
                        .filter(h -> h.getNewState().getId().equals(stageId))
                        .findFirst()
                        .map(h -> h.getUpdateDate().toLocalDate().toString())
                        .orElse("-");
            }
            progress.add(new ProcessProgressDto(stageName, date, stageId == currentStageId));
        }
        return progress;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats(String careerAcronym, String planCode) {
        Page<Student> filteredStudents = studentRepository.findFiltered("", careerAcronym,
                planCode, Pageable.unpaged());
        List<Student> students = filteredStudents.getContent();

        Map<String, Long> counts = students.stream()
                .map(student -> findByStudentId(student.getId()))
                .collect(Collectors.groupingBy(
                        (StudentProcess process) -> process.getProcessStatus().getDescription(),
                        Collectors.counting()
                ));

        return new DashboardStatsDto(
                (int) filteredStudents.getTotalElements(),
                counts.getOrDefault("REGISTRADO", 0L).intValue(),
                counts.getOrDefault("DOC_INICIAL", 0L).intValue(),
                counts.getOrDefault("CARTAS", 0L).intValue(),
                counts.getOrDefault("DOC_FINAL", 0L).intValue()
        );
    }
}

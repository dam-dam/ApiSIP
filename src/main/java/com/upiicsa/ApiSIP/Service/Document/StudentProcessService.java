package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Dto.ProcessProgressDto;
import com.upiicsa.ApiSIP.Exception.ResourceNotFoundException;
import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import com.upiicsa.ApiSIP.Model.Enum.StateProcessEnum;
import com.upiicsa.ApiSIP.Model.History;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Repository.Catalogs.ProcessStatusRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.StudentProcessRepository;
import com.upiicsa.ApiSIP.Service.HistoryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentProcessService {

    private StudentProcessRepository processRepository;
    private ProcessStatusRepository processStatusRepository;
    private HistoryService historyService;

    public StudentProcessService(StudentProcessRepository processRepository, ProcessStatusRepository processStatusRepository,
                                 HistoryService historyService) {
        this.processRepository = processRepository;
        this.processStatusRepository = processStatusRepository;
        this.historyService = historyService;
    }

    @Transactional
    public void setFirstState(Student student) {
        var state = processStatusRepository.findByDescription(StateProcessEnum.REGISTERED.getName())
                        .orElseThrow(()-> new ResourceNotFoundException("State not found"));

        StudentProcess firstProcess = StudentProcess.builder()
                .startDate(LocalDateTime.now())
                .student(student)
                .processStatus(state)
                .reasonLeaving(null)
                .build();

        processRepository.save(firstProcess);
    }

    @Transactional
    public void updateProcessStatus(StudentProcess process, StateProcessEnum nextProcess) {

        StateProcessEnum currentState = StateProcessEnum.fromId(process.getProcessStatus().getId());

        if (currentState.getNextId() == nextProcess.getId() ||
                nextProcess == StateProcessEnum.CANCELLATION) {
            ProcessStatus newState = processStatusRepository.findByDescription(nextProcess.getName())
                            .orElseThrow(() -> new ResourceNotFoundException("State not found"));
            process.setProcessStatus(newState);

            historyService.saveHistory(process, currentState, nextProcess);
            processRepository.save(process);
        }else {
            throw new IllegalStateException("Transición no permitida");
        }
    }

    public List<ProcessProgressDto> getProcessHistory(Integer userId) {
        StudentProcess process = getByStudentId(userId);
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

    public StudentProcess getByStudentId(Integer userId) {
        return processRepository.findByStudentIdAndReasonLeavingIsNull(userId)
                .orElseThrow(()->new IllegalArgumentException("Process not found"));
    }
}

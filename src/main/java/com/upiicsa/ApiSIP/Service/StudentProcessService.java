package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.ProcessProgressDto;
import com.upiicsa.ApiSIP.Exception.ResourceNotFoundException;
import com.upiicsa.ApiSIP.Model.Catalogs.ProcessState;
import com.upiicsa.ApiSIP.Model.Enum.StateProcessEnum;
import com.upiicsa.ApiSIP.Model.History;
import com.upiicsa.ApiSIP.Model.Student;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Repository.Catalogs.ProcessStateRepository;
import com.upiicsa.ApiSIP.Repository.Document_Process.StudentProcessRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentProcessService {

    private StudentProcessRepository processRepository;
    private ProcessStateRepository processStateRepository;
    private HistoryService historyService;

    public StudentProcessService(StudentProcessRepository processRepository, ProcessStateRepository processStateRepository,
                                 HistoryService historyService) {
        this.processRepository = processRepository;
        this.processStateRepository = processStateRepository;
        this.historyService = historyService;
    }

    @Transactional
    public void setFirstState(Student student) {
        var state = processStateRepository.findByDescription(StateProcessEnum.REGISTERED.getName())
                        .orElseThrow(()-> new ResourceNotFoundException("State not found"));

        StudentProcess firstProcess = StudentProcess.builder()
                .startDate(LocalDateTime.now())
                .active(true)
                .student(student)
                .processState(state)
                .observations("")
                .build();

        processRepository.save(firstProcess);
    }

    @Transactional
    public void updateProcessStatus(StudentProcess process, StateProcessEnum nextProcess) {

        StateProcessEnum currentState = StateProcessEnum.fromId(process.getProcessState().getId());

        if (currentState.getNextId() == nextProcess.getId() ||
                nextProcess == StateProcessEnum.CANCELLATION) {
            ProcessState newState = processStateRepository.findByDescription(nextProcess.getName())
                            .orElseThrow(() -> new ResourceNotFoundException("State not found"));
            process.setProcessState(newState);

            historyService.saveHistory(process, currentState, nextProcess);
            processRepository.save(process);
        }else {
            throw new IllegalStateException("Transición no permitida");
        }
    }

    public List<ProcessProgressDto> getProcessHistory(Integer userId) {
        StudentProcess process = getByStudentId(userId);
        int currentStageId = process.getProcessState().getId();
        List<History> history = historyService.getHistoriesByProcess(process);

        String[] stages = {
                "Registrado", "Documentación de inicio", "Carta de aceptación",
                "Finalización de informes", "Documentación de término", "Liberación"
        };

        List<ProcessProgressDto> progress = new ArrayList<>();

        for (int i = 0; i < stages.length; i++) {
            int stageId = i + 1;
            String date = "-";

            if (stageId == 1 && currentStageId > 1) {
                date = process.getStartDate().toLocalDate().toString();
            } else if (stageId > 1 && stageId < currentStageId) {
                date = history.stream()
                        .filter(h -> h.getProcess().getId().equals(stageId))
                        .findFirst()
                        .map(h -> h.getUpdateDate().toLocalDate().toString())
                        .orElse("-");
            }

            progress.add(new ProcessProgressDto(stages[i], date, stageId == currentStageId));
        }
        return progress;
    }

    public StudentProcess getByStudentId(Integer userId) {
        return processRepository.findByActiveIsTrueAndStudentId(userId)
                .orElseThrow(()->new IllegalArgumentException("Process not found"));
    }
}

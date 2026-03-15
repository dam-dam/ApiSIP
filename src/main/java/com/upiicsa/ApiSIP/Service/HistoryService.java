package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import com.upiicsa.ApiSIP.Model.Enum.StateProcessEnum;
import com.upiicsa.ApiSIP.Model.History;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.HistoryRepository;
import com.upiicsa.ApiSIP.Repository.Catalogs.ProcessStatusRepository;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoryService {

    @Value("${default.user}")
    private Integer defaultUser;
    private HistoryRepository historyRepository;
    private UserRepository userRepository;
    private ProcessStatusRepository stateRepository;

    public HistoryService(HistoryRepository historyRepository, UserRepository userRepository,
                          ProcessStatusRepository stateRepository) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
    }

    @Transactional(readOnly = true)
    public List<History> getHistoriesByProcess(StudentProcess process){
        return  historyRepository.findByProcessOrderByUpdateDateAsc(process);
    }

    @Transactional
    public void saveHistory(StudentProcess process, StateProcessEnum oldState, StateProcessEnum newState) {
        ProcessStatus newStateProcess = stateRepository.findByDescription(newState.getName())
                .orElseThrow(()-> new EntityNotFoundException("State not found"));
        ProcessStatus oldStateProcess = stateRepository.findByDescription(oldState.getName())
                .orElseThrow(()-> new EntityNotFoundException("State not found"));

        History newHistory = History.builder()
                .process(process)
                .user(getDefaultUser())
                .updateDate(LocalDateTime.now())
                .newState(newStateProcess)
                .oldState(oldStateProcess)
                .build();

        historyRepository.save(newHistory);
    }

    private UserSIP getDefaultUser(){
        UserSIP user = userRepository.findById(defaultUser)
                .orElse(null);
        return user;
    }
}

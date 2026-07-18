package com.rhy.interviewprep.service;

import com.rhy.interviewprep.dto.MatchHistoryDTO;
import com.rhy.interviewprep.dto.MatchResultDTO;
import com.rhy.interviewprep.entity.MatchAnalysis;

import java.util.List;

public interface MatchAnalysisService {

    MatchResultDTO analyze(Long userId, Long jdId);

    MatchAnalysis getById(Long userId, Long id);

    List<MatchHistoryDTO> listHistoryByUserId(Long userId);

    List<MatchHistoryDTO> listHistoryByJdId(Long userId, Long jdId);

    void deleteById(Long userId, Long id);
}
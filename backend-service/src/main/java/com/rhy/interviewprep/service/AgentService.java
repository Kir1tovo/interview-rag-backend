package com.rhy.interviewprep.service;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.AgentMessageRequest;
import com.rhy.interviewprep.dto.AgentMessageResponse;

public interface AgentService {
    Result<AgentMessageResponse> chat(AgentMessageRequest request);
}
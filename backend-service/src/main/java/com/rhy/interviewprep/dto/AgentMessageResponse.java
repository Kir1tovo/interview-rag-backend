package com.rhy.interviewprep.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentMessageResponse {
    private String reply;
}
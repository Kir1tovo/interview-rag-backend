package com.rhy.interviewprep.dto;

import lombok.Data;

@Data
public class AgentMessageRequest {
    private String message;
    /** JD图片文件路径（前端先上传图片获取路径，再传给Agent） */
    private String filePath;
}
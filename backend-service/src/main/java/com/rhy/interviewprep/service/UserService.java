package com.rhy.interviewprep.service;

import com.rhy.interviewprep.dto.RegisterDTO;
import com.rhy.interviewprep.common.Result;

public interface UserService {
    /**
     * 用户注册
     */
    Result<String> register(RegisterDTO dto);
}
package com.rhy.interviewprep.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    /** 旧密码 */
    private String oldPassword;
    /** 新密码 */
    private String newPassword;
}
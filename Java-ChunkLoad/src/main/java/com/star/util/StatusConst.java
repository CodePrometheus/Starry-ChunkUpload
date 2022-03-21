package com.star.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: zzStar
 * @Date: 03-21-2022 15:55
 */
@Getter
@AllArgsConstructor
public enum StatusConst {

    /**
     * 成功
     */
    SUCCESS(2000, "操作成功"),

    /**
     * 系统异常
     */
    SYSTEM_ERROR(5000, "系统异常"),

    /**
     * 失败
     */
    FAIL(4040, "操作失败");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String desc;

}

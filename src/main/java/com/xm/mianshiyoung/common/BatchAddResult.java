package com.xm.mianshiyoung.common;

import lombok.Data;

import java.util.List;


/**
 * 批处理添加结果类
 * 提高可观测行
 */
@Data
public class BatchAddResult {
    private int total;
    private int successCount;
    private int failCount;

    private List<String> failureReasons;
}

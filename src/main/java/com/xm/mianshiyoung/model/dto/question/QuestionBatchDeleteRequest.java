package com.xm.mianshiyoung.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑题目请求
 *
 */
@Data
public class QuestionBatchDeleteRequest implements Serializable {

    /**
     * 题目列表id
     */
    private List<Long> questionIdList;


    private static final long serialVersionUID = 1L;
}
package com.xm.mianshiyoung.model.dto.question;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.xm.mianshiyoung.model.entity.Question;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子 ES 包装类
 *
 **/
// todo 取消注释开启 ES（须先配置 ES）
@Document(indexName = "question")
@Data
public class QuestionEsDTO implements Serializable {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @Field( type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionEsDTO objToDto(Question question) {
        if (question == null) {
            return null;
        }
        QuestionEsDTO QuestionEsDTO = new QuestionEsDTO();
        BeanUtils.copyProperties(question, QuestionEsDTO);
        String tagsStr = question.getTags();
        if (StringUtils.isNotBlank(tagsStr)) {
            QuestionEsDTO.setTags(JSONUtil.toList(tagsStr, String.class));
        }
        return QuestionEsDTO;
    }

    /**
     * 包装类转对象
     *
     * @param QuestionEsDTO
     * @return
     */
    public static Question dtoToObj(QuestionEsDTO QuestionEsDTO) {
        if (QuestionEsDTO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(QuestionEsDTO, question);
        List<String> tagList = QuestionEsDTO.getTags();
        if (CollUtil.isNotEmpty(tagList)) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        return question;
    }
}

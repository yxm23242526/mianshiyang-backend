package com.xm.mianshiyoung.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.mianshiyoung.common.BatchAddResult;
import com.xm.mianshiyoung.common.ErrorCode;
import com.xm.mianshiyoung.constant.CommonConstant;
import com.xm.mianshiyoung.exception.BusinessException;
import com.xm.mianshiyoung.exception.ThrowUtils;
import com.xm.mianshiyoung.mapper.QuestionBankQuestionMapper;
import com.xm.mianshiyoung.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import com.xm.mianshiyoung.model.entity.Question;
import com.xm.mianshiyoung.model.entity.QuestionBank;
import com.xm.mianshiyoung.model.entity.QuestionBankQuestion;
import com.xm.mianshiyoung.model.entity.User;
import com.xm.mianshiyoung.model.vo.QuestionBankQuestionVO;
import com.xm.mianshiyoung.model.vo.UserVO;
import com.xm.mianshiyoung.service.QuestionBankQuestionService;
import com.xm.mianshiyoung.service.QuestionBankService;
import com.xm.mianshiyoung.service.QuestionService;
import com.xm.mianshiyoung.service.UserService;
import com.xm.mianshiyoung.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 题库题目关联服务实现
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;


    @Resource
    @Lazy
    private QuestionService questionService;

    @Resource
    private QuestionBankService questionBankService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add                  对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);

        // 题目和题库必须存在
        Long questionId = questionBankQuestion.getQuestionId();
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        if (questionBankId != null) {
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }


        // 不需要校验
        // // todo 从对象中取值
        // String title = questionBankQuestion.getTitle();
        // // 创建数据时，参数不能为空
        // if (add) {
        //     // todo 补充校验规则
        //     ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        // }
        // // 修改数据时，有参数则校验
        // // todo 补充校验规则
        // if (StringUtils.isNotBlank(title)) {
        //     ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        // }
    }

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = questionBankQuestionQueryRequest.getId();
        Long notId = questionBankQuestionQueryRequest.getNotId();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        // todo 补充需要的查询条件
        // 模糊查询
        // JSON 数组查询
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库题目关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);
        // endregion

        return questionBankQuestionVO;
    }

    /**
     * 分页获取题库题目关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(questionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(questionBankQuestion);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        questionBankQuestionVOList.forEach(questionBankQuestionVO -> {
            Long userId = questionBankQuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestionVO.setUser(userService.getUserVO(user));
        });
        // endregion

        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }


    /**
     * 批量插入题目至题库中
     *
     * @param questionIds
     * @param questionBankId
     * @param loginUser
     */
    @Override
    public BatchAddResult batchAddQuestionsToBank(List<Long> questionIds, Long questionBankId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIds), ErrorCode.PARAMS_ERROR, "题目列表为空");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库ID为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 检查题目ID是否存在


        //优化1，不用select *
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = Wrappers.lambdaQuery(Question.class)
                .select(Question::getId)
                .in(Question::getId, questionIds);
        // List<Question> questionList = questionService.list(questionLambdaQueryWrapper);
        // List<Long> validQuestionIds = questionList
        //         .stream().map(Question::getId).collect(Collectors.toList());

        //优化2， 不用对象，采用long，节省内存空间
        List<Long> validQuestionIds = questionService.listObjs(questionLambdaQueryWrapper, obj -> (Long)obj);

        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIds), ErrorCode.PARAMS_ERROR, "合法的题目列表为空");
        // 检查题库中已经存在的题目，避免重复插入题目
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, validQuestionIds);
        List<QuestionBankQuestion> existQuestionList = this.list(lambdaQueryWrapper);
        validQuestionIds = validQuestionIds.stream().filter(questionId -> !existQuestionList.contains(questionId)).collect(Collectors.toList());
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIds), ErrorCode.PARAMS_ERROR, "所有题目都已经存在题库中");
        // 检查题库ID是否存在
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");


        // 自定义线程池
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                20,                         // 核心线程数
                50,                        // 最大线程数
                60L,                       // 线程空闲存活时间
                TimeUnit.SECONDS,           // 存活时间单位
                new LinkedBlockingQueue<>(1000),  // 阻塞队列容量
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用线程处理任务
        );

        //保存所有批次任务
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // 分批处理， 避免长事务， 假设每次处理1000条数据
        int batchSize = 1000;
        int totalQuestionListSize = validQuestionIds.size();

        //封装批处理结果集 不够精准， 只是代码段
        BatchAddResult batchAddResult = new BatchAddResult();
        batchAddResult.setTotal(totalQuestionListSize);
        for (int i = 0; i < totalQuestionListSize; i += batchSize) {
            int batchIndex = i / batchSize;
            // 生成每批次的数据
            List<Long> subList = validQuestionIds.subList(i, Math.min(i + batchSize, totalQuestionListSize));
            List<QuestionBankQuestion> questionBankQuestions = subList.stream().map(questionId -> {
                QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
                questionBankQuestion.setQuestionId(questionId);
                questionBankQuestion.setQuestionBankId(questionBankId);
                questionBankQuestion.setUserId(loginUser.getId());
                return questionBankQuestion;
            }).collect(Collectors.toList());

            // 这里由于是方法内部，需要用到AOP代理对象
            QuestionBankQuestionService questionBankQuestionService = (QuestionBankQuestionServiceImpl) AopContext.currentProxy();
            //异步处理每批数据，将任务添加到异步任务列表
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                questionBankQuestionService.batchAddQuestionsToBankInner(questionBankQuestions);
            }, customExecutor).exceptionally(ex -> {
                log.error("批处理任务执行失败", ex);
                batchAddResult.getFailureReasons().add("第" + batchIndex + "个批处理失败: " + ex.getMessage());
                return null;
            });
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        customExecutor.shutdown();
        return batchAddResult;
    }


    /**
     * 批量添加题目到题库 （事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestions) {
        // 尽量细分异常，否则抛给前端的不一定能懂
        try {
            boolean result = this.saveBatch(questionBankQuestions);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        } catch (DataIntegrityViolationException e) {
            log.error("数据库唯一键冲突或违反其他完整性约束， 错误信息: {}",
                    e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
        } catch (DataAccessException e) {
            log.error("数据库连接问题、事务问题等导致操作失败，错误信息: {}",
                    e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        } catch (Exception e) {
            // 捕获其他异常，做通用处理
            log.error("添加题目到题库时发生未知错误， 错误信息: {}",
                    e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }

    }

    /**
     * 批量从题库删除题目
     *
     * @param questionIds
     * @param questionBankId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionsFromBank(List<Long> questionIds, Long questionBankId) {
        // 参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIds), ErrorCode.PARAMS_ERROR, "题目列表为空");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库ID为空");
        // 执行插入
        for (Long questionId : questionIds) {
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, questionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            boolean result = this.remove(lambdaQueryWrapper);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "向题库删除题目失败");
        }
    }

}

package com.paolua.tomatoclockbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.common.util.JwtUtil;
import com.paolua.tomatoclockbackend.dto.StatisticsDTO;
import com.paolua.tomatoclockbackend.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StatisticsController 单元测试
 * 测试统计控制器的HTTP接口
 */
@WebMvcTest(StatisticsController.class)
@DisplayName("StatisticsController 单元测试")
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatisticsService statisticsService;

    @MockBean
    private JwtUtil jwtUtil;

    private static final String TEST_TOKEN = "Bearer test_jwt_token";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Mock JWT工具类行为
        when(jwtUtil.getUserIdFromToken("test_jwt_token")).thenReturn(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试获取今日统计")
    void test_getTodayStatistics() throws Exception {
        // Given
        StatisticsDTO mockStats = createMockStatistics("today");
        when(statisticsService.getTodayStatistics(TEST_USER_ID)).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/today")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.period").value("today"))
                .andExpect(jsonPath("$.data.completedTomatoes").value(5))
                .andExpect(jsonPath("$.data.focusMinutes").value(125));

        verify(statisticsService).getTodayStatistics(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试获取本周统计")
    void test_getWeekStatistics() throws Exception {
        // Given
        StatisticsDTO mockStats = createMockStatistics("week");
        when(statisticsService.getWeekStatistics(TEST_USER_ID)).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/week")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.period").value("week"))
                .andExpect(jsonPath("$.data.completedTomatoes").value(20));

        verify(statisticsService).getWeekStatistics(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试获取本月统计")
    void test_getMonthStatistics() throws Exception {
        // Given
        StatisticsDTO mockStats = createMockStatistics("month");
        when(statisticsService.getMonthStatistics(TEST_USER_ID)).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/month")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.period").value("month"))
                .andExpect(jsonPath("$.data.completedTomatoes").value(50));

        verify(statisticsService).getMonthStatistics(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试获取自定义范围统计")
    void test_getCustomRangeStatistics() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        StatisticsDTO mockStats = createMockStatistics("custom");
        when(statisticsService.getCustomRangeStatistics(eq(TEST_USER_ID), eq(startDate), eq(endDate)))
                .thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/custom")
                        .header("Authorization", TEST_TOKEN)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.period").value("custom"))
                .andExpect(jsonPath("$.data.startDate").value(startDate.toString()))
                .andExpect(jsonPath("$.data.endDate").value(endDate.toString()));

        verify(statisticsService).getCustomRangeStatistics(eq(TEST_USER_ID), eq(startDate), eq(endDate));
    }

    @Test
    @DisplayName("测试缺少Authorization header")
    void test_missingAuthorizationHeader() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/statistics/today"))
                .andExpect(status().isBadRequest());

        verify(statisticsService, never()).getTodayStatistics(anyLong());
    }

    @Test
    @DisplayName("测试无效的Authorization格式")
    void test_invalidAuthorizationFormat() throws Exception {
        // Given
        when(jwtUtil.getUserIdFromToken("invalid_token"))
                .thenThrow(new BusinessException(ResultCode.UNAUTHORIZED));

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/today")
                        .header("Authorization", "invalid_token"))
                .andExpect(status().isBadRequest());

        verify(statisticsService, never()).getTodayStatistics(anyLong());
    }

    @Test
    @DisplayName("测试统计数据包含任务排行")
    void test_statisticsContainsTaskRanking() throws Exception {
        // Given
        StatisticsDTO mockStats = createMockStatisticsWithRanking("today");
        when(statisticsService.getTodayStatistics(TEST_USER_ID)).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/today")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskRanking").isArray())
                .andExpect(jsonPath("$.data.taskRanking.length()").value(2))
                .andExpect(jsonPath("$.data.taskRanking[0].taskId").value(1))
                .andExpect(jsonPath("$.data.taskRanking[0].taskName").value("热门任务"))
                .andExpect(jsonPath("$.data.taskRanking[0].completedTomatoes").value(5));
    }

    @Test
    @DisplayName("测试统计数据包含每日数据")
    void test_statisticsContainsDailyData() throws Exception {
        // Given
        StatisticsDTO mockStats = createMockStatisticsWithDailyData("week");
        when(statisticsService.getWeekStatistics(TEST_USER_ID)).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/week")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.dailyData").isArray())
                .andExpect(jsonPath("$.data.dailyData.length()").value(7))
                .andExpect(jsonPath("$.data.dailyData[0].date").exists())
                .andExpect(jsonPath("$.data.dailyData[0].completedTomatoes").exists())
                .andExpect(jsonPath("$.data.dailyData[0].focusMinutes").exists());
    }

    @Test
    @DisplayName("测试空统计数据")
    void test_emptyStatistics() throws Exception {
        // Given
        StatisticsDTO emptyStats = new StatisticsDTO();
        emptyStats.setPeriod("today");
        emptyStats.setCompletedTomatoes(0);
        emptyStats.setFocusMinutes(0);
        emptyStats.setCompletedTasks(0);
        emptyStats.setTotalTasks(0);
        emptyStats.setCompletionRate(0);

        when(statisticsService.getTodayStatistics(TEST_USER_ID)).thenReturn(emptyStats);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/today")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.completedTomatoes").value(0))
                .andExpect(jsonPath("$.data.focusMinutes").value(0))
                .andExpect(jsonPath("$.data.completionRate").value(0));
    }

    /**
     * 辅助方法：创建模拟统计数据
     */
    private StatisticsDTO createMockStatistics(String period) {
        StatisticsDTO stats = new StatisticsDTO();
        stats.setPeriod(period);
        stats.setStartDate(LocalDate.now().minusDays(7));
        stats.setEndDate(LocalDate.now());
        stats.setCompletedTomatoes(period.equals("today") ? 5 : period.equals("week") ? 20 : 50);
        stats.setFocusMinutes(125);
        stats.setCompletedTasks(2);
        stats.setTotalTasks(4);
        stats.setCompletionRate(50);
        return stats;
    }

    private StatisticsDTO createMockStatisticsWithRanking(String period) {
        StatisticsDTO stats = createMockStatistics(period);

        StatisticsDTO.TaskRankingDTO rank1 = new StatisticsDTO.TaskRankingDTO();
        rank1.setTaskId(1L);
        rank1.setTaskName("热门任务");
        rank1.setTaskMode((byte) 1);
        rank1.setCompletedTomatoes(5);
        rank1.setTotalCompletedTomatoes(5);

        StatisticsDTO.TaskRankingDTO rank2 = new StatisticsDTO.TaskRankingDTO();
        rank2.setTaskId(2L);
        rank2.setTaskName("普通任务");
        rank2.setTaskMode((byte) 2);
        rank2.setCompletedTomatoes(2);
        rank2.setTotalCompletedTomatoes(2);

        stats.setTaskRanking(Arrays.asList(rank1, rank2));
        return stats;
    }

    private StatisticsDTO createMockStatisticsWithDailyData(String period) {
        StatisticsDTO stats = createMockStatistics(period);

        java.util.List<StatisticsDTO.DailyDataDTO> dailyData = new java.util.ArrayList<>();
        LocalDate date = LocalDate.now().minusDays(6);

        for (int i = 0; i < 7; i++) {
            StatisticsDTO.DailyDataDTO dayData = new StatisticsDTO.DailyDataDTO();
            dayData.setDate(date.plusDays(i));
            dayData.setCompletedTomatoes(i + 1);
            dayData.setFocusMinutes((i + 1) * 25);
            dailyData.add(dayData);
        }

        stats.setDailyData(dailyData);
        return stats;
    }
}

package com.library.config;

import com.library.service.ReservationService;
import com.library.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务配置
 */
@Component
@EnableScheduling
public class ScheduleConfig {
    
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 每5分钟检查一次过期预约
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void handleExpiredReservations() {
        reservationService.handleExpiredReservations();
    }
    
    /**
     * 每天凌晨重置流水号
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailySequence() {
        systemConfigService.resetDailySequence();
    }
}
package com.library.task;

import com.library.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预约相关定时任务
 */
@Component
public class ReservationTask {
    
    private static final Logger log = LoggerFactory.getLogger(ReservationTask.class);
    
    @Autowired
    private ReservationService reservationService;
    
    /**
     * 每10分钟执行一次，处理即将过期的预约
     * 查询当前时间已超过"预约结束时间前20分钟"的记录，将状态设置为"爽约"
     * 例如：预约结束时间是15:00，那么在14:40之后就会被标记为爽约
     */
    @Scheduled(cron = "0 0/1 * * * ? ") // 30分钟 = 30 * 60 * 1000毫秒
    public void handleSoonExpiredReservations() {
        log.info("开始执行定时任务：处理即将过期的预约");
        try {
            reservationService.handleSoonExpiredReservations();
            log.info("定时任务执行完成：处理即将过期的预约");
        } catch (Exception e) {
            log.error("定时任务执行失败：处理即将过期的预约", e);
        }
    }
    
    /**
     * 每小时执行一次，处理已经过期的预约
     * 作为兜底机制，处理已经超过结束时间的预约
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 60分钟 = 60 * 60 * 1000毫秒
    public void handleExpiredReservations() {
        log.info("开始执行定时任务：处理已过期的预约");
        try {
            reservationService.handleExpiredReservations();
            log.info("定时任务执行完成：处理已过期的预约");
        } catch (Exception e) {
            log.error("定时任务执行失败：处理已过期的预约", e);
        }
    }
}
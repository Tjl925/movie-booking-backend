package com.example.movie_booking_backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时间冲突检查结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictCheckVO {
    /**
     * 是否存在冲突
     */
    private Boolean hasConflict;
    
    /**
     * 冲突信息
     */
    private String message;
    
    /**
     * 冲突的场次ID
     */
    private Long conflictSessionId;
    
    /**
     * 冲突的电影名称
     */
    private String conflictMovieName;
    
    /**
     * 冲突的场次开始时间
     */
    private String conflictStartTime;
    
    /**
     * 冲突的场次结束时间
     */
    private String conflictEndTime;
    
    /**
     * 创建无冲突结果
     */
    public static ConflictCheckVO noConflict() {
        return ConflictCheckVO.builder()
                .hasConflict(false)
                .build();
    }
    
    /**
     * 创建有冲突结果
     */
    public static ConflictCheckVO conflict(Long sessionId, String movieName, String startTime, String endTime) {
        String message = String.format("与电影 '%s' 的场次时间冲突，该场次时间为 %s - %s", 
                movieName, startTime, endTime);
        
        return ConflictCheckVO.builder()
                .hasConflict(true)
                .message(message)
                .conflictSessionId(sessionId)
                .conflictMovieName(movieName)
                .conflictStartTime(startTime)
                .conflictEndTime(endTime)
                .build();
    }
}
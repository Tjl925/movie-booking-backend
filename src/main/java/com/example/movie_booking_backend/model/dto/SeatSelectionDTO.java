package com.example.movie_booking_backend.model.dto;

public class SeatSelectionDTO {
    private Long seatId;
    private String status; // "RESERVED" or "AVAILABLE"
    private     Long sessionId;
    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

// getters and setters
}
package com.gymmanagement.dao;

/** Lỗi truy cập dữ liệu, kèm thông báo tiếng Việt để hiển thị cho người dùng. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

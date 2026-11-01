package com.gymmanagement.model;

/** Mức quyền trên một màn hình. */
public enum AccessLevel {
    /** Chỉ xem. */
    VIEW,
    /** Chỉ thấy và cập nhật công việc được giao cho chính mình. */
    OWN,
    /** Toàn quyền thêm, sửa, xóa. */
    MANAGE
}

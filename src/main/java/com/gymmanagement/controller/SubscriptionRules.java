package com.gymmanagement.controller;

import com.gymmanagement.model.Subscription;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Quy tắc ngày tháng của đăng ký gói tập, tách riêng để kiểm tra được mà không cần giao diện hay database. */
public final class SubscriptionRules {
    private SubscriptionRules() {}

    /** Ngày kết thúc theo số ngày của gói, cùng cách tính với hộp thoại đăng ký (bắt đầu + số ngày). */
    public static LocalDate endFor(LocalDate start, int durationDays) {
        return start.plusDays(durationDays);
    }

    /** Gia hạn: nối tiếp ngay sau ngày kết thúc của gói cũ; gói cũ đã hết hạn thì bắt đầu từ hôm nay. */
    public static LocalDate renewStart(Subscription old, LocalDate today) {
        LocalDate next = old.getEndDate().plusDays(1);
        boolean expired = old.state(today) == Subscription.State.EXPIRED;
        return expired || next.isBefore(today) ? today : next;
    }

    /**
     * Đăng ký còn hạn kết thúc muộn nhất của hội viên (chưa hết hạn, không tạm dừng), bỏ qua excludeId.
     * Dùng để đặt ngày bắt đầu của gói mới ngay sau gói này, tránh hai gói chồng nhau.
     */
    public static Optional<Subscription> latestRunning(List<Subscription> all, String memberId, LocalDate today, String excludeId) {
        return all.stream()
                .filter(s -> s.getMemberId().equalsIgnoreCase(memberId))
                .filter(s -> excludeId == null || !s.getSubscriptionId().equalsIgnoreCase(excludeId))
                .filter(s -> {
                    Subscription.State st = s.state(today);
                    return st == Subscription.State.ACTIVE || st == Subscription.State.EXPIRING;
                })
                .max(Comparator.comparing(Subscription::getEndDate));
    }
}

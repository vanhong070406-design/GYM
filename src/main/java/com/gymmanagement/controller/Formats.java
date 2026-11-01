package com.gymmanagement.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Chuyển đổi ngày giờ giữa ô nhập liệu và kiểu java.time. Dữ liệu sai định dạng ném DateTimeParseException. */
final class Formats {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    static final String NO_CHECKOUT = "--:--";

    private Formats() {}

    /** Ô trống trả về null. */
    static LocalDate parseDate(String text) {
        return text.isEmpty() ? null : LocalDate.parse(text);
    }

    static LocalDateTime parseDateTime(String text) {
        return LocalDateTime.parse(text, DATE_TIME);
    }

    static String text(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    static String text(LocalDateTime time) {
        return time == null ? "" : time.format(DATE_TIME);
    }

    /** Chuỗi rỗng thành null để lưu NULL thay vì chuỗi trống. */
    static String emptyToNull(String text) {
        return text.isEmpty() ? null : text;
    }

    static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }
}

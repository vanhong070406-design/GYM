package com.gymmanagement.model;

import java.time.LocalDate;

/**
 * Model đại diện cho Hội viên (Member)
 */
public class Member {
    private String memberId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate joinDate;
    private String address;
    private LocalDate birthDate;
    private String gender;

    public Member() {}

    public Member(String memberId, String fullName, String email, String phone, LocalDate joinDate, String address, LocalDate birthDate, String gender) {
        this.memberId = memberId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.joinDate = joinDate;
        this.address = address;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    @Override
    public String toString() {
        return memberId + " - " + fullName;
    }
}

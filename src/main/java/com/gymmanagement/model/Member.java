package com.gymmanagement.model;

/**
 * Model đại diện cho Hội viên (Member)
 * Các trường: MemberID, FullName, Email, Phone, JoinDate, Address, BirthDate, Gender
 */
public class Member {
    private String memberId;
    private String fullName;
    private String email;
    private String phone;
    private String joinDate;
    private String address;
    private String birthDate;
    private String gender;

    public Member() {}

    public Member(String memberId, String fullName, String email, String phone, 
                  String joinDate, String address, String birthDate, String gender) {
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

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    @Override
    public String toString() {
        return memberId + " - " + fullName;
    }
}

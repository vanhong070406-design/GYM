package com.gymmanagement.model;

/**
 * Model đại diện cho Huấn luyện viên (Trainer)
 * Các trường: TrainerID, FullName, Phone, Experience, Specialty
 */
public class Trainer {
    private String trainerId;
    private String fullName;
    private String phone;
    private String experience; // Ví dụ: "5 năm", "3 năm"
    private String specialty;  // Ví dụ: "Thể hình & Tăng cơ", "Giảm cân & Calisthenics", "Boxing"

    public Trainer() {}

    public Trainer(String trainerId, String fullName, String phone, String experience, String specialty) {
        this.trainerId = trainerId;
        this.fullName = fullName;
        this.phone = phone;
        this.experience = experience;
        this.specialty = specialty;
    }

    public String getTrainerId() { return trainerId; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    @Override
    public String toString() {
        return trainerId + " - " + fullName;
    }
}

package com.gymmanagement.model;

import java.util.List;

/**
 * Model đại diện cho Huấn luyện viên (Trainer)
 */
public class Trainer {
    private String trainerId;
    private String fullName;
    private String phone;
    private int experienceYears;
    private List<String> specialties;

    public Trainer() {}

    public Trainer(String trainerId, String fullName, String phone, int experienceYears, List<String> specialties) {
        this.trainerId = trainerId;
        this.fullName = fullName;
        this.phone = phone;
        this.experienceYears = experienceYears;
        this.specialties = specialties;
    }

    public String getTrainerId() { return trainerId; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }
    public List<String> getSpecialties() { return specialties; }
    public void setSpecialties(List<String> specialties) { this.specialties = specialties; }

    @Override
    public String toString() {
        return trainerId + " - " + fullName;
    }
}

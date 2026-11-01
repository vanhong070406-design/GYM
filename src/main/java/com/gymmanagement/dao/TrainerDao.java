package com.gymmanagement.dao;

import com.gymmanagement.model.Trainer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TrainerDao {
    public List<Trainer> findAll() {
        return Jdbc.query("SELECT t.TrainerID, t.FullName, t.Phone, t.ExperienceYears, "
                        + "(SELECT STRING_AGG(sp.SpecialtyName, N'|') FROM TrainerSpecialty ts "
                        + " JOIN Specialty sp ON sp.SpecialtyID = ts.SpecialtyID WHERE ts.TrainerID = t.TrainerID) "
                        + "FROM Trainer t ORDER BY t.TrainerID",
                rs -> {
                    String joined = rs.getString(5);
                    List<String> specialties = joined == null ? List.of() : List.of(joined.split("\\|"));
                    return new Trainer(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), specialties);
                });
    }

    public void insert(Trainer t) {
        Jdbc.inTransaction(c -> {
            Jdbc.update(c, "INSERT INTO Trainer (TrainerID, FullName, Phone, ExperienceYears) VALUES (?,?,?,?)",
                    t.getTrainerId(), t.getFullName(), t.getPhone(), t.getExperienceYears());
            saveSpecialties(c, t);
        });
    }

    public void update(Trainer t) {
        Jdbc.inTransaction(c -> {
            Jdbc.update(c, "UPDATE Trainer SET FullName=?, Phone=?, ExperienceYears=? WHERE TrainerID=?",
                    t.getFullName(), t.getPhone(), t.getExperienceYears(), t.getTrainerId());
            Jdbc.update(c, "DELETE FROM TrainerSpecialty WHERE TrainerID=?", t.getTrainerId());
            saveSpecialties(c, t);
        });
    }

    private void saveSpecialties(Connection c, Trainer t) throws SQLException {
        for (String name : t.getSpecialties()) {
            Jdbc.update(c, "IF NOT EXISTS (SELECT 1 FROM Specialty WHERE SpecialtyName = ?) INSERT INTO Specialty (SpecialtyName) VALUES (?)", name, name);
            Jdbc.update(c, "INSERT INTO TrainerSpecialty (TrainerID, SpecialtyID) SELECT ?, SpecialtyID FROM Specialty WHERE SpecialtyName = ?",
                    t.getTrainerId(), name);
        }
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Trainer WHERE TrainerID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Trainer", "TrainerID", "TR", 3);
    }
}

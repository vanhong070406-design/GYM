package com.gymmanagement.dao;

import com.gymmanagement.model.PackageModel;
import java.util.List;

public class PackageDao {
    public List<PackageModel> findAll() {
        return Jdbc.query("SELECT PackageID, PackageName, Price, DurationDays, Description FROM GymPackage ORDER BY PackageID",
                rs -> new PackageModel(rs.getString(1), rs.getString(2), rs.getBigDecimal(3), rs.getInt(4), rs.getString(5)));
    }

    public void insert(PackageModel x) {
        Jdbc.update("INSERT INTO GymPackage (PackageID, PackageName, Price, DurationDays, Description) VALUES (?,?,?,?,?)",
                x.getPackageId(), x.getPackageName(), x.getPrice(), x.getDuration(), x.getDescription());
    }

    public void update(PackageModel x) {
        Jdbc.update("UPDATE GymPackage SET PackageName=?, Price=?, DurationDays=?, Description=? WHERE PackageID=?",
                x.getPackageName(), x.getPrice(), x.getDuration(), x.getDescription(), x.getPackageId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM GymPackage WHERE PackageID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("GymPackage", "PackageID", "PK", 2);
    }
}

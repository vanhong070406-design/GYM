package com.gymmanagement.dao;

import com.gymmanagement.model.Member;
import java.time.LocalDate;
import java.util.List;

public class MemberDao {
    public List<Member> findAll() {
        return Jdbc.query("SELECT MemberID, FullName, Email, Phone, JoinDate, Address, BirthDate, Gender FROM Member ORDER BY MemberID",
                rs -> new Member(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getObject(5, LocalDate.class), rs.getString(6), rs.getObject(7, LocalDate.class), rs.getString(8)));
    }

    public void insert(Member x) {
        Jdbc.update("INSERT INTO Member (MemberID, FullName, Email, Phone, JoinDate, Address, BirthDate, Gender) VALUES (?,?,?,?,?,?,?,?)",
                x.getMemberId(), x.getFullName(), x.getEmail(), x.getPhone(), x.getJoinDate(), x.getAddress(), x.getBirthDate(), x.getGender());
    }

    public void update(Member x) {
        Jdbc.update("UPDATE Member SET FullName=?, Email=?, Phone=?, JoinDate=?, Address=?, BirthDate=?, Gender=? WHERE MemberID=?",
                x.getFullName(), x.getEmail(), x.getPhone(), x.getJoinDate(), x.getAddress(), x.getBirthDate(), x.getGender(), x.getMemberId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Member WHERE MemberID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Member", "MemberID", "MB", 3);
    }
}

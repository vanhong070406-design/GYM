package com.gymmanagement.dao;

import com.gymmanagement.model.Payment;
import java.time.LocalDate;
import java.util.List;

public class PaymentDao {
    public List<Payment> findAll() {
        return Jdbc.query("SELECT PaymentID, PaymentDate, PaymentMethod, Amount, SubscriptionID, ReceptionistID FROM Payment ORDER BY PaymentID",
                rs -> new Payment(rs.getString(1), rs.getObject(2, LocalDate.class), rs.getString(3), rs.getBigDecimal(4), rs.getString(5), rs.getString(6)));
    }

    public void insert(Payment x) {
        Jdbc.update("INSERT INTO Payment (PaymentID, PaymentDate, PaymentMethod, Amount, SubscriptionID, ReceptionistID) VALUES (?,?,?,?,?,?)",
                x.getPaymentId(), x.getPaymentDate(), x.getPaymentMethod(), x.getAmount(), x.getSubscriptionId(), x.getReceptionistId());
    }

    public void delete(String id) {
        Jdbc.update("DELETE FROM Payment WHERE PaymentID=?", id);
    }

    public String nextId() {
        return Jdbc.nextCode("Payment", "PaymentID", "PM", 3);
    }
}

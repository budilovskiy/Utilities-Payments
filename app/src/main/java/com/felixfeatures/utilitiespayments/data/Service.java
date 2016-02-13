package com.felixfeatures.utilitiespayments.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class provides municipal service
 */
public class Service {
    private String name;
    private RateType rateType;
    private Map<Period, Payment> payments;

    public enum RateType {
        Fixed, Simple, Differentiated
    }

    public Service(String name, RateType rateType) {
        this.name = name;
        this.rateType = rateType;
        payments = new TreeMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RateType getRateType() {
        return rateType;
    }

    public void addPayment(Period period, Payment payment) {
        payments.put(period, payment);
    }

    public List<Payment> getPayments() {
        List<Payment> list = new ArrayList<Payment>(payments.values());
        return list;
    }

    public void setPayments(List<Payment> paymentsList) {
        payments = new TreeMap<>();
        for (Payment payment : paymentsList) {
            Period period = payment.getPeriod();
            payments.put(period, payment);
        }
    }

    public Payment getPayment(Period period) {
        return payments.get(period);
    }

    @Override
    public String toString() {
        return String.format("%s(%s rate)", name, rateType.name());
    }
}

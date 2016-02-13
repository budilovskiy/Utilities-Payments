package com.felixfeatures.utilitiespayments.data;

/**
 * TODO comment
 */
public class FixedPayment extends Payment {

    public FixedPayment(Period period, UtilityRate rate) {
        super(period, rate);
    }

    @Override
    public double getPaymentSum() {
        return rate.getSum(0, 0);
    }

}

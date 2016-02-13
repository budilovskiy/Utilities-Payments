package com.felixfeatures.utilitiespayments.data;

/**
 * TODO comment
 */
abstract public class Payment {
    private Period period;
    UtilityRate rate;

    public Payment(Period period, UtilityRate rate) {
        this.period = period;
        this.rate = rate;
    }

    public Period getPeriod() {
        return period;
    }

    public UtilityRate getRate() {
        return rate;
    }

    abstract public double getPaymentSum();
}

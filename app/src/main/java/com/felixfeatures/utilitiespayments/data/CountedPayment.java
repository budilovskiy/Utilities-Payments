package com.felixfeatures.utilitiespayments.data;

/**
 * TODO comment
 */
public class CountedPayment extends Payment {
    private int previousCounterData;
    private int currentCounterData;

    public CountedPayment(Period period, int previousCounterData, int currentCounterData, UtilityRate rate) {
        super(period, rate);
        setCounterData(previousCounterData, currentCounterData);
    }

    public int getPreviousCounterData() {
        return previousCounterData;
    }

    public int getCurrentCounterData() {
        return currentCounterData;
    }

    public void setCounterData(int previousCounterData, int currentCounterData) {
        this.previousCounterData = previousCounterData;
        this.currentCounterData = currentCounterData;
    }

    @Override
    public double getPaymentSum() {
        return rate.getSum(previousCounterData, currentCounterData);
    }
}

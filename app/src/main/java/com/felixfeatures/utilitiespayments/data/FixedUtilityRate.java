package com.felixfeatures.utilitiespayments.data;

/**
 * TODO comment
 */
public class FixedUtilityRate extends UtilityRate {
    private double rateValue;

    public FixedUtilityRate(double rateValue) {
        this.rateValue = rateValue;
    }

    public double getRateValue() {
        return rateValue;
    }

    public void setRateValue(double rateValue) {
        this.rateValue = rateValue;
    }

    @Override
    public double getSum(int previousCounterData, int currentCounterData) {
        return rateValue;
    }
}

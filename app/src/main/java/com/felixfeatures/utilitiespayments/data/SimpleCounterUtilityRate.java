package com.felixfeatures.utilitiespayments.data;

/**
 * TODO comment
 */
public class SimpleCounterUtilityRate extends UtilityRate {
    private double rateValue;

    public SimpleCounterUtilityRate(double rateValue) {
        this.rateValue = rateValue;
    }

    public double getRateValue() {
        return rateValue;
    }

    @Override
    public double getSum(int previousCounterData, int currentCounterData) throws IllegalArgumentException {
        int difference = currentCounterData - previousCounterData;
        if (difference < 0) {
            throw new IllegalArgumentException(String.format("Previous counter data %d" +
                    " is greater than current counter data %d", previousCounterData, currentCounterData));
            // TODO: 25.01.2016 exception
        }
        return difference * rateValue;
    }

}

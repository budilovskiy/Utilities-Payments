package com.felixfeatures.utilitiespayments.data;

/**
 * TODO comment
 */
public class DiffCounterUtilityRate extends UtilityRate {
    private double rateValue1, rateValue2, rateValue3;
    private int border12, border23;

    public DiffCounterUtilityRate(double rateValue1, int border12, double rateValue2, int border23, double rateValue3) {
        this.rateValue1 = rateValue1;
        this.rateValue2 = rateValue2;
        this.rateValue3 = rateValue3;
        this.border12 = border12;
        this.border23 = border23;
    }

    public int getBorder12() {
        return border12;
    }

    public int getBorder23() {
        return border23;
    }

    public double getRateValue1() {
        return rateValue1;
    }

    public double getRateValue2() {
        return rateValue2;
    }

    public double getRateValue3() {
        return rateValue3;
    }

    @Override
    public double getSum(int previousCounterData, int currentCounterData) throws IllegalArgumentException {
        int difference = currentCounterData - previousCounterData;
        if (difference < 0) {
            throw new IllegalArgumentException(String.format("Previous counter data %d" +
                    " is greater than current counter data %d", previousCounterData, currentCounterData));
        }
        double sum1 = 0;
        double sum2 = 0;
        double sum3 = 0;
        if (difference > border23) {
            sum3 = (difference - border23) * rateValue3;
            difference = border23;
        }
        if (difference > border12) {
            sum2 = (difference - border12) * rateValue2;
            difference = border12;
        }
        sum1 = difference * rateValue1;
        return sum1 + sum2 + sum3;
    }
}

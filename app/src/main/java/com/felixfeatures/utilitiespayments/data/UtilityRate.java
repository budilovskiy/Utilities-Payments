package com.felixfeatures.utilitiespayments.data;

/**
 * TODO comment
 */
abstract public class UtilityRate {
    abstract public double getSum(int previousCounterData, int currentCounterData);
}

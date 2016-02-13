package com.felixfeatures.utilitiespayments.data;

import android.support.annotation.NonNull;

import com.felixfeatures.utilitiespayments.MainActivity;

/**
 * TODO comment
 */
public class Period implements Comparable {
    private final static String DIVIDER = " ";
    private final static String[] monthsNames = MainActivity.months;

    private int month;
    private int year;

    public Period(int month, int year) {
        this.month = month;
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return String.format("%s%s%d", monthsNames[month], DIVIDER, year);
    }

    @Override
    public int compareTo(@NonNull Object another) {
        Period anotherPeriod = (Period) another;
        if (this.year < anotherPeriod.year) {
            return -1;
        } else if (this.year > anotherPeriod.year) {
            return 1;
        } else if (this.year == anotherPeriod.year) {
            if (this.month < anotherPeriod.month) {
                return -1;
            } else if (this.month > anotherPeriod.month) {
                return 1;
            }
        }
        return 0;
    }
}

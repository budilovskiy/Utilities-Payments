package com.felixfeatures.utilitiespayments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.felixfeatures.utilitiespayments.data.CountedPayment;
import com.felixfeatures.utilitiespayments.data.DiffCounterUtilityRate;
import com.felixfeatures.utilitiespayments.data.FixedPayment;
import com.felixfeatures.utilitiespayments.data.FixedUtilityRate;
import com.felixfeatures.utilitiespayments.data.Period;
import com.felixfeatures.utilitiespayments.data.Payment;
import com.felixfeatures.utilitiespayments.data.Service;
import com.felixfeatures.utilitiespayments.data.SimpleCounterUtilityRate;

import java.util.List;

/**
 * TODO comment
 */
public class PaymentsListItemAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Payment> payments;
    private Service.RateType rateType;

    public PaymentsListItemAdapter(Context context, List<Payment> payments, Service.RateType rateType) {
        this.context = context;
        this.payments = payments;
        this.rateType = rateType;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return payments.size();
    }

    @Override
    public Object getItem(int position) {
        return payments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.service_list_item, parent, false);
        }
        TextView twSum = (TextView) view.findViewById(R.id.twSum);
        TextView twRate = (TextView) view.findViewById(R.id.twRate);
        switch (rateType) {
            case Fixed: {
                ((TextView) view.findViewById(R.id.twRateLabel)).setText(context.getString(R.string.fixed_rate_name));
                ((TextView) view.findViewById(R.id.twCurrentTitle)).setText("");
                ((TextView) view.findViewById(R.id.twPreviousTitle)).setText("");
                ((TextView) view.findViewById(R.id.twDifferenceTitle)).setText("");
                ((TextView) view.findViewById(R.id.twCurrent)).setText("");
                ((TextView) view.findViewById(R.id.twPreviouus)).setText("");
                ((TextView) view.findViewById(R.id.twDifference)).setText("");
                FixedPayment payment = (FixedPayment) getPayment(position);
                if (payment != null) {
                    Period period = payment.getPeriod();
                    FixedUtilityRate rate = (FixedUtilityRate) payment.getRate();
                    double rateValue = rate.getRateValue();
                    double paymentSum = rate.getSum(0, 0);
                    ((TextView) view.findViewById(R.id.twName)).setText(period.toString());
                    twRate.setText(String.format("%.3f", rateValue));
                    twSum.setText(String.format("%.2f", paymentSum));
                }
                break;
            }
            case Simple: {
                ((TextView) view.findViewById(R.id.twRateLabel)).setText(context.getString(R.string.simple_rate_name));
                ((TextView) view.findViewById(R.id.twCurrentTitle)).setText(R.string.current_data);
                ((TextView) view.findViewById(R.id.twPreviousTitle)).setText(R.string.previous_data);
                ((TextView) view.findViewById(R.id.twDifferenceTitle)).setText(R.string.difference);
                CountedPayment payment = (CountedPayment) getPayment(position);
                if (payment != null) {
                    Period period = payment.getPeriod();
                    SimpleCounterUtilityRate rate = (SimpleCounterUtilityRate) payment.getRate();
                    double rateValue = rate.getRateValue();
                    int currentData = payment.getCurrentCounterData();
                    int previousData = payment.getPreviousCounterData();
                    int difference = currentData - previousData;
                    double paymentSum = rate.getSum(previousData, currentData);
                    ((TextView) view.findViewById(R.id.twName)).setText(period.toString());
                    ((TextView) view.findViewById(R.id.twCurrent)).setText(String.format("%d", currentData));
                    ((TextView) view.findViewById(R.id.twPreviouus)).setText(String.format("%d", previousData));
                    ((TextView) view.findViewById(R.id.twDifference)).setText(String.format("%d", difference));
                    twRate.setText(String.format("%.3f", rateValue));
                    twSum.setText(String.format("%.2f", paymentSum));
                }
                break;
            }
            case Differentiated: {
                ((TextView) view.findViewById(R.id.twRateLabel)).setText(context.getString(R.string.diff_rate_name));
                ((TextView) view.findViewById(R.id.twCurrentTitle)).setText(R.string.current_data);
                ((TextView) view.findViewById(R.id.twPreviousTitle)).setText(R.string.previous_data);
                ((TextView) view.findViewById(R.id.twDifferenceTitle)).setText(R.string.difference);
                CountedPayment payment = (CountedPayment) getPayment(position);
                if (payment != null) {
                    Period period = payment.getPeriod();
                    DiffCounterUtilityRate rate = (DiffCounterUtilityRate) payment.getRate();
                    double rateValue = rate.getRateValue1();
                    int currentData = payment.getCurrentCounterData();
                    int previousData = payment.getPreviousCounterData();
                    int difference = currentData - previousData;
                    double paymentSum = rate.getSum(previousData, currentData);
                    ((TextView) view.findViewById(R.id.twName)).setText(period.toString());
                    ((TextView) view.findViewById(R.id.twCurrent)).setText(String.format("%d", currentData));
                    ((TextView) view.findViewById(R.id.twPreviouus)).setText(String.format("%d", previousData));
                    ((TextView) view.findViewById(R.id.twDifference)).setText(String.format("%d", difference));
                    twRate.setText(String.format("%.3f", rateValue));
                    twSum.setText(String.format("%.2f", paymentSum));
                }
                break;
            }
        }
        return view;
    }

    private Payment getPayment(int position) {
        return (Payment) getItem(getCount() - 1 - position);
    }
}

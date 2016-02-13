package com.felixfeatures.utilitiespayments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.felixfeatures.utilitiespayments.data.CountedPayment;
import com.felixfeatures.utilitiespayments.data.DiffCounterUtilityRate;
import com.felixfeatures.utilitiespayments.data.FixedUtilityRate;
import com.felixfeatures.utilitiespayments.data.Period;
import com.felixfeatures.utilitiespayments.data.Payment;
import com.felixfeatures.utilitiespayments.data.Service;
import com.felixfeatures.utilitiespayments.data.SimpleCounterUtilityRate;

import java.util.List;

/**
 * TODO comment
 */
public class ServicesListItemAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Service> services;
    private Period period;
    private String rateName = "";

    public ServicesListItemAdapter(Context context, List<Service> services, Period period) {
        this.context = context;
        this.services = services;
        this.period = period;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return services.size();
    }

    @Override
    public Object getItem(int position) {
        return services.get(position);
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

        Service service = getService(position);
        String serviceName = service.getName();
        Service.RateType rateType = service.getRateType();

        ((TextView) view.findViewById(R.id.twName)).setText(serviceName);
        TextView twSum = (TextView) view.findViewById(R.id.twSum);
        TextView twRate = (TextView) view.findViewById(R.id.twRate);
        switch (rateType) {
            case Fixed: {
                rateName = context.getString(R.string.fixed_rate_name);
                ((TextView) view.findViewById(R.id.twCurrentTitle)).setText("");
                ((TextView) view.findViewById(R.id.twPreviousTitle)).setText("");
                ((TextView) view.findViewById(R.id.twCurrent)).setText("");
                ((TextView) view.findViewById(R.id.twPreviouus)).setText("");
                ((TextView) view.findViewById(R.id.twDifferenceTitle)).setText("");
                ((TextView) view.findViewById(R.id.twDifference)).setText("");
                Payment payment = service.getPayment(period);
                if (payment != null) {
                    double paymentSum = payment.getPaymentSum();
                    FixedUtilityRate rate = (FixedUtilityRate) payment.getRate();
                    double paymentRate = rate.getRateValue();
                    twSum.setText(String.format("%.2f", paymentSum));
                    twRate.setText(String.format("%.2f", paymentRate));
                } else {
                    twSum.setText(String.format("%.2f", 0.0));
                    twRate.setText(String.format("%.3f", 0.0));
                }
                break;
            }
            default: {
                ((TextView) view.findViewById(R.id.twCurrentTitle)).setText(R.string.current_data);
                ((TextView) view.findViewById(R.id.twPreviousTitle)).setText(R.string.previous_data);
                ((TextView) view.findViewById(R.id.twDifferenceTitle)).setText(R.string.difference);
                CountedPayment payment = (CountedPayment) service.getPayment(period);
                if (payment != null) {
                    int currentData = payment.getCurrentCounterData();
                    int previousData = payment.getPreviousCounterData();
                    ((TextView) view.findViewById(R.id.twCurrent)).setText(String.format("%d", currentData));
                    ((TextView) view.findViewById(R.id.twPreviouus)).setText(String.format("%d", previousData));
                    ((TextView) view.findViewById(R.id.twDifference)).setText(String.format("%d", currentData - previousData));
                    double paymentSum = payment.getPaymentSum();
                    twSum.setText(String.format("%.2f", paymentSum));
                } else {
                    ((TextView) view.findViewById(R.id.twCurrent)).setText(String.format("%d", 0));
                    ((TextView) view.findViewById(R.id.twPreviouus)).setText(String.format("%d", 0));
                    ((TextView) view.findViewById(R.id.twDifference)).setText(String.format("%d", 0));
                    twSum.setText(String.format("%.2f", 0.0));
                    twRate.setText(String.format("%.3f", 0.0));
                }
                if (rateType == Service.RateType.Simple) {
                    rateName = context.getString(R.string.simple_rate_name);
                    if (payment != null) {
                        SimpleCounterUtilityRate rate = (SimpleCounterUtilityRate) payment.getRate();
                        double paymentRate = rate.getRateValue();
                        twRate.setText(String.format("%.3f", paymentRate));
                    } else {
                        twRate.setText(String.format("%.3f", 0.0));
                    }
                }
                if (rateType == Service.RateType.Differentiated) {
                    rateName = context.getString(R.string.diff_rate_name);
                    if (payment != null) {
                        DiffCounterUtilityRate rate = (DiffCounterUtilityRate) payment.getRate();
                        double paymentRate = rate.getRateValue1();
                        twRate.setText(String.format("%.3f", paymentRate));
                    } else {
                        twRate.setText(String.format("%.3f", 0.0));
                    }
                }
                break;
            }
        }
        ((TextView) view.findViewById(R.id.twRateLabel)).setText(rateName);
        return view;
    }

    private Service getService(int position) {
        return ((Service) getItem(position));
    }

}

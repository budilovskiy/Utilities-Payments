package com.felixfeatures.utilitiespayments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felixfeatures.utilitiespayments.data.CountedPayment;
import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.DiffCounterUtilityRate;
import com.felixfeatures.utilitiespayments.data.Period;
import com.felixfeatures.utilitiespayments.data.Payment;
import com.felixfeatures.utilitiespayments.data.Service;

import java.util.Locale;

public class EditDiffPaymentActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "UtilityService_log";

    private DataManager dataManager = DataManager.getInstance(this);

    private TextView twName;
    private TextView twDate;
    private EditText etRate1;
    private EditText etRate2;
    private EditText etRate3;
    private EditText etBorder12;
    private EditText etBorder23;
    private EditText etCurrentData;
    private EditText etPreviousData;

    private int serviceID;
    private Period paymentPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diff_payment);
        initFields();
        initButtons();
    }

    private void initFields() {
        Intent intent = getIntent();
        Service service = getService(intent);
        Log.d(TAG, String.format("Service to edit payment %s", service));

        String serviceName = service.getName();
        double paymentRate1 = getPaymentRate(serviceID, 1);
        double paymentRate2 = getPaymentRate(serviceID, 2);
        double paymentRate3 = getPaymentRate(serviceID, 3);
        int paymentBorder12 = getPaymentBorder(serviceID, 1);
        int paymentBorder23 = getPaymentBorder(serviceID, 2);
        int currCounterData = getCurrentCounterData(serviceID);
        int prevCounterData = getPreviousCounterData(serviceID);

        twName = (TextView) findViewById(R.id.twName_diff_payment);
        etRate1 = (EditText) findViewById(R.id.etRate1_diff);
        etRate2 = (EditText) findViewById(R.id.etRate2_diff);
        etRate3 = (EditText) findViewById(R.id.etRate3_diff);
        etBorder12 = (EditText) findViewById(R.id.etBorder1_diff);
        etBorder23 = (EditText) findViewById(R.id.etBorder2_diff);
        twDate = (TextView) findViewById(R.id.twDiffPaymentDate);
        twDate.setBackgroundColor(Color.LTGRAY);
        twDate.setTextColor(Color.BLACK);
        twName.setText(serviceName);
        etCurrentData = (EditText) findViewById(R.id.etCurrent_diff_payment);
        etPreviousData = (EditText) findViewById(R.id.etPrevious_diff_payment);
        twDate.setText(paymentPeriod.toString());
        etRate1.setText(String.format(Locale.ENGLISH, "%.3f", paymentRate1));
        etRate2.setText(String.format(Locale.ENGLISH, "%.3f", paymentRate2));
        etRate3.setText(String.format(Locale.ENGLISH, "%.3f", paymentRate3));
        etBorder12.setText(String.format(Locale.ENGLISH, "%d", paymentBorder12));
        etBorder23.setText(String.format(Locale.ENGLISH, "%d", paymentBorder23));
        etCurrentData.setText(String.format(Locale.ENGLISH, "%d", currCounterData));
        etPreviousData.setText(String.format(Locale.ENGLISH, "%d", prevCounterData));
    }

    private void initButtons() {
        Button btnAddPayment = (Button) findViewById(R.id.btn_add_diff_payment);
        btnAddPayment.setText(R.string.save_btn_text);
        Button btnCancel = (Button) findViewById(R.id.btn_add_diff_payment_cancel);
        btnAddPayment.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    private Service getService(Intent intent) {
        paymentPeriod = getPaymentPeriod(intent);
        serviceID = intent.getIntExtra(PaymentsFragment.EXTRA_KEY_SERVICE_ID, -1);
        if (serviceID == -1) {
            Toast.makeText(this, "Can not find utility service", Toast.LENGTH_SHORT).show();
        }
        return dataManager.getService(serviceID);
    }

    private Period getPaymentPeriod(Intent intent) {
        return new Period(intent.getIntExtra(PaymentsFragment.EXTRA_KEY_MONTH, 0),
                intent.getIntExtra(PaymentsFragment.EXTRA_KEY_YEAR, 0));
    }

    private double getPaymentRate(int serviceID, int rateNumber) {
        double result = -1;
        Payment payment = dataManager.getPayment(paymentPeriod, serviceID);
        DiffCounterUtilityRate rate = (DiffCounterUtilityRate) payment.getRate();
        switch (rateNumber) {
            case 1: {
                result = rate.getRateValue1();
                break;
            }
            case 2: {
                result = rate.getRateValue2();
                break;
            }
            case 3: {
                result = rate.getRateValue3();
                break;
            }
            default: {
                break;
            }
        }
        return result;
    }

    private int getPaymentBorder(int serviceID, int borderNumber) {
        int result = -1;
        Payment payment = dataManager.getPayment(paymentPeriod, serviceID);
        DiffCounterUtilityRate rate = (DiffCounterUtilityRate) payment.getRate();
        switch (borderNumber) {
            case 1: {
                result = rate.getBorder12();
                break;
            }
            case 2: {
                result = rate.getBorder23();
                break;
            }
            default: {
                break;
            }
        }
        return result;
    }

    private int getPreviousCounterData(int serviceID) {
        CountedPayment payment = (CountedPayment) dataManager.getPayment(paymentPeriod, serviceID);
        return payment.getPreviousCounterData();
    }

    private int getCurrentCounterData(int serviceID) {
        CountedPayment payment = (CountedPayment) dataManager.getPayment(paymentPeriod, serviceID);
        return payment.getCurrentCounterData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.btn_add_diff_payment): {
                try {
                    replacePayment();
                } catch (NumberFormatException ex) {
                    Toast.makeText(this, R.string.wrong_counter_data_or_rate, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case (R.id.btn_add_diff_payment_cancel): {
                finish();
                break;
            }
        }
    }

    private void replacePayment() {
        double rate1 = Double.parseDouble(etRate1.getText().toString());
        double rate2 = Double.parseDouble(etRate2.getText().toString());
        double rate3 = Double.parseDouble(etRate3.getText().toString());
        int border12 = Integer.parseInt(etBorder12.getText().toString());
        int border23 = Integer.parseInt(etBorder23.getText().toString());
        int prevCounterData = Integer.parseInt(etPreviousData.getText().toString());
        int currCounterData = Integer.parseInt(etCurrentData.getText().toString());
        Payment payment = new CountedPayment(paymentPeriod, prevCounterData, currCounterData, new DiffCounterUtilityRate(rate1, border12, rate2, border23, rate3));
        dataManager.addPayment(paymentPeriod, serviceID, payment);
        Toast.makeText(this, String.format("%s %s", paymentPeriod, getString(R.string.payment_added)), Toast.LENGTH_SHORT).show();
        finish();
    }
}

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

import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.FixedPayment;
import com.felixfeatures.utilitiespayments.data.FixedUtilityRate;
import com.felixfeatures.utilitiespayments.data.Period;
import com.felixfeatures.utilitiespayments.data.Payment;
import com.felixfeatures.utilitiespayments.data.Service;

import java.util.Locale;

public class EditFixedPaymentActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "UtilityService_log";

    private DataManager dataManager = DataManager.getInstance(this);

    private TextView twName;
    private TextView twDate;
    private EditText etRate;

    private int serviceID;
    private Period paymentPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fixed_payment);
        initFields();
        initButtons();
    }

    private void initFields() {
        Intent intent = getIntent();
        Service service = getService(intent);
        Log.d(TAG, String.format("Service to edit payment %s", service));

        String serviceName = service.getName();
        double paymentRate = getPaymentRate(serviceID);

        twName = (TextView) findViewById(R.id.twName_fixed_payment);
        etRate = (EditText) findViewById(R.id.etRate_fixed_payment);
        twDate = (TextView) findViewById(R.id.twFixedPaymentDate);
        twDate.setBackgroundColor(Color.LTGRAY);
        twDate.setTextColor(Color.BLACK);
        twName.setText(serviceName);
        twDate.setText(paymentPeriod.toString());
        twDate.setEnabled(false);
        etRate.setText(String.format(Locale.ENGLISH, "%.2f", paymentRate));
    }

    private void initButtons() {
        Button btnAddPayment = (Button) findViewById(R.id.btn_add_fixed_payment);
        btnAddPayment.setText(R.string.save_btn_text);
        Button btnCancel = (Button) findViewById(R.id.btn_add_fixed_payment_cancel);
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

    private double getPaymentRate(int serviceID) {
        Payment payment = dataManager.getPayment(paymentPeriod, serviceID);
        FixedUtilityRate rate = (FixedUtilityRate) payment.getRate();
        return rate.getRateValue();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.btn_add_fixed_payment): {
                try {
                    replacePayment();
                } catch (NumberFormatException ex) {
                    Toast.makeText(this, R.string.wrong_rate_message, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case (R.id.btn_add_fixed_payment_cancel): {
                finish();
                break;
            }
        }
    }

    private void replacePayment() {
        double rate = Double.parseDouble(etRate.getText().toString());
        Payment payment = new FixedPayment(paymentPeriod, new FixedUtilityRate(rate));
        dataManager.addPayment(paymentPeriod, serviceID, payment);
        Toast.makeText(this, String.format("%s %s", paymentPeriod, getString(R.string.payment_added)), Toast.LENGTH_SHORT).show();
        finish();
    }
}

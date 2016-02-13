package com.felixfeatures.utilitiespayments;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.FixedPayment;
import com.felixfeatures.utilitiespayments.data.FixedUtilityRate;
import com.felixfeatures.utilitiespayments.data.Period;
import com.felixfeatures.utilitiespayments.data.Payment;
import com.felixfeatures.utilitiespayments.data.Service;

import java.util.Calendar;
import java.util.Locale;

public class AddFixedPaymentActivity extends AppCompatActivity
        implements View.OnClickListener, ReplacePaymentDialog.NoticeDialogListener {

    private final String TAG = "UtilityService_log";

    private DataManager dataManager = DataManager.getInstance(this);

    private TextView twName;
    private TextView twDate;
    private EditText etRate;

    private final Calendar calendar = Calendar.getInstance();

    private Service service;
    private int serviceID;
    private Period paymentPeriod;

    private int calendarPaymentMonth = calendar.get(Calendar.MONTH);
    private int calendarPaymentYear = calendar.get(Calendar.YEAR);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fixed_payment);
        initFields();
        initButtons();
    }

    private void initFields() {
        Intent intent = getIntent();
        service = getService(intent);
        Log.d(TAG, String.format("Service to add payment %s", service));

        String serviceName = service.getName();
        double previousPaymentRate = getPreviousPaymentRate(serviceID);

        twName = (TextView) findViewById(R.id.twName_fixed_payment);
        etRate = (EditText) findViewById(R.id.etRate_fixed_payment);
        twDate = (TextView) findViewById(R.id.twFixedPaymentDate);
        twDate.setOnClickListener(this);
        twName.setText(serviceName);
        twDate.setText(paymentPeriod.toString());
        if (previousPaymentRate > 0) {
            etRate.setText(String.format(Locale.ENGLISH, "%.2f", previousPaymentRate));
        }
    }

    private void initButtons() {
        Button btnAddPayment = (Button) findViewById(R.id.btn_add_fixed_payment);
        Button btnCancel = (Button) findViewById(R.id.btn_add_fixed_payment_cancel);
        btnAddPayment.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.twFixedPaymentDate): {
                showDatePicker();
                break;
            }
            case (R.id.btn_add_fixed_payment): {
                try {
                    addPayment(false);
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

    private void addPayment(boolean replacePayment) {
        if (service.getPayment(paymentPeriod) == null || replacePayment) {
            double rate = Double.parseDouble(etRate.getText().toString());
            Payment payment = new FixedPayment(paymentPeriod, new FixedUtilityRate(rate));
            dataManager.addPayment(paymentPeriod, serviceID, payment);
            Toast.makeText(this, String.format("%s %s", paymentPeriod, getString(R.string.payment_added)), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            showReplaceDialog();
        }
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
        return new Period(intent.getIntExtra(PaymentsFragment.EXTRA_KEY_MONTH, calendarPaymentMonth),
                intent.getIntExtra(PaymentsFragment.EXTRA_KEY_YEAR, calendarPaymentYear));
    }

    private Period getPreviousPaymentPeriod() {
        int month = paymentPeriod.getMonth() == 0 ? 11 : calendar.get(Calendar.MONTH) - 1;
        int year = paymentPeriod.getMonth() == 0 ? calendar.get(Calendar.YEAR) - 1 : calendar.get(Calendar.YEAR);
        return new Period(month, year);
    }

    private double getPreviousPaymentRate(int serviceID) {
        double previousRate = 0;
        Period previousPaymentPeriod = getPreviousPaymentPeriod();
        Payment previousPayment = dataManager.getPayment(previousPaymentPeriod, serviceID);
        if (previousPayment != null) {
            previousRate = previousPayment.getPaymentSum();
        }
        return previousRate;
    }

    private void showDatePicker() {
        MonthPickerDialog date = new MonthPickerDialog();
        date.setCallBackListener(new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                paymentPeriod = new Period(monthOfYear, year);
                twDate.setText(paymentPeriod.toString());

            }
        });
        date.show(getSupportFragmentManager(), getString(R.string.date_picker_dialog_tag));
    }

    private void showReplaceDialog() {
        ReplacePaymentDialog dialog = new ReplacePaymentDialog();
        dialog.show(getFragmentManager(), "Replace payment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        addPayment(true);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}

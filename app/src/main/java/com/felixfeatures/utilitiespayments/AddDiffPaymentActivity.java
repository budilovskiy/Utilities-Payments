package com.felixfeatures.utilitiespayments;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felixfeatures.utilitiespayments.data.CountedPayment;
import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.DiffCounterUtilityRate;
import com.felixfeatures.utilitiespayments.data.Period;
import com.felixfeatures.utilitiespayments.data.Payment;
import com.felixfeatures.utilitiespayments.data.Service;

import java.util.Calendar;
import java.util.Locale;

public class AddDiffPaymentActivity extends AppCompatActivity
        implements View.OnClickListener, ReplacePaymentDialog.NoticeDialogListener {

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

    private final Calendar calendar = Calendar.getInstance();

    private Service service;
    private int serviceID;
    private Period paymentPeriod;

    private int calendarPaymentMonth = calendar.get(Calendar.MONTH);
    private int calendarPaymentYear = calendar.get(Calendar.YEAR);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diff_payment);
        initFields();
        initButtons();
    }

    private void initFields() {
        Intent intent = getIntent();
        service = getService(intent);
        Log.d(TAG, String.format("Service to add payment %s", service));

        String serviceName = service.getName();
        double previousPaymentRate1 = getPreviousPaymentRate(serviceID, 1);
        double previousPaymentRate2 = getPreviousPaymentRate(serviceID, 2);
        double previousPaymentRate3 = getPreviousPaymentRate(serviceID, 3);
        int previousBorder12 = getPreviousBorder(serviceID, 1);
        int previousBorder23 = getPreviousBorder(serviceID, 2);
        int previousCounterData = getPreviousCounterData(serviceID);

        twName = (TextView) findViewById(R.id.twName_diff_payment);
        etRate1 = (EditText) findViewById(R.id.etRate1_diff);
        etRate2 = (EditText) findViewById(R.id.etRate2_diff);
        etRate3 = (EditText) findViewById(R.id.etRate3_diff);
        etBorder12 = (EditText) findViewById(R.id.etBorder1_diff);
        etBorder23 = (EditText) findViewById(R.id.etBorder2_diff);
        twDate = (TextView) findViewById(R.id.twDiffPaymentDate);
        twDate.setOnClickListener(this);
        twName.setText(serviceName);
        etCurrentData = (EditText) findViewById(R.id.etCurrent_diff_payment);
        etPreviousData = (EditText) findViewById(R.id.etPrevious_diff_payment);
        twDate.setText(paymentPeriod.toString());
        if (previousPaymentRate1 > 0) {
            etRate1.setText(String.format(Locale.ENGLISH, "%.3f", previousPaymentRate1));
        }
        if (previousPaymentRate2 > 0) {
            etRate2.setText(String.format(Locale.ENGLISH, "%.3f", previousPaymentRate2));
        }
        if (previousPaymentRate3 > 0) {
            etRate3.setText(String.format(Locale.ENGLISH, "%.3f", previousPaymentRate3));
        }
        if (previousBorder12 > 0) {
            etBorder12.setText(String.format(Locale.ENGLISH, "%d", previousBorder12));
        }
        if (previousBorder23 > 0) {
            etBorder23.setText(String.format(Locale.ENGLISH, "%d", previousBorder23));
        }
        if (previousCounterData > 0) {
            etPreviousData.setText(String.format(Locale.ENGLISH, "%d", previousCounterData));
        }
    }

    private void initButtons() {
        Button btnAddPayment = (Button) findViewById(R.id.btn_add_diff_payment);
        Button btnCancel = (Button) findViewById(R.id.btn_add_diff_payment_cancel);
        btnAddPayment.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.twDiffPaymentDate): {
                showDatePicker();
                break;
            }
            case (R.id.btn_add_diff_payment): {
                try {
                    addPayment(false);
                } catch (NumberFormatException ex) {
                    Toast.makeText(this, R.string.wrong_rate_message, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case (R.id.btn_add_diff_payment_cancel): {
                finish();
                break;
            }
        }
    }

    private void addPayment(boolean replacePayment) {
        if (service.getPayment(paymentPeriod) == null || replacePayment) {
            try {
                double rate1 = Double.parseDouble(etRate1.getText().toString());
                double rate2 = Double.parseDouble(etRate2.getText().toString());
                double rate3 = Double.parseDouble(etRate3.getText().toString());
                int border12 = Integer.parseInt(etBorder12.getText().toString());
                int border23 = Integer.parseInt(etBorder23.getText().toString());
                int previousData = Integer.parseInt(etPreviousData.getText().toString());
                int currentData = Integer.parseInt(etCurrentData.getText().toString());
                if (currentData > previousData) {
                    Payment payment = new CountedPayment(paymentPeriod, previousData, currentData,
                            new DiffCounterUtilityRate(rate1, border12, rate2, border23, rate3));
                    dataManager.addPayment(paymentPeriod, serviceID, payment);
                    Toast.makeText(this, String.format("%s %s", paymentPeriod,
                            getString(R.string.payment_added)), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, R.string.counter_data_error, Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException ex) {
                Toast.makeText(this, R.string.wrong_counter_data_or_rate, Toast.LENGTH_SHORT).show();
            }
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

    private double getPreviousPaymentRate(int serviceID, int rateNumber) {
        double previousRate = 0;
        Period previousPaymentPeriod = getPreviousPaymentPeriod();
        Payment previousPayment = dataManager.getPayment(previousPaymentPeriod, serviceID);
        if (previousPayment != null) {
            DiffCounterUtilityRate rate = (DiffCounterUtilityRate) previousPayment.getRate();
            switch (rateNumber) {
                case 1: {
                    previousRate = rate.getRateValue1();
                    break;
                }
                case 2: {
                    previousRate = rate.getRateValue2();
                    break;
                }
                case 3: {
                    previousRate = rate.getRateValue3();
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return previousRate;
    }

    private int getPreviousCounterData(int serviceID) {
        int previousCounterData = 0;
        Period previousPaymentPeriod = getPreviousPaymentPeriod();
        CountedPayment previousPayment = (CountedPayment) dataManager.getPayment(previousPaymentPeriod, serviceID);
        if (previousPayment != null) {
            previousCounterData = previousPayment.getCurrentCounterData();
        }
        return previousCounterData;
    }

    private int getPreviousBorder(int serviceID, int borderNumber) {
        int previousBorder = 0;
        Period previousPaymentPeriod = getPreviousPaymentPeriod();
        Payment previousPayment = dataManager.getPayment(previousPaymentPeriod, serviceID);
        if (previousPayment != null) {
            DiffCounterUtilityRate rate = (DiffCounterUtilityRate) previousPayment.getRate();
            switch (borderNumber) {
                case 1: {
                    previousBorder = rate.getBorder12();
                    break;
                }
                case 2: {
                    previousBorder = rate.getBorder23();
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return previousBorder;
    }

    private void showDatePicker() {
        MonthPickerDialog date = new MonthPickerDialog();
        date.setCallBackListener(new DatePickerDialog.OnDateSetListener() {
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

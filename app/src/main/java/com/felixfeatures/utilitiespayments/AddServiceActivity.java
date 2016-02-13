package com.felixfeatures.utilitiespayments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.Service;


public class AddServiceActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "UtilityService_log";

    private DataManager dataManager = DataManager.getInstance(this);

    private EditText etName;
    private CheckBox chkBoxCounter;
    private CheckBox chkBoxDiffRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);
        initFields();
        initButtons();
    }

    private void initFields() {
        etName = (EditText) findViewById(R.id.etName);
        chkBoxCounter = (CheckBox) findViewById(R.id.chkbox_counter);
        chkBoxCounter.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (buttonView.getId()) {
                    case R.id.chkbox_counter: {
                        if (isChecked) {
                            chkBoxDiffRate.setEnabled(true);
                        } else {
                            chkBoxDiffRate.setChecked(false);
                            chkBoxDiffRate.setEnabled(false);
                        }
                        break;
                    }
                }
            }
        });
        chkBoxDiffRate = (CheckBox) findViewById(R.id.chkbox_diff_rate);
    }

    private void initButtons() {
        Button btnAddService = (Button) findViewById(R.id.btn_add_service);
        Button btnCancel = (Button) findViewById(R.id.btn_add_service_cancel);
        btnAddService.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_service: {
                String serviceName = etName.getText().toString();
                Service.RateType serviceRateType = defineServiceRateType();
                Log.d(TAG, String.format("Adding service: %s", serviceName));
                dataManager.addService(serviceName, serviceRateType);
                Toast.makeText(this, String.format("%s %s %s", getString(R.string.service), serviceName, getString(R.string.added_for_service)),
                        Toast.LENGTH_SHORT).show();
                finish();
                break;
            }
            case R.id.btn_add_service_cancel: {
                finish();
                break;
            }
        }
    }

    private Service.RateType defineServiceRateType() {
        if (chkBoxCounter.isChecked()) {
            if (chkBoxDiffRate.isChecked()) {
                return Service.RateType.Differentiated;
            } else {
                return Service.RateType.Simple;
            }
        } else {
            return Service.RateType.Fixed;
        }
    }

}

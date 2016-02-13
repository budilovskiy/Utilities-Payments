package com.felixfeatures.utilitiespayments;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.Service;

public class EditServiceActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "UtilityService_log";

    private DataManager dataManager = DataManager.getInstance(this);

    private int serviceID;

    private EditText etName;
    private CheckBox chkBoxCounter;
    private CheckBox chkBoxDiffRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);
        serviceID = getIntent().getIntExtra(ServicesFragment.EXTRA_KEY_SERVICE_ID, -1);
        initFields(serviceID);
        initButtons();
    }

    private void initFields(int serviceID) {
        etName = (EditText) findViewById(R.id.etName);
        chkBoxCounter = (CheckBox) findViewById(R.id.chkbox_counter);
        chkBoxDiffRate = (CheckBox) findViewById(R.id.chkbox_diff_rate);
        Service service = dataManager.getService(serviceID);
        etName.setText(service.getName());
        Service.RateType rateType = service.getRateType();
        switch (rateType) {
            case Simple: {
                chkBoxCounter.setChecked(true);
                break;
            }
            case Differentiated: {
                chkBoxCounter.setChecked(true);
                chkBoxDiffRate.setChecked(true);
                break;
            }
        }
        chkBoxCounter.setEnabled(false);
        chkBoxDiffRate.setEnabled(false);
    }

    private void initButtons() {
        Button btnAddService = (Button) findViewById(R.id.btn_add_service);
        btnAddService.setText(R.string.save_btn_text);
        Button btnCancel = (Button) findViewById(R.id.btn_add_service_cancel);
        btnAddService.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_service: {
                String serviceName = etName.getText().toString();
                dataManager.replaceService(serviceID, serviceName);
                Toast.makeText(this, String.format("%s %s", serviceName, getString(R.string.saved)), Toast.LENGTH_SHORT).show();
                finish();
                break;
            }
            case R.id.btn_add_service_cancel: {
                finish();
                break;
            }
        }
    }
}

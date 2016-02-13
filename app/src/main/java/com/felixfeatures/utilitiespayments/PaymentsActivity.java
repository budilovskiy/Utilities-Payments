package com.felixfeatures.utilitiespayments;

import android.app.DialogFragment;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.Service;

import java.util.Calendar;

public class PaymentsActivity extends AppCompatActivity
        implements PaymentsFragment.OnFragmentInteractionListener, DeletePaymentDialog.NoticeDialogListener {

    private final String TAG = "UtilityService_log";

    private static final int MENU_ITEM_ID_ADD_PAYMENT = 0;
    private static final int MENU_ITEM_ID_CANCEL = 1;

    private TextView twServiceName;
    private Calendar calendar = Calendar.getInstance();
    private DataManager dataManager = DataManager.getInstance(this);
    private int serviceID;
    private Service service;
    private int deletePaymentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);
        serviceID = getIntent().getIntExtra(ServicesFragment.EXTRA_KEY_SERVICE_ID, -1);
        service = dataManager.getService(serviceID);
        twServiceName = (TextView) findViewById(R.id.twServiceName);
        twServiceName.setText(service.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayPayments();
    }

    private void displayPayments() {
        Log.d(TAG, String.format("Displaying %s payments", service.getName()));
        PaymentsFragment paymentsFragment = PaymentsFragment.newInstance(serviceID);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.payments_layout, paymentsFragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_ID_ADD_PAYMENT, 0, R.string.menu_item_add_payment);
        menu.add(0, MENU_ITEM_ID_CANCEL, 0, R.string.menu_item_cancel);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_ID_ADD_PAYMENT: {
                startAddPaymentActivity();
                break;
            }
            case MENU_ITEM_ID_CANCEL: {
                finish();
                break;
            }
            default: {
                break;
            }
        }
        return true;
    }

    private void startAddPaymentActivity() {
        Intent intent = null;
        switch (service.getRateType()) {
            case Fixed: {
                Log.d(TAG, "Adding fixed payment to " + service);
                intent = new Intent(this, AddFixedPaymentActivity.class);
                break;
            }
            case Simple: {
                Log.d(TAG, "Adding simple payment to " + service);
                intent = new Intent(this, AddSimplePaymentActivity.class);
                break;
            }
            case Differentiated: {
                Log.d(TAG, "Adding differentiated payment to " + service);
                intent = new Intent(this, AddDiffPaymentActivity.class);
                break;
            }
        }
        intent.putExtra(PaymentsFragment.EXTRA_KEY_YEAR, calendar.get(Calendar.YEAR));
        intent.putExtra(PaymentsFragment.EXTRA_KEY_MONTH, calendar.get(Calendar.MONTH));
        intent.putExtra(PaymentsFragment.EXTRA_KEY_SERVICE_ID, serviceID);
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(int paymentID) {
        deletePaymentID = paymentID;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        dataManager.deletePayment(serviceID, deletePaymentID);
        Log.d(TAG, String.format("Payment id %d was deleted from service %s (id: %d)", deletePaymentID, service.getName(), serviceID));
        displayPayments();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}

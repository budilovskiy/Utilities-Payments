package com.felixfeatures.utilitiespayments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.Period;
import com.felixfeatures.utilitiespayments.data.Payment;
import com.felixfeatures.utilitiespayments.data.Service;

import java.util.List;

/**
 * TODO comment
 */
public class PaymentsFragment extends Fragment {

    private final String TAG = "UtilityService_log";

    public static final String EXTRA_KEY_YEAR = "year";
    public static final String EXTRA_KEY_MONTH = "month";
    public static final String EXTRA_KEY_SERVICE_ID = "service id";

    private static final int CONTEXT_MENU_ITEM_ID_EDIT_PAYMENT = 0;
    private static final int CONTEXT_MENU_ITEM_ID_DELETE_PAYMENT = 1;

    private static final String ARG_SERVICE_ID = "Service id";

    private int serviceID;

    private ListView paymentsListView;

    private Service service;
    private DataManager dataManager;

    private OnFragmentInteractionListener mListener;

    public PaymentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ServicesFragment.
     */
    public static PaymentsFragment newInstance(int serviceId) {
        PaymentsFragment fragment = new PaymentsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SERVICE_ID, serviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payments, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dataManager = DataManager.getInstance(getActivity());
        if (getArguments() != null) {
            serviceID = getArguments().getInt(ARG_SERVICE_ID);
            Log.d(TAG, String.format("Getting arguments. serviceID: %d", serviceID));
            service = dataManager.getService(serviceID);
        }
        paymentsListView = (ListView) getActivity().findViewById(R.id.paymentsListView);
        registerForContextMenu(paymentsListView);
        displayPayments();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, CONTEXT_MENU_ITEM_ID_EDIT_PAYMENT, Menu.NONE, getString(R.string.edit_payment));
        menu.add(Menu.NONE, CONTEXT_MENU_ITEM_ID_DELETE_PAYMENT, Menu.NONE, getString(R.string.delete_service));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int paymentsLength = dataManager.getPaymentsSize(serviceID);
        int paymentID = paymentsLength - 1 - acmi.position;
        switch (item.getItemId()) {
            case CONTEXT_MENU_ITEM_ID_EDIT_PAYMENT: {
                Log.d(TAG, String.format("Starting EditPaymentActivity. Service ID: %d. Payment ID: %d",
                        serviceID, paymentID));
                startEditPaymentActivity(paymentID);
                break;
            }
            case CONTEXT_MENU_ITEM_ID_DELETE_PAYMENT: {
                Log.d(TAG, String.format("Starting to delete payment. Service ID: %d. Payment ID: %d",
                        serviceID, paymentID));
                deletePayment(paymentID);
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    private void startEditPaymentActivity(int paymentID) {
        Intent intent = null;
        switch (service.getRateType()) {
            case Fixed: {
                intent = new Intent(getActivity(), EditFixedPaymentActivity.class);
                break;
            }
            case Simple: {
                intent = new Intent(getActivity(), EditSimplePaymentActivity.class);
                break;
            }
            case Differentiated: {
                intent = new Intent(getActivity(), EditDiffPaymentActivity.class);
                break;
            }
        }
        Payment payment = dataManager.getPayment(serviceID, paymentID);
        Period paymentPeriod = payment.getPeriod();
        intent.putExtra(EXTRA_KEY_SERVICE_ID, serviceID);
        intent.putExtra(EXTRA_KEY_YEAR, paymentPeriod.getYear());
        intent.putExtra(EXTRA_KEY_MONTH, paymentPeriod.getMonth());
        startActivity(intent);
    }

    private void displayPayments() {
        List<Payment> payments = dataManager.getPayments(serviceID);
        Service.RateType rateType = dataManager.getService(serviceID).getRateType();
        PaymentsListItemAdapter paymentsListItemAdapter = new PaymentsListItemAdapter(getActivity(), payments, rateType);
        paymentsListView.setAdapter(paymentsListItemAdapter);
    }

    private void deletePayment(int position) {
        DeletePaymentDialog dialog = new DeletePaymentDialog();
        dialog.show(getActivity().getFragmentManager(), "Delete payment");
        mListener.onFragmentInteraction(position);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int paymentID);
    }
}

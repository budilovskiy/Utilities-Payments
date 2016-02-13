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
import android.widget.TextView;

import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.Period;
import com.felixfeatures.utilitiespayments.data.Payment;
import com.felixfeatures.utilitiespayments.data.Service;

import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ServicesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ServicesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServicesFragment extends Fragment {

    private static final int CONTEXT_MENU_ITEM_ID_DISPLAY_PAYMENTS = 0;
    private static final int CONTEXT_MENU_ITEM_ID_EDIT_SERVICE = 1;
    private static final int CONTEXT_MENU_ITEM_ID_DELETE_SERVICE = 2;

    public static final String EXTRA_KEY_SERVICE_ID = "Service id";

    private final String TAG = "UtilityService_log";

    private static final String ARG_MONTH = "period";
    private static final String ARG_YEAR = "year";

    private int currentMonth;
    private int currentYear;

    private TextView twTotalLabel;
    private TextView twSumTotal;
    private ListView servicesListView;

    private Period period;
    private DataManager dataManager;
    private ServicesListItemAdapter serviceItemAdapter;

    private static List<Service> services;

    private OnFragmentInteractionListener mListener;

    public ServicesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ServicesFragment.
     */
    public static ServicesFragment newInstance(int month, int year) {
        ServicesFragment fragment = new ServicesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_YEAR, year);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentMonth = getArguments().getInt(ARG_MONTH);
            currentYear = getArguments().getInt(ARG_YEAR);
            Log.d(TAG, String.format("Getting arguments. Month: %d, Year: %d", currentMonth, currentYear));
            period = new Period(currentMonth, currentYear);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_services, container, false);
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
        twSumTotal = (TextView) getActivity().findViewById(R.id.twSumTotal);
        servicesListView = (ListView) getActivity().findViewById(R.id.servicesListView);
        twTotalLabel = (TextView) getActivity().findViewById(R.id.twTotalLabel);
        registerForContextMenu(servicesListView);
        twTotalLabel.setText(String.format("%s ", period.toString()));
        displayServices();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, CONTEXT_MENU_ITEM_ID_DISPLAY_PAYMENTS, Menu.NONE, getString(R.string.display_payments));
        menu.add(Menu.NONE, CONTEXT_MENU_ITEM_ID_EDIT_SERVICE, Menu.NONE, getString(R.string.edit_service));
        menu.add(Menu.NONE, CONTEXT_MENU_ITEM_ID_DELETE_SERVICE, Menu.NONE, getString(R.string.delete_service));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONTEXT_MENU_ITEM_ID_DISPLAY_PAYMENTS: {
                AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                startPaymentsActivity(acmi.position);
                break;
            }
            case CONTEXT_MENU_ITEM_ID_EDIT_SERVICE: {
                AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                startEditServiceActivity(acmi.position);
                break;
            }
            case CONTEXT_MENU_ITEM_ID_DELETE_SERVICE: {
                AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                deleteService(acmi.position);
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    private void displayServices() {
        getServices();
        Log.d(TAG, String.format("%s services: %s", period.toString(),
                Arrays.toString(services.toArray())));
        serviceItemAdapter = new ServicesListItemAdapter(getActivity(), services, period);
        servicesListView.setAdapter(serviceItemAdapter);
        recalculateTotalSum();
    }

    private void startPaymentsActivity(int position) {
        Intent intent = new Intent(getActivity(), PaymentsActivity.class);
        intent.putExtra(EXTRA_KEY_SERVICE_ID, position);
        startActivity(intent);
    }

    private void startEditServiceActivity(int serviceID) {
        Log.d(TAG, "Editing service " + services.get(serviceID));
        Intent intent = new Intent(getActivity(), EditServiceActivity.class);
        intent.putExtra(EXTRA_KEY_SERVICE_ID, serviceID);
        startActivity(intent);
    }

    private void deleteService(int position) {
        DeleteServiceDialog dialog = new DeleteServiceDialog();
        dialog.show(getActivity().getFragmentManager(), "Delete service");
        mListener.onFragmentInteraction(position);
    }

    private void recalculateTotalSum() {
        double sum = 0;
        for (Service service : services) {
            Payment payment = service.getPayment(period);
            if (payment != null) {
                sum += payment.getPaymentSum();
            }
        }
        String totalSum = String.format("%.2f", sum);
        twSumTotal.setText(totalSum);
    }

    private void getServices() {
        dataManager = DataManager.getInstance(getActivity());
        services = dataManager.getServices();
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
        void onFragmentInteraction(int serviceID);
    }
}

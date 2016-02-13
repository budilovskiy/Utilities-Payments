package com.felixfeatures.utilitiespayments;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.felixfeatures.utilitiespayments.data.DataManager;
import com.felixfeatures.utilitiespayments.data.Period;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements ServicesFragment.OnFragmentInteractionListener, DeleteServiceDialog.NoticeDialogListener {

    public static String[] months;

    private static final int MENU_ITEM_ID_ADD_SERVICE = 1;
    private static final int MENU_ITEM_ID_EXIT = 2;

    private static final int NO_ANIMATION = 0;
    private static final int ANIMATION_RIGHT = 1;
    private static final int ANIMATION_LEFT = 2;
    private static final String TAG = "UtilityService_log";

    private Period currentPeriod;
    private Period minPeriod;
    private Period maxPeriod;
    private Calendar calendar = Calendar.getInstance();
    private DataManager dataManager;
    private GestureDetector gestureDetector;

    private int deleteServiceID;
    private SwipeGestureDetector swipeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeDetector = new SwipeGestureDetector();
        gestureDetector = new GestureDetector(this, swipeDetector);
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        findViewById(R.id.main_layout).setOnTouchListener(gestureListener);
        months = getResources().getStringArray(R.array.months_array);
        dataManager = DataManager.getInstance(this);
        dataManager.readServicesFromDB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentPeriod = getCurrentPeriod();
        displayServices(currentPeriod, NO_ANIMATION);
        minPeriod = dataManager.getMinPeriod();
        if (minPeriod == null) {
            minPeriod = currentPeriod;
        }
        maxPeriod = dataManager.getMaxPeriod();
        if (maxPeriod == null) {
            maxPeriod = currentPeriod;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dataManager.saveServicesToDB();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = swipeDetector.onTouchEvent(ev);
        if (!handled) {
            return super.dispatchTouchEvent(ev);
        }
        return handled;
    }

    /**
     * Adds new fragment that displays services with payments by the given period
     * with or without left or right swipe animation
     */
    private void displayServices(Period period, int animation) {
        currentPeriod = period;
        ServicesFragment servicesFragment = ServicesFragment.newInstance(period.getMonth(), period.getYear());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (animation) {
            case NO_ANIMATION: {
                break;
            }
            case ANIMATION_RIGHT: {
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                break;
            }
            case ANIMATION_LEFT: {
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            }
        }
        transaction.replace(R.id.main_layout, servicesFragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_ID_ADD_SERVICE, 0, R.string.menu_item_add_service);
        menu.add(0, MENU_ITEM_ID_EXIT, 0, R.string.menu_item_exit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_ID_ADD_SERVICE: {
                startAddServiceActivity();
                break;
            }
            case MENU_ITEM_ID_EXIT: {
                dataManager.saveServicesToDB();
                finish();
                break;
            }
            default: {
                break;
            }
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(int serviceID) {
        deleteServiceID = serviceID;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        dataManager.deleteService(deleteServiceID);
        displayServices(currentPeriod, NO_ANIMATION);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private void startAddServiceActivity() {
        Intent intent = new Intent(this, AddServiceActivity.class);
        startActivity(intent);
    }

    private Period getCurrentPeriod() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        return new Period(month, year);
    }

    private Period getPreviousPeriod() {
        int month = this.currentPeriod.getMonth() == 0 ? 11 : this.currentPeriod.getMonth() - 1;
        int year = this.currentPeriod.getMonth() == 0 ? this.currentPeriod.getYear() - 1 : this.currentPeriod.getYear();
        return new Period(month, year);
    }

    private Period getNextPeriod() {
        int month = this.currentPeriod.getMonth() == 11 ? 0 : this.currentPeriod.getMonth() + 1;
        int year = this.currentPeriod.getMonth() == 11 ? this.currentPeriod.getYear() + 1 : this.currentPeriod.getYear();
        return new Period(month, year);
    }

    private void onRightSwipe() {
        Period previousPeriod = getPreviousPeriod();
        Log.d(TAG, this.getLocalClassName() + ".onRightSwipe(). previousPeriod: " + previousPeriod);
        if (previousPeriod.compareTo(minPeriod) >= 0 && minPeriod.getYear() != 0) {
            displayServices(previousPeriod, ANIMATION_RIGHT);
        }
    }

    private void onLeftSwipe() {
        Period nextPeriod = getNextPeriod();
        Log.d(TAG, this.getLocalClassName() + ".onLeftSwipe(). nextPeriod: " + nextPeriod);
        if (nextPeriod.compareTo(maxPeriod) <= 0 || nextPeriod.compareTo(getCurrentPeriod()) <= 0) {
            displayServices(nextPeriod, ANIMATION_LEFT);
        }
    }

    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            MainActivity.this.onRightSwipe();
                        } else {
                            MainActivity.this.onLeftSwipe();
                        }
                    }
                    result = true;
                }
            } catch (Exception exception) {
                Log.e(MainActivity.TAG, "Error on gestures. " + exception.toString());
            }
            return result;
        }

        public boolean onTouchEvent(MotionEvent ev) {
            gestureDetector.onTouchEvent(ev);
            return MainActivity.this.onTouchEvent(ev);
        }
    }
}
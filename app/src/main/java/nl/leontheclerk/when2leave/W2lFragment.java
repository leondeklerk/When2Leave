package nl.leontheclerk.when2leave;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static android.content.Context.ALARM_SERVICE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class W2lFragment extends Fragment implements View.OnClickListener{
    View view;
    EditText etDate, etTime, etHour, etMinute, etDialog;
    Spinner spinnerHour, spinnerMinute;
    String[] arrayMinute = new String[60];
    String[] arrayHour = new String[48];
    ArrayAdapter<String> adapterHour, adapterMinute;
    AlertDialog.Builder alertBuilder;
    TimePickerDialog timePicker;
    DatePickerDialog datePicker;
    Button btnDate, btnTime, btnCalc, btnHour, btnMinute;
    NumberFormat formatter;
    AlertDialog.Builder builderHour, builderMinute;
    AlertDialog dialogHour, dialogMinute;
    Boolean switch_state, created, notification;
    LinearLayout layout;
    LinearLayout.LayoutParams params;
    SharedPreferences preferences;
    int output, alertTheme, leaveYear, curYear, curDay, curMonth, curHour, curMinute, pickerTheme, widthHeight, dimen, value;
    private int selecYear, selecMonth, selecDay, selecHour, selecMinute, travelHour, travelMinute;
    String result, curMonthF, curDayF, curHourF, curMinuteF, leaveMonthF, leaveDayF, leaveHourF, leaveMinuteF, curDate, leaveDate, curDateNoTime, leaveDateNoTime, outputHour, outputMinute;
    long curDateLong, leaveDateLong, millisValue;
    Set<String> set;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_w2l, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        alertTheme = R.style.AlertDialogTheme_dark;
        pickerTheme = R.style.PickerDialogTheme_dark;

        btnDate=view.findViewById(R.id.date);
        btnTime=view.findViewById(R.id.time);
        btnHour=view.findViewById(R.id.hour);
        btnMinute=view.findViewById(R.id.minute);
        btnCalc= view.findViewById(R.id.calc);
        etDate= view.findViewById(R.id.et_date);
        etTime=view.findViewById(R.id.et_time);
        etHour=view.findViewById(R.id.et_hour);
        etMinute=view.findViewById(R.id.et_minute);

        btnDate.setOnClickListener(this);
        btnTime.setOnClickListener(this);
        btnHour.setOnClickListener(this);
        btnMinute.setOnClickListener(this);
        btnCalc.setOnClickListener(this);

        spinnerHour = new Spinner(getContext(), Spinner.MODE_DIALOG);
        spinnerMinute = new Spinner(getContext(), Spinner.MODE_DIALOG);
        adapterHour = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, arrayHour);
        adapterMinute = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, arrayMinute);
        spinnerHour.setAdapter(adapterHour);
        spinnerMinute.setAdapter(adapterMinute);

        widthHeight = LinearLayout.LayoutParams.MATCH_PARENT;
        params = new LinearLayout.LayoutParams(widthHeight, widthHeight);
        dimen = getResources().getDimensionPixelSize(R.dimen.marginAlertDialog);
        formatter = new DecimalFormat("00");

        notification = false;

        for (int i = 0; i < arrayHour.length; i++) {
            arrayHour[i] = Integer.toString(i);
        }
        for (int i = 0; i < arrayMinute.length; i++) {
            arrayMinute[i] = Integer.toString(i);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.app_name);
    }

    @Override
    public void onClick(View v) {
        final Calendar cNow = Calendar.getInstance();
        layout = new LinearLayout(getContext());
        etDialog = new EditText(getContext());
        etDialog.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        etDialog.setInputType(InputType.TYPE_CLASS_NUMBER);
        etDialog.setRawInputType(Configuration.KEYBOARD_12KEY);
        params.setMargins(dimen, 0, dimen, 0);
        layout.addView(etDialog, params);
        switch_state = preferences.getBoolean("travel_switch", false);

        curYear = cNow.get(YEAR);
        curMonth = cNow.get(MONTH);
        curDay = cNow.get(DAY_OF_MONTH);
        curHour = cNow.get(HOUR_OF_DAY);
        curMinute = cNow.get(MINUTE);

        int id = v.getId();
        if (id == R.id.date) {
            datePicker = new DatePickerDialog(getContext(), pickerTheme, (view, year, month, day) -> {
                etDate.setText(String.format("%02d", day) + "-" + String.format("%02d", (month + 1)) + "-" + year);
                selecYear = year;
                selecMonth = month;
                selecDay = day;
            }, curYear, curMonth, curDay);
            datePicker.show();
        } else if (id == R.id.time) {
            timePicker = new TimePickerDialog(getContext(), pickerTheme, (view, hour, minute) -> {
                etTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
                selecHour = hour;
                selecMinute = minute;
            }, curHour, curMinute, true);
            timePicker.show();
        } else if (id == R.id.hour) {
            builderHour = new AlertDialog.Builder(requireContext(), alertTheme);
            if (switch_state) {
                builderHour.setTitle(getString(R.string.hour_fill_in));
                builderHour.setView(layout);
                builderHour.setPositiveButton(R.string.dialog_confirm, (dialog, whichButton) -> {
                    Editable input = etDialog.getText();

                    if (input.toString().equals("")) {
                        outputHour = "0";
                    } else {
                        outputHour = input.toString();
                    }
                    etHour.setText(outputHour);
                    travelHour = Integer.parseInt(outputHour);
                });
                builderHour.setNegativeButton(R.string.dialog_cancel, (dialog, whichButton) -> {
                });
                dialogHour = builderHour.create();
                dialogHour.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialogHour.show();
            } else {
                builderHour.setTitle(getString(R.string.hour_select)).setItems(arrayHour, (dialog, selected) -> {
                    etHour.setText(arrayHour[selected]);
                    travelHour = Integer.parseInt(arrayHour[selected]);
                });
                builderHour.setNegativeButton(R.string.dialog_cancel, (dialog, selected) -> {
                });
                builderHour.show();
            }
        } else if (id == R.id.minute) {
            builderMinute = new AlertDialog.Builder(requireContext(), alertTheme);
            if (switch_state) {
                builderMinute.setTitle(getString(R.string.minute_fill_in));
                builderMinute.setView(layout);
                builderMinute.setPositiveButton(R.string.dialog_confirm, (dialog, whichButton) -> {
                    Editable input = etDialog.getText();

                    if (input.toString().equals("")) {
                        outputMinute = "0";
                    } else {
                        outputMinute = input.toString();
                    }
                    etMinute.setText(outputMinute);
                    travelMinute = Integer.parseInt(outputMinute);
                });
                builderMinute.setNegativeButton(R.string.dialog_cancel, (dialog, whichButton) -> {
                });
                dialogMinute = builderMinute.create();
                dialogMinute.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialogMinute.show();
            } else {
                builderMinute.setTitle(getString(R.string.minute_select)).setItems(arrayMinute, (dialog, selected) -> {
                    etMinute.setText(arrayMinute[selected]);
                    travelMinute = Integer.parseInt(arrayMinute[selected]);
                });
                builderMinute.setNegativeButton(R.string.dialog_cancel, (dialog, selected) -> {
                });
                builderMinute.show();
            }
        } else if (id == R.id.calc) {
            alertBuilder = new AlertDialog.Builder(requireContext(), alertTheme);
            final Calendar cInput = Calendar.getInstance();
            cInput.set(selecYear, selecMonth, selecDay, selecHour, selecMinute, 0);
            cInput.set(Calendar.MILLISECOND, 0);
            cInput.add(HOUR_OF_DAY, -travelHour);
            cInput.add(MINUTE, -travelMinute);

            curMonthF = formatter.format(curMonth + 1);
            curDayF = formatter.format(curDay);
            curHourF = formatter.format(curHour);
            curMinuteF = formatter.format(curMinute);
            leaveYear = cInput.get(YEAR);
            leaveMonthF = formatter.format(cInput.get(MONTH) + 1);
            leaveDayF = formatter.format(cInput.get(DAY_OF_MONTH));
            leaveHourF = formatter.format(cInput.get(HOUR_OF_DAY));
            leaveMinuteF = formatter.format(cInput.get(MINUTE));

            curDate = String.format("%s%s%s%s%s", curYear, curMonthF, curDayF, curHourF, curMinuteF);
            leaveDate = String.format("%s%s%s%s%s", leaveYear, leaveMonthF, leaveDayF, leaveHourF, leaveMinuteF);
            curDateNoTime = String.format("%s%s%s", curYear, curMonthF, curDayF);
            leaveDateNoTime = String.format("%s%s%s", leaveYear, leaveMonthF, leaveDayF);

            curDateLong = Long.parseLong(curDate);
            leaveDateLong = Long.parseLong(leaveDate);

            notification = false;

            if (curDateLong > leaveDateLong) {
                output = 4;
            } else if (!curDate.equals(leaveDate) && leaveDateNoTime.equals(curDateNoTime)) {
                output = 1;
                notification = true;
            } else if (curDate.equals(leaveDate)) {
                output = 3;
            } else {
                output = 2;
                notification = true;
            }
            switch (output) {
                case 1:
                    result = getString(R.string.output_full) + " " + leaveHourF + ":" + leaveMinuteF + " " + getString(R.string.output_time_only);
                    break;
                case 2:
                    result = getString(R.string.output_full) + " " + leaveHourF + ":" + leaveMinuteF + " " + getString(R.string.output_inbtwn) + " " + leaveDayF + "-" + leaveMonthF + "-" + leaveYear;
                    break;
                case 3:
                    result = getString(R.string.output_now);
                    break;
                default:
                    result = getString(R.string.output_invalid);
                    break;
            }
            alertBuilder.setMessage(result).setPositiveButton(R.string.dialog_confirm, (dialog, id1) -> {
            });

            if (notification) {
                alertBuilder.setNeutralButton(R.string.set_notification, (dialogInterface, i) -> {

                    Set<String> setTest = new TreeSet<>();
                    set = preferences.getStringSet("notification_array", setTest);

                    cInput.getTimeInMillis();
                    value = (int) cInput.getTimeInMillis();

                    millisValue = cInput.getTimeInMillis();

                    created = false;
                    for (String holder : set) {
                        if (Integer.toString(value).equals(holder)) {
                            created = true;
                        }
                    }

                    if (!created) {

                        set = preferences.getStringSet("notification_array", setTest);
                        set.add(value + Long.toString(millisValue));
                        preferences.edit().putStringSet("notification_array", set).apply();

                        boolean workaround = preferences.getBoolean("workaround_switch", false);
                        workaround = !workaround;
                        preferences.edit().putBoolean("workaround_switch", workaround).apply();

                        Intent notificationIntent = new Intent(getContext(), NotificationPage.NotificationPublisher.class);
                        notificationIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                        notificationIntent.putExtra("ID", value);
                        PendingIntent pendingIntent = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            pendingIntent = PendingIntent.getBroadcast(getContext(), value, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                        } else {
                            pendingIntent = PendingIntent.getBroadcast(getContext(), value, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        }
                        AlarmManager alarmManager1 = (AlarmManager) requireActivity().getSystemService(ALARM_SERVICE);
                        alarmManager1.set(AlarmManager.RTC_WAKEUP, cInput.getTimeInMillis(), pendingIntent);
                    }
                });
            }
            alertBuilder.show();
        }
    }


}

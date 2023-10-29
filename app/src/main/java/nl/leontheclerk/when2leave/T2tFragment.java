package nl.leontheclerk.when2leave;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class T2tFragment extends Fragment implements View.OnTouchListener, View.OnClickListener {
    View view;
    EditText etKilometers;
    Button btnWalk, btnRun, btnCycle;
    ConstraintLayout touchInterceptor;
    int vId, walkSpeed, runSpeed, cycleSpeed, alertTheme;
    long hour, minute, second, longDistance, day;
    String input, output, suffixMetre, suffixSecond, suffixMinute, suffixHour, suffixDay, motion, space, and, gag, comma, stringMotion, stringDistance, stringDay, stringHour, stringMinute, stringSecond;
    Double distance, factor, result;
    SharedPreferences preferences;
    Rect outRect;
    InputMethodManager imm;
    AlertDialog.Builder alertBuilder;
    TextView text;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_t2t, container, false);
        getActivity().setTitle(R.string.menu_t2t);

        etKilometers = view.findViewById(R.id.et_km);
        btnWalk = view.findViewById(R.id.walk);
        btnRun = view.findViewById(R.id.run);
        btnCycle = view.findViewById(R.id.cycle);
        touchInterceptor = view.findViewById(R.id.touchInterceptor);
        text = view.findViewById(R.id.text_unit);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        alertTheme = R.style.AlertDialogTheme_dark;
        distance = 0.0;
        factor = 0.277778;

        etKilometers.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                input = etKilometers.getText().toString();
                if (input.equals("")) {
                    distance = 0.0;
                } else {
                    distance = Double.parseDouble(input);
                }

                text.setText(getString(R.string.unit_metre_p));
                if (distance == 1) {
                    text.setText(getString(R.string.unit_metre_s));
                }
            }
        });
        touchInterceptor.setOnTouchListener(this);
        btnWalk.setOnTouchListener(this);
        btnRun.setOnTouchListener(this);
        btnCycle.setOnTouchListener(this);
        btnWalk.setOnClickListener(this);
        btnRun.setOnClickListener(this);
        btnCycle.setOnClickListener(this);
        alertBuilder = new AlertDialog.Builder(requireContext(), alertTheme);

        space = " ";
        and = getString(R.string.and);
        gag = getString(R.string.t2t_gag);
        comma = getString(R.string.comma);

        return view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (etKilometers.isFocused()) {
                outRect = new Rect();
                etKilometers.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    etKilometers.clearFocus();
                    imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return false;
    }

    public void calculateTime(long seconds) {
        day = TimeUnit.SECONDS.toDays(seconds);
        hour = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
        minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
    }

    @Override
    public void onClick(View v) {
        vId = v.getId();
        if (distance > 0) {
            walkSpeed = Integer.parseInt(preferences.getString("speed_walk", "5"));
            runSpeed = Integer.parseInt(preferences.getString("speed_run", "10"));
            cycleSpeed = Integer.parseInt(preferences.getString("speed_cycle", "18"));
            if (vId == R.id.walk) {
                result = distance / (walkSpeed * factor);
                motion = getString(R.string.motion_walk);
            } else if (vId == R.id.run) {
                result = distance / (runSpeed * factor);
                motion = getString(R.string.motion_run);
            } else {
                result = distance / (cycleSpeed * factor);
                motion = getString(R.string.motion_cycle);
            }
            calculateTime(Math.round(result));

            suffixMetre = getString(R.string.unit_metre_s_l);
            suffixSecond = getString(R.string.unit_second_s);
            suffixMinute = getString(R.string.unit_minute_s);
            suffixHour = getString(R.string.unit_hour_s);
            suffixDay = getString(R.string.unit_day_s);

            if (distance != 1) {
                suffixMetre = getString(R.string.unit_metre_p_l);
            }
            if (second != 1) {
                suffixSecond = getString(R.string.unit_second_p);
            }
            if (minute != 1) {
                suffixMinute = getString(R.string.unit_minute_p);
            }
            if (hour != 1) {
                suffixHour = getString(R.string.unit_hour_p);
            }
            if (day != 1) {
                suffixDay = getString(R.string.unit_day_p);
            }
            longDistance = Math.round(distance);
            stringMotion = motion + space;
            stringDistance = longDistance + space + suffixMetre + space + gag;
            stringDay = space + day + space + suffixDay;
            stringHour = space + hour + space + suffixHour;
            stringMinute = space + minute + space + suffixMinute + space;
            stringSecond = space + second + space + suffixSecond;

            if (day == 0 && hour == 0 && minute == 0) {
                output = stringMotion + stringDistance + stringSecond;
            } else if (day == 0 && hour == 0 && second == 0) {
                output = stringMotion + stringDistance + stringMinute;
            } else if (day == 0 && minute == 0 && second == 0) {
                output = stringMotion + stringDistance + stringHour;
            } else if (hour == 0 && minute == 0 && second == 0) {
                output = stringMotion + stringDistance + stringDay;
            } else if (day == 0 && hour == 0) {
                output = stringMotion + stringDistance + stringMinute + and + stringSecond;
            } else if (day == 0) {
                output = stringMotion + stringDistance + stringHour + comma + stringMinute + and + stringSecond;
            } else if (second == 0) {
                output = stringMotion + stringDistance + stringDay + comma + stringHour + space + and + stringMinute;
            } else {
                output = stringMotion + stringDistance + stringDay + comma + stringHour + comma + stringMinute + and + stringSecond;
            }
        } else{
            output = getString(R.string.t2t_output_error);
        }
        alertBuilder.setMessage(output).setPositiveButton(R.string.dialog_confirm, (dialog, id) -> {
        });
        alertBuilder.show();
    }
}


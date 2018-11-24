package nl.leontheclerk.when2leave;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.ListView;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    ListView list;
    static SharedPreferences preferences;
    String themeId, editValue;
    int theme;
    PreferenceGroup pGrp;
    ListPreference listPref;
    EditTextPreference editTextPref;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list = getView().findViewById(android.R.id.list);
        list.setDivider(MainActivity.fetchColor(getActivity()));
        list.setDividerHeight(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initSummary(getPreferenceScreen());
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
        if (key.equals("theme_list")) {
            themeId = preferences.getString("theme_list", "1");
            switch (themeId) {
                case  "1": theme = R.style.Dark;
                    break;
                case  "2": theme = R.style.BlueGreen;
                    break;
                case  "3": theme = R.style.BlueGrey;
                    break;
                case  "4": theme = R.style.Brown;
                    break;
                case  "5": theme = R.style.Cyan;
                    break;
                case  "6": theme = R.style.DarkBlue;
                    break;
                case  "7": theme = R.style.Green;
                    break;
                case  "8": theme = R.style.LightBlue;
                    break;
                case  "9": theme = R.style.Orange;
                    break;
                case "10": theme = R.style.Pink;
                    break;
                case "11": theme = R.style.Purple;
                    break;
                case "12": theme = R.style.Red;
                    break;
                case "13": theme = R.style.Light;
                    break;
                default:   theme = R.style.Yellow;
                    break;
            }
            MainActivity.themeChanged = true;
            preferences.edit().putInt("theme_holder", theme).apply();
            getActivity().recreate();

        } else if (key.equals("speed_walk") || key.equals("speed_run") || key.equals("speed_cycle")) {
            editValue = ((EditTextPreference) findPreference(key)).getEditText().getText().toString();

            if (editValue.equals("") || Integer.parseInt(editValue) == 0) {
                ((EditTextPreference) findPreference(key)).setText("1");
            }
        }
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText() + " " + getString(R.string.unit));
        }
    }
}

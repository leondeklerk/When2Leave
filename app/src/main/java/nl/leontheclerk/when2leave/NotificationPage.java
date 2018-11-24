package nl.leontheclerk.when2leave;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static nl.leontheclerk.when2leave.NotificationPage.NotificationPublisher.removeLastChar;

public class NotificationPage extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    Toolbar toolbar;
    AlertDialog.Builder alertBuilder;
    static Button cancelButton;
    static SharedPreferences preferences;
    static int num, alertTheme;
    static Set<String> set, dateSet, setTest, removeSet;
    static TextView header;
    public static ListView notificationList;
    static List<String> arrayList;
    public static ArrayAdapter<String> adapterNotification;
    private static Context context;
    long millis;
    static int textLayout;
    static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putBoolean("destroyed", false).commit();
        setTheme(preferences.getInt("theme_holder", R.style.Dark));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_page);

        alertTheme = R.style.AlertDialogTheme_dark;
        int curtheme = preferences.getInt("theme_holder", R.style.Dark);
        if(curtheme == R.style.Yellow || curtheme == R.style.Light){
            textLayout = R.layout.list_item_dark_text;
        } else {
            textLayout = R.layout.list_item;
        }
        active = true;
        cancelButton = findViewById(R.id.cancel_button);
        header = findViewById(R.id.notification_header);
        notificationList = findViewById(R.id.not_list);
        notificationList.setOnItemClickListener(this);

        toolbar = findViewById(R.id.toolbarNotification);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cancelButton.setOnClickListener(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        context = getApplicationContext();
        setList(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onClick(View v) {
        if (v == cancelButton) {
            set = preferences.getStringSet("notification_array", setTest);
            for (String holder : set) {
                holder = removeLastChar(holder);
                Intent myIntent = new Intent(this, NotificationPublisher.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(holder), myIntent, 0);
                AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager1.cancel(pendingIntent);
            }
            set.clear();
            dateSet.clear();
            preferences.edit().putStringSet("notification_array", set).apply();

            Boolean workaround = preferences.getBoolean("workaround_switch", false);
            workaround = !workaround;
            preferences.edit().putBoolean("workaround_switch", workaround).apply();

            cancelButton.setAlpha(.5f);
            cancelButton.setEnabled(false);
            header.setText(R.string.notification_no_scheduled);
            arrayList = new ArrayList<>(dateSet);
            adapterNotification = new ArrayAdapter<>(this, textLayout, android.R.id.text1, arrayList);
            notificationList.setAdapter(adapterNotification);
        }
    }

    public static void setList(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        set = preferences.getStringSet("notification_array", setTest);
        dateSet = new TreeSet<>();
        for(String holder : set){
            dateSet.add(getDate(Long.parseLong(holder.substring(holder.length() - 13)), "dd/MM/yyyy HH:mm"));
        }
        if (active) {
            num = set.size();
            if (num < 1) {
                cancelButton.setAlpha(.5f);
                cancelButton.setEnabled(false);
                header.setText(R.string.notification_no_scheduled);
            } else {
                if(num > 1){
                    header.setText(String.format("%s%s%s%s%s", context.getString(R.string.noti_start_scheduled_p), " ", Integer.toString(num), " ", context.getString(R.string.noti_end_scheduled_p)));
                } else {
                    header.setText(String.format("%s%s%s%s%s", context.getString(R.string.noti_start_scheduled_s), " ", Integer.toString(num), " ", context.getString(R.string.noti_end_scheduled_s)));
                }
            }

            arrayList = new ArrayList<>(dateSet);
            adapterNotification = new ArrayAdapter<>(context, textLayout, android.R.id.text1, arrayList);
            notificationList.setAdapter(adapterNotification);

        }
    }

    public static String getDate(long milliSeconds, String dateFormat){
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
        alertBuilder = new AlertDialog.Builder(this, alertTheme);
        alertBuilder.setMessage(R.string.confirm_delete).setPositiveButton(R.string.confirm_delete_button, new DialogInterface.OnClickListener()  {
            public void onClick(DialogInterface dialog, int id) {
                String givenDateString = notificationList.getItemAtPosition(i).toString();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
                try {
                    Date mDate = sdf.parse(givenDateString);
                    millis = mDate.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                set = preferences.getStringSet("notification_array", setTest);
                removeSet = new TreeSet<>();
                for (String holder : set) {
                    String shortHolder = holder.substring(holder.length() - 13);
                    if(shortHolder.equals(Long.toString(millis))) {
                        Intent myIntent = new Intent(context, NotificationPublisher.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(removeLastChar(holder)), myIntent, 0);
                        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager1.cancel(pendingIntent);
                        removeSet.add(holder);
                    }
                }
                set.removeAll(removeSet);
                preferences.edit().putStringSet("notification_array", set).apply();

                Boolean workaround = preferences.getBoolean("workaround_switch", false);
                workaround = !workaround;
                preferences.edit().putBoolean("workaround_switch", workaround).apply();
                setList(NotificationPage.this);
            }
        });

        alertBuilder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertBuilder.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    public static class NotificationPublisher extends BroadcastReceiver {

        NotificationChannel Channel;
        NotificationManager notificationManager;
        long[] pattern;
        Context activityContext;
        Uri alarmSound;
        TaskStackBuilder stackBuilder;
        PendingIntent pIntent;
        Intent resultIntent;
        SharedPreferences preferences;
        int value;
        Set<String> set, removeSet;
        Set<String> setTest = new TreeSet<>();

        public void onReceive(Context context, Intent intent) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            activityContext = context;
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            pattern = new long[]{0, 800, 500, 800, 500, 800};

            resultIntent = new Intent(context, MainActivity.class);
            stackBuilder = TaskStackBuilder.create(context)
                    .addParentStack(MainActivity.class)
                    .addNextIntent(resultIntent);
            pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(context)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.not_t2l))
                    .setSmallIcon(R.mipmap.ic_directions_walk_white_24dp)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(context.getString(R.string.not_t2l))
                    .setVibrate(pattern)
                    .setLights(Color.WHITE, 3000, 3000)
                    .setSound(alarmSound)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(pIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId("1");
                createChannel(context);
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                builder.setShowWhen(true);
            }
            value = intent.getIntExtra("ID", (int)System.currentTimeMillis());
            notificationManager.notify(value, builder.build());
            set = preferences.getStringSet("notification_array", setTest);
            removeSet = new TreeSet<>();
            for(String holder : set){
                if(removeLastChar(holder).equals(Integer.toString(value))) {
                    removeSet.add(holder);
                }
            }
            set.removeAll(removeSet);
            preferences.edit().putStringSet("notification_array", set).apply();

            Boolean workaround = preferences.getBoolean("workaround_switch", false);
            workaround = !workaround;
            preferences.edit().putBoolean("workaround_switch", workaround).apply();
            setList(context);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void createChannel(Context context){
            Channel = new NotificationChannel("1", context.getString(R.string.not_channel_title), NotificationManager.IMPORTANCE_HIGH);
            Channel.setDescription(context.getString(R.string.not_channel_description));
            Channel.enableLights(true);
            Channel.enableVibration(true);
            Channel.setVibrationPattern(pattern);
            notificationManager.createNotificationChannel(Channel);
        }

        public static String removeLastChar(String str) {
            return str.substring(0, str.length() - 13);
        }

    }
}











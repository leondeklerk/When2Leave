package nl.leontheclerk.when2leave;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;

import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView nv;
    Fragment fragment;
    FragmentTransaction fragTrans;
    int id, menuItemId;
    Intent intent;
    private static final String MENU_ITEM = "menu_item",  TAG_MY_FRAGMENT = "myFragment";
    T2tFragment mFragment;
    public static boolean themeChanged = false;
    static SharedPreferences preferences;
    static TypedArray accent;
    static ColorDrawable color;
    Boolean firsInstall;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putBoolean("destroyed", false).apply();
        setTheme(preferences.getInt("theme_holder", R.style.Dark));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.menu_open, R.string.menu_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        nv = findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            displaySelectedScreen(R.id.nav_w2l);
        } else {
            mFragment = (T2tFragment) getSupportFragmentManager().findFragmentByTag(TAG_MY_FRAGMENT);
        }
        fragment = null;
        firsInstall = preferences.getBoolean("first_installation", true);
        if(firsInstall){
            Set<String> set = new TreeSet<>();
            preferences.edit().putStringSet("notification_array", set).apply();
            firsInstall = false;
            preferences.edit().putBoolean("first_installation", firsInstall).apply();
        }

        MainActivity.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void displaySelectedScreen(int itemId) {
        switch (itemId) {
            case R.id.nav_w2l:
                fragment = new W2lFragment();
                break;
            case R.id.nav_t2t:
                fragment = new T2tFragment();
                break;
            case R.id.nav_notifications:
                startActivity(intent = new Intent(this, NotificationPage.class));
                break;
            case R.id.nav_settings:
                startActivity(intent = new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_info:
                startActivity(intent = new Intent(this, AboutActivity.class));
                break;
        }

        if (fragment != null) {
            fragTrans = getSupportFragmentManager().beginTransaction();
            fragTrans.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            fragTrans.replace(R.id.content_frame, fragment);
            fragTrans.commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        id = item.getItemId();
        drawer.closeDrawer(GravityCompat.START);
        if(menuItemId != id){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    displaySelectedScreen(id);
                }
            }, 290);
        }
        if(id == R.id.nav_w2l || id == R.id.nav_t2t) {
            menuItemId = id;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MENU_ITEM, menuItemId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.menuItemId = savedInstanceState.getInt(MENU_ITEM);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(themeChanged) {
            themeChanged = false;
            recreate();
        }
    }

    public static ColorDrawable fetchColor(Activity activity) {
        if (preferences.getInt("theme_holder", R.style.Dark) == R.style.Dark) {
            color = new ColorDrawable(0x99E53935);
        } else {
            accent = activity.obtainStyledAttributes(new TypedValue().data, new int[]{R.attr.colorAccent});
            color = new ColorDrawable(accent.getColor(0, 0));
            accent.recycle();
        }
        return color;
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.edit().putBoolean("destroyed", true).commit();
    }
}

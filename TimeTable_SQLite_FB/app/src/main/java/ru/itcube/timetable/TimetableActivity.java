package ru.itcube.timetable;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimetableActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity);
        bundle = getIntent().getExtras();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());//создаем адаптер который будет возвращать фрагмент (один из 5)

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {

            private int scrollState = 0;
            @Override
            public void onPageScrollStateChanged(int state) {
                if(state==0 && scrollState == 1)//если значение state в предыдущей проверке было 1, а стало 0, значит пользователь пытается листать вправо от последней страницы, или влево от первой страницы
                {
                    if (tabLayout.getSelectedTabPosition() == 0)//если пользователь находится на первой странице, перелистываем в конец
                        mViewPager.setCurrentItem(tabLayout.getTabCount() - 1);
                    else//иначе пользователь находится на последней странице, перелистываем в начало
                        mViewPager.setCurrentItem(0);
                }
                scrollState = state;

            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        int day = new Date().getDay();//получаем текущую дату
        mViewPager.setCurrentItem(day  == 7? 0: day - 1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //Toast.makeText(this,"Hello" + String.valueOf(id), Toast.LENGTH_LONG).show();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Bundle b = new Bundle();
            b.putString("type", bundle.getString("type"));
            b.putString("value", bundle.getString("value"));
            getSupportActionBar().setTitle("Расписание: " + bundle.getString("value_title"));
            FragmentsTimetableList fragmentsList;
            switch (position){
                case 0:
                    b.putString("day", "1");
                    fragmentsList = new FragmentsTimetableList();
                    fragmentsList.setArguments(b);
                    return fragmentsList;
                case 1:
                    b.putString("day", "2");
                    fragmentsList = new FragmentsTimetableList();
                    fragmentsList.setArguments(b);
                    return fragmentsList;
                case 2:
                    b.putString("day", "3");
                    fragmentsList = new FragmentsTimetableList();
                    fragmentsList.setArguments(b);
                    return fragmentsList;
                case 3:
                    b.putString("day", "4");
                    fragmentsList = new FragmentsTimetableList();
                    fragmentsList.setArguments(b);
                    return fragmentsList;
                case 4:
                    b.putString("day", "5");
                    fragmentsList = new FragmentsTimetableList();
                    fragmentsList.setArguments(b);
                    return fragmentsList;
                case 5:
                    b.putString("day", "6");
                    fragmentsList = new FragmentsTimetableList();
                    fragmentsList.setArguments(b);
                    return fragmentsList;
                default:
                    return null;
            }
        }
        @Override
        public int getCount() {
            return 6;
        }

    }
}

package ru.itcube.timetable;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    DatabaseHelper sqlHelper;

    private boolean isDoneGetVersion = true;
    private boolean isDoneGetDB = true;

    long valid_version = 0;
    long version = 1;
    TextView textViewLoad;
    ProgressBar progressBar;
    Handler mHandler = new Handler();

    public static boolean isOnline(Context context)//метод для проверки подключения к сети Интернет
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
            return true;
        return false;
    }
    private Runnable updaterVersionRunnable = new Runnable() {//метод для проверки состояния чтения метаданных файла с облака
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void run() {//проверяем окончание загрузки
            if(isDoneGetVersion)
                return;
            if(!isOnline(getApplicationContext())) {
                textViewLoad.setText(getString(R.string.no_connection_to_get_all));//устанавливаем вывод на экран сообщений пользователю
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                textViewLoad.setTextColor(getColor(R.color.colorPrimaryRed));
            }
            else {
                textViewLoad.setText(getString(R.string.get_version));
                progressBar.setVisibility(ProgressBar.VISIBLE);
                textViewLoad.setTextColor(getColor(R.color.colorPrimaryGreen));
            }
            mHandler.postDelayed(this, 200);
        }
    };
    private Runnable updaterDownloadRunnable = new Runnable() {//метод для проверки состояния чтения файла из облака
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void run() {//проверяем окончание загрузки
            if(isDoneGetDB)
                return;
            if(!isOnline(getApplicationContext())) {//устанавливаем вывод на экран сообщений пользователю
                textViewLoad.setText(getString(R.string.no_connection));
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                textViewLoad.setTextColor(getColor(R.color.colorPrimaryRed));
            }
            else {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                textViewLoad.setText(getString(R.string.loading));
                textViewLoad.setTextColor(getColor(R.color.colorPrimaryGreen));
            }
            mHandler.postDelayed(this, 200);
        }
    };
    void setMainView()//метод для установки разметки MainApplication, с 3 вкладками
    {
        setContentView(R.layout.activity_main);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());//создаем адаптер который будет возвращать нужный нам фрагмент (один из 3)
        mViewPager = (ViewPager)findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {// выбор одиной из 3 вкладок сверху
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        mViewPager.setCurrentItem(0);
                        getSupportActionBar().setTitle(getString(R.string.app_name) + ": " + getString(R.string.tab_text_teacher));
                        break;
                    case 1:
                        mViewPager.setCurrentItem(1);
                        getSupportActionBar().setTitle(getString(R.string.app_name) + ": " + getString(R.string.tab_text_lesson));
                        break;
                    case 2:
                        mViewPager.setCurrentItem(2);
                        getSupportActionBar().setTitle(getString(R.string.app_name) + ": " + getString(R.string.tab_text_class));
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {//добавляем слушателя на значение скролла между страницами
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
        mViewPager.setCurrentItem(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wait_load);//устанавливаем временный View для MainActivity

        textViewLoad = (TextView)findViewById(R.id.bottom_text);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        sqlHelper = new DatabaseHelper(this);

        SharedPreferences settings = getSharedPreferences("Version", MODE_PRIVATE);
        version = settings.getLong("version", 0);//считываем из SharedPreferences время загрузки в облако версии базы данных, которая была последней загружена в память устройства

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.full_url)).child(DatabaseHelper.DB_NAME);

        if(!isOnline(this) && sqlHelper.database_exist()){//если в памяти есть БД, но доступа к сети нет
            setMainView();
            Toast.makeText(this, getString(R.string.no_connection_load_db), Toast.LENGTH_LONG).show();
        }
        else {
            isDoneGetVersion = false;
            mHandler.post(updaterVersionRunnable);//начинаем поток для проверки статуса скачивания метаданных

            Task<StorageMetadata> metadata = storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {//скачиваем текущий облачные метаданный базы данных
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {//получаем дату последнего обновления базы данных
                    isDoneGetVersion = true;//флаг окончания загрузки метаданных
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    valid_version = storageMetadata.getUpdatedTimeMillis();
                    if (sqlHelper.database_exist() && valid_version == version) {
                        setMainView();//если база данных существует и актуальная
                    } else {//иначе загружаем базу данных из хранилища FireBase
                        isDoneGetDB = false;
                        mHandler.post(updaterDownloadRunnable);

                        textViewLoad.setText(getString(R.string.loading));

                        progressBar.setVisibility(ProgressBar.VISIBLE);

                        final long MAX_MEGABYTES = 256 * 1024 * 1024;//максимальное количество байтов, которое приложение сможет загрузить из облака
                        storageRef.getBytes(MAX_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                sqlHelper.create_db(bytes);
                                settings.edit().putLong("version", valid_version).apply();//при успешном скачивании новой версии базы данных добавляем время ее добавления в облако в SharedPreferences
                                isDoneGetDB = true;
                                setMainView();
                            }
                        });
                    }
                }
            });
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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
        public Fragment getItem(int position) {//возвращаем FragmentsMainList в зависимости от того, на какой вкладке находимся
            Bundle b = new Bundle();
            FragmentsMainList fragmentsList;

            switch (position){
                case 0:
                    b.putString("type", DatabaseHelper.COLUMN_TEACHER);//вкладываем в объект типа Bundle ключ - "type", значение нужное название столбца нашей БД
                    fragmentsList = new FragmentsMainList();
                    fragmentsList.setArguments(b);//передаем в качестве аргумента название колонки, по которой нужно отобразить расписание
                    return fragmentsList;
                case 1:
                    b.putString("type", DatabaseHelper.COLUMN_LESSON);
                    fragmentsList = new FragmentsMainList();
                    fragmentsList.setArguments(b);
                    return fragmentsList;
                case 2:
                    b.putString("type", DatabaseHelper.COLUMN_CLASS);
                    fragmentsList = new FragmentsMainList();
                    fragmentsList.setArguments(b);
                    return fragmentsList;
                default:
                    return null;
            }
        }
        @Override
        public int getCount() {
            return 3;
        }

    }
}

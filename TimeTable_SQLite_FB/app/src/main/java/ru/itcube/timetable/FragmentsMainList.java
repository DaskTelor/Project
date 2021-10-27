package ru.itcube.timetable;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.ListFragment;

import java.sql.SQLException;

public class FragmentsMainList extends ListFragment {
    DatabaseHelper sqlHelper;
    NumerableSimpleCursorAdapter userAdapter;
    Cursor userCursor;
    ListView mList;
    Bundle bundle;
    public FragmentsMainList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_main_activity, container, false);
        mList = rootView.findViewById(android.R.id.list);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bundle = getArguments();//получаем bundle
        sqlHelper = new DatabaseHelper(getActivity());
    }
    @Override
    public void onResume() {
        super.onResume();

        try {
            sqlHelper.open();
            {
                userCursor = sqlHelper.database.rawQuery("select * from " + DatabaseHelper.TABLE + " group by " + bundle.getString("type"), null);//создаем курсор, который будет работать с нашей базой данных, извлекать данные, которые соответствуют запросу
                String[] headers = new String[]{bundle.getString("type")};
                userAdapter = new NumerableSimpleCursorAdapter(getActivity(), R.layout.simple_list_item, userCursor, headers, new int[]{R.id.result}, 0);//создаем адаптер, который будет заполнять наш ListView информацией из нашей БД
                mList.setAdapter(userAdapter);//устанавливаем адаптер для нашего ListView
            }
        }
        catch (SQLException ex){}
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor cursor = (Cursor)getListView().getItemAtPosition(position);
        Bundle b = new Bundle();
        //сохраняем в объекте Bundle необходимые нам параметры для последующего использование объекта для передачи в Intent (намерение)
        b.putString("type", bundle.getString("type") + "_id");
        b.putString("value_title", cursor.getString(cursor.getColumnIndex(bundle.getString("type"))));
        b.putString("value", cursor.getString(cursor.getColumnIndex(b.getString("type"))));
        Intent intent = new Intent();
        intent.setClass(getContext(), TimetableActivity.class);

        intent.putExtras(b);
        startActivity(intent);//запускаем следующую необходимую нам активность

    }


}
package ru.itcube.timetable;


import android.database.Cursor;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import androidx.fragment.app.ListFragment;

import java.sql.SQLException;

public class FragmentsTimetableList extends ListFragment {
    public FragmentsTimetableList() {
        // Required empty public constructor
    }

    DatabaseHelper sqlHelper;
    NumerableSimpleCursorAdapter userAdapter;
    Cursor userCursor;
    ListView mList;
    Bundle bundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_timetable_activity, container, false);

        mList = rootView.findViewById(android.R.id.list);
        return rootView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bundle = getArguments();
        sqlHelper = new DatabaseHelper(getActivity());
    }
    @Override
    public void onResume() {
        super.onResume();
        try {
            sqlHelper.open();

            userCursor = sqlHelper.database.rawQuery("select * from " + DatabaseHelper.TABLE +//как и в классе FragmentsMainList создаем запрос к базе данных
                    " where " + bundle.getString("type") + "=" + bundle.getString("value") +
                    " and " + DatabaseHelper.COLUMN_DAY + "=" + bundle.getString("day") + " order by " + DatabaseHelper.COLUMN_TIME, null);

            String[] headers = new String[]{"time","teacher","class", "lesson"};
            userAdapter = new NumerableSimpleCursorAdapter(getActivity(), R.layout.item, userCursor, headers, new int[]{R.id.time, R.id.teacher,R.id.class_, R.id.lesson},0);

            mList.setAdapter(userAdapter);//устанавливаем поля в элементы ListView, используя данный из БД
        }

        catch (SQLException ex){}
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);


    }

}
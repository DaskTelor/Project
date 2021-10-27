package ru.itcube.timetable;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

class NumerableSimpleCursorAdapter extends SimpleCursorAdapter {//курсор, который помимо обычной работы курсора еще и нумерует элементы LIstView по порядку
    NumerableSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        TextView numberTextView = (TextView)view.findViewById(R.id.numeric);
        numberTextView.setText(String.valueOf(cursor.getPosition() + 1));
    }
}
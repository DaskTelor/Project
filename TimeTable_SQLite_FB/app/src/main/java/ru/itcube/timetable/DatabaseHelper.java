
package ru.itcube.timetable;
import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_PROP_NAME = "db.properties";
    public static final String DB_NAME = "rasp.db";
    private static final int SCHEMA = 1; // версия базы данных
    static final String TABLE = "rasp";//название таблицы
    private static String DB_PATH;

    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_TEACHER = "teacher";
    public static final String COLUMN_TEACHER_ID = "teacher_id";
    public static final String COLUMN_LESSON = "lesson";
    public static final String COLUMN_LESSON_ID = "lesson_id";
    public static final String COLUMN_CLASS = "class";
    public static final String COLUMN_CLASS_ID = "class_id";


    public SQLiteDatabase database;
    private Context myContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, SCHEMA);
        myContext = context;
        DB_PATH = context.getDatabasePath(DB_NAME).getPath();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {

    }

    public boolean database_exist()//проверка базы данных на существование в памяти устройства
    {
        File file = new File(DB_PATH + DB_NAME);
        return file.exists();
    }
    public void create_db(byte[] bytes){//создает в памяти устройства базу данных, используя массив байтов
        OutputStream myOutput = null;
            String outFileName = DB_PATH + DB_NAME;
        try {
            myOutput = new FileOutputStream(outFileName);
            myOutput.write(bytes, 0, bytes.length);
            myOutput.flush();
            myOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void open() throws SQLException {
        String path = DB_PATH + DB_NAME;
        database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }
    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }
}

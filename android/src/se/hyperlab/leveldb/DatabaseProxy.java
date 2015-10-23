package se.hyperlab.leveldb;

import android.app.Activity;
import android.content.Context;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import java.io.File;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;

import com.snappydb.DB;
import com.snappydb.SnappyDB;
import com.snappydb.SnappydbException;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.json.JSONException;
import org.json.JSONObject;

@Kroll.proxy(creatableInModule=LevelDBModule.class)
public class DatabaseProxy extends KrollProxy {
    private static final String LCAT = "DatabaseProxy";

    private String dbName = "SnappyDB";
    private DB db = null;

    public DatabaseProxy() {
        super();
    }

    private void createDatabase(String name) {
        Log.d(LCAT, "Trying to create database " + name);
        try {
            db = new SnappyDB.Builder(TiApplication.getInstance().getApplicationContext())
                    .name(name)
                    .build();
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new IllegalStateException("Can't create database " + name);
        }
    }

    private DB getDatabase() {
        if(db != null) {
            return db;
        } else {
            throw new IllegalStateException("No database is open.");
        }
    }

    private KrollDict stringToDict(String data) {
        JSONObject jsonData = null;
        try {
            jsonData = new JSONObject(data);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return (KrollDict)KrollDict.fromJSON(jsonData);
    }

    @Override
    public void handleCreationDict(KrollDict options) {
        super.handleCreationDict(options);

        if(options.containsKey("name")) {
            dbName = options.getString("name");
        }

        createDatabase(dbName);
    }

    @Kroll.method
    public void setObject(String key, KrollDict data) {
        Log.d(LCAT, "Trying to set object " + key);
        try {
            DB db = getDatabase();
            db.put(key, data.toString());
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @Kroll.method
    public KrollDict getObject(String key) {
        Log.d(LCAT, "Trying to get object " + key);
        String rawData = null;
        KrollDict result = null;
        try {
            rawData = getDatabase().get(key);
            result = stringToDict(rawData);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Kroll.method
    public Object[] query(KrollDict options) {
        if(options.containsKey("prefix")) {
            String prefix = options.getString("prefix");
            String[] data;
            ArrayList<Object> result = new ArrayList<Object>();

            try {
                data = getDatabase().findKeys(prefix);

                for (int i = 0; i < data.length; i++) {
                    String key = data[i];

                    if(key.substring(prefix.length()).indexOf(".") == -1) {
                        result.add(getObject(key));
                    }
                }
            } catch (SnappydbException e) {
                e.printStackTrace();
            }

            return result.toArray();
        } else {
            return null;
        }
    }

    @Kroll.method
    public void deleteObject(String key) {
        Log.d(LCAT, "Trying to delete object " + key);
        try {
            getDatabase().del(key);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @Kroll.method
    public void deleteAllObjects() {
        Log.d(LCAT, "Tring to destroy database");
        try {
            getDatabase().destroy();
            createDatabase(dbName);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @Kroll.method
    public void close() {
        try {
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(Activity activity) {
        try {
            getDatabase().close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}
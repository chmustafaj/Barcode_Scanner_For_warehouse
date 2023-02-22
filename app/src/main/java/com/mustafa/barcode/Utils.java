package com.mustafa.barcode;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Utils {
    private static Utils instance;
    private final SharedPreferences sharedPreferences;
    private static final String CONFIGURATION_KEY="config_key";
    //Using gson and SharedPreferences to save the configuration
    private Utils(Context context) {
        sharedPreferences = context.getSharedPreferences("data_base", Context.MODE_PRIVATE);
        //The Utils will be a singleton class. There will be only one instance of AllBooks
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        //Creating the new arrays for books in gson
        if (null == getConfiguration()) {
            editor.putString(CONFIGURATION_KEY, gson.toJson(new ArrayList<String>()));
            editor.commit();
        }
    }
    //There will be only one instace of the utils class
    public static Utils getInstance(Context context) {
        if (null != instance) {
            return instance;
        } else {
            instance = new Utils(context);
            return instance;
        }

    }
    //Converting the gson file to and Array list and returning
    public ArrayList<String> getConfiguration() {
        Gson gson = new Gson();

        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> config = gson.fromJson(sharedPreferences.getString(CONFIGURATION_KEY, null), type);
        Log.d("TAG", "getConfiguration: "+config);

        return config;
    }
    public void editConfiguration(ArrayList<String> newConfig){
        //newConfig holds spreadsheet id, product sheet name, order sheet name, api key
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //Removing the array from the database, then adding the updated one
        editor.remove(CONFIGURATION_KEY);
        editor.putString(CONFIGURATION_KEY, gson.toJson(newConfig));
        editor.commit();

    }
}

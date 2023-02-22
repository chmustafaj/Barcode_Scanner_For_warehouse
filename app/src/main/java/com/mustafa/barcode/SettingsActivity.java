package com.mustafa.barcode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {
    private EditText sheetOrder, sheetProduct, url, edtApiKey;
    private Button done;
    public static String SheetUrl;
    public static String orderSheetName;
    public static String productSheetName;
    public static String apiKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SheetUrl =url.getText().toString();
                productSheetName=sheetProduct.getText().toString();
                orderSheetName=sheetOrder.getText().toString();
                apiKey=edtApiKey.getText().toString();
                String sheetID=getSheetIDFromURL(url.getText().toString());
                Log.d("TAG", "onClick: sheet id "+sheetID);
                ArrayList<String> newConfiguration= new ArrayList<>();
                newConfiguration.add(sheetID);
                newConfiguration.add(orderSheetName);
                newConfiguration.add(productSheetName);
                newConfiguration.add(apiKey);
                Log.d("TAG", "onClick: sheet id "+sheetID);
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                Utils.getInstance(SettingsActivity.this).editConfiguration(newConfiguration);
            }
        });
    }

    private String getSheetIDFromURL(String url) {
        String regex = "\\/d\\/(.*?)(\\/|$)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            return (matcher.group(1));
        }
        return null;
    }

    void initViews(){
        sheetOrder=findViewById(R.id.edtSheetOrder);
        sheetProduct=findViewById(R.id.edtSheetProduct);
        url=findViewById(R.id.edtUrl);
        done=findViewById(R.id.btnDone);
        edtApiKey =findViewById(R.id.apiKey);
    }
}
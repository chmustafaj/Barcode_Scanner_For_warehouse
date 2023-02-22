package com.mustafa.barcode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText barcodeText;
    private TextView orderQuantity, showQuantity;
    private final ArrayList<Product> products= new ArrayList<>();
    private final ArrayList<Order> orders = new ArrayList<>();
    private RecyclerView productRV;
    private RecyclerViewAdapter productRVAdapter;
    private Button btnSettings, btnNext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        getDataFromAPI();
        barcodeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Order o = findOrderById(barcodeText.getText().toString());
                if(o!=null){
                    showQuantity.setText(o.getQuantity());
                    ArrayList<Product> productIDs =findProductById(o.getProductCode());
                    // passing array list to our adapter class.
                    productRVAdapter = new RecyclerViewAdapter(productIDs, MainActivity.this);

                    // setting layout manager to our recycler view.
                    productRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                    // setting adapter to our recycler view.
                    productRV.setAdapter(productRVAdapter);
                }else{
                    Toast.makeText(MainActivity.this, "Order does not exist!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeText.setText("");
            }
        });

    }

    private Order findOrderById(String id) {
        for (Order o :orders){
            if(o.getOrderCode().equals(id)){
                return o;
            }
        }
        return null;
    }

    private ArrayList<Product> findProductById(String ids) {
        ids=ids.replaceAll("\\s", "");
        String[] productIds; //The products ids will be a single string of product ids separated by commas. We need to parse them
        productIds=ids.split(",");

        ArrayList<Product> productsInOrder = new ArrayList<>();
        for(String id:productIds){
            for (Product p :products){
                if(p.getCode().equals(id)){
                    productsInOrder.add(p);
                }
            }
        }

        return productsInOrder;
    }

    void initViews(){
        barcodeText= findViewById(R.id.barcode_text);
        productRV=findViewById(R.id.idRVUsers);
        orderQuantity=findViewById(R.id.txtOrderQuantity);
        btnSettings=findViewById(R.id.btnSettings);
        showQuantity=findViewById(R.id.txtShowQuantity);
        btnNext=findViewById(R.id.btnScanNext);

    }


    private void getDataFromAPI() {
        String order_spreadsheet_id, order_tab_name, api_key, products_spreadsheet_id, product_tab_name;
        ArrayList<String> configuration= new ArrayList<>();
        configuration=Utils.getInstance(MainActivity.this).getConfiguration();
        Log.d("TAG", "getDataFromAPI: configuratino "+configuration);
        if(configuration!=null && configuration.size()>0){
            product_tab_name=configuration.get(2);
            order_spreadsheet_id= configuration.get(0);
            order_tab_name=configuration.get(1);
            api_key=configuration.get(3);
            Log.d("TAG", "getDataFromAPI:product tab name "+product_tab_name);
            Log.d("TAG", "getDataFromAPI:order tab name "+order_tab_name);
            Log.d("TAG", "getDataFromAPI:id "+order_spreadsheet_id);
            Log.d("TAG", "getDataFromAPI:api key "+api_key);

            // creating a string variable for URL.
            String urlOrders="https://sheets.googleapis.com/v4/spreadsheets/" +
                    order_spreadsheet_id + "/values/" + order_tab_name +
                    "?alt=json&key=" + api_key;
            String urlProducts="https://sheets.googleapis.com/v4/spreadsheets/" +
                    order_spreadsheet_id + "/values/" + product_tab_name +
                    "?alt=json&key=" + api_key;
            // creating a new variable for our request queue
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

            // creating a variable for our JSON object request and passing our URL to it.
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlOrders, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //loadingPB.setVisibility(View.GONE);
                    try {
                        //JSONObject feedObj = response.getJSONObject("values");
                        JSONArray entryArray = response.getJSONArray("values");
                        for(int i=1; i<entryArray.length(); i++){
                            JSONArray row = entryArray.getJSONArray(i);
                            String orderCode= row.getString(0);
                            String productCode= row.getString(1);
                            String quantity=row.getString(2);
                            orders.add(new Order(orderCode, productCode, quantity));


                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // handling on error listener method.
                    Toast.makeText(MainActivity.this, "Fail to get data..", Toast.LENGTH_SHORT).show();
                }
            });
            // calling a request queue method
            // and passing our json object
            queue.add(jsonObjectRequest);
            // creating a variable for our JSON object request and passing our URL to it.
            JsonObjectRequest jsonObjectRequestProducts = new JsonObjectRequest(Request.Method.GET, urlProducts, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //loadingPB.setVisibility(View.GONE);
                    try {
                        //JSONObject feedObj = response.getJSONObject("values");
                        JSONArray entryArray = response.getJSONArray("values");
                        Log.d("TAG", "onResponse: entry array "+entryArray);
                        for(int i=1; i<entryArray.length(); i++){
                            JSONArray row = entryArray.getJSONArray(i);
                            Log.d("TAG", "onResponse: object  "+row);
                            Log.d("TAG", "onResponse: ");
                            String productCode= row.getString(0);
                            String productDesc= row.getString(1);
                            String productLocation=row.getString(2);
                            products.add(new Product(productCode, productDesc, productLocation));


                        }
                        Log.d("TAG", "onResponse: products"+products);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // handling on error listener method.
                    Toast.makeText(MainActivity.this, "Fail to get data..", Toast.LENGTH_SHORT).show();
                }
            });
            // calling a request queue method
            // and passing our json object
            queue.add(jsonObjectRequestProducts);
            showQuantity.setVisibility(View.VISIBLE);
        }


    }
}
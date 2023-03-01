package com.mustafa.barcode;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OrdersFragment extends Fragment {
    private EditText barcodeText;
    private TextView showTotalQuantity, showNoOfProducts;
    private Button btnSettings, btnNext;
    private ArrayList<Order> orders;
    private final ArrayList<OrdersSheetRow> ordersSheetRows = new ArrayList<>();
    public static Order orderCurrentlyScanning;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_orders, container, false);
        orders=new ArrayList<>();
        initViews(view);
        barcodeText.requestFocus();
        getOrdersFromSheets();

        barcodeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                orderCurrentlyScanning = findOrderById(barcodeText.getText().toString());
                if(orderCurrentlyScanning!=null){
                    showTotalQuantity.setText(Integer.toString(orderCurrentlyScanning.getTotalQuantity()));
                    showNoOfProducts.setText(Integer.toString(orderCurrentlyScanning.getNoOfProducts()));
                    // passing array list to our adapter class.
                }else{
                    if(!barcodeText.getText().toString().equals("")){
                        Toast.makeText(getActivity(), "Order does not exist!", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeText.setText("");
                showTotalQuantity.setText("-");
                showNoOfProducts.setText("-");

            }
        });

        return view;
    }

    private void initOrders(ArrayList<OrdersSheetRow> rows) {
        Log.d(TAG, "initOrders: rows "+rows);
        for(OrdersSheetRow ordersSheetRow:rows){
            boolean orderPresentInList=false;
            for(Order order:orders){
                if(order.getOrderCode().equals(ordersSheetRow.getOrderCode())){
                    orderPresentInList=true;
                }
            }
            if(!orderPresentInList){
                Order order = new Order();
                int noOfProductsInOrder=0;
                ArrayList<Pair<String,Integer>> productList = new ArrayList<>();
                for(OrdersSheetRow ordersSheetRow1:rows){
                    if(ordersSheetRow.getOrderCode().equals(ordersSheetRow1.getOrderCode())){
                        noOfProductsInOrder++;
                        productList.add(new Pair<String, Integer>(ordersSheetRow1.getProductCode(),Integer.parseInt(ordersSheetRow1.getProductQuantity())));
                    }
                }
                order.setProductList(productList);
                order.setOrderCode(ordersSheetRow.getOrderCode());
                order.setNoOfProducts(noOfProductsInOrder);
                int totalProducts=0;
                for(Pair<String, Integer> productQuantityPair:productList){
                    totalProducts=totalProducts+productQuantityPair.second;
                }
                order.setTotalQuantity(totalProducts);
                orders.add(order);
            }

        }

    }

    void initViews(View v){
        barcodeText= v.findViewById(R.id.product_barcode);
        btnSettings=v.findViewById(R.id.btnSettings);
        showTotalQuantity =v.findViewById(R.id.txtShowQuantity);
        showNoOfProducts=v.findViewById(R.id.txtShowNoOfProducts);
        btnNext=v.findViewById(R.id.btnNext);

    }

    private void getOrdersFromSheets() {
        String order_spreadsheet_id, order_tab_name, api_key;
        ArrayList<String> configuration= new ArrayList<>();
        configuration=Utils.getInstance(getActivity()).getConfiguration();
        Log.d("TAG", "getDataFromAPI: configuratino "+configuration);
        if(configuration!=null && configuration.size()>0){
            order_spreadsheet_id= getSheetIDFromURL(configuration.get(0));
            order_tab_name=configuration.get(1);
            api_key=configuration.get(3);

            // creating a string variable for URL.
            String urlOrders="https://sheets.googleapis.com/v4/spreadsheets/" +
                    order_spreadsheet_id + "/values/" + order_tab_name +
                    "?alt=json&key=" + api_key;

            // creating a new variable for our request queue
            RequestQueue queue = Volley.newRequestQueue(getContext());

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
                            OrdersSheetRow ordersSheetRow= new OrdersSheetRow(orderCode, productCode, quantity,i);
                            Log.d(TAG, "onResponse: ordersheetrow "+ordersSheetRow);
                            ordersSheetRows.add(ordersSheetRow);
                        }
                        Log.d(TAG, "onResponse: ordersheet rows"+ordersSheetRows);
                        initOrders(ordersSheetRows);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // handling on error listener method.
                    Toast.makeText(getActivity(), "Fail to get data..", Toast.LENGTH_SHORT).show();
                }
            });
            // calling a request queue method
            // and passing our json object
            queue.add(jsonObjectRequest);
            // creating a variable for our JSON object request and passing our URL to it.

            //showQuantity.setVisibility(View.VISIBLE);
        }


    }
    private Order findOrderById(String id) {
        for (Order o :orders){
            if(o.getOrderCode().equals(id)){
                return o;
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeText.requestFocus();
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
}

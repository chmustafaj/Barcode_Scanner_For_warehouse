package com.mustafa.barcode;

import static com.mustafa.barcode.OrdersFragment.orderCurrentlyScanning;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AllProductsFragment extends Fragment {
    EditText edtProductBarcode;
    TextView txtProductCode, txtProductDesc, txtProductLoc;
    private Button btnNext, btnWipeAll;
    private ArrayList<Product> products;
    private RelativeLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_products, container, false);
        initViews(view);
        edtProductBarcode.requestFocus();
        products = new ArrayList<>();
        getProductsFromSheets();
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Product p = findProductById(edtProductBarcode.getText().toString());
                if (p != null) {
                    displayProduct(p);
                } else {

                    if (!edtProductBarcode.getText().toString().equals("")) {
                        edtProductBarcode.setText("");
                        edtProductBarcode.requestFocus();
                        Snackbar snackbar = Snackbar.make(layout, "Product Not Found!", Snackbar.LENGTH_INDEFINITE).setTextColor(Color.RED).setBackgroundTint(Color.WHITE);
                        snackbar.setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }
                }
            }
        };
        edtProductBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!edtProductBarcode.getText().toString().equals("")) {
                    handler.removeCallbacks(runnable);

                    // Schedule the runnable to run after 1 second
                    handler.postDelayed(runnable, 500);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtProductBarcode.setText("");
                edtProductBarcode.requestFocus();
                txtProductCode.setText("-");
                txtProductDesc.setText("-");
                txtProductLoc.setText("-");
            }
        });
        btnWipeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetProductInfo();
                edtProductBarcode.setText("");
                ProductsFragment.resetOrderScreen=true;
                ProductsFragment.productCurrentlyScanning=null;
                OrdersFragment.orderCurrentlyScanning=null;
            }
        });

        return view;
    }

    private void displayProduct(Product p) {
        txtProductLoc.setText(p.getLocation());
        txtProductDesc.setText(p.getDescription());
        txtProductCode.setText(p.getCode());
    }
    private void resetProductInfo(){
        txtProductLoc.setText("-");
        txtProductDesc.setText("-");
        txtProductCode.setText("-");
    }

    void initViews(View view) {
        edtProductBarcode = view.findViewById(R.id.edtProductBarcode);
        txtProductCode = view.findViewById(R.id.txtShowProductCode);
        txtProductDesc = view.findViewById(R.id.txtShowDesc);
        txtProductLoc = view.findViewById(R.id.txtShowLoc);
        btnNext = view.findViewById(R.id.btnNext);
        layout = view.findViewById(R.id.layout);
        btnWipeAll=view.findViewById(R.id.btnWipeAllAllProducts);
    }

    private void getProductsFromSheets() {
        String order_spreadsheet_id, api_key, products_spreadsheet_id, product_tab_name;
        ArrayList<String> configuration = new ArrayList<>();
        configuration = Utils.getInstance(getActivity()).getConfiguration();
        Log.d("TAG", "getDataFromAPI: configuratino " + configuration);
        if (configuration != null && configuration.size() > 0) {
            product_tab_name = configuration.get(2);
            order_spreadsheet_id = getSheetIDFromURL(configuration.get(0));
            api_key = configuration.get(3);
            String urlProducts = "https://sheets.googleapis.com/v4/spreadsheets/" +
                    order_spreadsheet_id + "/values/" + product_tab_name +
                    "?alt=json&key=" + api_key;
            RequestQueue queue = Volley.newRequestQueue(getContext());
            JsonObjectRequest jsonObjectRequestProducts = new JsonObjectRequest(Request.Method.GET, urlProducts, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //loadingPB.setVisibility(View.GONE);
                    try {
                        //JSONObject feedObj = response.getJSONObject("values");
                        JSONArray entryArray = response.getJSONArray("values");
                        Log.d("TAG", "onResponse: entry array " + entryArray);
                        for (int i = 1; i < entryArray.length(); i++) {
                            JSONArray row = entryArray.getJSONArray(i);
                            Log.d("TAG", "onResponse: object  " + row);
                            Log.d("TAG", "onResponse: ");
                            String productCode = row.getString(0);
                            String productDesc = row.getString(1);
                            String productLocation = row.getString(2);
                            products.add(new Product(productCode, productDesc, productLocation));


                        }
                        Log.d("TAG", "onResponse: products" + products);

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
            queue.add(jsonObjectRequestProducts);
        }
    }

    private Product findProductById(String id) {
        for (Product p : products) {
            if (p.getCode().equals(id)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        edtProductBarcode.requestFocus();
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
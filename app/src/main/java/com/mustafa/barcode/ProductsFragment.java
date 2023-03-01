package com.mustafa.barcode;

import static android.content.ContentValues.TAG;
import static com.mustafa.barcode.OrdersFragment.orderCurrentlyScanning;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class ProductsFragment extends Fragment {
    EditText edtProductBarcode;
    RelativeLayout layout;
    private Button btnSkip;
    private ArrayList<Product> products;
    private ArrayList<Product> productsInOrder;
    private Product productCurrentlyScanning;
    private ImageView checkMark;
    private int noOfProductsToScan, totalNoOfProducts, productListIterator;
    //TabLayout tabLayout;
    ArrayList<Pair<String, Integer>> productList = new ArrayList<>();
    Product p;
    TextView txtNoOfProductsToScan, txtProductCode, txtProductDesc, txtProductLoc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        initViews(view);
        edtProductBarcode.requestFocus();
        products = new ArrayList<>();
        productsInOrder = new ArrayList<>();
        getProductsFromSheets();
        edtProductBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                p = findProductById(edtProductBarcode.getText().toString());
                displayProductInfo();
                if(edtProductBarcode.getText().toString().length()>=12){
                    if (productCurrentlyScanning.equals(p)) {
                        if (noOfProductsToScan > 0) {
                            noOfProductsToScan--;
                        }
                        if (noOfProductsToScan >= 1) {
                            scanProducts(productList.get(productListIterator).first, noOfProductsToScan);
                        } else {
                            nextProduct();
                        }
                        edtProductBarcode.setText("");
                    } else {
                        Toast.makeText(getActivity(), "Wrong Product/Incorrect Quantity Picked", Toast.LENGTH_SHORT).show();
                        edtProductBarcode.setText("");
                        edtProductBarcode.requestFocus();

                    }
                    displayProductInfo();
                    Log.d(TAG, "onClick: productListIterator " + productListIterator);
                    Log.d(TAG, "onClick: productListSize " + (productList.size() - 1));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               nextProduct();
               displayProductInfo();

            }
        });
        return view;
    }
    void nextProduct(){
        if (productListIterator < (productList.size()-1 )) {
            productListIterator++;
            noOfProductsToScan = productList.get(productListIterator).second;
            totalNoOfProducts=noOfProductsToScan;
            scanProducts(productList.get(productListIterator).first, productList.get(productListIterator).second);
            showCheckMark();
        } else {
            Snackbar snackbar
                    = Snackbar
                    .make(
                            layout,
                            "Order Completed",
                            Snackbar.LENGTH_LONG)
                    .setAction(
                            "Scan Next",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    MainActivity.selectPage(0);
                                }
                            });

            snackbar.show();

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        edtProductBarcode.requestFocus();
        productListIterator=0;
        Log.d(TAG, "onResume: order currently scanning " + orderCurrentlyScanning);
        if (orderCurrentlyScanning != null) {
            productList = OrdersFragment.orderCurrentlyScanning.getProductList();
            productCurrentlyScanning = findProductById(productList.get(0).first);
            noOfProductsToScan = productList.get(0).second;
            Log.d(TAG, "onResume: no of products to scan: "+noOfProductsToScan);
            totalNoOfProducts=noOfProductsToScan;
            displayProductInfo();
            Log.d(TAG, "onResume: product currently scanning " + productCurrentlyScanning);
        }

    }

    private void scanProducts(String first, Integer second) {
        productCurrentlyScanning = findProductById(first);
        noOfProductsToScan = second;
    }

    private void displayProductInfo() {
        txtNoOfProductsToScan.setText(noOfProductsToScan +"/"+totalNoOfProducts);
        txtProductLoc.setText(productCurrentlyScanning.getLocation());
        txtProductDesc.setText(productCurrentlyScanning.getDescription());
        txtProductCode.setText(productCurrentlyScanning.getCode());
    }

    void initViews(View view) {
        layout = view.findViewById(R.id.relLayout);
        edtProductBarcode = view.findViewById(R.id.product_barcode);
        txtNoOfProductsToScan = view.findViewById(R.id.txtShowNoOfProductsToScan);
        txtProductCode = view.findViewById(R.id.txtShowProductCode);
        txtProductDesc = view.findViewById(R.id.txtShowLoc);
        txtProductLoc = view.findViewById(R.id.txtLoc);
        checkMark=view.findViewById(R.id.checkMark);
        btnSkip=view.findViewById(R.id.btnSkip);
    }

    private void getProductsFromSheets() {
        String order_spreadsheet_id, api_key, product_tab_name;
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
    private void showCheckMark(){
        int animationTime=2000;
        checkMark.setVisibility(View.VISIBLE);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(animationTime);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(animationTime);
        fadeOut.setDuration(animationTime);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        checkMark.setAnimation(animation);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkMark.setVisibility(View.GONE);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable,animationTime);


    }

    private Product findProductById(String id) {
        for (Product p : products) {
            if (p.getCode().equals(id)) {
                return p;
            }
        }
        return null;
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
package com.mustafa.barcode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private final ArrayList<Product> products;
    private final Context context;
    public  RecyclerViewAdapter(ArrayList<Product> products, Context context){
        this.products = products;
        this.context=context;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.code.setText(products.get(position).getCode());
        holder.loc.setText(products.get(position).getLocation());
        holder.desc.setText(products.get(position).getDescription());

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView code;
        private final TextView desc;
        private final TextView loc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            code=itemView.findViewById(R.id.txtProductCode);
            desc=itemView.findViewById(R.id.txtProductDescription);
            loc=itemView.findViewById(R.id.txtLoc);
        }
    }
}

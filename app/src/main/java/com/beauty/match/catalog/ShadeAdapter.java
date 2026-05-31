package com.beauty.match.catalog;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beauty.match.R;
import com.beauty.match.model.Shade;

import java.util.ArrayList;
import java.util.List;

public class ShadeAdapter extends RecyclerView.Adapter<ShadeAdapter.ShadeViewHolder> {

    private List<Shade> shades = new ArrayList<>();
    private OnShadeClickListener listener;

    public interface OnShadeClickListener {
        void onShadeClick(Shade shade);
    }

    public void setOnShadeClickListener(OnShadeClickListener listener) {
        this.listener = listener;
    }

    public void setShades(List<Shade> shades) {
        if (shades == null) {
            this.shades = new ArrayList<>();
        } else {
            this.shades = new ArrayList<>(shades);
        }
        notifyDataSetChanged();
    }

    public List<Shade> getShades() {
        return new ArrayList<>(shades);
    }

    @NonNull
    @Override
    public ShadeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shade, parent, false);
        return new ShadeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShadeViewHolder holder, int position) {
        Shade shade = shades.get(position);
        holder.bind(shade);
    }

    @Override
    public int getItemCount() {
        return shades.size();
    }

    class ShadeViewHolder extends RecyclerView.ViewHolder {
        private View vColor;
        private TextView tvBrand, tvCode, tvName, tvDetails;

        ShadeViewHolder(@NonNull View itemView) {
            super(itemView);
            vColor = itemView.findViewById(R.id.v_shade_color);
            tvBrand = itemView.findViewById(R.id.tv_shade_brand);
            tvCode = itemView.findViewById(R.id.tv_shade_code);
            tvName = itemView.findViewById(R.id.tv_shade_name);
            tvDetails = itemView.findViewById(R.id.tv_shade_details);
            
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onShadeClick(shades.get(pos));
                }
            });
        }

        void bind(Shade shade) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(shade.getColorRgb());
            drawable.setStroke(2, Color.LTGRAY);
            vColor.setBackground(drawable);

            tvBrand.setText(safe(shade.getBrandName()));
            tvCode.setText(safe(shade.getShadeCode()));
            tvName.setText(safe(shade.getShadeName()));
            
            String details = safe(shade.getProductLine()) + " • " + 
                    capitalize(safe(shade.getUndertone())) + " • " + 
                    capitalize(safe(shade.getCoverage())) + " coverage • " +
                    capitalize(safe(shade.getFinish()));
            tvDetails.setText(details);
        }
        
        private String capitalize(String input) {
            if (input == null || input.isEmpty()) return "";
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        }

        private String safe(String value) {
            return value != null ? value : "-";
        }
    }
}

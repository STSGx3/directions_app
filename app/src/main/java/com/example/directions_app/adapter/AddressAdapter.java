package com.example.directions_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.directions_app.R;
import com.example.directions_app.model.Address;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private String keyword = "";
    private Context context;

    public AddressAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Address> list, String keyword) {
        this.addressList = list;
        this.keyword = keyword != null ? keyword : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        String name = address.getDisplayName();

        // Loại bỏ zip code nếu có (các số ở cuối)
        name = name.replaceAll(",?\\s*\\d{4,5}", "");

        if (!keyword.isEmpty()) {
            String lowerName = name.toLowerCase();
            String lowerKeyword = keyword.toLowerCase();

            int start = lowerName.indexOf(lowerKeyword);
            if (start >= 0) {
                SpannableString spannable = new SpannableString(name);
                // In đậm
                spannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + keyword.length(),
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                // Màu đen
                spannable.setSpan(new ForegroundColorSpan(Color.BLACK), start, start + keyword.length(),
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.tvName.setText(spannable);
            } else {
                holder.tvName.setText(name); // không highlight
            }
        } else {
            holder.tvName.setText(name); // keyword trống
        }

        // Click mở Google Maps
        holder.itemView.setOnClickListener(v -> {
            Drawable original = holder.arrowIcon.getDrawable(); // lưu hình gốc
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor("#D3D3D3"));
            holder.arrowIcon.setBackground(circle);
            holder.arrowIcon.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() -> {
                String uri = "google.navigation:q=" + address.getLat() + "," + address.getLon();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                context.startActivity(intent);

                holder.arrowIcon.setBackground(null);
                holder.arrowIcon.setScaleX(1f);
                holder.arrowIcon.setScaleY(1f);
                holder.arrowIcon.setImageDrawable(original);
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return addressList == null ? 0 : addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        private ImageView arrowIcon;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAddress);
            arrowIcon=itemView.findViewById(R.id.arrow_icon);
        }
    }
}

package com.zkth.mst.client.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.zkth.mst.client.R;


/**
 * Created by Root on 2018/7/9.
 * <p>
 * y底部的sliding滑动适配器
 *
 * 根據type判定
 *
 * type:
 * 0:網絡對講
 * 1:視頻監控
 * 2:即時通信
 * 3:應急報警
 * 4:申請供彈
 */

public class ButtomSlidingAdapter extends RecyclerView.Adapter<ButtomSlidingAdapter.ViewHolder> {

    Context context;
    int[] images;
    int type;
    private OnItemClickListener onItemClickListener;

    public ButtomSlidingAdapter(Context context, int[] images, int type) {
        this.context = context;
        this.images = images;
        this.type = type;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.function3_button_activity, null);
        //实例化ViewHolder
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.imageButton.setBackgroundResource(images[position]);
        if (type ==0){
            holder.imageButton.setChecked(true);
        }
        if (onItemClickListener != null) {

            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton imageButton;
        public ViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.bottom_item_bg_btn);
        }
    }
    public interface OnItemClickListener {
        void onClick(int position);
    }
}

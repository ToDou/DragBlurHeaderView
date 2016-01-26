package com.loopeer.android.librarys.dragblurheaderview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StringRecyclerAdapter extends RecyclerView.Adapter<StringRecyclerAdapter.StringVH> {

    private List<String> mDatas;

    public StringRecyclerAdapter() {
        mDatas = new ArrayList<>();
    }

    public void setData(List<String> data) {
        mDatas.clear();
        mDatas.addAll(data);
    }

    public void updateData(List<String> datas) {
        setData(datas);
        notifyDataSetChanged();
    }

    @Override
    public StringVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_string, parent, false);
        return new StringVH(view);
    }

    @Override
    public void onBindViewHolder(StringVH holder, int position) {
        holder.mTextView.setText(mDatas.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static class StringVH extends RecyclerView.ViewHolder {

        private TextView mTextView;

        public StringVH(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(android.R.id.text1);
        }

    }
}

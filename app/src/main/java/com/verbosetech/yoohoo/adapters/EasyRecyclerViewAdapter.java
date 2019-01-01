package com.verbosetech.whatsclone.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.receivers.ConnectivityReceiver;

import java.util.ArrayList;
import java.util.List;

public abstract class EasyRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<T> itemsList;
    private Context context;
    private static final int LOADING_VIEW = Short.MAX_VALUE;
    private boolean isLoaderShowing = false;

    public EasyRecyclerViewAdapter(@NonNull Context context, @Nullable List<T> itemsList) {
        this.context = context;
        this.itemsList = itemsList == null ? new ArrayList<T>() : itemsList;
        if (this.itemsList.isEmpty() && ConnectivityReceiver.isConnected()) {
            showLoading();
        }
    }

    public void addItemOnTop(T item) {
        ArrayList<T> items = new ArrayList<>();
        items.add(item);
        addItemsOnTop(items);
    }

    public void addItemsOnTop(ArrayList<T> items) {
        addItemsOnTop(items, null);
    }

    public void addItemsOnTop(ArrayList<T> items, @Nullable EmptyViewListener emptyViewListener) {
        if (checkListEmpty(items, emptyViewListener)) return;
        this.itemsList.addAll(0, items);
        notifyItemRangeInserted(0, items.size());
        hideLoading();
    }

    public void addItemAtBottom(T item) {
        addItemAtBottom(item, null);
    }

    public void addItemAtBottom(T item, @Nullable EmptyViewListener emptyViewListener) {
        ArrayList<T> items = new ArrayList<>();
        items.add(item);
        addItemsAtBottom(items, emptyViewListener);
    }

    public void addItemsAtBottom(@Nullable ArrayList<T> items) {
        addItemsAtBottom(items, null);
    }

    public void addItemsAtBottom(@Nullable ArrayList<T> items, @Nullable EmptyViewListener emptyViewListener) {
        if (checkListEmpty(items, emptyViewListener)) return;
        int size = itemsList.size();
        //Handles a bug where nothing appears on first position in staggered grid layout manager
        if (size == 0) notifyDataSetChanged();
        //items will never be null at this point
        this.itemsList.addAll(items);
        notifyItemRangeInserted(size, items.size());
        hideLoading();
    }

    private boolean checkListEmpty(@Nullable ArrayList<T> items, @Nullable EmptyViewListener emptyViewListener) {
        if (items == null || items.size() == 0) {
            if (emptyViewListener != null) emptyViewListener.showEmptyView();
            hideLoading();
            return true;
        } else {
            if (emptyViewListener != null) emptyViewListener.hideEmptyView();
            return false;
        }
    }

    public void removeItemAtBottom() {
        int size = itemsList.size();
        if (itemsList.get(size - 1) == null)
            this.itemsList.remove(size - 1);
        notifyItemRemoved(size - 1);
    }

    public void removeItemsRange(int start, int end) {
        if (start < 0 || end >= getItemCount() || start > end) return;

        for (int i = end; i >= start; i--) {
            itemsList.remove(i);
        }
        notifyItemRangeRemoved(start, end - start + 1);
    }

    public void removeItemAt(int pos) {
        itemsList.remove(pos);
        notifyItemRemoved(pos);
    }

    public void clear() {
        int size = itemsList.size();
        itemsList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void findAndRemoveItem(T item) {
        for (int i = 0; i < getItemsListSize(); i++) {
            if (getItem(i).equals(item)) {
                removeItemAt(i);
                return;
            }
        }
    }

    public void hideLoading() {
        if (isLoaderShowing) {
            notifyItemRemoved(getItemCount() - 1);
            isLoaderShowing = false;
        }
    }

    public void showLoading() {
        if (!isLoaderShowing) {
            isLoaderShowing = true;
            notifyItemInserted(getItemCount() - 1);
        }
    }

    public boolean isLoaderShowing() {
        return isLoaderShowing;
    }

    protected T getItem(int position) {
        return itemsList.get(position);
    }

    protected int getItemsListSize() {
        if (itemsList == null) return -1;
        return itemsList.size();
    }

    public abstract RecyclerView.ViewHolder onCreateItemView(ViewGroup parent, int viewType);

    public abstract void onBindItemView(RecyclerView.ViewHolder holder, T item, int position);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOADING_VIEW) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.footer_view, parent, false);
            return new LoadingViewHolder(itemView);
        } else {
            return onCreateItemView(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingViewHolder) {
            ((LoadingViewHolder) holder).progressBar.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        } else {
            if (!itemsList.isEmpty()) {
                onBindItemView(holder, itemsList.get(position), position);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderShowing && position == getItemCount() - 1) {
            return LOADING_VIEW;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public int getItemCount() {
        int count = itemsList.size();
        if (isLoaderShowing) {
            count++;
        }
        return count;
    }

    public boolean contains(T item) {
        return itemsList.contains(item);
    }

    public void notifyItemChanged(T value) {
        int pos = itemsList.indexOf(value);
        if (pos != -1) {
            itemsList.set(pos, value);
            notifyItemChanged(pos);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {

        private ProgressBar progressBar;

        LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.loading_progress_bar);
            if (getAdapterPosition() % 2 == 0 && itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
            }
        }
    }

    public interface EmptyViewListener {
        void showEmptyView();

        void hideEmptyView();
    }
}

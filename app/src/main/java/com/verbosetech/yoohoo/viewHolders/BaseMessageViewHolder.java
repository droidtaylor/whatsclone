package com.verbosetech.whatsclone.viewHolders;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.interfaces.OnMessageItemClick;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.DownloadFileEvent;
import com.verbosetech.whatsclone.models.Message;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.utils.GeneralUtils;
import com.verbosetech.whatsclone.utils.Helper;

import java.util.HashMap;

import static com.verbosetech.whatsclone.adapters.MessageAdapter.OTHER;

/**
 * Created by mayank on 11/5/17.
 */

public class BaseMessageViewHolder extends RecyclerView.ViewHolder {
    protected static int lastPosition;
    public static boolean animate;
    protected static View newMessageView;
    private int attachmentType;
    protected Context context;

    private static int _48dpInPx = -1;
    private Message message;
    private OnMessageItemClick itemClickListener;

    TextView time, senderName;
    CardView cardView;

    public BaseMessageViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView);
        if (itemClickListener != null)
            this.itemClickListener = itemClickListener;
        context = itemView.getContext();
        time = itemView.findViewById(R.id.time);
        senderName = itemView.findViewById(R.id.senderName);
        cardView = itemView.findViewById(R.id.card_view);
        if (_48dpInPx == -1) _48dpInPx = GeneralUtils.dpToPx(itemView.getContext(), 48);
    }

    public BaseMessageViewHolder(View itemView, int attachmentType, OnMessageItemClick itemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
        this.attachmentType = attachmentType;
    }

    public BaseMessageViewHolder(View itemView, View newMessage, OnMessageItemClick itemClickListener) {
        this(itemView, itemClickListener);
        this.itemClickListener = itemClickListener;
        if (newMessageView == null) newMessageView = newMessage;
    }

    protected boolean isMine() {
        return (getItemViewType() & OTHER) != OTHER;
    }

    public void setData(Message message, int position) {
        this.message = message;
        if (attachmentType == AttachmentTypes.NONE_TYPING)
            return;
        time.setText(Helper.getTime(message.getDate()));
        if (message.getRecipientId().startsWith(Helper.GROUP_PREFIX)) {
            senderName.setText(isMine() ? "You" : message.getSenderName());
            senderName.setVisibility(View.VISIBLE);
        } else {
            senderName.setVisibility(View.GONE);
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) cardView.getLayoutParams();
        if (isMine()) {
            layoutParams.gravity = Gravity.END;
            layoutParams.leftMargin = _48dpInPx;
            time.setCompoundDrawablesWithIntrinsicBounds(0, 0, message.isSent() ? (message.isDelivered() ? R.drawable.ic_done_all_black : R.drawable.ic_done_black) : R.drawable.ic_waiting, 0);
        } else {
            layoutParams.gravity = Gravity.START;
            layoutParams.rightMargin = _48dpInPx;
            //itemView.startAnimation(AnimationUtils.makeInAnimation(itemView.getContext(), true));
        }
        cardView.setLayoutParams(layoutParams);
    }

    void onItemClick(boolean b) {
        if (itemClickListener != null && message != null) {
            if (b)
                itemClickListener.OnMessageClick(message, getAdapterPosition());
            else
                itemClickListener.OnMessageLongClick(message, getAdapterPosition());
        }
    }

    void broadcastDownloadEvent(DownloadFileEvent downloadFileEvent) {
        Intent intent = new Intent(Helper.BROADCAST_DOWNLOAD_EVENT);
        intent.putExtra("data", downloadFileEvent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    void broadcastDownloadEvent() {
        Intent intent = new Intent(Helper.BROADCAST_DOWNLOAD_EVENT);
        intent.putExtra("data", new DownloadFileEvent(message.getAttachmentType(), message.getAttachment(), getAdapterPosition()));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}

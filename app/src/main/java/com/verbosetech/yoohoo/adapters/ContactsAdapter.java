package com.verbosetech.whatsclone.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.verbosetech.whatsclone.R;

import java.util.List;

import ezvcard.property.Email;
import ezvcard.property.Telephone;

/**
 * Created by a_man on 5/12/2017.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {
    private Context context;
    private List<Telephone> dataListTelephone;
    private List<Email> dataListEmail;

    public ContactsAdapter(Context context, List<Telephone> telephoneNumbers, List<Email> emails) {
        this.context = context;
        this.dataListTelephone = telephoneNumbers;
        this.dataListEmail = emails;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_contact_number, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (dataListTelephone != null)
            holder.setData(dataListTelephone.get(position));
        else if (dataListEmail != null)
            holder.setData(dataListEmail.get(position));
    }

    @Override
    public int getItemCount() {
        if (dataListTelephone != null)
            return dataListTelephone.size();
        else if (dataListEmail != null)
            return dataListEmail.size();
        else
            return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView type, phone;

        public MyViewHolder(View itemView) {
            super(itemView);
            type = (TextView) itemView.findViewById(R.id.phoneType);
            phone = (TextView) itemView.findViewById(R.id.phoneNumber);
            if (dataListTelephone != null) {
                itemView.findViewById(R.id.phoneCall).setVisibility(View.VISIBLE);
                itemView.findViewById(R.id.phoneCall).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + dataListTelephone.get(getAdapterPosition()).getText()));
                        context.startActivity(callIntent);
                    }
                });
            } else
                itemView.findViewById(R.id.phoneCall).setVisibility(View.GONE);
        }

        public void setData(Telephone telephone) {
            type.setText(telephone.getTypes().get(0).getValue());
            phone.setText(telephone.getText());
        }

        public void setData(Email email) {
            type.setText(email.getTypes().get(0).getValue());
            phone.setText(email.getValue());
        }
    }
}

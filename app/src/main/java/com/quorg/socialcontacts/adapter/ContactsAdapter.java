package com.quorg.socialcontacts.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quorg.socialcontacts.R;
import com.quorg.socialcontacts.database.SConnections;

import java.util.List;

/**
 * Created by Nitish Singh on 16/4/17.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    Context mContext;
    List<SConnections> sConnectionsList ;
    LayoutInflater inflater;

    public ContactsAdapter(Context mContext, List<SConnections> sConnectionsList) {
        this.mContext = mContext;
        this.sConnectionsList = sConnectionsList;
        inflater = (LayoutInflater.from(mContext));
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_contact, parent, false);
        return new ContactsAdapter.ContactsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactsViewHolder holder, int position) {

            if(sConnectionsList.get(position).getName() != null){
                holder.txtName.setText(sConnectionsList.get(position).getName());
            } else {
                holder.txtName.setText(" ------- ");
            }

        if(sConnectionsList.get(position).getEmail() != null){
            holder.txtEmail.setText(sConnectionsList.get(position).getEmail());
        } else {
            holder.txtEmail.setText(" -----  ");
        }

        if(sConnectionsList.get(position).getPhoneNumber() != null){
            holder.txtPhoneNumber.setText(sConnectionsList.get(position).getPhoneNumber());
        } else {
            holder.txtPhoneNumber.setText("  -----  ");
        }


    }

    @Override
    public int getItemCount() {

        return sConnectionsList.size();
    }


    
    public class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView txtName;
        TextView txtEmail;
        TextView txtPhoneNumber;

        public ContactsViewHolder(View itemView) {
            super(itemView);

            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtEmail = (TextView) itemView.findViewById(R.id.txtEmail);
            txtPhoneNumber = (TextView) itemView.findViewById(R.id.txtPhone);
        }
    }
}

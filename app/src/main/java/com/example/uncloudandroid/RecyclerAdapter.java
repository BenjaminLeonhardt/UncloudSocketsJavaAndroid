package com.example.uncloudandroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    public Context context;

    private List<Peer> list;
    public RecyclerAdapter(ArrayList<Peer> list, Context context){
        this.list = list;
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view_layout,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view, context, list);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.ipAdresse.setText(list.get(position).ip);
        holder.name.setText(list.get(position).name);
        holder.os.setText(list.get(position).os);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView ipAdresse;
        TextView name;
        TextView os;
        Context context;
        List<Peer> peerList;

        public MyViewHolder(@NonNull View view, Context context, List<Peer> list) {
            super(view);
            ipAdresse = view.findViewById(R.id.ip);
            name = view.findViewById(R.id.name);
            os = view.findViewById(R.id.os);
            view.setOnClickListener(this);
            this.context = context;
            peerList = list;
        }

        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent(context, PeerFilesActivity.class);
                intent.putExtra("ipAdresse", peerList.get(getAdapterPosition()).ip);
                intent.putExtra("name", peerList.get(getAdapterPosition()).name);
                intent.putExtra("os", peerList.get(getAdapterPosition()).os);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }catch (Exception ex){
                System.out.println(ex.toString());
            }
        }
    }

}



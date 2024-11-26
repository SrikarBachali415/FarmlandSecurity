package com.finalyearproject.farmlandsecurity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Message> messages;
    private final MessagesDatabaseHelper dbHelper;

    public MessagesAdapter(List<Message> messages, MessagesDatabaseHelper dbHelper) {
        this.messages = messages;
        this.dbHelper = dbHelper;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageTextView.setText(message.getMessage());
        holder.timestampTextView.setText(message.getTimestamp());

        holder.deleteButton.setOnClickListener(v -> {
            dbHelper.deleteMessage(message.getId());
            setMessages(dbHelper.getAllMessages());
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timestampTextView;
        Button deleteButton;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}

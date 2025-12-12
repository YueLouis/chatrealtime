package vn.hcmute.chatrealtime;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> items;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(List<Message> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_SENT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentVH(v);
        } else if (viewType == Message.TYPE_RECEIVED) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_system, parent, false);
            return new SystemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        Message m = items.get(position);
        String time = sdf.format(m.time);

        if (h instanceof SentVH) {
            SentVH vh = (SentVH) h;
            vh.txtMsg.setText(m.text);
            vh.txtMeta.setText("you • " + time);
        } else if (h instanceof ReceivedVH) {
            ReceivedVH vh = (ReceivedVH) h;
            vh.txtMsg.setText(m.text);
            vh.txtMeta.setText(m.role + " • " + time);

            String avatar = (m.role != null && m.role.length() > 0)
                    ? ("" + Character.toUpperCase(m.role.charAt(0)))
                    : "U";
            vh.txtAvatar.setText(avatar);
        } else {
            SystemVH vh = (SystemVH) h;
            vh.txtChip.setText(m.text);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SentVH extends RecyclerView.ViewHolder {
        TextView txtMsg, txtMeta;
        SentVH(@NonNull View itemView) {
            super(itemView);
            txtMsg = itemView.findViewById(R.id.txtMsg);
            txtMeta = itemView.findViewById(R.id.txtMeta);
        }
    }

    static class ReceivedVH extends RecyclerView.ViewHolder {
        TextView txtAvatar, txtMsg, txtMeta;
        ReceivedVH(@NonNull View itemView) {
            super(itemView);
            txtAvatar = itemView.findViewById(R.id.txtAvatar);
            txtMsg = itemView.findViewById(R.id.txtMsg);
            txtMeta = itemView.findViewById(R.id.txtMeta);
        }
    }

    static class SystemVH extends RecyclerView.ViewHolder {
        TextView txtChip;
        SystemVH(@NonNull View itemView) {
            super(itemView);
            txtChip = itemView.findViewById(R.id.txtChip);
        }
    }
}

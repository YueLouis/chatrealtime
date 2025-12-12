package vn.hcmute.chatrealtime;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.view.View;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;

    private TextView txtStatus;
    private EditText edtMsg, edtRoomId, edtUserId;
    private Button btnSend, btnCustomer, btnManager;

    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private ArrayList<Message> messages = new ArrayList<>();

    private String roomId = null;
    private String userId = null;
    private String role = null;
    private boolean joined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);

        edtMsg = findViewById(R.id.edtMsg);
        edtRoomId = findViewById(R.id.edtRoomId);
        edtUserId = findViewById(R.id.edtUserId);

        btnSend = findViewById(R.id.btnSend);
        btnCustomer = findViewById(R.id.btnCustomer);
        btnManager = findViewById(R.id.btnManager);

        rvChat = findViewById(R.id.rvChat);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        adapter = new ChatAdapter(messages);
        rvChat.setAdapter(adapter);

        try {
            mSocket = IO.socket("http://10.0.2.2:3000"); // emulator
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
            txtStatus.setText("● Online");
            txtStatus.setTextColor(0xFF10B981); // xanh lá
            addSystem("Connected: " + mSocket.id());
        }));

        mSocket.on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> {
            txtStatus.setText("● Offline");
            txtStatus.setTextColor(0xFF6B7280);
            addSystem("Disconnected");
        }));

        mSocket.on("receive_message", onReceiveMessage);
        mSocket.on("system", onSystem);

        mSocket.connect();

        btnCustomer.setOnClickListener(v -> joinAs("customer"));
        btnManager.setOnClickListener(v -> joinAs("manager"));
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void joinAs(String selectedRole) {
        roomId = edtRoomId.getText().toString().trim();
        userId = edtUserId.getText().toString().trim();
        role = selectedRole;

        if (roomId.isEmpty() || userId.isEmpty()) {
            addSystem("Please enter Room ID and User ID");
            return;
        }

        JSONObject obj = new JSONObject();
        try {
            obj.put("roomId", roomId);
            obj.put("userId", userId);
            obj.put("role", role);
        } catch (JSONException ignored) {}

        mSocket.emit("join_room", obj);
        joined = true;

        addSystem("Joined " + roomId + " as " + role);

        View card = findViewById(R.id.cardJoin);
        card.animate()
                .alpha(0f)
                .translationY(-20f)
                .setDuration(220)
                .withEndAction(() -> card.setVisibility(View.GONE))
                .start();

        btnCustomer.setEnabled(false);
        btnManager.setEnabled(false);
        edtRoomId.setEnabled(false);
        edtUserId.setEnabled(false);
    }

    private void sendMessage() {
        if (!joined) {
            addSystem("Join room first (Customer/Manager)");
            return;
        }

        String text = edtMsg.getText().toString().trim();
        if (text.isEmpty()) return;

        long now = System.currentTimeMillis();

        JSONObject obj = new JSONObject();
        try {
            obj.put("roomId", roomId);
            obj.put("userId", userId);
            obj.put("role", role);
            obj.put("text", text);
            obj.put("time", now);
        } catch (JSONException ignored) {}

        mSocket.emit("send_message", obj);
        edtMsg.setText("");
    }

    private final Emitter.Listener onReceiveMessage = args -> runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];

        String rId = data.optString("roomId");
        String uId = data.optString("userId");
        String r = data.optString("role");
        String text = data.optString("text");
        long t = data.optLong("time", System.currentTimeMillis());

        boolean isMine = (userId != null && userId.equals(uId) && role != null && role.equals(r));
        int type = isMine ? Message.TYPE_SENT : Message.TYPE_RECEIVED;

        messages.add(new Message(type, rId, uId, r, text, t));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    });

    private final Emitter.Listener onSystem = args -> runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        addSystem(data.optString("text"));
    });

    private void addSystem(String text) {
        long now = System.currentTimeMillis();
        messages.add(new Message(Message.TYPE_SYSTEM, roomId, "system", "system", "ℹ️ " + text, now));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off();
        }
    }
}

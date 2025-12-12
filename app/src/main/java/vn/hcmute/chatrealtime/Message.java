package vn.hcmute.chatrealtime;

public class Message {
    public static final int TYPE_SYSTEM = 0;
    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 2;

    public int type;

    public String roomId;
    public String userId;
    public String role;
    public String text;
    public long time;

    public Message(int type, String roomId, String userId, String role, String text, long time) {
        this.type = type;
        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
        this.text = text;
        this.time = time;
    }
}

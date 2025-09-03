package com.pleasure.pleasureaicoding.model.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * 聊天消息表定义
 *
 * @author PleaSure乐事
 */
public class ChatMessageTableDef extends TableDef {

    public static final ChatMessageTableDef CHAT_MESSAGE = new ChatMessageTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn MESSAGE_TYPE = new QueryColumn(this, "messageType");
    public final QueryColumn ROOM_ID = new QueryColumn(this, "roomId");
    public final QueryColumn SENDER_ID = new QueryColumn(this, "senderId");
    public final QueryColumn RECEIVER_ID = new QueryColumn(this, "receiverId");
    public final QueryColumn CONTENT_TYPE = new QueryColumn(this, "contentType");
    public final QueryColumn CONTENT = new QueryColumn(this, "content");
    public final QueryColumn FILE_URL = new QueryColumn(this, "fileUrl");
    public final QueryColumn FILE_NAME = new QueryColumn(this, "fileName");
    public final QueryColumn FILE_SIZE = new QueryColumn(this, "fileSize");
    public final QueryColumn REPLY_TO_ID = new QueryColumn(this, "replyToId");
    public final QueryColumn IS_RECALLED = new QueryColumn(this, "isRecalled");
    public final QueryColumn SEND_TIME = new QueryColumn(this, "sendTime");
    public final QueryColumn IS_DELETE = new QueryColumn(this, "isDelete");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public ChatMessageTableDef() {
        super("", "chat_message");
    }

    public ChatMessageTableDef(String schema) {
        super(schema, "chat_message");
    }

    public ChatMessageTableDef(String schema, String name) {
        super(schema, name);
    }

    public ChatMessageTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ChatMessageTableDef("", alias));
    }
}
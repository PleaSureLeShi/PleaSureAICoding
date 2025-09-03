package com.pleasure.pleasureaicoding.model.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * 聊天房间表定义
 */
public class ChatRoomTableDef extends TableDef {

    public static final ChatRoomTableDef CHAT_ROOM = new ChatRoomTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn ROOM_NAME = new QueryColumn(this, "roomName");
    public final QueryColumn ROOM_DESCRIPTION = new QueryColumn(this, "roomDescription");
    public final QueryColumn ROOM_TYPE = new QueryColumn(this, "roomType");
    public final QueryColumn MAX_MEMBERS = new QueryColumn(this, "maxMembers");
    public final QueryColumn CURRENT_MEMBERS = new QueryColumn(this, "currentMembers");
    public final QueryColumn OWNER_ID = new QueryColumn(this, "ownerId");
    public final QueryColumn IS_PUBLIC = new QueryColumn(this, "isPublic");
    public final QueryColumn PASSWORD = new QueryColumn(this, "password");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "createTime");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "updateTime");
    public final QueryColumn IS_DELETE = new QueryColumn(this, "isDelete");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public ChatRoomTableDef() {
        super("", "chat_room");
    }

    public ChatRoomTableDef(String schema) {
        super(schema, "chat_room");
    }

    public ChatRoomTableDef(String schema, String name) {
        super(schema, name);
    }

    public ChatRoomTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ChatRoomTableDef("", alias));
    }
}
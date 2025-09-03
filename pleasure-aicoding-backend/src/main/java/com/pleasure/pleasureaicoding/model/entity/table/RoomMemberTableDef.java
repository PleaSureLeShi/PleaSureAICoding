package com.pleasure.pleasureaicoding.model.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * 房间成员表定义
 *
 * @author PleaSure乐事
 */
public class RoomMemberTableDef extends TableDef {

    public static final RoomMemberTableDef ROOM_MEMBER = new RoomMemberTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn ROOM_ID = new QueryColumn(this, "roomId");
    public final QueryColumn USER_ID = new QueryColumn(this, "userId");
    public final QueryColumn ROLE = new QueryColumn(this, "role");
    public final QueryColumn JOIN_TIME = new QueryColumn(this, "joinTime");
    public final QueryColumn LAST_READ_TIME = new QueryColumn(this, "lastReadTime");
    public final QueryColumn IS_MUTED = new QueryColumn(this, "isMuted");
    public final QueryColumn MUTED_UNTIL = new QueryColumn(this, "mutedUntil");
    public final QueryColumn IS_DELETE = new QueryColumn(this, "isDelete");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public RoomMemberTableDef() {
        super("", "room_member");
    }

    public RoomMemberTableDef(String schema) {
        super(schema, "room_member");
    }

    public RoomMemberTableDef(String schema, String name) {
        super(schema, name);
    }

    public RoomMemberTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new RoomMemberTableDef("", alias));
    }
}
package one.rewind.android.automator.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DBName(value = "wechat")
@DatabaseTable(tableName = "wechat_friends")
public class WechatFriend extends Model {

	private static final Logger logger = LogManager.getLogger(WechatFriend.class.getName());

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String device_id;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String user_name;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String user_id;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String friend_name;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String friend_id;

	public WechatFriend() {}

	public WechatFriend(String device_id, String user_id, String user_name, String friend_id, String friend_name) {
		this.device_id = device_id;
		this.user_id = user_id;
		this.user_name = user_name;
		this.friend_id = friend_id;
		this.friend_name = friend_name;
	}
}

package one.rewind.android.automator.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.data.raw.model.base.ModelL;
import one.rewind.db.DBName;

/**
 * @author maxuefeng[m17793873123@163.com]
 */
@DBName(value = "raw")
@DatabaseTable(tableName = "wechat_subscribe_account")
public class SubscribeMedia extends ModelL {

    @DatabaseField(dataType = DataType.STRING, width = 32, index = true, canBeNull = false)
    public String udid;


    @DatabaseField(dataType = DataType.STRING, width = 32, index = true, canBeNull = false)
    public String media_name;


    @DatabaseField(dataType = DataType.STRING, width = 32, index = true)
    public String media_id;

    @DatabaseField(dataType = DataType.INTEGER)
    public int status;

    //文章总量
    @DatabaseField(dataType = DataType.INTEGER)
    public int number;

    //重试次数
    @DatabaseField(dataType = DataType.INTEGER)
    public int retry_count;

    @DatabaseField(dataType = DataType.STRING)
    public String request_id;


    public enum State {

        FINISH(1),

        // 表示搜索不到当前公众号
        NOT_EXIST(2),

        NOT_FINISH(0);

        public int status;

        State(int status) {
            this.status = status;
        }
    }

    @Override
    public String toString() {
        return "SubscribeMedia{" +
                "udid='" + udid + '\'' +
                ", media_name='" + media_name + '\'' +
                ", media_id='" + media_id + '\'' +
                ", status=" + status +
                ", number=" + number +
                ", retry_count=" + retry_count +
                ", id=" + id +
                ", insert_time=" + insert_time +
                ", update_time=" + update_time +
                '}';
    }
}

package one.rewind.android.automator.manager;

import one.rewind.android.automator.AndroidDevice;
import one.rewind.android.automator.adapter.LooseWechatAdapter;
import one.rewind.android.automator.adapter.LooseWechatAdapter2;
import one.rewind.android.automator.model.DBTab;
import one.rewind.android.automator.model.SubscribeMedia;
import one.rewind.android.automator.util.AndroidUtil;
import one.rewind.android.automator.util.DBUtil;
import one.rewind.db.DaoManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create By 2018/10/19
 * Description
 *
 * @see DefaultDeviceManager 集中式线程任务分配
 * @see LooseDeviceManager 分散式线程任务分配
 */
public class LooseDeviceManager {

    private LooseDeviceManager() {
    }

    private static ConcurrentHashMap<String, AndroidDevice> devices = new ConcurrentHashMap<>();

    private static LooseDeviceManager instance;

    private static final int DEFAULT_LOCAL_PROXY_PORT = 48454;

    public static LooseDeviceManager getInstance() {
        synchronized (LooseDeviceManager.class) {
            if (instance == null) {
                instance = new LooseDeviceManager();
            }
            return instance;
        }
    }

    private static List<AndroidDevice> obtainAvailableDevices() {
        synchronized (LooseDeviceManager.class) {
            List<AndroidDevice> availableDevices = new ArrayList<>();
            devices.forEach((k, v) -> {
                if (v.state.equals(AndroidDevice.State.INIT)) {
                    availableDevices.add(v);
                }
            });
            return availableDevices;
        }
    }

    static {
        try {
            DBTab.subscribeDao = DaoManager.getDao(SubscribeMedia.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] var = AndroidUtil.obtainDevices();
        Random random = new Random();
        for (int i = 0; i < var.length; i++) {
            AndroidDevice device = new AndroidDevice(var[i], random.nextInt(50000));
            device.state = AndroidDevice.State.INIT;
            device.initApp(DEFAULT_LOCAL_PROXY_PORT + i);
            devices.put(var[i], device);
        }
    }

    public void startManager() throws ClassNotFoundException, SQLException {

        Class.forName("one.rewind.android.automator.model.DBTab");

        DBUtil.reset();

        List<AndroidDevice> androidDevices = obtainAvailableDevices();

        for (AndroidDevice device : androidDevices) {

            LooseWechatAdapter adapter =
                    new LooseWechatAdapter.
                            Builder().
                            device(device).
                            build();
            try {
                adapter.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new DBUtil.ResetTokenState().startTimer();
    }

    public void startManager2() throws ClassNotFoundException, SQLException {

        Class.forName("one.rewind.android.automator.model.DBTab");

        DBUtil.reset();

        List<AndroidDevice> androidDevices = obtainAvailableDevices();

        for (AndroidDevice device : androidDevices) {

            LooseWechatAdapter2 adapter =
                    new LooseWechatAdapter2.
                            Builder().
                            device(device).
                            build();
            try {
                adapter.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new DBUtil.ResetTokenState().startTimer();
    }


}

package one.rewind.android.automator.adapter;

import com.j256.ormlite.dao.Dao;
import com.sun.istack.internal.NotNull;
import one.rewind.android.automator.AndroidDevice;
import one.rewind.android.automator.Callback;
import one.rewind.android.automator.exception.AndroidCollapseException;
import one.rewind.android.automator.exception.InvokingBaiduAPIException;
import one.rewind.android.automator.model.*;
import one.rewind.android.automator.util.AndroidUtil;
import one.rewind.android.automator.util.BaiduAPIUtil;
import one.rewind.db.DaoManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * Create By 2018/10/10
 * Description:
 */
@SuppressWarnings("JavaDoc")
public class DefaultWechatAdapter extends Adapter {

    private boolean isLastPage = false;

    private boolean isFirstPage = true;

    @NotNull
    private ExecutorService executor;

    private TaskType taskType = null;

    private static final int ESSAY_NUM = 100;

    /**
     * Init the executor For current object(this)
     * Has the executor to execute task
     *
     * @param device
     */
    DefaultWechatAdapter(AndroidDevice device) {
        super(device);
    }

    private static Dao<TaskFailRecord, String> dao1;

    private static Dao<Essays, String> dao2;

    private static Dao<SubscribeAccount, String> dao3;

    static {
        try {
            dao1 = DaoManager.getDao(TaskFailRecord.class);
            dao2 = DaoManager.getDao(Essays.class);
            dao3 = DaoManager.getDao(SubscribeAccount.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<WordsPoint> obtainClickPoints() throws InterruptedException, InvokingBaiduAPIException {
        String filePrefix = UUID.randomUUID().toString();
        String fileName = filePrefix + ".png";
        String path = System.getProperty("user.dir") + "/screen/";
        AndroidUtil.screenshot(fileName, path, driver);
        //图像分析   截图完成之后需要去掉头部的截图信息  头部包括一些数据
        List<WordsPoint> wordsPoints = analysisImage(path + fileName);
        if (wordsPoints != null && wordsPoints.size() > 0) {
            return wordsPoints;
        } else {

            //此处已经出现异常：====>>>> 异常的具体原因是点击没反应，程序自动点击叉号进行关闭，已经返回到上一页面

            //当前公众号不能继续抓取了
            AndroidUtil.returnPrevious(driver);
            return null;
        }
    }

    /**
     * 分析图像
     *
     * @param filePath
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private List<WordsPoint> analysisImage(String filePath) throws InvokingBaiduAPIException {
        JSONObject jsonObject = BaiduAPIUtil.executeImageRecognitionRequest(filePath);
        //得到即将要点击的坐标位置
        return analysisWordsPoint(jsonObject.getJSONArray("words_result"));

    }


    /**
     * {"words":"My Bastis三种批量插入方式的性能","location":{"top":1305,"left":42,"width":932,"height":78}}
     * {"words":"找工作交流群(北上广深杭成都重庆", "location":{"top":1676,"left":42,"width":972,"height":72}}
     * {"words":"南京武汉长沙西安)",            "location":{"top":1758,"left":55,"width":505,"height":72}}
     * {"words":"从初级程序员到编程大牛,只需要每","location":{"top":2040,"left":40,"width":978,"height":85}}
     * {"words":"天坚持做这件事情.",           "location":{"top":2130,"left":43,"width":493,"height":71}}
     *
     * @param array
     * @return
     */
    public List<WordsPoint> analysisWordsPoint(JSONArray array) {

        array.remove(0);

        List<WordsPoint> wordsPoints = new ArrayList<>();

        //计算坐标  文章的标题最多有两行  标题过长微信会使用省略号代替掉
        for (Object o : array) {

            JSONObject outJSON = (JSONObject) o;

            JSONObject inJSON = outJSON.getJSONObject("location");

            String words = outJSON.getString("words");

            if (words.contains("已无更多")) {

                System.out.println("============================没有更多文章===================================");

                isLastPage = true;

                System.out.println(isLastPage);

            }

            int top = inJSON.getInt("top");

            int left = inJSON.getInt("left");

            int width = inJSON.getInt("width");

            int height = inJSON.getInt("height");

            //确保时间标签的位置   有可能有年月日字符串的在文章标题中   为了防止这种情况   left<=80

            if (words.contains("年") && words.contains("月") && words.contains("日") && left <= 80) {

                wordsPoints.add(new WordsPoint((top), left, width, height, words));
            }
        }
        return wordsPoints;
    }

    /**
     * 获取公众号的文章列表
     *
     * @param wxPublicName
     * @throws InterruptedException
     */
    public void getIntoPublicAccountEssayList(String wxPublicName, boolean retry) throws AndroidCollapseException {
        try {
            if (retry) {
                TaskFailRecord record = AndroidUtil.retry(wxPublicName, dao2, device.udid);
                if (record == null) {
                    //当前公众号抓取的文章已经达到100篇以上
                    SubscribeAccount var = dao3.queryBuilder().where().eq("media_name", wxPublicName).queryForFirst();
                    if (var.status == 1) {
                        return;
                    } else {
                        long count = dao2.queryBuilder().where().eq("media_nick", wxPublicName).countOf();
                        if (count > 100) {
                            return;
                        } else {
                            AndroidUtil.updateProcess(wxPublicName, udid);
                        }
                    }
                }
                record = AndroidUtil.retry(wxPublicName, dao2, device.udid);
                if (record != null) {
                    isFirstPage = false;
                    //下滑到第一页
                    AndroidUtil.slideToPoint(431, 1250, 431, 455, driver, 1000);
                    //向下划指定页数
                    for (int i = 0; i < record.slideNumByPage; i++) {
                        AndroidUtil.slideToPoint(606, 2387, 606, 960, driver, 1000);
                    }
                }
            }
            while (!isLastPage) {
                //下滑到指定的位置
                if (isFirstPage) {

                    AndroidUtil.slideToPoint(431, 1250, 431, 455, driver, 0);

                    isFirstPage = false;

                } else {

                    AndroidUtil.slideToPoint(606, 2387, 606, 960, driver, 5000);

                }
                //获取模拟点击的坐标位置
                List<WordsPoint> wordsPoints = obtainClickPoints();

                if (wordsPoints == null) {

                    logger.error("链路出现雪崩的情况了！one.rewind.android.automator.adapter.WechatAdapter.getIntoPublicAccountEssayList");

                    throw new AndroidCollapseException("可能是系统崩溃！请检查百度API调用和安卓系统是否崩溃 one.rewind.android.automator.adapter.WechatAdapter.getIntoPublicAccountEssayList");
                } else {
                    //点击计算出来的坐标
                    openEssays(wordsPoints);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("=========================当前设备{}已经崩溃了=============================", device.udid);

            throw new AndroidCollapseException("链路出现雪崩的情况了:one.rewind.android.automator.adapter.WechatAdapter.getIntoPublicAccountEssayList");
        }
    }


    private void openEssays(List<WordsPoint> wordsPoints) throws InterruptedException, AndroidCollapseException {

        int neverClickCount = 0;
        for (WordsPoint wordsPoint : wordsPoints) {

            if (neverClickCount > 3) {
                throw new AndroidCollapseException("安卓系统卡住点不动了！");
            }

            AndroidUtil.clickPoint(320, wordsPoint.top, 5000, driver);

            // 有很大的概率点击不进去
            //所以去判断下是否点击成功    成功：返回上一页面   失败：不返回上一页面  continue
            if (this.device.isClickEffect()) {
                System.out.println("文章点进去了....");
                for (int i = 0; i < 2; i++) {
                    AndroidUtil.slideToPoint(457, 2369, 457, 277, driver, 500);
                }
                Thread.sleep(1000);
                //关闭文章
                AndroidUtil.closeEssay(driver);
                //设置为默认值
                this.device.setClickEffect(false);
            } else {
                ++neverClickCount;
            }
        }
    }


    final class Start implements Runnable {

        private Callback callback;

        Start(Callback callback) {
            this.callback = callback;
        }

        private boolean retry = false;

        public boolean getRetry() {
            return retry;
        }

        public void setRetry(boolean retry) {
            this.retry = retry;
        }

        @Override
        public void run() {
            try {
                assert taskType != null;
                if (TaskType.SUBSCRIBE.equals(taskType)) {
                    for (String var : device.queue) {
                        digestionSubscribe(var, false);
                    }
                } else if (TaskType.CRAWLER.equals(taskType)) {
                    for (String var : device.queue) {
                        isLastPage = false;
                        digestionCrawler(var, getRetry());
                        for (int i = 0; i < 5; i++) {
                            driver.navigate().back();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (AndroidDevice.State.RUNNING.equals(device.state)) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(new AndroidCollapseException("安卓设备启动失败!"), device.queue);
                }
            }
        }
    }

    public void start(Callback callback) {
        Start start = new Start(callback);
        start.setRetry(true);
        this.executor.execute(start);
    }

    /**
     * 订阅公众号
     *
     * @param wxPublicName
     * @throws Exception
     */
    public void subscribeWxAccount(String wxPublicName) throws Exception {

        int k = 3;

        // A 点搜索
        WebElement searchButton = driver.findElement(By.xpath("//android.widget.TextView[contains(@content-desc,'搜索')]"));
        searchButton.click();
        Thread.sleep(500);

        // B 点公众号
        WebElement publicAccountLink = driver.findElement(By.xpath("//android.widget.TextView[contains(@text,'公众号')]"));
        publicAccountLink.click();
        Thread.sleep(2000);

        // C1 输入框输入搜索信息
        driver.findElement(By.className("android.widget.EditText")).sendKeys(wxPublicName);

        // C2 点击搜索输入框
        AndroidUtil.clickPoint(223, 172, 0, driver);

        // C3 点击软键盘的搜索键
        AndroidUtil.clickPoint(1350, 2250, 4000, driver); //TODO 时间适当调整

        // D 点击第一个结果
        AndroidUtil.clickPoint(720, 600, 2000, driver);

        // 点击订阅
        try {
            driver.findElement(By.xpath("//android.widget.TextView[contains(@text,'关注公众号')]"))
                    .click();

            Thread.sleep(3000);  // TODO 时间适当调整
            driver.findElement(By.xpath("//android.widget.ImageView[contains(@content-desc,'返回')]")).click();
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Already add public account: {}", wxPublicName);
            driver.navigate().back();
            --k;
        }
        saveSubscribeRecord(wxPublicName);
        Thread.sleep(1500);
        for (int i = 0; i < k; i++) {
            driver.findElement(By.xpath("//android.widget.ImageView[contains(@content-desc,'返回')]")).click();
            Thread.sleep(500);
        }
    }

    private void saveSubscribeRecord(String wxPublicName) throws Exception {
        long tempCount = dao3.queryBuilder().where()
                .eq("media_name", wxPublicName)
                .countOf();
        if (tempCount == 0) {
            //订阅完成之后再数据库存储记录
            SubscribeAccount e = new SubscribeAccount();
            e.udid = device.udid;
            e.media_name = wxPublicName;
            e.insert();
        }
    }


    /**
     * 针对于在抓取微信公众号文章时候的异常处理   失败无限重试  直到当前公众号的所有文章抓取完成
     *
     * @param wxAccountName
     */
    private void digestionCrawler(String wxAccountName, boolean retry) {
        try {
            //继续获取文章
            AndroidUtil.enterEssaysPage(wxAccountName, device);
            getIntoPublicAccountEssayList(wxAccountName, retry);
        } catch (AndroidCollapseException e) {
            e.printStackTrace();
            try {
                //当前设备系统卡死   进入重试    直到设备不报异常为止
                //截图查看图片中是否存在无响应
                AndroidUtil.closeApp(driver);
                Thread.sleep(10000);
                AndroidUtil.activeWechat(device);

                // 如果每个公众号抓取的文章数量太小的话  启动重试机制
                long number = dao2.queryBuilder().where().eq("media_nick", wxAccountName).countOf();

                if (number < ESSAY_NUM && !this.isLastPage) {
                    digestionCrawler(wxAccountName, true);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅公众号重试机制
     *
     * @param wxPublicName
     * @param retry
     */
    private void digestionSubscribe(String wxPublicName, boolean retry) throws Exception {
        try {
            if (retry) {
                AndroidUtil.closeApp(driver);
                AndroidUtil.activeWechat(device);
            }
            subscribeWxAccount(wxPublicName);
        } catch (Exception e) {
            e.printStackTrace();
            Dao<SubscribeAccount, String> dao = DaoManager.getDao(SubscribeAccount.class);
            SubscribeAccount forFirst = dao.queryBuilder().where().eq("media_name", wxPublicName).queryForFirst();
            if (forFirst == null) {
                digestionSubscribe(wxPublicName, true);
            }
        }
    }


    public static class Builder {

        private TaskType taskType;
        private ExecutorService executor;
        private AndroidDevice device;

        public Builder device(AndroidDevice device) {
            this.device = device;
            return this;
        }

        public Builder taskType(TaskType taskType) {
            this.taskType = taskType;
            return this;
        }

        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public DefaultWechatAdapter build() {
            DefaultWechatAdapter adapter = new DefaultWechatAdapter(this.device);
            adapter.taskType = this.taskType;
            adapter.executor = this.executor;
            return adapter;
        }
    }

}
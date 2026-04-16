package com.example.siyuanmalite.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.example.siyuanmalite.R;
import com.example.siyuanmalite.helpers.SettingsHelper;
import com.example.siyuanmalite.models.*;
import com.example.siyuanmalite.services.TokenService;
import com.example.siyuanmalite.services.UnicodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UnicodeWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "ExampleAppWidget";

    // 启动ExampleAppWidgetService服务对应的action
    private final Intent EXAMPLE_SERVICE_INTENT =
            new Intent("android.appwidget.action.EXAMPLE_APP_WIDGET_SERVICE");
    // 更新 widget 的广播对应的action
    private final String ACTION_UPDATE_ALL = "com.example.siyuanmalite.widget.UPDATE_ALL";
    // 保存 widget 的id的HashSet，每新建一个 widget 都会为该 widget 分配一个 id。
    private static Set idsSet = new HashSet();

    // onUpdate() 在更新 widget 时，被执行，
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 每次 widget 被创建时，对应的将widget的id添加到set中
        for (int appWidgetId : appWidgetIds) {
            idsSet.add(Integer.valueOf(appWidgetId));
        }
        final int N = appWidgetIds.length;

        // 遍历所有实例
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // 创建 RemoteViews 对象
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.unicode_appwidget);

            // 设置按钮的点击事件
            Intent intent = new Intent(context, UnicodeWidgetProvider.class);
            intent.setAction("com.example.siyuanmalite.widget.click");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.ivCode, pendingIntent);

            // 更新 Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                CommonResult < UnicodeOfflineEntity > res = GetOfflineCodes(context);
                if (!res.success)
                {
                    Toast.makeText(context, "自动更新思源码失败："+res.message, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    try
                    {
                        File dataDir = context.getDataDir();
                        File jsonFile = new File(dataDir, "unicode.json");
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(jsonFile, res.result);
                    }catch (Exception ex)
                    {

                    }
                }
            }
        }).start();
    }

    // 当 widget 被初次添加 或者 当 widget 的大小被改变时，被调用
    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId,
                                          Bundle newOptions) {


    }

    // widget被删除时调用
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // 当 widget 被删除时，对应的删除set中保存的widget的id
        for (int appWidgetId : appWidgetIds) {
            idsSet.remove(Integer.valueOf(appWidgetId));
        }
        super.onDeleted(context, appWidgetIds);
    }

    // 第一个widget被创建时调用
    @Override
    public void onEnabled(Context context) {

        super.onEnabled(context);
    }

    // 最后一个widget被删除时调用
    @Override
    public void onDisabled(Context context) {
        // 在最后一个 widget 被删除时，终止服务
        //context.stopService(EXAMPLE_SERVICE_INTENT);
        super.onDisabled(context);
    }


    // 接收广播的回调函数
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.example.siyuanmalite.widget.click")) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, UnicodeWidgetProvider.class);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.unicode_appwidget);
            // 设置按钮的点击事件
            Intent intentClick = new Intent(context, UnicodeWidgetProvider.class);
            intentClick.setAction("com.example.siyuanmalite.widget.click");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.ivCode, pendingIntent);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    CommonResult<Bitmap> res = UpdateCode(context, intent);

                    if (!res.success)
                    {
                        views.setTextViewText(R.id.tvFail1, "请求失败："+res.message);
                        views.setImageViewResource(R.id.ivCode, R.drawable.pay_code_inactive);
                    }
                    else
                    {
                        views.setTextViewText(R.id.tvFail1, "");
                        views.setImageViewBitmap(R.id.ivCode, res.result);
                    }
                    appWidgetManager.updateAppWidget(thisWidget, views);
                }
            });
            t.start();
        }
        super.onReceive(context, intent);
    }

    private CommonResult<Bitmap> UpdateCode(Context context, Intent intent)
    {
        File dataDir = context.getDataDir();
        File jsonFile = new File(dataDir, "unicode.json");
        if (!jsonFile.exists())
        {
            return new CommonResult<>(false, "请先打开APP进行初始化");
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UnicodeOfflineEntity unicodeOffline = objectMapper.readValue(jsonFile, UnicodeOfflineEntity.class);

            if (unicodeOffline.getOfflineCodes().length <= 0)
            {
                return new CommonResult<>(false, "离线码用完啦！请打开APP以获取新的离线码");
            }
            long currentTimestamp = Instant.now().atZone(ZoneId.of("Asia/Shanghai")).toInstant().getEpochSecond();
            System.out.println(currentTimestamp);
            if (currentTimestamp > unicodeOffline.getOfflineCodesExp())
            {
                return new CommonResult<>(false, "离线码过期啦！请打开APP以获取新的离线码");
            }
            String codefinal = unicodeOffline.getOfflineCodes()[0];
            String[] newOfflineCodes = new String[unicodeOffline.getOfflineCodes().length - 1];
            System.arraycopy(unicodeOffline.getOfflineCodes(), 1, newOfflineCodes, 0, newOfflineCodes.length);
            unicodeOffline.setOfflineCodes(newOfflineCodes);
            objectMapper.writeValue(jsonFile, unicodeOffline);

            Bitmap bitmap = create_QR_code(codefinal);
            return new CommonResult<>(true, "", bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            return new CommonResult<>(false, e.getMessage());
        }
    }

    private CommonResult<UnicodeOfflineEntity> GetOfflineCodes(Context context)
    {
        SharedPreferences pre =  context.getSharedPreferences("tmp", Context.MODE_PRIVATE);
        SettingsHelper.ReadSettings(pre);
        String refreshtoken = pre.getString("refreshtoken", "");
        if (refreshtoken == "")
        {
            return new CommonResult<>(false, "Token为空，请重新登录");
        }
        CommonResult<TokenB> res1 = TokenService.RefreshTokenB(refreshtoken);
        if (!res1.success)
        {
            return new CommonResult<>(false, "Token失效，请重新登录。"+res1.message);
        }
        String userCode = pre.getString("userCode", "");
        if (userCode == "")
        {
            return new CommonResult<>(false, "请先在APP内登录");
        }
//        String codefinal = "";
//        if (SettingsHelper.useIndentity)
//        {
//            CommonResult<String> res2 = UnicodeService.GetUnicodeIdentity();
//            if (!res2.success)
//            {
//                Toast.makeText(context, "请求失败："+res2.message, Toast.LENGTH_SHORT).show();
//                return new CommonResult<>(false, "");
//            }
//            codefinal = codefinal + res2.result;
//        }
//        if (SettingsHelper.usePay)
//        {
            CommonResult<UnicodePayEntity> res2 = UnicodeService.GetUnicodePay(userCode, SettingsHelper.supplier);
            if (!res2.success)
            {
                return new CommonResult<>(false, "请求失败："+res2.message);
            }
//            codefinal = codefinal + res2.result;
//        }
        return new CommonResult<>(true, "", res2.result.toOfflineEntity());
    }

    private Bitmap create_QR_code(String content) {
        HashMap hashMap = new HashMap();
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        //定义二维码的纠错级别，为L
        hashMap.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //设置字符编码为utf-8
        hashMap.put(EncodeHintType.MARGIN, 1);
        //设置margin属性为2,也可以不设置
        String contents = content; //定义二维码的内容
        BitMatrix bitMatrix = null;   //这个类是用来描述二维码的,可以看做是个布尔类型的数组
        try {
            bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, 250, 250, hashMap);
            //调用encode()方法,第一次参数是二维码的内容，第二个参数是生二维码的类型，第三个参数是width，第四个参数是height，最后一个参数是hints属性
        } catch (WriterException e) {
            e.printStackTrace();
        }

        int width = bitMatrix.getWidth();//获取width
        int height = bitMatrix.getHeight();//获取height
        int[] pixels = new int[width * height]; //创建一个新的数组,大小是width*height
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //通过两层循环,为二维码设置颜色
                if (bitMatrix.get(i, j)) {
                    pixels[i * width + j] = Color.BLACK;  //设置为黑色

                } else {
                    pixels[i * width + j] = Color.WHITE; //设置为白色
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        //调用Bitmap的createBitmap()，第一个参数是width,第二个参数是height,最后一个是config配置，可以设置成RGB_565
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //调用setPixels(),第一个参数就是上面的那个数组，偏移为0，x,y也都可为0，根据实际需求来,最后是width ,和height
        return bitmap;
    }
}

package com.example.siyuanmalite;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.siyuanmalite.databinding.FragUnicodeBinding;
import com.example.siyuanmalite.helpers.SettingsHelper;
import com.example.siyuanmalite.models.*;
import com.example.siyuanmalite.services.ProfileService;
import com.example.siyuanmalite.services.TokenService;
import com.example.siyuanmalite.services.UnicodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class UnicodeFragment extends Fragment {

    private FragUnicodeBinding binding;
    private Timer timerUpdate;
    private Timer timerPay;
    private Timer timerIdentity;

    private Boolean isInit = false;
    private CommonResult<String> lastCodeIdentity;
    private CommonResult<UnicodePayEntity> lastCodePay;
    private CommonResult<ProfileDto> profile;
    private String userCode;
    private SharedPreferences pre;
    private LocalDateTime lastUpdated;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        System.out.println("onAttach");
        // 获取当前 Activity 的 Window 对象
        Window window = getActivity().getWindow();
        // 设置 status bar 的颜色
        window.setStatusBarColor(getResources().getColor(R.color.darkblue));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(0);

        pre =  getActivity().getSharedPreferences("tmp", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        System.out.println("onCreateView");
        binding = FragUnicodeBinding.inflate(inflater, container, false);

        binding.checkPay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UpdateQrCode();
            }
        });

        binding.checkIdentity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UpdateQrCode();
            }
        });

        binding.checkHideInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UpdateUserInfo();
            }
        });

        binding.ivCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (binding.wechatPay.isChecked()) {
                    binding.ivPayHint.setImageResource(R.drawable.ic_wechat);
                    binding.tvPayHint.setText("微信支付");
                }
                if (binding.walletPay.isChecked()) {
                    binding.ivPayHint.setImageResource(R.drawable.ic_wallet);
                    binding.tvPayHint.setText("思源码钱包支付");
                }
                if (binding.bocPay.isChecked()) {
                    binding.ivPayHint.setImageResource(R.drawable.ic_boc);
                    binding.tvPayHint.setText("中国银行支付");
                }
                binding.ivCode.setImageResource(R.drawable.pay_code_inactive);
            }
        });

        SettingsHelper.ReadSettings(pre);
        this.binding.checkIdentity.setChecked(SettingsHelper.useIndentity);
        this.binding.checkPay.setChecked(SettingsHelper.usePay);
        this.binding.checkHideInfo.setChecked(SettingsHelper.hideInfo);
        this.binding.wechatPay.setChecked(SettingsHelper.supplier.equals("weixiao"));
        this.binding.walletPay.setChecked(SettingsHelper.supplier.equals("wallet"));
        this.binding.bocPay.setChecked(SettingsHelper.supplier.equals("boc"));

        return binding.getRoot();
    }


    @Override
    public void onStart()
    {
        super.onStart();

        if (!isInit)
        {
            isInit = true;
            String refreshtoken = pre.getString("refreshtoken", "");
            if (refreshtoken == "")
            {
                GoLogin();
                isInit = false;
                return;
            }

            new Thread(()->{
                CommonResult<TokenB> res = TokenService.RefreshTokenB(refreshtoken);
                if (!res.success)
                {
                    getActivity().runOnUiThread(()->{
                        Toast.makeText(this.getContext(), "Token失效，请重新登录", Toast.LENGTH_SHORT).show();
                        GoLogin();
                    });
                    isInit = false;
                    return;
                }

                pre.edit().putString("refreshtoken", res.result.getRefreshToken()).commit();

                userCode = pre.getString("userCode", "");
                if (userCode == "")
                {
                    GetUserInfo();
                    if (!profile.success)
                    {
                        return;
                    }
                    userCode = profile.result.getEntities()[0].getCode();
                    pre.edit().putString("userCode", userCode).commit();
                }

                Thread t1 = new Thread(()->{QueryIdentity();});
                Thread t2 = new Thread(()->{QueryPay();});
                if (binding.checkIdentity.isChecked())
                    t1.start();
                if (binding.checkPay.isChecked())
                    t2.start();
                try {
                    if (binding.checkIdentity.isChecked())
                        t1.join();
                    if (binding.checkPay.isChecked())
                        t2.join();
                    UpdateQrCode();
                }
                catch (Exception ex)
                {

                }

                new Thread(()->{GetUserInfo(); UpdateUserInfo();}).start();
            }).start();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        SetBrightness();
        timerUpdate = new Timer();
        timerIdentity = new Timer();
        timerPay = new Timer();
        StartLoop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (timerUpdate != null)
            timerUpdate.cancel();
        if (timerIdentity != null)
            timerIdentity.cancel();
        if (timerPay != null)
            timerPay.cancel();

        SharedPreferences pre =  getActivity().getSharedPreferences("tmp", Context.MODE_PRIVATE);
        SettingsHelper.WriteSettings(pre);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void updateSettings()
    {
        SettingsHelper.useIndentity = binding.checkIdentity.isChecked();
        SettingsHelper.usePay = binding.checkPay.isChecked();
        SettingsHelper.hideInfo = binding.checkHideInfo.isChecked();
        if (binding.wechatPay.isChecked()) {
            SettingsHelper.supplier = "weixiao";
        }
        else if (binding.walletPay.isChecked()) {
            SettingsHelper.supplier = "wallet";
        }
        else if (binding.bocPay.isChecked()) {
            SettingsHelper.supplier = "boc";
        }
    }

    private void SetBrightness()
    {
        // 获取当前窗口的属性
        Window window = getActivity().getWindow();

        // 计算要设置的亮度值（0.0 到 1.0 之间的值）
        float brightnessValue = 1f; // 例如，设置为 0.5 表示一半的亮度

        // 设置亮度值到窗口属性
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightnessValue;
        window.setAttributes(layoutParams);
    }

    private void UpdateQrCode()
    {
        try {
            Activity act = getActivity();
            if (act == null)
                return;
            if (!binding.checkPay.isChecked() && !binding.checkIdentity.isChecked())
            {
                act.runOnUiThread(()->{
                    binding.groupFail.setVisibility(View.VISIBLE);
                    binding.tvFail1.setText("请至少勾选“身份”和“支付”中的一项");
                    binding.ivCode.setImageBitmap(null);
                });
                return;
            }
            if (binding.checkIdentity.isChecked() && lastCodeIdentity == null)
            {
                act.runOnUiThread(()->{
                    binding.groupFail.setVisibility(View.GONE);
                    binding.ivCode.setImageResource(R.drawable.pay_code_inactive);
                });
                return;
            }
            if (binding.checkPay.isChecked() && lastCodePay == null)
            {
                act.runOnUiThread(()->{
                    binding.groupFail.setVisibility(View.GONE);
                    binding.ivCode.setImageResource(R.drawable.pay_code_inactive);
                });
                return;
            }
            if (binding.checkIdentity.isChecked() && !lastCodeIdentity.success)
            {
                act.runOnUiThread(()->{
                    binding.groupFail.setVisibility(View.VISIBLE);
                    binding.tvFail1.setText(lastCodeIdentity.message);
                    binding.ivCode.setImageBitmap(null);
                });
                return;
            }
            if (binding.checkPay.isChecked() && (!lastCodePay.success))
            {
                act.runOnUiThread(()->{
                    binding.groupFail.setVisibility(View.VISIBLE);
                    binding.tvFail1.setText(lastCodePay.message);
                    binding.ivCode.setImageBitmap(null);
                });
                return;
            }
            if (binding.checkPay.isChecked() && (lastCodePay.result.getStatus() != 0))
            {
                act.runOnUiThread(()->{
                    binding.groupFail.setVisibility(View.VISIBLE);
                    binding.tvFail1.setText(lastCodePay.result.getMessage());
                    binding.ivCode.setImageBitmap(null);
                });
                return;
            }
            String codefinal = "";
            if (binding.checkIdentity.isChecked())
                codefinal += lastCodeIdentity.result;
            if (binding.checkPay.isChecked())
                codefinal += lastCodePay.result.getCode();

            act.runOnUiThread(()->{
                binding.groupFail.setVisibility(View.GONE);
            });
            Bitmap bitmap = create_QR_code(codefinal);
            act.runOnUiThread(()->{
                binding.ivCode.setImageBitmap(bitmap);
            });
        }
        catch (Exception ex)
        {

        }
    }

    private void QueryIdentity()
    {
        if (TokenService.tokenB == null)
            return;
        lastCodeIdentity = UnicodeService.GetUnicodeIdentity();
    }

    private void QueryPay()
    {
        if (TokenService.tokenB == null)
            return;
        updateSettings();
        lastCodePay = UnicodeService.GetUnicodePay(userCode, SettingsHelper.supplier);
        if (lastCodePay.success)
        {
            try
            {
                File dataDir = getActivity().getDataDir();
                File jsonFile = new File(dataDir, "unicode.json");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(jsonFile, lastCodePay.result.toOfflineEntity());
            }
            catch (Exception ex)
            {

            }
        }
        else {

        }
    }

    private void GetUserInfo()
    {
        profile = ProfileService.GetProfile();
        if (!profile.success)
        {
            getActivity().runOnUiThread(()->{
                Toast.makeText(this.getContext(), "获取用户信息失败："+profile.message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void UpdateUserInfo()
    {
        if (binding.checkHideInfo.isChecked())
        {
            getActivity().runOnUiThread(()->{
                binding.tvName.setText("交小喵 1234567890");
                binding.tvOrganize.setText("上海交通大学");
                binding.ivPhoto.setImageResource(R.drawable.card_photo_default_male);
            });
        }
        else
        {
            if (!profile.success)
                return;
            ProfileIdentity[] identities = profile.result.getEntities()[0].getIdentities();
            getActivity().runOnUiThread(()->{
                binding.tvName.setText(profile.result.getEntities()[0].getName() + " " + profile.result.getEntities()[0].getCode());
                binding.tvOrganize.setText(identities[identities.length-1].getOrganize().getName());
            });

            String imageUrl = identities[identities.length-1].getPhotoURL();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(imageUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        final Bitmap bitmap = BitmapFactory.decodeStream(input);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.ivPhoto.setImageBitmap(bitmap);  // 在UI主线程更新ImageView
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();  // 处理异常
                    }
                }
            }).start();
        }
    }

    private void StartLoop() {
//        timerUpdate.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (lastUpdated != null)
//                {
//                    long diff = ChronoUnit.MILLIS.between(lastUpdated, LocalDateTime.now());
//                    if (diff < 2000)
//                    {
//                        lastUpdated = LocalDateTime.now();
//                        return;
//                    }
//                }
//                lastUpdated = LocalDateTime.now();
//                UpdateQrCode();
//            }
//        }, 0, 3000);
        timerIdentity.schedule(new TimerTask() {
            @Override
            public void run() {
                QueryIdentity();
                UpdateQrCode();
            }
        }, 1000, 3000);
        timerPay.schedule(new TimerTask() {
            @Override
            public void run() {
                QueryPay();
            }
        }, 2000, 3000);
    }

    private void GoLogin() {
        // 获取FragmentManager
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        // 开始Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (Fragment frag:
                fragmentManager.getFragments()) {
            transaction.remove(frag);
        }
        LoginFragment loginFragment = new LoginFragment();
        transaction.add(R.id.container, loginFragment);

        transaction.show(loginFragment);
        transaction.commit();
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
            bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, 300, 300, hashMap);
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
                if (bitMatrix.get(j, i)) {
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
        //调用setImageBitmap()方法，将二维码设置到imageview控件里
    }
}
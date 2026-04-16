package com.example.siyuanmalite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.siyuanmalite.databinding.FragLoginBinding;
import com.example.siyuanmalite.helpers.JacFastLoginHelper;
import com.example.siyuanmalite.models.TokenB;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

public class LoginFragment extends Fragment{

    private FragLoginBinding binding;
    private JacFastLoginHelper helper;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragLoginBinding.inflate(inflater, container, false);
        create_QR_code("请稍后");
        binding.text1.setText("请稍后...");
        binding.textUser.setText("请使用微信或交我办扫码登录");
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        helper = new JacFastLoginHelper();
        helper.OnLoginFail = new JacFastLoginHelper.LoginFailHandler()
        {
            @Override
            public void LoginFail (String message){
                // 1. 获取Activity引用
                androidx.fragment.app.FragmentActivity activity = getActivity();

                // 2. 关键判断：如果Activity为空，或者Fragment已经未被添加，则不再执行UI操作
                if (activity == null || !isAdded()) {
                    return;
                }
                // 3. 安全地运行 UI 操作
                activity.runOnUiThread(() -> {
                    // 这里为了安全，建议再次检查 binding 是否为空（防止 onDestroyView 后崩溃）
                    if (binding != null) {
                        binding.maskGrid.setVisibility(View.VISIBLE);
                        binding.text1.setText(message + "\n轻触以重试");
                        System.out.println(message);
                    }
                });
            }
        };
        helper.OnPrepared = new JacFastLoginHelper.PreparedHandler() {
            @Override
            public void Prepared() {
                create_QR_code(helper.GetQrcodeStr());
                getActivity().runOnUiThread(()->{
                    binding.maskGrid.setVisibility(View.INVISIBLE);
                });
            }
        };
        helper.OnLoginSuccess = new JacFastLoginHelper.LoginSuccessHandler() {
            @Override
            public void LoginSuccess(TokenB tokenB) {
                System.out.println(tokenB.getRefreshToken());

                SharedPreferences pre =  getActivity().getSharedPreferences("tmp", Context.MODE_PRIVATE);
                pre.edit().putBoolean("logined", true).commit();
                pre.edit().putString("refreshtoken", tokenB.getRefreshToken()).commit();

                // 获取FragmentManager
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                // 开始Fragment事务
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                for (Fragment frag:
                        fragmentManager.getFragments()) {
                    transaction.remove(frag);
                }
                UnicodeFragment unicodeFragment = new UnicodeFragment();
                transaction.add(R.id.container, unicodeFragment);

                transaction.show(unicodeFragment);
                transaction.commit();
            }
        };
        helper.OnScanComplete = new JacFastLoginHelper.ScanCompleteHandler() {
            @Override
            public void ScanComplete() {
                getActivity().runOnUiThread(()->{
                    binding.maskGrid.setVisibility(View.VISIBLE);
                    binding.text1.setText("扫码成功，验证中...");
                });
            }
        };
        helper.OnLoginNeedSmsCode = new JacFastLoginHelper.LoginNeedSmsCodeHandler() {
            @Override
            public void LoginNeedSmsCode(String msg) {
                getActivity().runOnUiThread(()->{
                    showInputDialog(msg);
                });
            }
        };
        helper.Start();

        binding.maskGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (helper.getState() == JacFastLoginHelper.StateEnum.Error)
                    helper.Refresh();
            }
        });

        binding.btnJwb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uriString = "jaccount://login?uuid=" + helper.getUuid();
                Uri uri = Uri.parse(uriString);

                Intent loginIntent = new Intent(Intent.ACTION_VIEW, uri);

                PackageManager packageManager = getActivity().getPackageManager();

                List<ResolveInfo> activities = packageManager.queryIntentActivities(loginIntent, 0);

                boolean isIntentSafe = activities.size() > 0;

                if (isIntentSafe) {
                    startActivity(loginIntent);
                } else {
                    // 如果URI不能被打开，可以进行相应的处理，比如显示提示信息
                    Toast.makeText(getActivity(), "您还没有安装交我办！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showInputDialog(String msg) {
        LoginFragment self = this;
        // 创建一个EditText作为用户输入的界面
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("请输入验证码");
        input.setPadding(10,0,0,10);

        // 创建AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(msg); // 设置对话框标题
        builder.setView(input); // 设置对话框的内容视图

        // 设置对话框的正向按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = input.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        helper.UseSmsCode(userInput);
                    }
                }).start();
            }
        });

        // 设置对话框的反向按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                helper.Refresh();
                Toast.makeText(self.getContext(), "请重新登录", Toast.LENGTH_SHORT);
            }
        });

        // 创建并显示AlertDialog
        builder.create().show();
    }

    private void create_QR_code(String content) {
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

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //调用Bitmap的createBitmap()，第一个参数是width,第二个参数是height,最后一个是config配置，可以设置成RGB_565
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //调用setPixels(),第一个参数就是上面的那个数组，偏移为0，x,y也都可为0，根据实际需求来,最后是width ,和height
        getActivity().runOnUiThread(()->{
            binding.image1.setImageBitmap(bitmap);
        });

        //调用setImageBitmap()方法，将二维码设置到imageview控件里
    }
}
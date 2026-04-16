package com.example.siyuanmalite.helpers;

import com.example.siyuanmalite.models.*;
import com.example.siyuanmalite.services.ProfileService;
import com.example.siyuanmalite.services.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JacFastLoginHelper {
    public enum StateEnum {
        None,
        Init,
        InitSocket,
        WaitForScan,
        ScanComplete,
        SendSms,
        WaitForSmsCode,
        CheckSmsCode,
        CodeGet,
        Done,
        Error,
    }

    private static final String JWB_AUTH_URL = "https://jaccount.sjtu.edu.cn/oauth2/authorize?client_id=KfiGI09p9N4cPSHn5V7p&response_type=code&redirect_uri=https%3A%2F%2Fnet.sjtu.edu.cn&display=android_mobile&scope=&state=8b127234-295f-4d82-9ce8-f92c4599d867";

    //---Private---//
    private String url1;
    private WebSocket ws;
    private OkHttpClient client;
    private StateEnum state;
    private Timer timer;
    private String jaloginUrl;
    private String finalCallbackUrl;
    private String account;
    private String smsCode;
    //---End---//

    //---Fields---//
    public String uuid;
    public long ts;
    public String sig;

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
    public String getSig()
    {
        return sig;
    }
    public void setSig(String sig) {
        this.sig = sig;
    }
    public long getTs() {
        return ts;
    }
    public void setTs(long ts) {
        this.ts = ts;
    }
    //---End---//

    //---Constructor---//
    public JacFastLoginHelper()
    {
        CookieJar cookieJar =
                new JavaNetCookieJar(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        this.client = new OkHttpClient.Builder()
                .followRedirects(true)
                .cookieJar(cookieJar)
                .build();
        this.url1 = JWB_AUTH_URL;
        this.state = StateEnum.None;
    }
    //---End---//

    //---Events---//
    public static interface PreparedHandler {
        public void Prepared();
    }
    public static interface ScanCompleteHandler {
        public void ScanComplete();
    }
    public static interface LoginFailHandler {
        public void LoginFail(String message);
    }
    public static interface LoginSuccessHandler {
        public void LoginSuccess(TokenB tokenB);
    }
    public static interface LoginNeedSmsCodeHandler {
        public void LoginNeedSmsCode(String msg);
    }
    public LoginFailHandler OnLoginFail;
    public LoginSuccessHandler OnLoginSuccess;
    public PreparedHandler OnPrepared;
    public ScanCompleteHandler OnScanComplete;
    public LoginNeedSmsCodeHandler OnLoginNeedSmsCode;
    //---End---//

    //---Interfaces---//
    public String GetQrcodeStr() {
        return String.format("https://jaccount.sjtu.edu.cn/jaccount/confirmscancode?uuid=%s&ts=%s&sig=%s", uuid, ts, sig);
    }

    public StateEnum getState() {
        return state;
    }

    public void UseSmsCode(String code) {
        smsCode = code;
        state = StateEnum.CheckSmsCode;
        MainLoop();
    }

    public void Start() {
        MainLoop();
    }

    public void StartTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                DoSendGetPayload();
            }
        }, 0, 50 * 1000);
    }

    public void Refresh() {
        Dispose();
        Start();
    }

    private void Fail(String message) {
        if (state == StateEnum.None)
            return;
        state = StateEnum.Error;
        OnLoginFail.LoginFail(message);
        Dispose();
    }

    public void Dispose() {
        state = StateEnum.None;
        jaloginUrl = null;
        try
        {
            timer.cancel();
        }
        catch (Exception ex)
        {

        }
        try
        {
            ws.close(0, null);
        }
        catch (Exception ex)
        {

        }
    }
    //---End---//

    private void MainLoop() {
        new Thread(() -> {
            boolean loop = true;
            while (loop) {
                System.out.println("state: " + state.toString());
                switch (state) {
                    case None:
                        timer = new Timer();
                        jaloginUrl = url1;
                        state = StateEnum.Init;
                        break;
                    case Init:
                        CommonResult<String> res1 = DoRequestJaLogin();
                        if (!res1.success)
                        {
                            Fail(res1.message);
                            loop = false;
                            break;
                        }
                        if (res1.success && res1.message == "authorized") {
                            state = StateEnum.CodeGet;
                            break;
                        }
                        if (res1.success && res1.message == "sms") {
                            state = StateEnum.SendSms;
                            break;
                        }
                        state = StateEnum.InitSocket;
                        break;
                    case InitSocket:
                        CommonResult<Boolean> res2 = DoInitWebSocket();
                        if (!res2.success)
                        {
                            Fail(res2.message);
                            loop = false;
                            break;
                        }
                        state = StateEnum.WaitForScan;
                        loop = false;
                        break;
                    case ScanComplete:
                        OnScanComplete.ScanComplete();
                        jaloginUrl = "https://jaccount.sjtu.edu.cn/jaccount/expresslogin?uuid=" + uuid;
                        state = StateEnum.Init;
                        break;
                    case SendSms:
                        CommonResult<String> res3 = DoSendVerifyCode();
                        if (!res3.success)
                        {
                            Fail(res3.message);
                            loop = false;
                            break;
                        }
                        OnLoginNeedSmsCode.LoginNeedSmsCode(res3.message);
                        state = StateEnum.WaitForSmsCode;
                        loop = false;
                        break;
                    case CheckSmsCode:
                        CommonResult<Boolean> res4 = DoCheckVerifyCode(smsCode);
                        if (!res4.success) {
                            Fail(res4.message);
                            loop = false;
                            break;
                        }
                        state = StateEnum.Init;
                        break;
                    case CodeGet:
                        CommonResult<Boolean> res5 = DoGetToken();
                        if (!res5.success) {
                            Fail(res5.message);
                            loop = false;
                            break;
                        }
                        state = StateEnum.Done;
                        break;
                    case Done:
                        OnLoginSuccess.LoginSuccess(TokenService.tokenB);
                        Dispose();
                        loop = false;
                        break;
                    case Error:
                        break;
                    case WaitForScan:
                        break;
                    case WaitForSmsCode:
                        break;
                }
            }
        }).start();
    }

    //---Inner Logic---//
    private CommonResult<String> DoRequestJaLogin() {
        try {
            Request request = new Request.Builder()
                    .get()
                    .url(jaloginUrl)
                    .build();
            Response resp = client.newCall(request).execute();
//            if (resp.code() == 302) {
//                String callbackurl = "";
//                Headers responseHeaders = resp.headers();
//                for (int i = 0; i < responseHeaders.size(); i++) {
//                    if (responseHeaders.name(i).toLowerCase() == "location")
//                        callbackurl = responseHeaders.value(i);
//                }
//                GetCode(callbackurl);
//                return new CommonResult<String>(true, "已授权");
//            }
            if (resp.request().url().toString().contains("net.sjtu.edu.cn"))
            {
                finalCallbackUrl = resp.request().url().toString();
                return new CommonResult<String>(true, "authorized");
            }
            if (!resp.isSuccessful()) {
                return new CommonResult<String>(false, String.valueOf(resp.code()));
            }

            String html = resp.body().string();
            if (html.equals(""))
                return new CommonResult<String>(false, "空内容");

            if (html.contains("Two-Step Verification")) {
                Matcher matches = Pattern.compile("account: '(.+?)',").matcher(html);

                if (!matches.find()) {
                    return new CommonResult<String>(false, "匹配失败，找不到account");
                }

                account = matches.group(1);
                jaloginUrl = resp.request().url().toString();
                return new CommonResult<String>(true, "sms");
            }

            Matcher matches = Pattern.compile("uuid: \"(.+?)\"").matcher(html);
            if (!matches.find()) {
                return new CommonResult<String>(false, "未找到uuid");
            }
            uuid = matches.group(1);
            return new CommonResult<String>(true, "ok", uuid);
        } catch (Exception ex) {
            System.out.println(ex);
            return new CommonResult<String>(false, ex.getMessage());
        }
    }

    private CommonResult<Boolean> DoInitWebSocket() {
        try
        {
            OkHttpClient clientws = new OkHttpClient.Builder()
                    .pingInterval(50, TimeUnit.SECONDS)
                    .build();
            Request reqws = new Request.Builder()
                    .url("wss://jaccount.sjtu.edu.cn/jaccount/sub/" + uuid)
                    .build();
            clientws.newWebSocket(reqws, new WebSocketListener() {
                @Override
                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    super.onClosed(webSocket, code, reason);
                }

                @Override
                public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    super.onClosing(webSocket, code, reason);
                    Fail("Socket已关闭：" + reason);
                }

                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                    super.onFailure(webSocket, t, response);
                    Fail("Socket错误：" + t.getMessage());
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    super.onMessage(webSocket, text);
                    try
                    {
                        ObjectMapper mapper = new ObjectMapper();
                        //使用readValue方法进行转换
                        LoginPayloadDto dto = mapper.readValue(text, LoginPayloadDto.class);
                        if (dto.getError() != 0)
                            throw new Exception("服务器返回：" + dto.getError());
                        switch(dto.getType().toUpperCase())
                        {
                            case "UPDATE_QR_CODE":
                                setTs(dto.getPayload().getTs());
                                setSig(dto.getPayload().getSig());
                                OnPrepared.Prepared();
                                break;
                            case "LOGIN":
                                state = StateEnum.ScanComplete;
                                MainLoop();
                                break;
                        }
                    }
                    catch(Exception ex)
                    {
                        Fail(ex.getMessage());
                    }
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                    super.onMessage(webSocket, bytes);

                }

                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    super.onOpen(webSocket, response);
                    ws = webSocket;
                    //worker.StartRun();
                    StartTimer();
                }
            });
            return new CommonResult(true, "");
        }
        catch(Exception ex)
        {
            return new CommonResult(false, "发生异常：" + ex.getMessage());
        }
    }

    public CommonResult<Boolean> DoSendGetPayload() {
        Boolean res = ws.send("{ \"type\": \"UPDATE_QR_CODE\" }");
        return new CommonResult<Boolean>(res,"");
    }

    private CommonResult<String> DoSendVerifyCode() {
        try {
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            formBodyBuilder.add("c", "sms");
            FormBody formBody = formBodyBuilder.build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url("https://jaccount.sjtu.edu.cn/jaccount/2fa/loginVerify")
                    .addHeader("accept-language", "zh-CN,zh;q=0.9")
                    .build();
            Response resp = client.newCall(request).execute();
            if (!resp.isSuccessful()) {
                Fail("发送验证码失败");
                return new CommonResult<String>(false, "");
            }
            ObjectMapper mapper = new ObjectMapper();
            JacLoginSmsVerifyResDto dto = mapper.readValue(resp.body().string(), JacLoginSmsVerifyResDto.class);
            if (dto.getErrno() != 0) {
                Fail("发送验证码失败：" + dto.getError());
                return new CommonResult<String>(false, "");
            }
            return new CommonResult<String>(true, dto.getEntities()[0].getMsg());
        } catch (Exception ex) {
            Fail(ex.getMessage());
            return new CommonResult<String>(false, "");
        }
    }

    private CommonResult<Boolean> DoCheckVerifyCode(String code) {
        try {
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            formBodyBuilder.add("account", account);
            formBodyBuilder.add("captcha", code);
            formBodyBuilder.add("trust", "true");
            FormBody formBody = formBodyBuilder.build();

            Request request = new Request.Builder()
                    .post(formBody)
                    .url("https://jaccount.sjtu.edu.cn/jaccount/smsVerify")
                    .build();
            Response resp = client.newCall(request).execute();
            if (!resp.isSuccessful()) {
                return new CommonResult<Boolean>(false, "验证失败");
            }
            ObjectMapper mapper = new ObjectMapper();
            JacLoginSmsVerifyResDto dto = mapper.readValue(resp.body().string(), JacLoginSmsVerifyResDto.class);
            if (!dto.getSuccess()) {
                return new CommonResult<Boolean>(false, "验证失败：" + dto.getError());
            }
            return new CommonResult<Boolean>(true, "ok", true);
        } catch (Exception ex) {
            return new CommonResult<Boolean>(false, ex.getMessage());
        }
    }

    private CommonResult<Boolean> DoGetToken() {
        try
        {
            Matcher matches = Pattern.compile("https?://net\\.sjtu\\.edu\\.cn/\\?code=(.+)&state=").matcher(finalCallbackUrl);

            if (!matches.find())
            {
                return new CommonResult<>(false, "匹配失败，找不到code");
            }

            String code = matches.group(1);

            RequestBody forms = new FormBody.Builder()
                    .add("code", code)
                    .add("grant_type", "authorization_code")
                    .add("client_secret", "64626D9129612AB319752F3298AD94FD97E9B9F7F8F7EAB9")
                    .add("redirect_uri", "https://net.sjtu.edu.cn")
                    .add("client_id", "KfiGI09p9N4cPSHn5V7p")
                    .build();
            Headers headers = new Headers.Builder()
                    .add("Accept", "application/json")
                    .add("Content-Type", "application/json")
                    .build();
            Request request = new Request.Builder()
                    .url("https://jaccount.sjtu.edu.cn/oauth2/token")
                    .headers(headers)
                    .post(forms)
                    .build();
            Response resp = client.newCall(request).execute();

            if (!resp.isSuccessful())
            {
                return new CommonResult<>(false, "请求失败：" + String.valueOf(resp.code()) + "\r\n" + resp.body().string());
            }
            ObjectMapper mapper = new ObjectMapper();
            //使用readValue方法进行转换
            TokenService.tokenB = mapper.readValue(resp.body().string(), TokenB.class);

            CommonResult<ProfileDto> res = ProfileService.GetProfile();
            if (!res.success)
            {
                return new CommonResult<>(false, "获取用户信息失败：\n" + res.message);
            }
            return new CommonResult<>(true, "ok");
        }
        catch(Exception ex)
        {
            return new CommonResult<Boolean>(false, ex.getMessage());
        }
    }
    //---End---//
}

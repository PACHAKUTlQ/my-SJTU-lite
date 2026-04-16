package com.example.siyuanmalite.services;

import com.example.siyuanmalite.models.CommonResult;
import com.example.siyuanmalite.models.TokenB;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

public class TokenService {
    public static TokenB tokenB;

    public static CommonResult<TokenB> RefreshTokenB(String refreshtoken)
    {
        try
        {
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .build();
            Headers headers = new Headers.Builder()
                    .add("Accept", "application/json")
                    .add("Content-Type", "application/json")
                    .build();
            FormBody forms = new FormBody.Builder()
                    .add("refresh_token", refreshtoken)
                    .add("grant_type", "refresh_token")
                    .add("scope", "")
                    .add("client_secret", "64626D9129612AB319752F3298AD94FD97E9B9F7F8F7EAB9")
                    .add("redirect_uri", "https://net.sjtu.edu.cn")
                    .add("client_id", "KfiGI09p9N4cPSHn5V7p")
                    .build();
            Request req = new Request.Builder()
                    .url("https://jaccount.sjtu.edu.cn/oauth2/token")
                    .headers(headers)
                    .post(forms)
                    .build();

            Response resp = client.newCall(req).execute();
            if (!resp.isSuccessful())
                return new CommonResult<TokenB>(false, "返回代码：" + String.valueOf(resp.code()));

            ObjectMapper mapper = new ObjectMapper();
            tokenB = mapper.readValue(resp.body().string(), TokenB.class);
            return new CommonResult<TokenB>(true, "", tokenB);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return new CommonResult<TokenB>(false, ex.getMessage());
        }
    }
}

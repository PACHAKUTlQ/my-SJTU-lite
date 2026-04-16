package com.example.siyuanmalite.services;

import com.example.siyuanmalite.models.CommonResult;
import com.example.siyuanmalite.models.UnicodeIdentity;
import com.example.siyuanmalite.models.UnicodePay;
import com.example.siyuanmalite.models.UnicodePayEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

public class UnicodeService {

    public static CommonResult<String> GetUnicodeIdentity()
    {
        try
        {
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .build();
            HttpUrl url1 = HttpUrl.parse("https://api.sjtu.edu.cn/v1/unicode/identity")
                    .newBuilder()
                    .addQueryParameter("access_token", TokenService.tokenB.getAccessToken())
                    .addQueryParameter("checkSupplier", "false")
                    .addQueryParameter("checkUpgrade", "false")
                    .build();
            Headers headers1 = new Headers.Builder()
                    .add("User-Agent", "Siyuanma/1.0 (Third-Party)")
                    .add("Authorization", "Bearer " + TokenService.tokenB.getAccessToken())
                    .build();
            Request req1 = new Request.Builder()
                    .get()
                    .url(url1)
                    .headers(headers1)
                    .build();
            Response resp1 = client.newCall(req1).execute();
            if (!resp1.isSuccessful())
                return new CommonResult<String>(false, "返回代码：" + String.valueOf(resp1.code()));

            ObjectMapper mapper = new ObjectMapper();
            String resp1str = resp1.body().string();
            UnicodeIdentity identity = mapper.readValue(resp1str, UnicodeIdentity.class);

            return new CommonResult<String>(true, "", identity.getEntities()[0].getCode());
        }
        catch(Exception ex)
        {
            return new CommonResult<String>(false, ex.getMessage());
        }
    }
    public static CommonResult<UnicodePayEntity> GetUnicodePay(String userCode, String supplier)
    {
        try
        {
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .build();
            HttpUrl url2 = HttpUrl.parse("https://api.sjtu.edu.cn/v1/unicode/pay")
                    .newBuilder()
                    .addQueryParameter("access_token", TokenService.tokenB.getAccessToken())
                    .addQueryParameter("supplier", supplier)
                    .addQueryParameter("userCode", userCode)
                    .addQueryParameter("offline", "true")
                    .build();
            Headers headers2 = new Headers.Builder()
                    .add("User-Agent", "Siyuanma/1.0 (Third-Party)")
                    .add("Authorization", "Bearer " + TokenService.tokenB.getAccessToken())
                    .build();

            Request req2 = new Request.Builder()
                    .get()
                    .url(url2)
                    .headers(headers2)
                    .build();

            Response resp2 = client.newCall(req2).execute();
            if (!resp2.isSuccessful())
                return new CommonResult<>(false, "返回代码：" + String.valueOf(resp2.code()));

            ObjectMapper mapper = new ObjectMapper();
            String resp2str = resp2.body().string();
            UnicodePay pay = mapper.readValue(resp2str, UnicodePay.class);

            return new CommonResult<>(true, "", pay.getEntities()[0]);
        }
        catch(Exception ex)
        {
            return new CommonResult<>(false, ex.getMessage());
        }
    }
}

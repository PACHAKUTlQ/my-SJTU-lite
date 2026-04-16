package com.example.siyuanmalite.services;

import com.example.siyuanmalite.models.CommonResult;
import com.example.siyuanmalite.models.ProfileDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

public class ProfileService {
    private static ProfileDto dto;

    public static ProfileDto getDto() {
        return dto;
    }

    public static void setDto(ProfileDto dto2) {
        dto = dto2;
    }

    public static CommonResult<ProfileDto> GetProfile()
    {
        try
        {
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .build();
            HttpUrl url = HttpUrl.parse("https://api.sjtu.edu.cn/v1/me/profile")
                    .newBuilder()
                    .addQueryParameter("access_token", TokenService.tokenB.getAccessToken())
                    .build();
            Headers headers = new Headers.Builder()
                    .add("User-Agent", "Siyuanma/1.0 (Third-Party)")
                    .add("Authorization", "Bearer " + TokenService.tokenB.getAccessToken())
                    .build();
            Request req = new Request.Builder()
                    .get()
                    .url(url)
                    .headers(headers)
                    .build();
            Response resp = client.newCall(req).execute();
            if (!resp.isSuccessful())
                return new CommonResult<ProfileDto>(false, "返回代码：" + String.valueOf(resp.code()));

            ObjectMapper mapper = new ObjectMapper();
            dto = mapper.readValue(resp.body().string(), ProfileDto.class);
            return new CommonResult<ProfileDto>(true, "", dto);
        }
        catch (Exception ex)
        {
            return new CommonResult<ProfileDto>(false, ex.getMessage());
        }
    }
}

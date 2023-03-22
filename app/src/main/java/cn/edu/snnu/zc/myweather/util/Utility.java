package cn.edu.snnu.zc.myweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.snnu.zc.myweather.db.City;
import cn.edu.snnu.zc.myweather.db.Country;
import cn.edu.snnu.zc.myweather.db.Province;
import cn.edu.snnu.zc.myweather.gson.Weather;

public class Utility {

//    解析何处理服务器返回的省级数据
    public static boolean handleProvinceResponse(String response){

        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
//                    将数据存储在数据库中
                    province.save();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    return false;
    }

//    解析和处理服务器返回的市级数据
    public static boolean handleCityResponse(String response, int provinceId){
        if (TextUtils.isEmpty(response)){
            try {
                JSONArray allCities= new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }


//    解析和处理返回的县级数据
    public static boolean handleCountryResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties= new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countObject = allCounties.getJSONObject(i);
                    Country country = new Country();
                    country.setCountryName(countObject.getString("name"));
                    country.setWeatherId(countObject.getString("weather_id"));
                    country.setCityId(cityId);
                    country.save();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public static Weather  handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

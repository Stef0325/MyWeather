package cn.edu.snnu.zc.myweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.litepal.LitePal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.snnu.zc.myweather.db.City;
import cn.edu.snnu.zc.myweather.db.Country;
import cn.edu.snnu.zc.myweather.db.Province;
import cn.edu.snnu.zc.myweather.util.HttpUtil;
import cn.edu.snnu.zc.myweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE=0;

    public static final int LEVEL_CITY=1;

    public static final int LEVEL_COUNTY=2;

    public ProgressDialog mProgressDialog;

    private TextView mTitleText;

    private Button mBackButton;

    private ListView mListView;

    private ArrayAdapter<String> mAdapter;

    private List<String> mDataList=new ArrayList<>();

//    省份列表
    private List<Province> mProvinceList;

//    市级列表
    private List<City> mCityList;

//  县列表
    private List<Country> mCountryList;

//    选中的省份
    private Province mSelectedProvince;

//    选中的城市
    private City mSelectedCity;

//    选中的级别
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTitleText=(TextView)view.findViewById(R.id.title_text);
        mBackButton=(Button) view.findViewById(R.id.back_button);
        mListView=(ListView) view.findViewById(R.id.list_view);
        mAdapter=new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,mDataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    mSelectedProvince=mProvinceList.get(position);
                    queryCities();
                } else if (currentLevel==LEVEL_CITY) {
                    mSelectedCity=mCityList.get(position);
                    queryCountries();
                }else if (currentLevel==LEVEL_COUNTY){
                    String weatherId=mCountryList.get(position).getWeatherId();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
        mTitleText.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinceList= LitePal.findAll(Province.class);
        if (mProvinceList.size()>0){
            mDataList.clear();
            for (Province province:mProvinceList) {
                mDataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private void queryCities(){
        mTitleText.setText(mSelectedProvince.getProvinceName());
        mBackButton.setVisibility(View.VISIBLE);
        mCityList=LitePal.where("provinceid=?",String.valueOf(mSelectedProvince.getId())).find(City.class);
        if (mCityList.size()>0){
            mDataList.clear();
            for (City city:mCityList) {
                mDataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceCode=mSelectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china"+provinceCode;
            queryFromServer(address,"city");
        }


        mCountryList=LitePal.where("cityid = ? ",String.valueOf(mSelectedCity.getId())).find(Country.class);
        if (mCountryList.size()>0){
            mDataList.clear();
            for (Country country:mCountryList) {
                mDataList.add(country.getCountryName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            int cityCode = mSelectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"country");
        }

    }

    private void queryCountries(){
        mTitleText.setText(mSelectedCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        mCountryList= LitePal.where("cityid=?", String.valueOf(mSelectedCity.getId())).find(Country.class);
        if (mCountryList.size()>0){
            mDataList.clear();
            for (Country country:mCountryList) {
                mDataList.add(country.getCountryName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode=mSelectedProvince.getProvinceCode();
            int cityCode = mSelectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"country");
        }
    }



//根据传入的地址和类型访问服务器查询省市数据
    private void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
//            访问失败
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                closeProgressDialog();
//                通过调用runOnUiThread方法返回主线程
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result=Utility.handleCityResponse(responseText, mSelectedProvince.getId());
                } else if ("country".equals(type)) {
                    result=Utility.handleCountryResponse(responseText,mSelectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }

//    显示进度对话框
    private void showProgressDialog(){
        if (mProgressDialog==null){
            mProgressDialog=new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
//    关闭进度对话框
    private void closeProgressDialog(){
        if (mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
    }

}

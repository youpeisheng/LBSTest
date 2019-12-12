package com.example.lbstest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public LocationClient mlocationClient;
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mlocationClient=new LocationClient(getApplicationContext());
        mlocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView=(MapView) findViewById(R.id.bmapView);  //设置地图显示
        positionText=(TextView) findViewById(R.id.position_text_view);
        baiduMap=mapView.getMap(); //获取地图实例化对象
        baiduMap.setMyLocationEnabled(true);
        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
        !=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)
        !=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        !=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String []permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            registerLocation();
        }
    }
    private void navigateTo(BDLocation location){
        if (isFirstLocate){
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude()); //将数据装入 纬度 经度
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll); //装入数据更新
            baiduMap.animateMapStatus(update);
            update =MapStatusUpdateFactory.zoomTo(16f); //设置缩放比例 值越大 地图越精细
            baiduMap.animateMapStatus(update);
            isFirstLocate=false;
        }
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }
    private void registerLocation(){
        initLocation();
        mlocationClient.start();
    }
    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll"); //bd09ll  表示百度经纬度坐标， gcj02   表示经过国测局加密的坐标， wgs84   表示gps获取的坐标。
        option.setIsNeedAddress(true);
        mlocationClient.setLocOption(option);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        if (result!= PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    registerLocation();
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
                    break;
        }

    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            StringBuilder currentPosition=new StringBuilder();
            currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\r\n");
            currentPosition.append("经线：").append(bdLocation.getLongitude()).append("\r\n");
            currentPosition.append("国家：").append(bdLocation.getCountry()).append("\r\n");
            currentPosition.append("省：").append(bdLocation.getProvince()).append("\r\n");
            currentPosition.append("市：").append(bdLocation.getCity()).append("\r\n");
            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\r\n");
            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\r\n");
            currentPosition.append("定位方式：");
            if (bdLocation.getLocType()==BDLocation.TypeGpsLocation||bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(bdLocation);
            }
            positionText.setText(currentPosition);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mlocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}

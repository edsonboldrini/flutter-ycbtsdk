package com.edsonboldrini.flutter_ycbtsdk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import com.edsonboldrini.flutter_ycbtsdk.DeviceAdapter;
// import com.edsonboldrini.ConnectEvent;

import static com.yucheng.ycbtsdk.Constants.BLEState.ReadWriteOK;

import com.yucheng.ycbtsdk.AITools;
import com.yucheng.ycbtsdk.Constants;
import com.yucheng.ycbtsdk.YCBTClient;
import com.yucheng.ycbtsdk.bean.AIDataBean;
import com.yucheng.ycbtsdk.bean.HRVNormBean;
import com.yucheng.ycbtsdk.bean.ScanDeviceBean;
import com.yucheng.ycbtsdk.response.BleAIDiagnosisHRVNormResponse;
import com.yucheng.ycbtsdk.response.BleAIDiagnosisResponse;
import com.yucheng.ycbtsdk.response.BleConnectResponse;
import com.yucheng.ycbtsdk.response.BleDataResponse;
import com.yucheng.ycbtsdk.response.BleDeviceToAppDataResponse;
import com.yucheng.ycbtsdk.response.BleRealDataResponse;
import com.yucheng.ycbtsdk.response.BleScanResponse;
import com.yucheng.ycbtsdk.utils.YCBTLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/** FlutterYcbtsdkPlugin */
public class FlutterYcbtsdkPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native
  /// Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine
  /// and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_ycbtsdk");
    channel.setMethodCallHandler(this);

    EventBus.getDefault().register(this);
  }

  private List<ScanDeviceBean> listModel = new ArrayList<>();
  private List<String> listVal = new ArrayList<>();
  DeviceAdapter deviceAdapter = new DeviceAdapter(listModel);

  private Handler handler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(@NonNull Message msg) {
      if (msg.what == 0) {
        handler.sendEmptyMessageDelayed(0, 1000);
        YCBTClient.getAllRealDataFromDevice(new BleDataResponse() {
          @Override
          public void onDataResponse(int i, float v, HashMap hashMap) {
            Log.e("debug", hashMap.toString());
          }
        });
      } else if (msg.what == 1) {
        Log.e("debug", "1");
      } else if (msg.what == 2) {
        Log.e("debug", "2");
      } else if (msg.what == 3) {
        Log.e("debug", "3");
      } else if (msg.what == 4) {
        Log.e("debug", "4");
      }
      return false;
    }
  });

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getPlatformVersion":
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      case "startScan": {
        YCBTClient.startScanBle(new BleScanResponse() {
          @Override
          public void onScanResponse(int i, ScanDeviceBean scanDeviceBean) {

            if (scanDeviceBean != null) {
              if (!listVal.contains(scanDeviceBean.getDeviceMac())) {
                listVal.add(scanDeviceBean.getDeviceMac());
                deviceAdapter.addModel(scanDeviceBean);
              }

              Log.e("device", "mac=" + scanDeviceBean.getDeviceMac() + ";name=" + scanDeviceBean.getDeviceName()
                  + "rssi=" + scanDeviceBean.getDeviceRssi());

            }
          }
        }, 6);

        break;
      }
      case "stopScan": {
        YCBTClient.stopScanBle();
        break;
      }
      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
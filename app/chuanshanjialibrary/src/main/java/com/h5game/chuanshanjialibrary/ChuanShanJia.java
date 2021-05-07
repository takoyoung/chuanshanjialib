package com.h5game.chuanshanjialibrary;

import android.app.Activity;
import android.app.Application;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.h5game.thirdpartycallback.ThirdPartyCallback;

import java.util.HashMap;
import java.util.Map;

public class ChuanShanJia extends ThirdPartyCallback {
    private TTAdManager mTTAdManager;
    private TTAdNative mTTAdNative;
    private TTRewardVideoAd mTTRewardVideoAd;
    private boolean mRewardVerify;

    public ChuanShanJia(Activity activity, String appId, String className){
        super(className);
        mActivity = activity;

        init(appId);
    }

    private void init(String appId){
        TTAdSdk.init(mActivity.getApplicationContext(),
                new TTAdConfig.Builder()
                        .appId(String.valueOf(appId))
                        .useTextureView(false) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                        .allowShowNotify(true) //是否允许sdk展示通知栏提示
                        .allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
                        .debug(true) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_4G) //允许直接下载的网络状态集合
                        .supportMultiProcess(false) //是否支持多进程，true支持
                        .build());

        mTTAdManager = TTAdSdk.getAdManager();
        mTTAdNative = mTTAdManager.createAdNative(mActivity);
        mTTAdManager.requestPermissionIfNecessary(mActivity);
    }

    public void openRewardVideoAd(int callbackId, Map map){
        if(!checkCallbackId(callbackId)){
            return;
        }

        int codeId = (int)map.get("codeId");
        int uid = (int)map.get("uid");
        String mediaExtra = (String)map.get("mediaExtra");
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(String.valueOf(codeId))
                .setImageAcceptedSize(1080, 1920)
                //非必传参数，仅奖励发放服务端回调时需要使用
                .setUserID(String.valueOf(uid))
                .setMediaExtra(mediaExtra)
                .build();
        mTTAdNative.loadRewardVideoAd(adSlot, mLoadRewardVideoAdListener);
    }

    private void showAdVideo(){
        mTTRewardVideoAd.showRewardVideoAd(mActivity);
    }

    private TTAdNative.RewardVideoAdListener mLoadRewardVideoAdListener = new TTAdNative.RewardVideoAdListener() {
        @Override
        public void onError(int code, String message) {
            log("Loading Rewarded Video creative encountered an error: " + (code) + ",error message:" + message);
            callErr(-1, 1, "广告视频发生错误：" + message);
        }

        @Override
        public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
            log(" TTRewardVideoAd ：" + ad);
            if (ad != null) {
                mTTRewardVideoAd = ad;
                mTTRewardVideoAd.setRewardAdInteractionListener(mRewardAdInteractionListener);
                showAdVideo();
            } else {
                log(" TTRewardVideoAd is null !");
                callErr(-1, 2, "广告视频加载错误");
            }
        }

        @Override
        public void onRewardVideoCached() {
            log("TTRewardVideoAd onRewardVideoCached...");
        }
    };

    private TTRewardVideoAd.RewardAdInteractionListener mRewardAdInteractionListener = new TTRewardVideoAd.RewardAdInteractionListener() {
        @Override
        public void onAdShow() {
            log("TTRewardVideoAd onAdShow...");
        }

        @Override
        public void onAdVideoBarClick() {
            log("TTRewardVideoAd onAdVideoBarClick...");
        }

        @Override
        public void onAdClose() {
            log("TTRewardVideoAd onAdClose...");
        }

        @Override
        public void onVideoComplete() {
            log("TTRewardVideoAd onVideoComplete...");
            Map map = new HashMap();
            map.put("rewardVerify", mRewardVerify);
            callSuccess(map);
        }

        @Override
        public void onVideoError() {
            log("TTRewardVideoAd onVideoError...");
            callErr(-1, 3 , "广告视频发生错误");
        }

        @Override
        public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
            mRewardVerify = rewardVerify;
            log("TTRewardVideoAd onRewardVerify...rewardVerify：" + rewardVerify + "，rewardAmount=" + rewardAmount + "，rewardName=" + rewardName);
        }

        @Override
        public void onSkippedVideo() {
            log("TTRewardVideoAd onSkippedVideo...");
            callErr(-1, 4 , "用户跳过视频");
        }
    };
}

package com.wiot.alps_ax6737.uhf_plugin;

import com.handheld.UHFHelper;
import com.handheld.UHFListener;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * UhfPlugin
 */
public class UHFPlugin implements FlutterPlugin, MethodCallHandler {

    private static final String CHANNEL_IsStarted = "isStarted";
    private static final String CHANNEL_Start = "start";
    private static final String CHANNEL_Stop = "stop";
    private static final String CHANNEL_ClearData = "clearData";
    private static final String CHANNEL_IsEmptyTags = "isEmptyTags";
    private static final String CHANNEL_Close = "close";
    private static final String CHANNEL_Connect = "connect";
    private static final String CHANNEL_IsConnected = "isConnected";

    private static PublishSubject<Boolean> connectedStatus = PublishSubject.create();
    private static PublishSubject<String> tagsStatus = PublishSubject.create();
    private static final String CHANNEL_ConnectedStatus = "ConnectedStatus";
    private static final String CHANNEL_TagsStatus = "TagsStatus";


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        final MethodChannel channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "uhf_plugin");
        initConnectedEvent(flutterPluginBinding.getBinaryMessenger());
        initReadEvent(flutterPluginBinding.getBinaryMessenger());

        channel.setMethodCallHandler(new UHFPlugin());
        UHFHelper.getInstance().init(new UHFListener() {
            @Override
            public void onRead(String tagsJson) {
//                for (Map<String, Object> map : tagsList) {
//                    String tag = TagKey.getTag(map);
                if (tagsJson != null)
                    tagsStatus.onNext(tagsJson);
//                }
            }

            @Override
            public void onConnect(boolean isConnected, int powerLevel) {
                connectedStatus.onNext(isConnected);
            }
        });
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "uhf_plugin");
        initConnectedEvent(registrar.messenger());
        initReadEvent(registrar.messenger());
        channel.setMethodCallHandler(new UHFPlugin());

        UHFHelper.getInstance().init(new UHFListener() {
            @Override
            public void onRead(String tagsJson) {
//                for (Map<String, Object> map : tagsList) {
//                    String tag = TagKey.getTag(map);
                if (tagsJson != null)
                    tagsStatus.onNext(tagsJson);
//                }
            }

            @Override
            public void onConnect(boolean isConnected, int powerLevel) {
                connectedStatus.onNext(isConnected);
            }
        });


    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        handleMethods(call.method, result);
    }

    private void handleMethods(String method, Result result) {
        switch (method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case CHANNEL_IsStarted:
                result.success(UHFHelper.getInstance().isStarted());
                break;
            case CHANNEL_Start:
                UHFHelper.getInstance().start();
                result.success(true);
                break;
            case CHANNEL_Stop:
                UHFHelper.getInstance().stop();
                result.success(true);
                break;
            case CHANNEL_ClearData:
                UHFHelper.getInstance().clearData();
                result.success(true);
                break;
            case CHANNEL_IsEmptyTags:
                result.success(UHFHelper.getInstance().isEmptyTags());
                break;
            case CHANNEL_Close:
                UHFHelper.getInstance().close();
                result.success(true);
                break;
            case CHANNEL_Connect:
                UHFHelper.getInstance().connect();
                result.success(true);
                break;
            case CHANNEL_IsConnected:
                result.success(UHFHelper.getInstance().isConnected());
                break;
            default:
                result.notImplemented();
        }
    }

    private static void initConnectedEvent(BinaryMessenger messenger) {
        final EventChannel scannerEventChannel = new EventChannel(messenger, CHANNEL_ConnectedStatus);
        scannerEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, final EventChannel.EventSink eventSink) {
                connectedStatus
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean isConnected) {
                        eventSink.success(isConnected);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void onCancel(Object o) {

            }
        });
    }

    private static void initReadEvent(BinaryMessenger messenger) {
        final EventChannel scannerEventChannel = new EventChannel(messenger, CHANNEL_TagsStatus);
        scannerEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, final EventChannel.EventSink eventSink) {
                tagsStatus
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String tag) {
                        eventSink.success(tag);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void onCancel(Object o) {

            }
        });
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    }
}

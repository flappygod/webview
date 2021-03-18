package io.flutter.plugins.webviewflutter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugins.webviewflutter.WebViewFlutterPlugin;
import static android.app.Activity.RESULT_OK;

//集成文件上传
public class WebChromeFileClient extends WebChromeClient implements PluginRegistry.ActivityResultListener {

    //上传文件
    private ValueCallback<Uri> mUploadMessage;

    //上传文件
    public ValueCallback<Uri[]> mUploadMessageForAndroid5;

    //早期的Android 5.0之前的
    public final static int FILECHOOSER_RESULTCODE_ANDROID_EARLY = 1;

    //现在的Android 5.0之后的
    public final static int FILECHOOSER_RESULTCODE_ANDROID_NOWER = 2;


    //构造器
    public WebChromeFileClient() {
        super();
        initBinding();
    }

    //init Binding
    private void initBinding() {
        //绑定
        ActivityPluginBinding binding = WebViewFlutterPlugin.activityPluginBinding;
        //绑定
        if (binding != null) {
            binding.addActivityResultListener(this);
        }
    }


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        //旧版本
        if (requestCode == FILECHOOSER_RESULTCODE_ANDROID_EARLY) {
            if (null == mUploadMessage)
                return false;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }
        //新版本
        else if (requestCode == FILECHOOSER_RESULTCODE_ANDROID_NOWER) {
            if (null == mUploadMessageForAndroid5)
                return false;
            Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
            if (result != null) {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
            } else {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
            }
            mUploadMessageForAndroid5 = null;
        }
        return false;
    }


    //For Early Android
    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        Activity activity = WebViewFlutterPlugin.activity;
        if (activity != null) {
            activity.startActivityForResult(Intent.createChooser(intent, "文件选择"), FILECHOOSER_RESULTCODE_ANDROID_EARLY);
        }
    }

    //For android5.0
    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
        mUploadMessageForAndroid5 = uploadMsg;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "文件选择");
        Activity activity = WebViewFlutterPlugin.activity;
        if (activity != null) {
            activity.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE_ANDROID_NOWER);
        }
    }


    //android>5.0用这个方法
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {
        openFileChooserImplForAndroid5(filePathCallback);
        return true;
    }

    // js上传文件的<input type="file" name="fileField" id="fileField" />事件捕获
    // Android > 4.1.1 调用这个方法
    public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                String acceptType,
                                String capture) {
        openFileChooserImpl(uploadMsg);
    }

    // 3.0 + 调用这个方法
    public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                String acceptType) {
        openFileChooserImpl(uploadMsg);
    }

    // Android < 3.0 调用这个方法
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooserImpl(uploadMsg);
    }


}


package com.youqude.storyflow.baidu;

import com.youqude.storyflow.utils.StoryLogger;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class BaiduOAuthViaDialog extends BaiduOAuth
{
    private static String TAG = BaiduOAuthViaDialog.class.getSimpleName();

    private BaiduDialogListener mAuthDialogListener;

    public BaiduOAuthViaDialog(String consumerKey)
    {
        super(consumerKey);
    }

    public BaiduOAuthViaDialog(String consumerKey, String accessToken)
    {
        super(consumerKey, accessToken);
    }

    public boolean startDialogAuth(Context context, String[] permissions,
            final BaiduDialogListener listener)
    {
        if (listener == null)
        {
            return false;
        }

        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();

        cookieManager.removeSessionCookie();
        CookieSyncManager.getInstance().sync();

        this.mAuthDialogListener = listener;

        if (context.checkCallingOrSelfPermission("android.permission.INTERNET") != 0) {
            this.mAuthDialogListener
                    .onException("Application requires permission to access the Internet");
        }

        dialog(context, permissions, new BaiduDialogListener() {

            @Override
            public void onException(String msg) {
                BaiduOAuthViaDialog.this.mAuthDialogListener.onException(msg);
            }

            @Override
            public void onComplete(Bundle values) {
                CookieSyncManager.getInstance().sync();

                BaiduOAuthViaDialog.this.setAccessToken(values.getString("access_token"));
                BaiduOAuthViaDialog.this.setAccessExpiresIn(values.getString("expires_in"));
                BaiduOAuthViaDialog.this.setmSessionKey(values.getString("session_key"));
                BaiduOAuthViaDialog.this.setmSessionSecret(values.getString("session_secret"));

                if (BaiduOAuthViaDialog.this.IsSessionValid())
                    mAuthDialogListener.onComplete(values);
                else
                    mAuthDialogListener.onException("access_token not valid");
            }

            @Override
            public void onCancel() {
                mAuthDialogListener.onCancel();
            }
        });
        return true;
    }

    public boolean startDialogAuth(Context activity, BaiduDialogListener listener) throws Exception
    {
        return startDialogAuth(activity, new String[0], listener);
    }

    private void dialog(Context context, String[] permissions, BaiduDialogListener listener)
    {
        String scope = "";
        if (permissions.length > 0) {
            scope = "&scope=" + TextUtils.join(" ", permissions);
        }
        StoryLogger.e("scope", scope);
        String url = "https://openapi.baidu.com/oauth/2.0/authorize?response_type=token&redirect_uri=oob&display=mobile&client_id="
                + getConsumerKey();
         url = url + scope;
        Log.e("BaiduOAuth", url);
        new OAuthWebViewDlg(context, url, listener).show();
    }

    private Bundle parseUrl(String url)
    {
        try
        {
            URL u = new URL(url);
            Bundle b = decodeUrl(u.getQuery());
            b.putAll(decodeUrl(u.getRef()));
            return b;
        } catch (MalformedURLException e) {
        }
        return new Bundle();
    }

    private Bundle decodeUrl(String s)
    {
        Bundle params = new Bundle();
        if (s != null) {
            String[] array = s.split("&");
            for (String parameter : array) {
                String[] v = parameter.split("=");
                params.putString(URLDecoder.decode(v[0]), URLDecoder.decode(v[1]));
            }
        }
        return params;
    }

    public static enum DialogLayout
    {
        FULLSCREEN_MODE,
        MODE_QVGA_,

        MODE_HVGA,

        MODE_WVGA,

        MODE_WSVGA,

        MODE_WXGA,

        MODE_WXGAPLUS,

        DIALOG_MODE_480_320;
    }

    public class OAuthWebViewDlg extends Dialog {

        final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        private String mUrl;
        private BaiduDialogListener mListener;
        private WebView mWebView;
        private RelativeLayout mContent;
        private ProgressDialog mSpinner;

        protected OAuthWebViewDlg(Context context, String url,
                BaiduDialogListener listener) {
            super(context);
            this.mUrl = url;
            this.mListener = listener;
        }

        public void onBackPressed()
        {
            dismiss();
            this.mListener.onCancel();
        }

        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            this.mSpinner = new ProgressDialog(getContext());
            this.mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.mContent = new RelativeLayout(getContext());

            setUpWebView(10);

            setContentView(this.mContent);
        }

        private void setUpWebView(int margin)
        {
            /*
             * LinearLayout webViewContainer = new LinearLayout(getContext());
             * this.mWebView = new WebView(getContext());
             * this.mWebView.addJavascriptInterface(new MyJavaScript(),
             * "HTMLOUT"); this.mWebView.setVerticalScrollBarEnabled(false);
             * this.mWebView.setHorizontalScrollBarEnabled(false);
             * this.mWebView.setWebViewClient(new OAuthWebViewClient());
             * this.mWebView.getSettings().setJavaScriptEnabled(true);
             * this.mWebView.loadUrl(this.mUrl); this.mWebView.setLayoutParams(
             * new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,
             * LayoutParams.FILL_PARENT));
             * this.mWebView.getSettings().setUseWideViewPort(true);
             * this.mWebView.getSettings().setLoadWithOverviewMode(true);
             * webViewContainer.setPadding(margin, margin, margin, margin);
             * webViewContainer.addView(this.mWebView);
             * this.mContent.addView(webViewContainer);
             */

            RelativeLayout webViewContainer = new RelativeLayout(getContext());

            mWebView = new WebView(getContext());
            mWebView.setVerticalScrollBarEnabled(false);
            mWebView.setHorizontalScrollBarEnabled(false);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.addJavascriptInterface(new MyJavaScript(), "HTMLOUT");
            mWebView.setWebViewClient(new OAuthWebViewClient());
            mWebView.loadUrl(mUrl);
            mWebView.setLayoutParams(FILL);

            webViewContainer.addView(mWebView);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT);
            Resources resources = getContext().getResources();
            mContent.addView(webViewContainer, lp);

        }

        private class MyJavaScript
        {
            private MyJavaScript()
            {}

            public void isError(String html)
            {
                if (html.contains("错误码")) {
                    String errorcode = html.split("#")[1].substring(0, 4);
                    BaiduOAuthViaDialog.OAuthWebViewDlg.this.mListener.onException("ErrorCode: "
                            + errorcode);
                    BaiduOAuthViaDialog.OAuthWebViewDlg.this.dismiss();
                }
            }
        }

        public class OAuthWebViewClient extends WebViewClient
        {
            public OAuthWebViewClient()
            {
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Log.e(TAG, "shouldOverrideUrlLoading:" + url);
                if (url.contains("error=access_denied")) {
                    mListener.onCancel();
                    mSpinner.dismiss();
                    dismiss();
                    return true;
                }

                return false;
            }

            public void onReceivedError(WebView view, int errorCode, String description,
                    String failingUrl)
            {
                super.onReceivedError(view, errorCode, description, failingUrl);
                BaiduOAuthViaDialog.OAuthWebViewDlg.this.mListener.onException(description);
                BaiduOAuthViaDialog.OAuthWebViewDlg.this.mSpinner.dismiss();
                BaiduOAuthViaDialog.OAuthWebViewDlg.this.dismiss();
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                Log.e(TAG, "onPageStarted:" + url);
                if (url.contains("http://wap.baidu.com/?")) {
                    BaiduOAuthViaDialog.OAuthWebViewDlg.this.mWebView
                            .loadUrl(BaiduOAuthViaDialog.OAuthWebViewDlg.this.mUrl);
                }
                else {
                    super.onPageStarted(view, url, favicon);

                    if ((url.contains("authorize?response_type=token"))
                            && (!BaiduOAuthViaDialog.OAuthWebViewDlg.this.mSpinner.isShowing()))
                        BaiduOAuthViaDialog.OAuthWebViewDlg.this.mSpinner.show();
                }
            }

            public void onPageFinished(WebView view, String url)
            {
                Log.e(TAG, "onPageFinished:" + url);
                super.onPageFinished(view, url);
                view.loadUrl("javascript:window.HTMLOUT.isError(document.getElementsByTagName('p')[0].innerHTML);");
                if (BaiduOAuthViaDialog.OAuthWebViewDlg.this.mSpinner.isShowing()) {
                    BaiduOAuthViaDialog.OAuthWebViewDlg.this.mSpinner.dismiss();
                }

                if (!url.contains("/login_success"))
                    return;
                
                Bundle values = BaiduOAuthViaDialog.this.parseUrl(url);
                BaiduOAuthViaDialog.OAuthWebViewDlg.this.mListener.onComplete(values);
                BaiduOAuthViaDialog.OAuthWebViewDlg.this.dismiss();
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
            {
                handler.proceed();
            }
        }
    }

}

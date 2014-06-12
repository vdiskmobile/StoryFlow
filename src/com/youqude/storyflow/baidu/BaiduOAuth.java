package com.youqude.storyflow.baidu;

import android.content.Context;
import java.util.List;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public abstract class BaiduOAuth
{
  static final String TAG_URL_TOKEN = "access_token";
  static final String TAG_URL_EXPIRES = "expires_in";
  static final String TAG_URL_SESSION_KEY = "session_key";
  static final String TAG_URL_SESSION_SECRET = "session_secret";
  private long mExpiresDate;
  private String mSessionKey;
  private String mSessionSecret;
  private String mConsumerKey;
  private String mAccessToken = null;
  protected static final String AUTH_URL = "https://openapi.baidu.com/oauth/2.0/authorize?response_type=token&redirect_uri=oob&display=mobile&scope=netdisk";
  public static final String SUCCESS_URL = "/login_success";
  public static final String REGISTERSUCCESS_URL = "http://wap.baidu.com/?";
  public static final int OAUTH_ERR_GENERAL = -1;

  public BaiduOAuth(String consumerKey)
  {
    this.mConsumerKey = consumerKey;
  }

  public BaiduOAuth(String consumerKey, String mAccessToken)
  {
    if (consumerKey == null) {
      throw new IllegalArgumentException("'cientid' must be non-null");
    }
    this.mConsumerKey = consumerKey;
    setAccessToken(mAccessToken);
  }

  public boolean IsSessionValid()
  {
    return (getAccessToken() != null) && ((
      (getAccessExpires() == 0L) || (System.currentTimeMillis() < getAccessExpires())));
  }

  public void signInHeader(HttpRequest request)
  {
    request.addHeader(
      "Cookie", 
      "BDUSS=9BUU5tcS1PWXBaZHNhUmFCeDNhVmk2aHRRNWJ-Q0NPNEUzRm9MNHhmOUJNZTlPQUFBQUFBJCQAAAAAAAAAAAouSSCTkHMeeWlkaXNrX3Rlc3QwMQAAAAAAAAAAAAAAAAAAAAAAAACAYIArMAAAAOAahn4AAAAAuWZCAAAAAAAxMC42NS4yMkGkx05BpMdOZk");
  }

  public void signInUri(List<NameValuePair> params)
  {
    synchronized (this) {
      params.add(new BasicNameValuePair("access_token", getAccessToken()));
    }
  }

  public void addCommonParameter(List<NameValuePair> params)
  {
    params.add(new BasicNameValuePair("clienttype", "1"));
    params.add(new BasicNameValuePair("channel", ""));

    params.add(new BasicNameValuePair("version", "1"));
  }

  private long getAccessExpires() {
    synchronized (this) {
      return this.mExpiresDate;
    }
  }

  public abstract boolean startDialogAuth(Context paramContext, BaiduDialogListener paramDialogListener)
    throws Exception;

  public abstract boolean startDialogAuth(Context paramContext, String[] paramArrayOfString, BaiduDialogListener paramDialogListener);

  public boolean isLinked()
  {
    synchronized (this) {
      return this.mAccessToken != null;
    }
  }

  public void unlink() {
    setAccessToken(null);
  }

  protected void setAccessExpiresIn(String expiresIn)
  {
    if ((expiresIn != null) && (!expiresIn.equals("0")))
      setAccessExpires(System.currentTimeMillis() + Long.parseLong(expiresIn) * 1000L);
  }

  public void setAccessExpires(long time)
  {
    synchronized (this) {
      this.mExpiresDate = time;
    }
  }

  public long getExpireExpires() {
    synchronized (this) {
      return this.mExpiresDate;
    }
  }

  public void setConsumerKey(String consumerID) {
    synchronized (this) {
      this.mConsumerKey = consumerID;
    }
  }

  public String getConsumerKey() {
    synchronized (this) {
      return this.mConsumerKey;
    }
  }

  public String getAccessToken() {
    synchronized (this) {
      return this.mAccessToken;
    }
  }

  public void setAccessToken(String mAccessToken) {
    synchronized (this) {
      this.mAccessToken = mAccessToken;
    }
  }

  protected void setmSessionKey(String mSessionKey)
  {
    synchronized (this) {
      this.mSessionKey = mSessionKey;
    }
  }

  protected void setmSessionSecret(String secret) {
    synchronized (this) {
      this.mSessionSecret = secret;
    }
  }
  
  public String getSessionKey(){
      synchronized (this) {
          return this.mSessionKey;
      }
  }
  
  public String getSessionSecret(){
      synchronized (this) {
          return this.mSessionSecret;
      }
  }

  protected void setmExpiresDate(long mExpiresDate) {
    synchronized (this) {
      this.mExpiresDate = mExpiresDate;
    }
  }
}
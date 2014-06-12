package com.youqude.storyflow.pcs;

import com.youqude.storyflow.pcs.HttpHelper.BadRequestException;
import com.youqude.storyflow.pcs.HttpHelper.RestHttpException;
import com.youqude.storyflow.pcs.exception.PcsException;
import com.youqude.storyflow.pcs.exception.PcsHttpException;
import com.youqude.storyflow.pcs.exception.PcsKnownException;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * PcsClient 对象，包含请求服务器server所需要的方法接口 
 */
public class PcsClient {
	private static final String TAG = PcsClient.class.getSimpleName();

	public static final String VERSION = "0.9";
	public static final long MAX_UPLOAD_SIZE = 1024 * 1024 * 1024; // 1G
	public static final String PCSHOST = "https://pcs.baidu.com/";

	public static final String ORDER_BY_TIME = "time";
	public static final String ORDER_BY_NAME = "name";
	public static final String ORDER_BY_SIZE = "size";

	public static final String ORDER_DESC = "desc";
	public static final String ORDER_SC = "sc";

	private String accessToken = null;
	private String appRoot = null;

	private HttpHelper httpHelper = new HttpHelper();
	/**
	 * 实例化一个PcsClient对象
	 * @param accessToken : 用户的access token字符串，用来对用户身份进行认证。
	 * @param appRoot : app 的root目录 信息，需要以/结尾，如/apps/YOUR_APP_ROOT/
	 */
	public PcsClient(String accessToken, String appRoot) {
		this.accessToken = accessToken;
		this.appRoot = appRoot;
	}
	
	/**
	 * 上传一个本地文件到pcs.
	 * 
	 * @param localPath 上传文件的本地绝对路径，例如'd:/england/roony.jpg'。
	 * @param remoteDir 指定文件存放路径，必须是当前应用的根目录或者子目录，例如'/apps/YOUR_APP_ROOT/'或者'/apps/YOUR_APP_ROOT/英超足球'。
	 * @param remoteName 指定创建的文件名
	 * @return 
	 * 	PcsUploadResult
	 * 
	 * @throws PcsException
	 */
	public PcsUploadResult uploadFile(String localPath, String remoteDir, String remoteName) throws PcsException {
		assertAuthenticated();
		Logger.i(TAG, TAG+"upload " + localPath + " to " + remoteDir + " / " + remoteName);
		String url = PCSHOST + "rest/2.0/pcs/file";

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("method", "upload"));
		params.add(new BasicNameValuePair("access_token", this.accessToken));
		params.add(new BasicNameValuePair("dir", remoteDir));
		params.add(new BasicNameValuePair("filename", remoteName));

		try {
			url = HttpHelper.buildURL(url, params);
			String resp = httpHelper.doPostMultipart(url, localPath, null);
			return new PcsUploadResult(resp);
		} catch (HttpException e) {
			e.printStackTrace();
			throw new PcsHttpException(e.toString());
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw new PcsException("bad request");
		} catch (RestHttpException e) {
			e.printStackTrace();
			throw new PcsKnownException(e.responseBody);
		}
	}

	/**
	 * 下载文件到本地
	 * @param remotePath 下载文件的地址，如'/apps/YOUR_APP_ROOT/英超足球/鲁尼.jpg'。
	 * @param localPath 指定文件存放的本地绝对目录。如'd:/a.txt'。
	 * @throws PcsException
	 */
	public void downloadToFile(String remotePath, String localPath) throws PcsException {
		assertAuthenticated();
		Logger.i(TAG, TAG +"download " + remotePath + " to " + localPath);
		String url = PCSHOST + "rest/2.0/pcs/file";

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("method", "download"));
		params.add(new BasicNameValuePair("access_token", this.accessToken));
		params.add(new BasicNameValuePair("path", remotePath));
		try {
			url = HttpHelper.buildURL(url, params);
			httpHelper.doGetToFile(url, localPath);
		} catch (HttpException e) {
			e.printStackTrace();
			throw new PcsHttpException(e.toString());
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw new PcsException("bad request");
		} catch (RestHttpException e) {
			e.printStackTrace();
			throw new PcsKnownException(e.responseBody);
		}
	}

	/**
	 * 列目录
	 * 
	 *   相当与： return list(remoteDir, ORDER_BY_TIME, ORDER_DESC, 0, 100);
	 * @param remoteDir 指定的目录路径，必须是当前应用的根目录或者子目录，例如'/apps/YOUR_APP_ROOT/'或者'/apps/YOUR_APP_ROOT/英超足球'。
	 * @return 返回搜索到的文件列表，结果为PcsFileEntry 构成的List
	 * @throws PcsException
	 */
	public List<PcsFileEntry> list(String remoteDir) throws PcsException {
		return list(remoteDir, ORDER_BY_TIME, ORDER_DESC, 0, 100);
	}

	/**
	 * 列目录, 可以制定排序方式，list范围等.
	 * @param remoteDir 指定的目录路径，必须是当前应用的根目录或者子目录，例如'/apps/YOUR_APP_ROOT/'或者'/apps/YOUR_APP_ROOT/英超足球'。
	 * @param orderBy 指定按某个列排序, 可以是PcsClient.ORDER_BY_TIME/PcsClient.ORDER_BY_NAME/PcsClient.ORDER_BY_SIZE
	 * @param sortOrder  指定升序或降序, 可以是 PcsClient.ORDER_DESC/PcsClient.ORDER_SC 
	 * @param limit_start 返回list列表的起始位置
	 * @param limit_end   返回list列表的结束位置
	 * @return 返回搜索到的文件列表，结果为PcsFileEntry 构成的List
	 * @throws PcsException
	 */
	public ArrayList<PcsFileEntry> list(String remoteDir, String orderBy, String sortOrder, int limit_start,
			int limit_end) throws PcsException {
		assertAuthenticated();
		Logger.i(TAG, TAG+"list " + remoteDir);
		String url = PCSHOST + "rest/2.0/pcs/file";
		String limit = limit_start + "-" + limit_end;
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("method", "list"));
		params.add(new BasicNameValuePair("access_token", this.accessToken));
		params.add(new BasicNameValuePair("dir", remoteDir));
		params.add(new BasicNameValuePair("by", orderBy));
		params.add(new BasicNameValuePair("order", sortOrder));

		params.add(new BasicNameValuePair("limit", limit));
		String responseBody = httpGet(url, params);
		return PcsFileEntry.parseArrayFromJsonString(responseBody);
	}

	/**
	 * 按部分或者完整文件名搜索文件，不支持正则表达式，返回命中的文件的元数据。（搜索结果不命中文件夹）
	 * @param remoteDir 指定要搜索的目录，必须是当前应用的根目录或者子目录。例如'/apps/YOUR_APP_ROOT/'或者'/apps/YOUR_APP_ROOT/英超足球'
	 * @param keyword 要搜索的文件名关键字，不支持正则匹配，且不支持匹配多个关键字。
	 * @param recursive false 表示非递归搜索，true表示递归搜索
	 * @return 返回搜索到的文件列表，结果为PcsFileEntry 构成的List
	 * @throws PcsException
	 */
	public ArrayList<PcsFileEntry> search(String remoteDir, String keyword, boolean recursive) throws PcsException {
		assertAuthenticated();
		Logger.i(TAG, TAG+"search " + remoteDir + " for " + keyword);
		String url = PCSHOST + "rest/2.0/pcs/file";

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("method", "search"));
		params.add(new BasicNameValuePair("access_token", this.accessToken));
		params.add(new BasicNameValuePair("dir", remoteDir));
		params.add(new BasicNameValuePair("wd", keyword));
		params.add(new BasicNameValuePair("re", new Boolean(recursive).toString()));

		String responseBody = httpGet(url, params);
		return PcsFileEntry.parseArrayFromJsonString(responseBody);
	}

	/**
	 * 删除指定路径的文件或者目录。
	 * @param remotePath 指定要删除的目录
	 * @throws PcsException
	 */
	public void delete(String remotePath) throws PcsException {
		Logger.i(TAG, TAG +"delete " + remotePath);
		assertAuthenticated();
		String url = PCSHOST + "rest/2.0/pcs/file";

		ArrayList<NameValuePair> qs_params = new ArrayList<NameValuePair>();
		qs_params.add(new BasicNameValuePair("method", "delete"));
		qs_params.add(new BasicNameValuePair("access_token", this.accessToken));
		qs_params.add(new BasicNameValuePair("path", remotePath));
		httpPost(url, qs_params);
	}
	
	
	/**
	 * 创建指定路径的目录，例如'/apps/YOUR_APP_ROOT/英超足球。
	 * @param remotePath 指定要删除的目录
	 * @throws PcsException
	 */
	public void mkdir(String remotePath) throws PcsException {
		Logger.i(TAG,"mkdir " + remotePath);
		assertAuthenticated();
		String url = PCSHOST + "rest/2.0/pcs/file";

		ArrayList<NameValuePair> qs_params = new ArrayList<NameValuePair>();
		qs_params.add(new BasicNameValuePair("method", "mkdir"));
		qs_params.add(new BasicNameValuePair("access_token", this.accessToken));
		qs_params.add(new BasicNameValuePair("path", remotePath));

		httpPost(url, qs_params);
	}

	/**
	 * 将文件/目录 从一个路径移动到另外一个路径，可当作'rename'使用
	 * @param remoteFromPath 源路径，如：/apps/YOUR_APP_ROOT/英超足球/鲁尼.jpg 
	 * @param remoteToPath 目标路径，如：/apps/YOUR_APP_ROOT/英超足球/鲁尼2.jpg
	 * @throws PcsException
	 */
	public void move(String remoteFromPath, String remoteToPath) throws PcsException {
		moveOrCopy(remoteFromPath, remoteToPath, "move");
	}
	/**
	 * 将文件/目录 从一个路径 拷贝 到另外一个路径
	 * @param remoteFromPath 源路径，如：/apps/YOUR_APP_ROOT/英超足球/鲁尼.jpg 
	 * @param remoteToPath 目标路径，如：/apps/YOUR_APP_ROOT/英超足球/鲁尼2.jpg
	 * @throws PcsException
	 */
	public void copy(String remoteFromPath, String remoteToPath) throws PcsException {
		moveOrCopy(remoteFromPath, remoteToPath, "copy");
	}

	/**
	 * 移动或拷贝 服务器端文件
	 * @param from
	 * @param to
	 * @param method
	 * @throws PcsException
	 */
	private void moveOrCopy(String from, String to, String method) throws PcsException {
		assertAuthenticated();
		String url = PCSHOST + "rest/2.0/pcs/file";

		ArrayList<NameValuePair> qs_params = new ArrayList<NameValuePair>();
		qs_params.add(new BasicNameValuePair("method", method));
		qs_params.add(new BasicNameValuePair("access_token", this.accessToken));
		qs_params.add(new BasicNameValuePair("from", from));
		qs_params.add(new BasicNameValuePair("to", to));
		
		httpPost(url, qs_params);
	}

	/**
	 * 一个简单封装，qs_params 是query string 中的param PCS 的post都有一个共同的格式:
	 * 如果不使用批量接口，就不需要这个函数了.
	 * post 为一个multipart , multipar 中 param的值为一个json串 : 
	 * {"list":[{"path":"\/apps\/album\/a\/b\/c"}，{"path":"\/apps\/album\/a\/b\/d"}]}
	 * 
	 * @param url
	 * @param qs_params
	 * @param jsonObject
	 * @return
	 * @throws PcsException
	 * @Deprecated
	 */
	private String httpPostParam(String url, ArrayList<NameValuePair> qs_params, JSONObject jsonObject) throws PcsException {
		try {
			url = HttpHelper.buildURL(url, qs_params);
			String jsonString = jsonObject.toJSONString();
			Logger.d(TAG, TAG+"post json: " + jsonString);

			ArrayList<NameValuePair> post_params = new ArrayList<NameValuePair>();
			post_params.add(new BasicNameValuePair("param", jsonString));

			return httpHelper.doPostMultipart(url, null/* file */, post_params);
		} catch (HttpException e) {
			e.printStackTrace();
			throw new PcsHttpException(e.toString());
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw new PcsException("bad request");
		} catch (RestHttpException e) {
			e.printStackTrace();
			throw new PcsKnownException(e.responseBody);
		}
	}

	private String httpGet(String url, ArrayList<NameValuePair> params) throws PcsException {
		try {
			url = HttpHelper.buildURL(url, params);
			return httpHelper.doGet(url);
		} catch (HttpException e) {
			e.printStackTrace();
			throw new PcsHttpException(e.toString());
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw new PcsException("bad request");
		} catch (RestHttpException e) {
			e.printStackTrace();
			throw new PcsKnownException(e.responseBody);
		}
	}
	
	private void httpPost(String url, ArrayList<NameValuePair> qs_params) throws PcsException, PcsKnownException {
		try {
			url = HttpHelper.buildURL(url, qs_params);
			httpHelper.doPostMultipart(url, null/* file */, null/*params*/);
		} catch (HttpException e) {
			e.printStackTrace();
			throw new PcsHttpException(e.toString());
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw new PcsException("bad request");
		} catch (RestHttpException e) {
			e.printStackTrace();
			throw new PcsKnownException(e.responseBody);
		}
	}
	private void assertAuthenticated() throws PcsException {
		if (this.accessToken == null)
			throw new PcsException("no access_token set");
	}

}

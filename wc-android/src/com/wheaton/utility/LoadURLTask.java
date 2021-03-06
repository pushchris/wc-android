package com.wheaton.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class LoadURLTask extends AsyncTask<Void, Void, String> {
	public LoadURLTask(String url, RunnableOfT<String> callback) {
		mUrl = url;
		mCallback = callback;
	}

	@Override
	protected String doInBackground(Void... params) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = Utils.sslClient(new DefaultHttpClient());
		HttpGet httpGet = new HttpGet(mUrl);
		BufferedReader reader = null;
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();

				reader = new BufferedReader(new InputStreamReader(new BOMInputStream(content, false, ByteOrderMark.UTF_8)));
				String line;
				while ((line = reader.readLine()) != null)
					builder.append(line);
			} else {
				Log.e(TAG, "Failed to download file:" + statusCode);
			}
		} catch (Exception e) {
			Log.e(TAG, "doInBackground", e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {}
		}
		return builder.toString();
	}

	@Override
	protected void onPostExecute(String result) {
		if (isCancelled())
			return;
		if (result != null)
			mCallback.run(result);
	}

	public static abstract class RunnableOfT<T> implements RunnableWithParam<T>{}

	private interface RunnableWithParam<T> {
		public void run(T result);
	}

	private static final String TAG = LoadURLTask.class.toString();

	private final String mUrl;
	private final RunnableOfT<String> mCallback;
}

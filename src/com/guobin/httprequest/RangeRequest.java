package com.guobin.httprequest;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This class intends to fetch a part of data from a remote URL and save it to a local file.
 *
 * @author  Guobin He
 */
public class RangeRequest {

    static final int TIMEOUT_DEFAULT = 30000;
    static final int BUFFER_SIZE = 4*1024;
    
    private URL url;
    private long rangeStart;
    private long rangeEnd;
    private String outputFileName;
    private int timeoutRead;
    private int timeoutConnect;
    private Map<String, String> requestHeaders;
    
	/**
     * Constructor
     * Specify range for issuing a range request
     * 
     * @param urlString : the URL
     * @param start : range start
     * @param end : range end
     * @param outputFileName : output file name
     */
	public RangeRequest(String urlString, long start, long end, String outputFileName)
			throws MalformedURLException, IllegalArgumentException {
		setUrl(urlString);
		setRange(start, end);
		this.outputFileName = outputFileName;
		timeoutConnect = TIMEOUT_DEFAULT;
		timeoutRead = TIMEOUT_DEFAULT;
		requestHeaders = null;
		setDefaultHeaders();
	}

    /**
     * Constructor
     * Do not specify range. The whole file will be retrieved.
     * 
     * @param urlString : the URL
     * @param start : range start
     * @param end : range end
     * @param outputFileName : output file name
     */
	public RangeRequest(String urlString, String outputFileName)
			throws MalformedURLException, IllegalArgumentException {
		this(urlString, 0, 0, outputFileName);
	}
	
    public int getTimeoutRead() {
		return timeoutRead;
	}

	public void setTimeoutRead(int timeoutRead) {
		this.timeoutRead = timeoutRead;
	}

	public int getTimeoutConnect() {
		return timeoutConnect;
	}

	public void setTimeoutConnect(int timeoutConnect) {
		this.timeoutConnect = timeoutConnect;
	}

	public void setUrl(String urlString) throws MalformedURLException {
        try {
        	this.url = new URL(urlString);
        }catch(MalformedURLException e) {
        	throw e;
        }
	}

	public void setRange(long start, long end) throws IllegalArgumentException {
		if(start>end || start<0 || end<0) {
			IllegalArgumentException e = new IllegalArgumentException("range scope is not valid");
			throw e;
		}
		this.rangeStart = start;
		this.rangeEnd = end;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public URL getUrl() {
		return url;
	}

	/*
	 * if the key already exists, the value is replaced.
	 */
	public void addHeader(String key, String value) {
		if(requestHeaders==null) {
			requestHeaders = new HashMap<String, String>();
		}
		requestHeaders.put(key, value);
	}

	public void removeHeader(String key) {
		if(requestHeaders!=null) {
			requestHeaders.remove(key);
		}
	}

	public void setDefaultHeaders() {
		addHeader("User-Agent", "Red Bend Software OMA DM Protocol Engine(TM) RedBend-vdm-11.5.0.7_kyocera_hf1 android");
		addHeader("Accept-Charset", "utf-8");
		addHeader("Connection", "close");
		addHeader("Accept-Language", "en");
		addHeader("Cache-Control", "no-cache");
		addHeader("Accept", "*/*");
		addHeader("Host", url.getHost() + ":" + url.getPort());
		if(rangeStart!=0 || rangeEnd!=0) {
			addHeader("Range", "bytes=" + rangeStart + "-" + rangeEnd);
		}
	}

	public void httpGet() throws IOException  {
		HttpURLConnection conn = null;
		InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
		try {
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setConnectTimeout(timeoutConnect);
	        conn.setReadTimeout(timeoutRead);
	        conn.setRequestMethod("GET");
	        conn.setUseCaches(false);
	        conn.setDoInput(true);
	        conn.setDoOutput(false);
	        if(requestHeaders!=null) {
	        	for(String key : requestHeaders.keySet()) {
	        		conn.setRequestProperty(key, requestHeaders.get(key));
	        	}
	        }
	        conn.connect();
	        int responseCode = conn.getResponseCode();
	        if(responseCode==HttpURLConnection.HTTP_PARTIAL || responseCode==HttpURLConnection.HTTP_OK) {
	        	inputStream = conn.getInputStream();
	            byte[] buffer = new byte[BUFFER_SIZE];
	        	int len;
	        	fileOutputStream = new FileOutputStream(this.outputFileName);
	        	while ((len = inputStream.read(buffer)) != -1) 
	        	{
	        		fileOutputStream.write(buffer, 0, len);
	        	}
	        } else {
	        	throw new IOException("response code was : " + responseCode);
	        }
	    } catch (IOException e) {
            throw e;
        } finally {
        	if(inputStream!=null) {
            	inputStream.close();
        	}
        	if(fileOutputStream!=null) {
        		fileOutputStream.close();
        	}
        	if(conn!=null) {
        		conn.disconnect();
        	}
        }
    }
	
	public static void printStack(Exception e) {
	    StackTraceElement[] stackTrace = e.getStackTrace();
        System.out.println("Exception is catched:" + e);
	    for(StackTraceElement st : stackTrace)  
	    {  
	        System.out.println("\t" + st);  
	    }  
	}
	
	public static RangeRequest getWholeFile(String url) {
		RangeRequest rr;
		try {
			rr = new RangeRequest(url,
					0, 99,
					"c:\\temp\\range.txt");
			rr.httpGet();
		}catch(Exception e){
			printStack(e);
			return null;
		}
		return rr;
	}

	public static void main(String[] args) {
//		String urlString = "http://neptune.redbend.com:8080/Android/and_demo_dp_hiro";
//		String urlString = "https://rp-rdemo1.redbend.com:443/VrmDLServerWEB/servlet/RequestDPServlet/EXTERNAL/t.txt";
		String urlString = "https://xota.uat.asia.avnext2.xyz:8443/VrmDLServerWEB/servlet/RequestDPServlet/EXTERNAL/t.txt";
		System.out.println("url=" + urlString);
		RangeRequest rr = getWholeFile(urlString);
		if(rr!=null) {
			System.out.println("protocol:" + rr.getUrl().getProtocol());
			System.out.println("port:" + rr.getUrl().getPort());
			System.out.println(rr.getUrl().getHost() + ":" + rr.getUrl().getPort());
		}
    }
}

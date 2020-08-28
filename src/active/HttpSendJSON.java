package entry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class HttpSendJSON {

	public static class ResponceNot200Exception extends Exception{
		ResponceNot200Exception(String status){
			this.status = status;
		}
		public String status = "";
	}
	
    public String callPost(String strPostUrl, String JSON) throws Exception {
    	boolean isHttps = strPostUrl.contains("https");
        HttpURLConnection con = null;
        StringBuilder result = new StringBuilder();
        try {
        	
        	SSLSocketFactory factory = null;
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new NonAuthentication[] { new NonAuthentication() }, null);
			factory = ctx.getSocketFactory();

            URL url = new URL(strPostUrl);
            // ごり押し HTTP / HTTPS 対応
            if(isHttps) {
            	con = (HttpsURLConnection) url.openConnection();
    	        ((HttpsURLConnection) con).setSSLSocketFactory(factory);
            }else {
            	con = (HttpURLConnection) url.openConnection();
            }
            
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "jp");
            con.setRequestProperty("Content-Type", "application/JSON; charset=utf-8");
           	int len = JSON.getBytes(Charset.forName("utf-8")).length;
            con.setRequestProperty("Content-Length", String.valueOf(len));
            try(OutputStream out = con.getOutputStream();PrintStream ps = new PrintStream(out, true, "utf-8");){
	            ps.print(JSON);
            }
            con.connect();

            // HTTPレスポンスコード
            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                String encoding = con.getContentEncoding();
                if (null == encoding) {
                    encoding = "UTF-8";
                }
                try(InputStream in = con.getInputStream();
                	InputStreamReader inReader = new InputStreamReader(in, encoding);
                	BufferedReader bufReader = new BufferedReader(inReader);){
	                String line = null;
	                // 1行ずつテキストを読み込む
	                while ((line = bufReader.readLine()) != null) {
	                    result.append(line + "\r\n");
	                }
                }

            } else {
                throw new ResponceNot200Exception(String.valueOf(status));
            }

        } catch (NullPointerException e1) {
            e1.printStackTrace();
        } catch (ClassCastException e1) {
            e1.printStackTrace();
        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }
        return result.toString();
    }

	class NonAuthentication implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
}

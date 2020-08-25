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
    /**
     * JSON文字列の送信
     * @param strPostUrl 送信先URL
     * @param JSON 送信するJSON文字列
     * @return     
     * @throws Exception 
     */
    public String callPost(String strPostUrl, String JSON) throws Exception {
    	boolean isHttps = strPostUrl.contains("https");
        HttpURLConnection con = null;
        StringBuffer result = new StringBuffer();
        try {
        	
        	SSLSocketFactory factory = null;
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new NonAuthentication[] { new NonAuthentication() },
					null);
			factory = ctx.getSocketFactory();

            URL url = new URL(strPostUrl);
            // ごり押し HTTP / HTTPS 対応
            if(isHttps) {
            	con = (HttpsURLConnection) url.openConnection();
    	        ((HttpsURLConnection) con).setSSLSocketFactory(factory);
            }else {
            	con = (HttpURLConnection) url.openConnection();
            }
            
            // HTTPリクエストコード
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "jp");
            // データがJSONであること、エンコードを指定する
            con.setRequestProperty("Content-Type", "application/JSON; charset=utf-8");//utf-8
            // POSTデータの長さを設定
           	int len = JSON.getBytes(Charset.forName("utf-8")).length;
            con.setRequestProperty("Content-Length", String.valueOf(len)); //String.valueOf(JSON.length()));
            // リクエストのbodyにJSON文字列を書き込む
            OutputStream out = con.getOutputStream();
//            out.write(JSON);
//            out.flush();
            final PrintStream ps = new PrintStream(out, true, "utf-8");
            ps.print(JSON);
            ps.close();
            con.connect();

            // HTTPレスポンスコード
            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                final InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                if (null == encoding) {
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while ((line = bufReader.readLine()) != null) {
                    result.append(line + "\r\n");
                }
                bufReader.close();
                inReader.close();
                in.close();
            } else {
                // 通信が失敗した場合のレスポンスコードを表示
                System.out.println(status);
            }

        } catch (Exception e1) {
            e1.printStackTrace();
            throw e1;
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

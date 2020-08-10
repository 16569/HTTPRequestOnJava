package active;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        final String postURL = "http://127.0.0.1:9000/api/login.php";
        String json = "";
        try {
			List<String> lines = Files.readAllLines(Paths.get("./json.txt"));
			json = lines.stream().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
        final int threadNum = 3;

        ExecutorService es = null;
        CopyOnWriteArrayList<Response> resultList = new CopyOnWriteArrayList<Response>();
        
        es = Executors.newFixedThreadPool(threadNum);
        
        for(int i=0;i<threadNum;i++) {
        	es.execute(new RunTask(postURL, json, resultList));
        }
        
		es.shutdown();
        
		while(!es.isTerminated()) {
	        try {
				es.awaitTermination(50, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
        
    }
	
    static class Response{
    	Response(long time, String json, boolean isSucces){
    		this.time = time;
    		this.json = json;
    		this.isSucces = isSucces;
    	}
    	public long time = 0;
    	public String json = null;
    	public boolean isSucces = false;
    }
    
    /**
     * HTTPリクエストするタスク
     */
	static class RunTask implements Runnable{

		public RunTask(String postURL, String json, CopyOnWriteArrayList<Response> list) {
			this.postURL = postURL;
			this.json = json;
			this.refList = list;
		}
		
		private String postURL;
		private String json;
		private CopyOnWriteArrayList<Response> refList;
		@Override
		public void run() {
			long startTime = new Date().getTime();
	        String result;
			try {
		        HttpSendJSON httpSendJSON = new HttpSendJSON();
				result = httpSendJSON.callPost(postURL, json);
		        long endTime = new Date().getTime();
		        this.refList.add(new Response(endTime - startTime, result, true));
			} catch (Exception e) {
				e.printStackTrace();
				
		        long endTime = new Date().getTime();
		        this.refList.add(new Response(endTime - startTime, "error", false));
			}
		}

		
	}

}

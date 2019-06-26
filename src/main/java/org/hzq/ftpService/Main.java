package org.hzq.ftpService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hzq.security.BlastInspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Main {
	public static final Logger log=LoggerFactory.getLogger(Main.class);
	public static List<User> users = new ArrayList<User>();
	public static String rootDir;
	public static String logDir;
	public static MyLog myLog;
	public static boolean logPrint=true;
	public static String runTimePath=null;
	public static volatile FtpServer ftpServer=null;
	public static boolean restartFlag=false;
	public static BlastInspect blastInspect;
	public static void main(String[] args) {
		try {
			if(args.length>0){
				runTimePath=args[0];
			}else{
				File file=new File("");
				String absolutePath = file.getAbsolutePath();
				System.out.println(absolutePath);
				runTimePath=absolutePath;
			}
			Util.initDataFromXML("server.xml");
			myLog = new  MyLog();
			myLog.start();
			FtpServer.portPoll=PortPool.getPortPool();
			new CheckOut().start();
			blastInspect = new BlastInspect();
			ftpServer=new FtpServer();
		} catch (Exception e) {
			String stackTrace = Util.getStackTrace(e);
			log.error(stackTrace);
			e.printStackTrace();
		}
	}
	public static void initPASV(String ip) throws Exception{
		FtpServer.serverAddr=ip;
		String[] split = ip.split("\\.");
		StringBuilder sb=new StringBuilder();
		for (String string : split) {
			sb.append(string+(char)44);
		}
		FtpServer.loaclAddr=sb.toString();
	}
}

package org.hzq.ftpService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FtpServer {
	public static PortPool portPoll;
	public static String fielPermission = "-rwxrwxrwxr    1 0        0";
	public static String dirPermission = "drwxrwxrwxr    1 0        0";
	public static String dirPermission2 ="drwxrwxrwx   2 root     root           ";
	public static String fielPermission2 = "-rwxrwxrwx 1 root    root       ";
	public static String loaclAddr;
	public static String serverAddr;
	private ServerSocket s;
	public FtpServer() {
		new Thread(){

			@Override
			public void run() {
				try {
					// 监听21号端口,21口用于控制,20口用于传数据
					 s = new ServerSocket(21,100,InetAddress.getByName(serverAddr));
					 if (Main.logPrint) {
					 System.out.println("service starting!"+s.toString());
					 }
					for (;;) {
						// 接受客户端请求
						Socket incoming = s.accept();
						 if (Main.logPrint) {
							 System.out.println("接入："+incoming.toString()+"准备开始通信");
							 }
						PrintWriter out = new PrintWriter(incoming.getOutputStream(),
								true);// 文本文本输出流
						out.println("220 hello");// 命令正确的提示

						// 创建服务线程
						FtpHandler h = new FtpHandler(incoming);
						h.start();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}.start();

	}
	public  void close() throws IOException{
		s.close();
	}

}
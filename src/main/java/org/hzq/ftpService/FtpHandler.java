package org.hzq.ftpService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FtpHandler extends Thread {
	Socket ctrlSocket; // 用于控制的套接字
	private Socket dataSocket; // 用于传输的套接字
	private String cmd = ""; // 存放指令(空格前)
	private String param = ""; // 放当前指令之后的参数(空格后)
	private String remoteHost = " "; // 客户IP
	private int remotePort = 0; // 客户TCP 端口号
	private String dir = Main.rootDir;// 当前目录
	private int state = 0; // 用户状态标识符,在checkPASS中设置
	private String reply; // 返回报告
	private PrintWriter ctrlOutput;
	private int type = 0; // 文件类型(ascII 或 bin)
	private String requestfile = "";
	private boolean dataFalg = false;
	private ServerSocket pasvDataSocket;
	private List<String> portKey = new ArrayList<String>();
	private User suser = null;
	private String nowDir = "/";
	private String clentAddr;
	private String pasvMessage;

	// FtpHandler方法
	// 构造方法
	public FtpHandler(Socket s) {
		ctrlSocket = s;
	}

	/**
	 * 初始化被动模式，被动模式绑定线程，线程结束时释放端口关闭socket
	 */
	private void initPasv() {
		if (pasvDataSocket != null && !pasvDataSocket.isClosed()) {
			return;
		}
		String port = FtpServer.portPoll.getPort();
		portKey.add(port);
		String[] split = port.split(",");
		pasvMessage = "Entering Passive Mode (" + FtpServer.loaclAddr + split[1] + "," + split[2] + ").";
		InetAddress byName = null;
		try {
			byName = InetAddress.getByName(FtpServer.serverAddr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (byName == null) {
			try {
				pasvDataSocket = new ServerSocket(Integer.valueOf(split[0]), 20, byName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				pasvDataSocket = new ServerSocket(Integer.valueOf(split[0]));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (pasvDataSocket == null) {
			initPasv();
		}

	}

	public void run() {
		String str = "";
		int parseResult; // 与cmd 一一对应的号
		try {
			InetAddress inetAddress = ctrlSocket.getInetAddress();
			clentAddr = inetAddress.getHostAddress();
			if (Main.logPrint) {
				System.out.println("->对端ip:" + inetAddress.getHostAddress());
			}
			InputStream inputStream = ctrlSocket.getInputStream();
			ctrlOutput = new PrintWriter(new OutputStreamWriter(ctrlSocket.getOutputStream(), "GBK"), true);
			state = FtpState.FS_WAIT_LOGIN; // 0
			boolean finished = false;
			while (!finished) {
				if (Main.restartFlag) {
					break;
				}
				byte[] b = new byte[1024];
				int len = -1;
				try {
					len = inputStream.read(b);
				} catch (Exception e) {
					e.printStackTrace();
					finished = true;
					break;
				}
				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				if (len != -1) {
					bao.write(b, 0, len - 2);
					str = bao.toString("GBK");
				} else {
					finished = true; // 跳出while
					break;
				}
				String[] split = str.split("\r\n");
				for (String string : split) {
					parseResult = parseInput(string); // 指令转化为指令号
					if (Main.logPrint) {
						System.out.println("指令:" + cmd + " 参数:" + param);
						System.out.print("->");
					}
					switch (state) // 用户状态开关
					{
					case FtpState.FS_WAIT_LOGIN:
						finished = commandUSER();
						break;
					case FtpState.FS_WAIT_PASS:
						finished = commandPASS();
						break;
					case FtpState.FS_LOGIN: {
						switch (parseResult)// 指令号开关,决定程序是否继续运行的关键
						{
						case -1:
							errCMD(); // 语法错
							break;
						case 4:
							finished = commandCDUP(); // 到上一层目录
							break;
						case 6:
							finished = commandCWD(); // 到指定的目录
							break;
						case 7:
							finished = commandQUIT(); // 退出
							break;
						case 9:
							finished = commandPORT(); // 客户端IP:地址+TCP 端口号
							break;
						case 11:
							finished = commandTYPE(); // 文件类型设置(ascII 或 bin)
							break;
						case 14:
							finished = commandRETR(); // 从服务器中获得文件
							break;
						case 15:
							finished = commandSTOR(); // 向服务器中发送文件
							break;
						case 22:
							finished = commandABOR(); // 关闭传输用连接dataSocket
							break;
						case 23:
							finished = commandDELE(); // 删除服务器上的指定文件
							break;
						case 25:
							finished = commandMKD(); // 建立目录
							break;
						case 27:
							finished = commandLIST(); // 文件和目录的列表
							break;
						case 26:
						case 33:
							finished = commandPWD(); // "当前目录" 信息
							break;
						case 32:
							finished = commandNOOP();
							break;
						case 34:
							finished = commandCharSet(); // "opts" 信息
							break;
						case 35:
							finished = commandSYST();// 让客户端以gbk编码发送数据
							break;
						case 36:
							finished = commandSIZE(); // 文件or文件夹大小
							break;
						case 37:
							finished = commandPASV(); // 被动模式
							break;
						case 38:
							finished = commandRMD(); // 删除目录
							break;
						case 39:
							finished = commandRNFR(); // 要重命名的文件
							break;
						case 40:
							finished = commandRNTO(); // 新名称
							break;
						case 41:
							finished = commandREST(); // 断点续传标识位
							break;
						}
					}
						break;

					}
					if (dataFalg) {
						dataFalg = false;
					} else {
						ctrlOutput.println(reply);
						ctrlOutput.flush();
					}
				}

			}
			ctrlSocket.close();
		} catch (Exception e) {
			String stackTrace = Util.getStackTrace(e);
			Main.log.error(stackTrace);
			try {
				ctrlSocket.close();
			} catch (Exception e1) {
				try {
					ctrlSocket.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				e1.printStackTrace();
			}
		}
		release();
	}

	private void release() {
		// 关闭pasv模式的socket
		try {
			if (pasvDataSocket != null) {
				pasvDataSocket.close();
			}
			if (ctrlSocket != null && ctrlSocket.isClosed()) {
				ctrlSocket.close();
			}
		} catch (IOException e) {
			Main.log.error(Util.getStackTrace(e));
		}
		// 释放端口
		portKey.stream().forEach(p -> FtpServer.portPoll.release(p));
		if (Main.logPrint) {
			System.out.println("已经释放资源!!" + pasvMessage);
		}
	}

	// 设置偏移量，用于断点续传
	private long deviation = 0;

	private boolean commandREST() {
		deviation = Long.valueOf(param);
		reply = "350 Resuming at " + param;
		return false;
	}

	private boolean commandCharSet() {
		reply = "502 Command not implemented.";
		return false;
	}

	private boolean commandRNTO() {
		File file = new File(dir + "/" + oldFileName);
		String nowDate = Util.getNowDate();
		// 判断是否是主目录
		if (!nowDir.equals("/")) {
			// 如果不是主目录,检查是否是文件夹,是文件夹的话 允许修改
			if (file.exists() && file.isDirectory()) {
				if (file.renameTo(new File(dir + "/" + param))) {
					reply = "250 file name change";
					Main.myLog.addMessige(new Messige(suser.getUsername(), nowDate, "rename",
							"rename file or dir for " + file.toString(), "1", clentAddr));
				} else {
					reply = "553 change error";
				}
			} else {
				reply = "553 change error";
			}
			// 如果是主目录需要判断是否有delete权限才能修改
		} else if (suser.getPermission()[2].equals("d")) {
			if (file.exists()) {
				if (file.renameTo(new File(dir + "/" + param))) {
					reply = "250 file name change";
					Main.myLog.addMessige(new Messige(suser.getUsername(), nowDate, "rename",
							"rename file or dir for " + file.toString(), "1", clentAddr));
				} else {
					reply = "553 change error";
				}
			} else {
				reply = "501 change error";
			}
		} else {
			reply = "553 change error";
		}
		return false;
	}

	// 修改目录名称前,会先发送原名称,在这存储
	private String oldFileName;

	private boolean commandRNFR() {
		oldFileName = param;
		// 直接返回350
		reply = "350 ";
		return false;
	}

	private boolean commandRMD() {
		if (param.indexOf("/") == -1) {
			param = "/" + param;
		}
		File f = new File(dir + param);
		String[] permission = suser.getPermission();
		if (permission[2].equals("d")) {
			if (f.exists()) {
				boolean delete = f.delete();
				if (delete) {
					reply = "250 remove sussess!";
					String nowDate = Util.getNowDate();
					Main.myLog.addMessige(new Messige(suser.getUsername(), nowDate, "delete",
							"delete dir for " + f.toString(), "2", clentAddr));
				} else {
					reply = "502 remove sussess!";
				}
			} else {
				reply = "501 not find";
			}
		} else {
			reply = "501 not find";
		}
		return false;
	}

	private boolean commandPASV() throws Exception {
		initPasv();
		reply = "227 " + pasvMessage;
		if (Main.logPrint) {
			System.out.println("进入被动模式:" + pasvMessage);
		}
		ctrlOutput.println(reply);
		ctrlOutput.flush();
		dataSocket = pasvDataSocket.accept();
		dataFalg = true;
		return false;
	}

	private boolean commandSIZE() {
		File file = new File(param);
		if (!file.exists()) {
			file = new File(dir + param);
			if (!file.exists()) {
				reply = "550 file not find!";
				return false;
			}
		}
		reply = "215 " + file.length();
		return false;
	}

	// parseInput方法
	int parseInput(String str) {
		int p = 0;
		int i = -1;
		p = str.indexOf(" ");
		if (p == -1) // 如果是无参数命令(无空格)
			cmd = str;
		else
			cmd = str.substring(0, p); // 有参数命令,过滤参数

		if (p >= str.length() || p == -1)// 如果无空格,或空格在读入的s串最后或之外
			param = "";
		else
			param = str.substring(p + 1, str.length());
		cmd = cmd.toUpperCase(); // 转换该 String 为大写

		if (cmd.equals("CDUP"))
			i = 4;
		if (cmd.equals("CWD"))
			i = 6;
		if (cmd.equals("QUIT"))
			i = 7;
		if (cmd.equals("PORT"))
			i = 9;
		if (cmd.equals("TYPE"))
			i = 11;
		if (cmd.equals("RETR"))
			i = 14;
		if (cmd.equals("STOR"))
			i = 15;
		if (cmd.equals("ABOR"))
			i = 22;
		if (cmd.equals("DELE"))
			i = 23;
		if (cmd.equals("MKD"))
			i = 25;
		if (cmd.equals("PWD"))
			i = 26;
		if (cmd.equals("LIST"))
			i = 27;
		if (cmd.equals("NOOP"))
			i = 32;
		if (cmd.equals("XPWD"))
			i = 33;
		if (cmd.equals("OPTS"))
			i = 34;
		if (cmd.equals("SYST"))
			i = 35;
		if (cmd.equals("SIZE"))
			i = 36;
		if (cmd.equals("PASV"))
			i = 37;
		if (cmd.equals("RMD"))
			i = 38;
		if (cmd.equals("RNFR"))
			i = 39;
		if (cmd.equals("RNTO"))
			i = 40;
		if (cmd.equals("REST"))
			i = 41;
		return i;
	}

	// commandUSER方法
	// 用户名是否正确
	boolean commandUSER() {
		if (cmd.equals("USER")) {
			if (param.equals("anonymous")) {
				reply = "332 用户名正确,需要口令";
				return false;
			}
			suser = Util.findUser(param);
			if (suser != null) {
				reply = "331 用户名正确,需要口令";
				state = FtpState.FS_WAIT_PASS;
			} else {
				reply = "501 user is not find";
				return false;
			}
		}
		return false;
	}

	// commandPASS 方法
	// 密码是否正确
	boolean commandPASS() {
		if (cmd.equals("PASS")) {
			if (suser == null) {
				reply = "530 没有登录";
				return false;
			}
			String hostAddress = ctrlSocket.getInetAddress().getHostAddress();
			if (Main.blastInspect.isBlackList(hostAddress)) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				reply = "501 检测到暴破行为！你的ip地址已被锁定和记录。";
				return true;
			}
			if (param.equals(suser.getPassword())) {
				reply = "230 用户登录了";
				state = FtpState.FS_LOGIN;
				if (Main.logPrint) {
					System.out.println("新消息: 用户: " + suser.getUsername() + " 来自于: " + remoteHost + "登录了");
					System.out.print("->");
				}
				return false;
			} else {
				Main.blastInspect.addBlackUsers(hostAddress);
				System.out.println(suser.getUsername() + "密码输入错误---" + Util.getNowDate() + "--------" + hostAddress);
				reply = "501 参数语法错误,密码不匹配";
				return true;
			}
		}
		return false;
	}

	void errCMD() {
		reply = "500 语法错误";
	}

	boolean commandCDUP()// 到上一层目录
	{
		/*
		 * dir = FtpServer.initDir; File f = new File(dir); if (f.getParent() !=
		 * null && (!dir.equals(rootdir)))// 有父路径 && 不是根路径 { dir =
		 * f.getParent(); reply = "200 命令正确"; } else { reply = "550 当前目录无父路径"; }
		 */
		return false;
	}// commandCDUP() end

	boolean commandCWD()// CWD (CHANGE WORKING DIRECTORY)
	{
		if (param.equals(nowDir)) {
			reply = "250 请求的文件处理结束, 当前目录变为: " + dir;
			return false;
		}
		if (param.equals("/")) {
			dir = Main.rootDir;
			nowDir = param;
			reply = "250 请求的文件处理结束, 当前目录变为: " + dir;
		} else if (param.indexOf("/") == -1) {
			dir = Main.rootDir + nowDir + param;
			nowDir = nowDir + param + "/";
			reply = "250 请求的文件处理结束, 当前目录变为: " + dir;
		} else {
			int flag = 0;
			String[] split = param.split("/");
			for (String str : suser.getWorkDir()) {
				if (str.equals(split[1])) {
					flag = 1;
					break;
				}
			}
			// && !suser.getPermission()[2].equals("d") 管理员可以看没有配置的工作目录文件
			if (flag == 0) {
				reply = "550 此路径不存在";
			} else {
				nowDir = param;
				dir = Main.rootDir + param;
				reply = "250 请求的文件处理结束, 当前目录变为: " + dir;
			}
		}
		return false;
	}

	boolean commandQUIT() {
		reply = "221 服务关闭连接";
		return true;
	}// commandQuit() end

	/*
	 * 使用该命令时，客户端必须发送客户端用于接收数据的32位IP 地址和16位 的TCP 端口号。
	 * 这些信息以8位为一组，使用十进制传输，中间用逗号隔开。
	 */
	boolean commandPORT() {
		try {
			int p1 = 0;
			int p2 = 0;
			int[] a = new int[6];// 存放ip+tcp
			int i = 0;
			while ((p2 = param.indexOf(",", p1)) != -1)// 前5位
			{
				a[i] = Integer.parseInt(param.substring(p1, p2));
				p2 = p2 + 1;
				p1 = p2;
				i++;
			}
			a[i] = Integer.parseInt(param.substring(p1, param.length()));// 最后一位
			remoteHost = a[0] + "." + a[1] + "." + a[2] + "." + a[3];
			remotePort = a[4] * 256 + a[5];
			int num = 0;
			while (num < 10) {
				num++;
				try {
					dataSocket = new Socket(remoteHost, remotePort, InetAddress.getByName("myServer"),
							PortPool.now_port);
					if (PortPool.now_port <= PortPool.max_port) {
						synchronized (Main.class) {
							PortPool.now_port += 1;
						}
					} else {
						synchronized (Main.class) {
							PortPool.now_port = PortPool.min_port;
						}
					}
					reply = "200 命令正确";
					break;
				} catch (Exception e) {
					if (num == 10) {
						reply = "502 cant not create conntion!";
					} else {
						if (PortPool.now_port <= PortPool.max_port) {
							synchronized (Main.class) {
								PortPool.now_port += 1;
							}
						} else {
							synchronized (Main.class) {
								PortPool.now_port = PortPool.min_port;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			reply = "501 PORT error";
			return false;
		}

		return false;
	}// commandPort() end

	private void closeSocket() throws Exception {
		dataSocket.close();
	}

	boolean commandLIST()// 文件和目录的列表
	{
		try {
			if (!suser.getPermission()[0].equals("r")) {
				reply = "451 you not read permission!";
				ctrlOutput.println(reply);
				return true;
			}
			File f = new File(dir);
			if (f.exists()) {
				reply = "150 文件状态正常,ls以 ASCII 方式操作";
				ctrlOutput.println(reply);
				ctrlOutput.flush();
				if (Main.logPrint) {
					System.out.println("已经开始传输list数据");
				}
				File[] fileList = f.listFiles();
				StringBuilder sb = new StringBuilder();
				List<String> workDir = suser.getWorkDir();
				/*
				 * boolean nowFlag = false; if (nowDir.equals("/")) { nowFlag =
				 * true; } if (nowFlag && !suser.getPermission()[2].equals("d"))
				 * { for (String work : workDir) { for (File file : fileList) {
				 * if (work.equals(file.getName())) { strAppend(file, sb);
				 * break; } } } } else { for (File file : fileList) {
				 * strAppend(file, sb); } }
				 */
				if (nowDir.equals("/")) {
					for (String work : workDir) {
						for (File file : fileList) {
							if (work.equals(file.getName())) {
								strAppend(file, sb);
								break;
							}
						}
					}
				} else {
					for (File file : fileList) {
						strAppend(file, sb);
					}
				}
				OutputStream out = dataSocket.getOutputStream();
				if (sb.length() > 1) {
					PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, "GBK"), true);
					printWriter.println(sb.toString());
					printWriter.flush();
					printWriter.close();
				}
				ctrlOutput.println("226 ok");
				ctrlOutput.flush();
				out.close();
				closeSocket();
				dataFalg = true;
			} else {
				reply = "500 network error";
			}
		} catch (Exception e) {
			try {
				closeSocket();
			} catch (Exception e2) {
				String stackTrace = Util.getStackTrace(e);
				Main.log.error(stackTrace);
			}
			String stackTrace = Util.getStackTrace(e);
			Main.log.error(stackTrace);
			reply = "451 Requested action aborted: local error in processing";
			e.printStackTrace();
			return false;
		}

		return false;
	}// commandLIST() end

	private void strAppend(File file, StringBuilder sb) throws Exception {
		if (file.isDirectory()) {
			sb.append(FtpServer.dirPermission2);
		} else {
			sb.append(FtpServer.fielPermission2);
		}
		Path testPath = Paths.get(file.toString());
		BasicFileAttributeView basicView = Files.getFileAttributeView(testPath, BasicFileAttributeView.class);
		BasicFileAttributes basicFileAttributes = basicView.readAttributes();
		long length = file.length();
		sb.append(length + " ");
		String[] split = new Date(basicFileAttributes.creationTime().toMillis()).toString().split("\\ ");
		sb.append(split[1] + " " + split[2] + " " + split[3].substring(0, split[3].lastIndexOf(":")) + " ");
		sb.append(file.getName() + "\r\n");
	}

	boolean commandTYPE() // TYPE 命令用来完成类型设置
	{
		if (param.equals("A")) {
			type = FtpState.FTYPE_ASCII;// 0
			reply = "200 命令正确 ,转 ASCII 模式";
		} else if (param.equals("I")) {
			type = FtpState.FTYPE_IMAGE;// 1
			reply = "200 命令正确 转 BINARY 模式";
		} else
			reply = "504 命令不能执行这种参数";

		return false;
	}

	// connamdRETR 方法
	// 从服务器中获得文件
	boolean commandRETR() {
		try {
			if (!suser.getPermission()[0].equals("r")) {
				reply = "550 文件不存在";
				closeSocket();
				return false;
			}
			requestfile = param;
			File f = new File(requestfile);
			if (!f.exists()) {
				f = new File(addTail(dir) + param);
				if (!f.exists()) {
					reply = "550 文件不存在";
					closeSocket();
					return false;
				}
				requestfile = addTail(dir) + param;
			}
			/*
			 * if (type == FtpState.FTYPE_IMAGE) // bin {
			 */
			OutputStream out = dataSocket.getOutputStream();
			ctrlOutput.println("150 文件状态正常,以二进治方式打开文件:  " + requestfile);
			BufferedInputStream fin = new BufferedInputStream(new FileInputStream(requestfile));
			if (deviation != 0) {
				fin.skip(deviation);
			}
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = fin.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			fin.close();
			out.flush();
			out.close();
			ctrlOutput.println("226 ok");
			ctrlOutput.flush();
			closeSocket();
			dataFalg = true;
			String nowDate = Util.getNowDate();
			Main.myLog.addMessige(new Messige(suser.getUsername(), nowDate, "download",
					"download file for " + requestfile.toString(), "2", clentAddr));

			// }
		} catch (Exception e) {
			try {
				closeSocket();
			} catch (Exception e2) {
				String stackTrace = Util.getStackTrace(e);
				Main.log.error(stackTrace);
			}
			String stackTrace = Util.getStackTrace(e);
			Main.log.error(stackTrace);
			reply = "451 file error";
			return false;
		}
		return false;

	}

	// commandSTOR 方法
	// 向服务器中发送文件STOR
	boolean commandSTOR() {
		try {
			if (param.equals("")) {
				reply = "501 参数语法错误";
				closeSocket();
				return false;
			}
			requestfile = addTail(dir) + param;
			boolean b = new File(requestfile).exists();
			boolean b2 = "/".equals(nowDir);
			if (b || b2) {
				b = !suser.getPermission()[2].equals("d");
			}
			if (!suser.getPermission()[1].equals("w") || b) {
				reply = "451 not permission";
				closeSocket();
				return false;
			}

			try {
				ctrlOutput.println("150 Opening Binary mode data connection for " + requestfile);
				BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(requestfile));
				BufferedInputStream dataInput = new BufferedInputStream(dataSocket.getInputStream());
				byte[] buf = new byte[1024];
				int l = 0;
				while ((l = dataInput.read(buf)) != -1) {
					fout.write(buf, 0, l);
				}
				dataInput.close();
				fout.close();
				ctrlOutput.println("226 ok");
				ctrlOutput.flush();
				closeSocket();
				dataFalg = true;
				String nowDate = Util.getNowDate();
				Main.myLog.addMessige(new Messige(suser.getUsername(), nowDate, "upload",
						"upload file for " + requestfile.toString(), "2", clentAddr));
			} catch (Exception e) {
				try {
					closeSocket();
				} catch (Exception e2) {
					String stackTrace = Util.getStackTrace(e);
					Main.log.error(stackTrace);
				}
				reply = "451 请求失败: 传输出故障";
				return false;
			}

		} catch (Exception e) {
			try {
				closeSocket();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return false;
	}

	boolean commandPWD() {
		reply = "257 " + "\"" + nowDir + "\"";
		return false;
	}

	boolean commandSYST() {

		reply = "215 " + "UNIX Type: L8";
		return false;
	}

	boolean commandNOOP() {
		reply = "200 命令正确.";
		return false;
	}

	// 强关dataSocket 流
	boolean commandABOR() {
		try {
			dataSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
			reply = "451 请求失败: 传输出故障";
			return false;
		}
		reply = "421 服务不可用, 关闭数据传送连接";
		return false;
	}

	// 删除服务器上的指定文件
	boolean commandDELE() {
		File file = new File(dir + "/" + param);
		String[] permission = suser.getPermission();
		if (permission[2].equals("d")) {
			if (file.exists()) {
				if (file.delete()) {
					reply = "250 请求的文件处理结束,成功删除服务器上文件";
					Main.myLog.addMessige(new Messige(suser.getUsername(), Util.getNowDate(), "delete",
							"delete file  for " + file.toString(), "2", clentAddr));
				} else {
					reply = "450 删除失败";
				}
			} else {
				reply = "450 删除失败";
			}
		} else {
			reply = "450 删除失败";
		}
		return false;

	}

	// 建立目录,要绝对路径
	boolean commandMKD() {
		File file = new File(dir + "/" + param);
		if (file.exists()) {
			reply = "550 dir exists";
		} else if (!suser.getPermission()[2].equals("d") && nowDir.equals("/")) {
			reply = "550 您没有权限在根目录创建文件夹,您可以在本市州目录下创建目录!";
		} else {
			file.mkdirs();
			reply = "250 success";
		}
		return false;
	}

	String addTail(String s) {
		if (!s.endsWith("/"))
			s = s + "/";
		return s;
	}

}

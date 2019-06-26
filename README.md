如何使用:
项目的运行目录结构因该是这样的：
ftpserver文件夹下面有一个ftpServer.jar文件以及一个bin文件夹。bin文件夹中存放server.xml以及checkOut.conf
在简单配置完成server.xml文件后（该文件有详细的注释引导您配置），在控制台输入java -jar ./ftpServer.jar即可运行服务端
我也做了一个demo大家可以直接拿来用
该项目拥有的功能：
1.权限控制，可指定用户对文件夹的可见，以及读、写、删除
2.日志系统，可通过修改server.xml来实时输入详细的运行日志，默认为关闭。用户的敏感操作会自动记录到日志文件，存放日志文件的目录由server.xml配置
您也可以在程序运行时修改bin目录下的checkOut.conf文件中的logcat属性来实时开启详细日志，该属性的取值为true时显示运行日志,为false不显示
3.防止暴力破解密码，短时间内密码输入错误多次会锁定目标ip，禁止登陆

项目基础jdk1.8开发，使用utf-8字符集。我个人开发，已经稳定运行1年多，支持多种客户端访问，对windows客户端支持尤为良好。代码中可能会有一些奇怪的逻辑，那是因为为了适配windows客户端
，由于windows的ftp客户端没有完全遵循ftp协议卷，所以我花了很大的力气适配他。项目可能还有缺点，欢迎大家指出！

How to use:
The project's running directory structure should look like this:
There is an ftpserver.jar file and a bin folder below the ftpserver folder.The bin folder holds server.xml and checkout.conf
After a simple configuration of the server.xml file (which has detailed annotations to guide your configuration), type java-jar. / ftpserver.jar into the console to run the server side
I also did a demo that you can use directly
The project has the following functions:
1. Permission control, which can specify users' visibility to folders, as well as read, write and delete
2. Log system. You can input detailed running log in real time by modifying server.xml, which is turned off by default.Sensitive actions by the user are automatically logged to a log file in a directory configured by server.xml
You can also start verbose logging in real time by modifying the logcat property in checkout.conf file in the bin directory when the program is running, which displays the running log when it is true and not when it is false
3. Prevent violent password cracking. If the password is entered incorrectly for several times within a short period of time, the target IP will be locked and the login will be prohibited
The foundation of the project was developed in jdk1.8, using the utf-8 character set.My personal development, has been stable operation for more than a year, support a variety of client access, especially good for Windows client support.There may be some strange logic in the code, and that's because it ADAPTS to Windows clients
Since the FTP client of Windows did not fully follow the FTP protocol volume, I made great efforts to adapt it.The project may have shortcomings, welcome to point out!
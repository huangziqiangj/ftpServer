���ʹ��:
��Ŀ������Ŀ¼�ṹ����������ģ�
ftpserver�ļ���������һ��ftpServer.jar�ļ��Լ�һ��bin�ļ��С�bin�ļ����д��server.xml�Լ�checkOut.conf
�ڼ��������server.xml�ļ��󣨸��ļ�����ϸ��ע�����������ã����ڿ���̨����java -jar ./ftpServer.jar�������з����
��Ҳ����һ��demo��ҿ���ֱ��������
����Ŀӵ�еĹ��ܣ�
1.Ȩ�޿��ƣ���ָ���û����ļ��еĿɼ����Լ�����д��ɾ��
2.��־ϵͳ����ͨ���޸�server.xml��ʵʱ������ϸ��������־��Ĭ��Ϊ�رա��û������в������Զ���¼����־�ļ��������־�ļ���Ŀ¼��server.xml����
��Ҳ�����ڳ�������ʱ�޸�binĿ¼�µ�checkOut.conf�ļ��е�logcat������ʵʱ������ϸ��־�������Ե�ȡֵΪtrueʱ��ʾ������־,Ϊfalse����ʾ
3.��ֹ�����ƽ����룬��ʱ����������������λ�����Ŀ��ip����ֹ��½

��Ŀ����jdk1.8������ʹ��utf-8�ַ������Ҹ��˿������Ѿ��ȶ�����1��֧࣬�ֶ��ֿͻ��˷��ʣ���windows�ͻ���֧����Ϊ���á������п��ܻ���һЩ��ֵ��߼���������ΪΪ������windows�ͻ���
������windows��ftp�ͻ���û����ȫ��ѭftpЭ��������һ��˺ܴ����������������Ŀ���ܻ���ȱ�㣬��ӭ���ָ����

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
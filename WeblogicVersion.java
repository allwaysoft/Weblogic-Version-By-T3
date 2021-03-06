import cn.hutool.core.util.ReUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: WeblogicVersion
 * @Description: TODO
 * @Author: Summer
 * @Date: 2020/11/24 14:06
 * @Version: v1.0.0
 * @Description:
 **/
public class WeblogicVersion {

	String IP = "";
	int PORT = 7001;

	/**
	 * <p>
	 * get weblogic version First get reponse body Second get version element by
	 * Jsoup End get version by regex if get version return version data
	 * </p>
	 * // * @param url weblogic url
	 * 
	 * @return weblogic version
	 */
	public static String getVersion(String ip, Integer port) {
		String webLogicUrl = "http://" + ip + ":" + port;
		String version = getVersionByHttp(webLogicUrl);
		if ("".equals(version)) {
			// http获取weblogic版本失败，改用T3协议
			version = getVersionByT3(ip, port);
		}
		return version;
	}

	/**
	 * 通过 HTTP 获取 weblogic 版本
	 * 
	 * @param url url
	 * @return 版本号
	 */
	public static String getVersionByHttp(String url) {
		String version = "";
		url += "/console/login/LoginForm.jsp";
		try {
			Document doc = Jsoup.connect(url).get();
			String versionTmpStr = doc.getElementById("footerVersion").text();
			version = getVersion(versionTmpStr);
		} catch (Exception e) {
			version = "";
		}
		return version;
	}

	/**
	 * 通过 T3 获取 weblogic 版本
	 * 
	 * @param ip   ip
	 * @param port 端口
	 * @return 版本号
	 */
	public static String getVersionByT3(String ip, Integer port) {
		String getVersionMsg = "74332031322e322e310a41533a3235350a484c3a31390a4d533a31303030303030300a50553a74333a2f2f75732d6c2d627265656e733a373030310a0a";
		String version = "";
		try {
			Socket socket = new Socket(ip, port);
			byte[] rspByte = sendSocket(getVersionMsg, socket);
			socket.close();
			version = getVersion(new String(rspByte));
		} catch (Exception e) {
			version = "";
		}
		return version;
	}

	/**
	 * @Description: 获取 weblogic 版本
	 * @param content
	 * @return:
	 */
	public static String getVersion(String content) {
		content = content.replace("HELO:", "").replace(".false", "").replace(".true", "");
		String getVersionRegex = "[\\d\\.]+";
		List<String> result = ReUtil.findAll(getVersionRegex, content, 0, new ArrayList<String>());
		return result != null && result.size() > 0 ? result.get(0) : "";
	}

	/**
	 * @Description:
	 * @param sendMessage 发送信息hexString字符串
	 * @param socket      socket 连接
	 * @return: 返回读取到的内容
	 */
	public static byte[] sendSocket(String sendMessage, Socket socket) throws Exception {
		OutputStream out = socket.getOutputStream();
		InputStream is = socket.getInputStream();
		out.write(hexStrToBinaryStr(sendMessage));
		out.flush();
		byte[] bytes = new byte[4096];
		int length = is.read(bytes);
		return Arrays.copyOfRange(bytes, 0, length);
	}

	public static byte[] hexStrToBinaryStr(String hexString) {
		hexString = hexString.replaceAll(" ", "");
		int len = hexString.length();
		int index = 0;
		byte[] bytes = new byte[len / 2];
		while (index < len) {
			String sub = hexString.substring(index, index + 2);
			bytes[index / 2] = (byte) Integer.parseInt(sub, 16);
			index += 2;
		}
		return bytes;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(getVersionByT3("127.0.0.1", 7001));
	}
}

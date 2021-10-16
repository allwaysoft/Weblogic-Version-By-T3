import cn.hutool.core.util.ReUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class WeblogicVersionByT3 {

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

	public static String getVersion(String content) {
		content = content.replace("HELO:", "").replace(".false", "").replace(".true", "");
		String getVersionRegex = "[\\d\\.]+";
		List<String> result = ReUtil.findAll(getVersionRegex, content, 0, new ArrayList<String>());
		return result != null && result.size() > 0 ? result.get(0) : "";
	}

	public static String byteToHex(byte num) {
		char[] hexDigits = new char[2];
		hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
		hexDigits[1] = Character.forDigit((num & 0xF), 16);
		return new String(hexDigits);
	}

	public static String encodeHexString(byte[] byteArray) {
		StringBuffer hexStringBuffer = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			hexStringBuffer.append(byteToHex(byteArray[i]));
		}
		return hexStringBuffer.toString();
	}

	private static String asciiToHex(String asciiValue) {
		byte[] bytes = asciiValue.getBytes();
		// return Hex.encodeHexString(bytes);
		return encodeHexString(bytes);
	}

	public static byte hexToByte(String hexString) {
		int firstDigit = toDigit(hexString.charAt(0));
		int secondDigit = toDigit(hexString.charAt(1));
		return (byte) ((firstDigit << 4) + secondDigit);
	}

	private static int toDigit(char hexChar) {
		int digit = Character.digit(hexChar, 16);
		if (digit == -1) {
			throw new IllegalArgumentException("Invalid Hexadecimal Character: " + hexChar);
		}
		return digit;
	}

	/**
	 * @param hexString
	 * @return
	 */
	public static byte[] decodeHexString(String hexString) {
		if (hexString.length() % 2 == 1) {
			throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
		}

		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < hexString.length(); i += 2) {
			bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
		}
		return bytes;
	}

	private static String hexToAscii(String hexString) {
		byte[] bytes = null;
		// bytes = Hex.decodeHex(hexString);
		bytes = decodeHexString(hexString);
		String s = new String(bytes);
		return s;
	}

	public static void main(String[] args) throws Exception {
		String msg = "t3 12.2.1\n" + "AS:255\n" + "HL:19\n" + "MS:10000000\n" + "PU:t3://us-l-breens:7001\n\n";
		System.out.println(
				"74332031322e322e310a41533a3235350a484c3a31390a4d533a31303030303030300a50553a74333a2f2f75732d6c2d627265656e733a373030310a0a");
		System.out.println(asciiToHex(msg));
		System.out.println(hexToAscii(asciiToHex(msg)));
		String version = "";
		try {
			Socket socket = new Socket("127.0.0.1", 7001);
			OutputStream out = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			out.write(hexStrToBinaryStr(asciiToHex(msg)));

			out.flush();
			Thread.sleep(1);
			byte[] bytes = new byte[4096];
			int length = is.read(bytes);
			byte[] rspByte = Arrays.copyOfRange(bytes, 0, length);
			socket.close();
			System.out.println(new String(rspByte));
			version = getVersion(new String(rspByte));
		} catch (Exception e) {
			version = "";
		}
		System.out.println(version);
	}
}

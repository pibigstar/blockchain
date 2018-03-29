package com.pibigstar.blockchain;
import java.security.MessageDigest;

import com.google.gson.GsonBuilder;
/**
 * 工具类
 * 创建数字签名、返回JSON格式数据、返回难度字符串目标
 * @author pibigstar
 *
 */
public class StringUtil {
	
	//将Sha256应用到一个字符串并返回结果 
	public static String applySha256(String input){
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] hash = digest.digest(input.getBytes("UTF-8"));
	        
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//返回JSON格式数据
	public static String getJson(Object o) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(o);
	}
	
	//返回难度字符串目标，与散列比较。难度5将返回“00000”  
	public static String getDificultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}
	
	
}

/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.concurrent.ExecutorService;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;


/**
 * Description: it's our hash factory
 * 
 * 
 * @author Sabina Jeger
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class Encoder {
	
	private static final Logger log = Tracing.createLoggerFor(Encoder.class);
	
	
	public enum Algorithm {
		/**
		 * md5 with one iteration and salted (conversion string to bytes made with UTF-8)
		 */
		md5("MD5", 1, true, null),
		/**
		 * md5 with one iteration without any salt (conversion string to bytes made with UTF-8)
		 */
		md5_noSalt("MD5", 1, false, null),
		/**
		 * md5 with one iteration and salted (conversion string to bytes made with ISO-8859-1)
		 */
		md5_iso_8859_1("MD5", 1, false, "ISO-8859-1"),
		/**
		 * md5 with one iteration and salted (conversion string to bytes made with UTF-8)
		 */
		md5_utf_8("MD5", 1, false, "UTF-8"),
		
		/**
		 * SHA-1 with 100 iterations and salted
		 */
		sha1("SHA-1", 100, true, null),
		/**
		 * SHA-256 with 100 iterations and salted
		 */
		sha256("SHA-256", 100, true, null),
		/**
		 * SHA-512 with 100 iterations and salted
		 */
		sha512("SHA-512", 100, true, null),
		/**
		 * PBKDF2 with 20'000 iterations and salted, made to be slow to prevent brute force attack
		 */
		pbkdf2("PBKDF2WithHmacSHA1", 20000, true, null),
		/**
		 * Argon 2 with 3 iterations, 19MB memory
		 */
		argon2id("Argon2", 3, true, null),
		/**
		 * Argon 2 with 3 iterations, 12MB memory, minimal recommendation OWASP:
		 * https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
		 */
		argon2id_owasp("Argon2", 3, true, null),
		/**
		 * SHA-256 with one iteration no salted
		 */
		sha256Exam("SHA-256", 1, false, null),
		/**
		 * AES
		 */
		aes("AES", 2000, true, null);
		

		private final boolean salted;
		private final int iterations;
		private final String algorithm;
		private final Charset charset;
		
		private Algorithm(String algorithm, int iterations, boolean salted, String charsetName) {
			this.algorithm = algorithm;
			this.iterations = iterations;
			this.salted = salted;
			charset = charsetName == null ? null : Charset.forName(charsetName);
		}
		
		public boolean isSalted() {
			return salted;
		}
		
		public String getAlgorithm() {
			return algorithm;
		}
		
		public int getIterations() {
			return iterations;
		}
		
		public Charset getCharset() {
			return charset;
		}
		
		public static final Algorithm find(String str) {
			if(StringHelper.containsNonWhitespace(str)) {
				for(Algorithm value:values()) {
					if(value.name().equals(str)) {
						return value;
					}
				}
			}
			return md5;
		}
		
		public static final Algorithm secureValueOf(String val, Algorithm defaultAlgorithm) {
			if(StringHelper.containsNonWhitespace(val)) {
				for(Algorithm value:values()) {
					if(value.name().equals(val)) {
						return value;
					}
				}
			}
			return defaultAlgorithm;
		}
	}

	/**
	 * The MD5 helper object for this class.
	 */
	public static final MD5Encoder md5Encoder = new MD5Encoder();

	/**
	 * encrypt the supplied argument with md5.
	 * 
	 * @param s
	 * @return MD5 encrypted string
	 */
	public static String md5hash(String s) {
		return md5(s, null, null);
	}
	
	public static String sha256Exam(String s) {
		try {
			String HEXES = "0123456789abcdef";
			
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] array = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(HEXES.charAt((array[i] & 0xF0) >> 4)).append(HEXES.charAt((array[i]  & 0x0F)));
            }
            return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			log.error("", e);
			return null;
    	}
	}
	
	public static String encrypt(String s, String salt, Algorithm algorithm) {
		switch(algorithm) {
			case md5:
				return md5(s, salt, algorithm.getCharset());
			case md5_noSalt:
				return md5(s, null, algorithm.getCharset());
			case md5_iso_8859_1:
				return md5(s, salt, algorithm.getCharset());
			case sha1, sha256, sha512:
				return digest(s, salt, algorithm);
			case pbkdf2:
				return secretKey(s, salt, algorithm);
			case argon2id:
				return jailedArgon2id(s, salt, algorithm);
			case argon2id_owasp:
				return jailedArgon2id_owasp(s, salt, algorithm);
			case aes:
				return encodeAes(s, "rk6R9pQy7dg3usJk", salt, algorithm.getIterations());
			default: return md5(s, salt, algorithm.getCharset());
		}
	}
	
	public static String decrypt(String s, String salt, Algorithm algorithm) {
		switch(algorithm) {
			case aes: return decodeAes(s, "rk6R9pQy7dg3usJk", salt, algorithm.getIterations());
			default: return null;
		}
	}

	protected static String md5(String s, String salt, Charset charset) {
		try {
			byte[] inbytes = charset == null ? s.getBytes() : s.getBytes(charset);
			MessageDigest digest = MessageDigest.getInstance(Algorithm.md5.algorithm);
			digest.reset();
			if(salt != null) {
				byte[] saltbytes = charset == null ? salt.getBytes() : salt.getBytes(charset);
				digest.update(saltbytes);
			}
			byte[] outbytes = digest.digest(inbytes);
			return md5Encoder.encode(outbytes);
		} catch (NoSuchAlgorithmException e) {
			log.error("", e);
			return null;
		}
	}
	
	protected static String digest(String password, String salt, Algorithm algorithm) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm.getAlgorithm());
			digest.reset();
			if(salt != null) {
				digest.update(salt.getBytes());
			}
			byte[] input = password.getBytes(StandardCharsets.UTF_8);
			for(int i=algorithm.getIterations(); i-->0; ) {
				input = digest.digest(input);
			}
			return byteToBase64(input);
		} catch (NoSuchAlgorithmException e) {
			log.error("", e);
			return null;
		}
	}
	
	protected static String secretKey(String password, String salt, Algorithm algorithm) {
		try {
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), algorithm.getIterations(), 160);
			SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm.getAlgorithm());
			return byteToBase64(f.generateSecret(spec).getEncoded());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			log.error("", e);
			return null;
		}
	}
	
	public static String jailedArgon2id(final String password, final String salt, final Algorithm algorithm) {
		return jailedArgon2id(password, algorithm.getIterations(), 19456, salt);
	}
	
	public static String jailedArgon2id_owasp(final String password, final String salt, final Algorithm algorithm) {
		return jailedArgon2id(password, algorithm.getIterations(), 12288, salt);
	}
	
	public static String jailedArgon2id(final String password, final int iterations, final int memLimit, final String salt) {
		try {
			ExecutorService pool = CoreSpringFactory.getBean("encoderExecutorService", ExecutorService.class);
			return pool
					.submit(() -> argon2id(password, iterations, memLimit, salt))
					.get();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private static String argon2id(String password, int iterations, int memLimit, String salt) {
		try {
			int hashLength = 128;
			int parallelism = 1;
		        
			Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
				.withVersion(Argon2Parameters.ARGON2_VERSION_13)
				.withIterations(iterations)
				.withMemoryAsKB(memLimit)
				.withParallelism(parallelism)
				.withSalt(salt.getBytes());
		    
			Argon2BytesGenerator generate = new Argon2BytesGenerator();
			generate.init(builder.build());
			byte[] result = new byte[hashLength];
			generate.generateBytes(password.getBytes(StandardCharsets.UTF_8), result, 0, result.length);
			return byteToBase64(result);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public static String getSalt() {
	    try {
			//Always use a SecureRandom generator
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			//Create array for salt
			byte[] salt = new byte[16];
			//Get a random salt
			sr.nextBytes(salt);
			//return salt
			return byteToBase64(salt);
		} catch (NoSuchAlgorithmException e) {
			log.error("", e);
			return null;
		}
	}

	public static String byteToBase64(byte[] data){
		return StringHelper.encodeBase64(data);
	}
	
	public static String encodeAes(String password, String secretKey, String salt, int iteration) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CTR/NOPADDING");
			cipher.init(Cipher.ENCRYPT_MODE, generateKey(secretKey, salt, iteration));
			byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
			return asHexString(encrypted);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public static String decodeAes(String password, String secretKey, String salt, int iteration) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CTR/NOPADDING");
			cipher.init(Cipher.DECRYPT_MODE, generateKey(secretKey, salt, iteration));
			byte[] encrypted = cipher.doFinal(toByteArray(password));
			return new String(encrypted);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private static SecretKey generateKey(String passphrase, String salt, int iteration) throws Exception {
		PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt.getBytes(), iteration, 128);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWITHSHA256AND128BITAES-CBC-BC");
		return keyFactory.generateSecret(keySpec);
	}
	
    private static final String asHexString(byte[] buf) {
        StringBuilder strbuf = new StringBuilder(buf.length * 2);
        int i;
        for (i = 0; i < buf.length; i++) {
            if (((int) buf[i] & 0xff) < 0x10) {
                strbuf.append("0");
            }
            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
        }
        return strbuf.toString();
    }
    
    private static final byte[] toByteArray(String hexString) {
        int arrLength = hexString.length() >> 1;
        byte[] buf = new byte[arrLength];
        for (int ii = 0; ii < arrLength; ii++) {
            int index = ii << 1;
            String ldigit = hexString.substring(index, index + 2);
            buf[ii] = (byte) Integer.parseInt(ldigit, 16);
        }
        return buf;
    }
}
/**
 * BASE64Encoder.java
 *
 * (C) Copyright IBM Corp. 2005, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author: Chung-hao Tan, IBM ,chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 *   17970    2005-08-11  pineiro5     Logon from z/OS not possible
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

/*
 * Created on Oct 17, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sblim.wbem.http;

import java.io.UnsupportedEncodingException;



public final class BASE64Encoder {
	private static byte BASE64_ALPHABET[] = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 
			101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122,
			48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 
			43, 47};

    private BASE64Encoder() {
    }

    public static String encode(byte[] plain) {
		byte encoded[];
        int totalBits = plain.length << 3;
        int remainder = totalBits % 24;
        int totalEncoded = totalBits / 24;

		int size = totalEncoded;
		if (remainder > 0) size++;
		
        encoded = new byte[size <<2];

		short highBits = 0;
		short lowBits = 0;
		short byte1 = 0;
		short byte2 = 0;
		short byte3 = 0;
        int dest = 0;
        int source   = 0;
        for (int i = 0; i < totalEncoded; i++) {
            byte1 = (short)(plain[source++] & 0xFF);
            byte2 = (short)(plain[source++] & 0xFF);
            byte3 = (short)(plain[source++] & 0xFF);

			highBits = (short) (byte1 & 0x03);
            lowBits  = (short) (byte2 & 0x0F);

            short val1 = (short) (byte1 >> 2);
			short val2 = (short) (byte2 >> 4);
			short val3 = (short) (byte3 >> 6);

            encoded[dest++] = BASE64_ALPHABET[val1];
            encoded[dest++] = BASE64_ALPHABET[val2 | (highBits << 4)];
            encoded[dest++] = BASE64_ALPHABET[(lowBits << 2)  | val3];
            encoded[dest++] = BASE64_ALPHABET[byte3 & 0x3f];
        }

        if (remainder == 8) {
            byte1 = (short)(plain[source] & 0xFF);
            highBits = (short) (byte1 & 0x03);
            short val1 = (short) (byte1 >> 2);
            encoded[dest++] = BASE64_ALPHABET[val1];
            encoded[dest++] = BASE64_ALPHABET[highBits << 4];
            encoded[dest++] = (byte)61; //'='
            encoded[dest++] = (byte)61; //'='
        } else if (remainder == 16) {
            byte1 = (short)(plain[source++] & 0xFF);
            byte2 = (short)(plain[source++] & 0xFF);
            highBits = (short) (byte1 & 0x03);
			lowBits  = (short) (byte2 & 0x0F);

            short val1 = (short) (byte1 >> 2);
            short val2 = (short) (byte2 >> 4);

            encoded[dest++] = BASE64_ALPHABET[val1];
            encoded[dest++] = BASE64_ALPHABET[val2 | (highBits << 4)];
            encoded[dest++] = BASE64_ALPHABET[lowBits << 2];
            encoded[dest++] = (byte)61; //'='
        }
        String res;
		try {
			res = new String(encoded, "ASCII");
		} catch (UnsupportedEncodingException e) {
			try {
				res = new String(encoded, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				res = new String(encoded);
			}
		}
		return res;
    }
   
}
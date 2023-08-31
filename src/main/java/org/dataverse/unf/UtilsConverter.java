// Copyright 2023 Dataverse Core Team <support@dataverse.org>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// SPDX-License-Identifier: Apache-2.0

package org.dataverse.unf;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Logger;

public class UtilsConverter implements UnfCons {

    private static Logger mLog = Logger.getLogger(UtilsConverter.class.getName());

    /**
     * Convert byte[] to String using nio
     *
     * @param inbyte byte array to be coded into String
     * @param outset String with name of the Charset
     * @return String with converted bytes decoded with outset
     */
    public static String convertnioByteToStr(final byte[] inbyte, String outset) {
        Charset original = Charset.defaultCharset();
        Charset cset = original;
        try {
            cset = Charset.forName(outset);
        } catch (UnsupportedCharsetException err0) {
            mLog.severe("CanonUnicode: " + err0.getMessage() + "...Using the machine default");
            err0.printStackTrace();
        } catch (IllegalCharsetNameException err1) {
            mLog.severe("CanonUnicode: " + err1.getMessage() + "...Using the machine default");
            err1.printStackTrace();
        } catch (IllegalArgumentException err2) {
            mLog.severe("CanonUnicode: " + err2.getMessage() + "...Using the machine default");
            err2.printStackTrace();
        }

        ByteBuffer bbuf = ByteBuffer.wrap(inbyte, 0, inbyte.length);
        CharBuffer cbuf;
        if (cset.canEncode()) {
            cbuf = cset.decode(bbuf);
        } else {
            cbuf = original.decode(bbuf);
        }

        return cbuf.toString();
    }

    /**
     * Convert char[] to byte[]
     *
     * @param colch char array
     * @param cset String with name of Charset
     * @return byte array
     */
    public static byte[] getBytes(final char[] colch, String cset) {
        Charset original = Charset.defaultCharset();
        Charset ccset = original;
        if (cset != null && Charset.isSupported(cset)) {
            ccset = Charset.forName(cset);
        }
        CharBuffer cb = CharBuffer.wrap(colch);
        ByteBuffer buf = null;
        if (ccset.canEncode()) {
            buf = ccset.encode(cb);
        } else {
            buf = original.encode(cb);
        }

        return buf.array();

    }

    /**
     * Convert char[] to byte[]
     * A different version of getBytes
     *
     * @param ch char array
     * @param cset String with name of Charset
     * @return byte array
     */
    public byte[] getBytes1(final char[] ch, String cset) {
        Charset original = Charset.defaultCharset();
        Charset ccset = original;
        if (cset != null && Charset.isSupported(cset)) {
            ccset = Charset.forName(cset);
        }
        return String.valueOf(ch).getBytes(ccset);

    }

    /**
     * Convert byte[] to byte[] different Encodings
     *
     * @param bin byte array to be encoded
     * @param cset String array with one or two elements
     *             first is the conversion encoding name and,
     *             possibly, second the original encoding for bin
     *
     * @return byte array
     */
    public static byte[] byteConverter(final byte[] bin, String... cset) throws
            UnsupportedEncodingException {
        //default Charset in JVM
        Charset original = Charset.defaultCharset();
        String strbin = null;
        //consistency checks
        if (cset.length > 2) {
            mLog.severe("canonUnicode: Provide only two values for encoding");
        } else if (cset.length <= 0) {
            mLog.severe("canonUnicode: Not provided encoding for array of byte");
            return bin;
        } else if (cset.length == 2 && cset[0].equals(cset[1])) {
            return bin;
        }
        //original encoding for byte array	element 1
        if (cset.length > 1) {

            if (Charset.isSupported(cset[ORG_ENC])) {
                Charset from = Charset.forName(cset[ORG_ENC]);
                if (from.canEncode()) {
                    strbin = new String(bin, cset[ORG_ENC]);
                } else {
                    mLog.severe("canonUnicode: Charset " + cset[ORG_ENC] + " no encode...Using default");
                    strbin = new String(bin, original);
                }
            } else {
                mLog.severe("canonUnicode: Charset " + cset[ORG_ENC] + " not supported...Using default");
                strbin = new String(bin, original);
            }
        }

        Charset to;// final encoding return for byte array element 0
        if (Charset.isSupported(cset[FINAL_ENC])) {
            to = Charset.forName(cset[FINAL_ENC]);
            if (!to.canEncode()) {
                to = original;
            }
        } else {
            mLog.severe("canonUnicode: Charset " + cset[FINAL_ENC] + " not supported...Using default");
            to = original;
        }

        byte[] bout = strbin.getBytes(to);
        return bout;
    }

    /**
     * Convert byte[] to byte[] different Encodings
     *
     * @param bin byte array to be encoded
     * @param cset Charset array with one or two elements
     *             first is the conversion encoding charset and,
     *             possibly, second the original encoding for bin
     *
     * @return byte array
     * @throws UnsupportedEncodingException
     */
    public static byte[] byteConverter(final byte[] bin, Charset... cset) throws
            UnsupportedEncodingException {
        if (cset.length > 2) {
            mLog.severe("canonUnicode: Provide only two values for encoding");
        } else if (cset.length <= 0) {
            mLog.severe("canonUnicode: Not provided encoding for array of byte");
            return bin;
        }
        String[] csetstr = new String[2];
        if (cset.length <= 1) {
            String dest = cset[0].name();
            return byteConverter(bin, dest);
        } else {
            csetstr[FINAL_ENC] = cset[FINAL_ENC].name();
            csetstr[ORG_ENC] = cset[ORG_ENC].name();
            return byteConverter(bin, csetstr);

        }
    }

    /**
     * Converts a byte array to a Hex String
     * @param messageDigest byte array to be converted in hex
     * @return hexadecimal  String representing messageDigest
     */
    public static String getHexStrng(byte[] messageDigest) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < messageDigest.length; i++) {
            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
        }
        return hexString.toString();
    }

    /**
     * Get
     * @param str String hex representation of byte array
     * @return byte array
     */
    public byte[] hexStrngToBytes(String str) {
        byte[] bts = new BigInteger(str, 16).toByteArray();
        return bts;
    }
}
	


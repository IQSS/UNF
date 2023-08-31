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

/**
 * Description: Base64 encoding algorithm. The base64(byte[] input) takes into account
 *              the Endianes of the byte stream and, by default, it turns  
 *              the byte array input to  BIG_ENDIAN order, before applying 
 *              the base64 algorithm. It uses  the classes in org.apache.commons.codec.*
 *              
 * @author evillalon@iq.harvard.edu            
 */
package org.dataverse.unf;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Base64Encoding implements UnfCons {

    private static String DEFAULT_CHAR_ENCODING = "UTF-8";
    private static Logger mLog = Logger.getLogger(Base64Encoding.class.getName());
    /** default byte order */
    private static ByteOrder border = ByteOrder.BIG_ENDIAN;

    public Base64Encoding() {
        if (!DEBUG) {
            mLog.setLevel(Level.WARNING);
        }
    }

    public Base64Encoding(ByteOrder ord) {
        border = ord;
    }

    /**
     *
     * @return ByteOrder
     */
    public static ByteOrder getBorder() {
        return border;
    }

    public static void setBorder(ByteOrder ord) {
        border = ord;
    }

    /**
     *
     * @param digest byte array for encoding in base 64,
     * @param  chngByteOrd boolean indicating if to change byte order
     * @return String the encoded base64 of digest
     */
    public static String tobase64(byte[] digest, boolean chngByteOrd) {

        byte[] tobase64 = null;
        ByteOrder local = ByteOrder.nativeOrder();
        String ordbyte = local.toString();
        mLog.finer("Native byte order is: " + ordbyte);
        ByteBuffer btstream = ByteBuffer.wrap(digest);
        btstream.order(ByteOrder.BIG_ENDIAN);
        byte[] revdigest = null;
        if (chngByteOrd) {
            revdigest = changeByteOrder(digest, local);
        }
        if (revdigest != null) {
            btstream.put(revdigest);
        } else {
            btstream.put(digest);
        }



        tobase64 = Base64.getEncoder().encode(btstream.array());

        return new String(tobase64);

    }

    /**
     *
     * @param digest byte array
     * @param enc String with final encoding
     * @return String the encoded base64 of digest
     * @throws UnsupportedEncodingException
     */
    public static String tobase64(byte[] digest, String enc)
            throws UnsupportedEncodingException {
        
        ByteArrayOutputStream btstream = new ByteArrayOutputStream();
        //this make sure is written in big-endian
        
        DataOutputStream stream = new DataOutputStream(btstream);
        
        byte[] tobase64 = null;
        byte[] revdigest = new byte[digest.length];
        revdigest = changeByteOrder(digest, ByteOrder.nativeOrder());
        try {
            
            stream.write(revdigest);
            stream.flush();
            
            tobase64 = Base64.getEncoder().encode(btstream.toByteArray());
            
        } catch (IOException io) {
            tobase64 = Base64.getEncoder().encode(digest);
        }
        
        return new String(tobase64, enc);
    }

    /**
     * Helper function to change the endianess of the byte array
     *
     * @param digest byte array
     * @param local ByteOrder
     * @return byte array with endianness according to getBorder()
     */
    public static byte[] changeByteOrder(byte[] digest, ByteOrder local) {
        byte[] revdigest = new byte[digest.length];

        if ((local.equals(ByteOrder.LITTLE_ENDIAN) && getBorder().equals(ByteOrder.BIG_ENDIAN)) ||
                (local.equals(ByteOrder.BIG_ENDIAN) && getBorder().equals(ByteOrder.LITTLE_ENDIAN))) {
            int ln = digest.length;
            for (int n = 0; n < ln; ++n) {
                revdigest[n] = digest[ln - 1 - n];
            }
        } else {
            revdigest = digest;
        }
        return revdigest;
    }
}

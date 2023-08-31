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

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author roberttreacy
 */
class UNF6UtilTest {
    
    static final Logger logger = Logger.getLogger(UNF6UtilTest.class.getName());
    
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_doubleArr() throws Exception {
        List testData = readFileData("test/DoubleTest");
        double[] numb = new double[testData.size()-1];
        String expResult =  (String) testData.get(0);
        for (int i=1; i < testData.size(); i++){
            numb[i-1] = Double.parseDouble((String) testData.get(i));
        }
        String result = UNFUtil.calculateUNF(numb);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_floatArr() throws Exception {
        System.out.println("calculateFloatUNF");
        List testData = readFileData("test/FloatTest");
        float[] numb = new float[testData.size() - 1];
        String expResult = (String) testData.get(0);
        for (int i = 1; i < testData.size(); i++) {
            numb[i - 1] = Float.parseFloat((String) testData.get(i));
        }
        String result = UNFUtil.calculateUNF(numb);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_shortArr() throws Exception {
        System.out.println("calculateShortUNF");
        List testData = readFileData("test/ShortTest");
        short[] numb = new short[testData.size() - 1];
        String expResult = (String) testData.get(0);
        for (int i = 1; i < testData.size(); i++) {
            numb[i - 1] = Short.parseShort((String) testData.get(i));
        }
        String result = UNFUtil.calculateUNF(numb);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_byteArr() throws Exception {
        System.out.println("calculateByteUNF");
        List testData = readFileData("test/ByteTest");
        byte[] numb = new byte[testData.size() - 1];
        String expResult = (String) testData.get(0);
        for (int i = 1; i < testData.size(); i++) {
            numb[i - 1] = Byte.parseByte((String) testData.get(i));
        }
        String result = UNFUtil.calculateUNF(numb);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_longArr() throws Exception {
        System.out.println("calculateLongUNF");
        List testData = readFileData("test/LongTest");
        long[] numb = new long[testData.size() - 1];
        String expResult = (String) testData.get(0);
        for (int i = 1; i < testData.size(); i++) {
            numb[i - 1] = Long.parseLong((String) testData.get(i));
        }
        String result = UNFUtil.calculateUNF(numb);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_intArr() throws Exception {
        System.out.println("calculateIntUNF");
        List testData = readFileData("test/IntTest");
        int[] numb = new int[testData.size() - 1];
        String expResult = (String) testData.get(0);
        for (int i = 1; i < testData.size(); i++) {
            numb[i - 1] = Integer.parseInt((String) testData.get(i));
        }
        String result = UNFUtil.calculateUNF(numb);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_booleanArr() throws Exception {
        System.out.println("calculateBooleanUNF");
        List testData = readFileData("test/BooleanTest");
        boolean[] numb = new boolean[testData.size() - 1];
        String expResult = (String) testData.get(0);
        for (int i = 1; i < testData.size(); i++) {
            numb[i - 1] = Boolean.parseBoolean((String) testData.get(i));
        }
        String result = UNFUtil.calculateUNF(numb);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_StringArr() throws Exception {
        System.out.println("calculateStringUNF");
        List testData = readFileData("test/StringTest");
        String[] chr = new String[testData.size() - 1];
        String expResult = (String) testData.get(0);
        for (int i = 1; i < testData.size(); i++) {
            chr[i - 1] = (String) testData.get(i);
        }
        String result = UNFUtil.calculateUNF(chr);
        assertEquals(expResult, result);
    }
    
    
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_StringArr_StringArr() throws Exception {
        System.out.println("calculateDateTimeUNF");
        List testData = readFileData("test/DateTimeTest");
        String[] chr = new String[testData.size() - 1];
        String[] sdfFormat = new String[testData.size() - 1];
        String expResult = (String) testData.get(0);
        for (int i = 1; i < testData.size(); i++) {
            String dateAndFormat = (String) testData.get(i);
            int separatorIndex = dateAndFormat.indexOf('~');
            chr[i - 1] = dateAndFormat.substring(0, separatorIndex);
            sdfFormat[i - 1] = dateAndFormat.substring(separatorIndex+2);
        }
        String result = UNFUtil.calculateUNF(chr, sdfFormat);
        assertEquals(expResult, result);
    }
    /**
     * Test of calculateUNF method, of class UNF5Util.
     */
    @Test
    void testCalculateUNF_BitStringArr() throws Exception {
        System.out.println("calculateUNF");
        List testData = readFileData("test/BitStringTest");
        BitString[] numb = new BitString[testData.size() - 1];
        String expResult = (String) testData.get(0);
        for (int i = 1; i < testData.size(); i++) {
            numb[i - 1] = new BitString((String) testData.get(i));
        }
        String result = UNFUtil.calculateUNF(numb);
        assertEquals(expResult, result);
    }
    
    private List<String> readFileData(String filename) {
        List<String> retList = new ArrayList<>();
        //File file = new File(filename);
        InputStream in = null;
        try {
            in = new FileInputStream("src/test/resources/"+filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                retList.add(line);
            }
        } catch (IOException x) {
            System.err.println(x);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
        return retList;
    }
}
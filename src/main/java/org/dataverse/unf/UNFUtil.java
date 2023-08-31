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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UNFUtil {

    /**
     * Obtains the unf of a one dimensional array of double
     * by calling the methods in Unf5Digest
     *
     * @param numb one dimensional array of double
     * @return String with unf calculation
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final double[] numb)
            throws NumberFormatException, UnfException, IOException {

        Double mat[][] = new Double[1][numb.length];
        for (int n = 0; n < numb.length; ++n) {
            mat[0][n] = numb[n];
        }
        UnfDigest.setTrnps(false);
        String[] res = UnfDigest.unf(mat);

        return res[0];
    }


    /**
     * Overloaded method
     * @param numb one dimensional array of float
     * @return String with unf calculation
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final float[] numb)
            throws NumberFormatException, UnfException, IOException {
        double[] toret = new double[numb.length];
        for (int k = 0; k < numb.length; ++k) {
             toret[k] =  numb[k] == Float.NaN ? Double.NaN : (double) numb[k];
        }
        String res = calculateUNF(toret);
        return res;
    }

    /**
     * Overloaded method
     * @param numb one dimensional array of short
     * @return String with unf calculation
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final short[] numb)
            throws NumberFormatException, UnfException, IOException {
        double[] toret = new double[numb.length];
        for (int k = 0; k < numb.length; ++k) {
           if (numb[k] == Short.MAX_VALUE){
                toret[k] = Double.NaN;
            } else {
                toret[k] = (double) numb[k];
           }
        }
        String res = calculateUNF(toret);
        return res;

    }

    /**
     * Overloaded method
     * @param numb one dimensional array of byte
     * @return String with unf calculation
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final byte[] numb)
            throws NumberFormatException, UnfException, IOException {
        double[] toret = new double[numb.length];
        for (int k = 0; k < numb.length; ++k) {
           if (numb[k] == Byte.MAX_VALUE){
                toret[k] = Double.NaN;
            } else {
                toret[k] = (double) numb[k];
           }
        }
        String res = calculateUNF(toret);
        return res;

    }

    /**
     * Overloaded method
     * @param numb one dimensional array of long
     * @return String with unf calculation
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final long[] numb)
            throws NumberFormatException, UnfException, IOException {

        double[] toret = new double[numb.length];
        for (int k = 0; k < numb.length; ++k) {
           if (numb[k] == Long.MAX_VALUE){
                toret[k] = Double.NaN;
            } else {
                toret[k] = (double) numb[k];
           }
        }
        String res = calculateUNF(toret);
        return res;
    }

    /**
     * Overloaded method
     * @param numb one dimensional array of integer
     * @return String with unf calculation
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final int[] numb)
            throws NumberFormatException, UnfException, IOException {

        double[] toret = new double[numb.length];
        for (int k = 0; k < numb.length; ++k) {
           if (numb[k] == Integer.MAX_VALUE){
                toret[k] = Double.NaN;
            } else {
                toret[k] = (double) numb[k];
           }
        }
        String res = calculateUNF(toret);
        return res;
    }

    /**
     * Overloaded method Converts boolean to 1 (true) or 0 (false).
     * @param numb one dimensional array of boolean
     * @return String with unf calculation
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final boolean[] numb)
            throws NumberFormatException, IOException {

        Boolean[] toret = new Boolean[numb.length];
        for (int k = 0; k < numb.length; ++k) {
            toret[k] = Boolean.valueOf(numb[k]);
        }
        String[] res = UnfDigest.unf(toret);
        return res[0];
    }

    /**
     * Overloaded method
     * @param numb List with generics types
     * @return String with unf calculation
     * @throws NumberFormatException
     * @throws IOException
     */
    public static <T> String calculateUNF(final List<T> numb)
            throws NumberFormatException, UnfException, IOException {
        if (numb.get(0) instanceof Number) {
            double[] arr = new double[numb.size()];
            int cnt = 0;
            for (T obj : numb) {
                arr[cnt] = (Double) obj;
                cnt++;
            }
            return calculateUNF(arr);
        }
        String[] topass = new String[numb.size()];
        topass = numb.toArray(new String[numb.size()]);
        return calculateUNF(topass);

    }

    /**
     * Overloaded method
     * @param beginDate one dimensional array of String
     * @return String with unf calculation
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final String[] chr)
            throws IOException, UnfException {
        String tosplit = ":";
        if (chr[0] != null) {
            String spres[] = chr[0].split(tosplit);
            if (spres.length >= 3 && chr[0].startsWith("UNF:")) {
                return UnfDigest.addUNFs(chr);
            }
            if (spres.length > 1) {
                //throw new UnfException("UNFUtil: Malformed unf");
            }
        }
        CharSequence[][] chseq = new CharSequence[1][chr.length];
        UnfDigest.setTrnps(false);
        int cnt = 0;
        for (String str : chr) {
            chseq[0][cnt] = (CharSequence) str;
            cnt++;
        }
        String[] res = UnfDigest.unf(chseq);
        return res[0];
    }

     public static String calculateUNF(final String[] chr, final String[] sdfFormat)
            throws  IOException, UnfException {
        String tosplit = ":";
         if (chr[0] != null) {
             String spres[] = chr[0].split(tosplit);
             if (spres.length >= 3 && chr[0].startsWith("UNF:")) {
                 return UnfDigest.addUNFs(chr);
             }

             if (spres.length > 1) {
                 //throw new UnfException("UNFUtil: Malformed unf");
             }
         }
        CharSequence[][] chseq = new CharSequence[1][chr.length];
        UnfDigest.setTrnps(false);
        int cnt = 0;
        for (String str : chr) {
            if (sdfFormat[cnt] != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(sdfFormat[cnt]);
                try {
                    Date d = sdf.parse(str);
                    UnfDateFormatter udf = new UnfDateFormatter(sdfFormat[cnt]);
                    SimpleDateFormat unfSdf = new SimpleDateFormat(udf.getUnfFormatString().toString());
                    if (udf.isTimeZoneSpecified()){
                        unfSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    }
                    str = unfSdf.format(d);
                    // remove any trailing 0s from milliseconds
                    if (sdfFormat[cnt].indexOf("S")> -1 && str.endsWith("0")){
                        while (str.endsWith("0")){
                            str = str.substring(0,str.length()-1);
                        }
                        // if all trailing milliseconds were 0s, there will now be a trailing . to remove
                        if (str.endsWith(".")) {
                            str = str.substring(0, str.length() - 1);
                        }
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(UNFUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            chseq[0][cnt] = (CharSequence) str;
            cnt++;
        }
        String[] res = UnfDigest.unf(chseq);
        return res[0];
    }

      public static String calculateUNF(final String[] beginDate, final String[] sdfFormat, final String[] endDate)
            throws  IOException, UnfException {
        String tosplit = ":";
         if (beginDate[0] != null) {
             String spres[] = beginDate[0].split(tosplit);
             if (spres.length >= 3 && beginDate[0].startsWith("UNF:")) {
                 return UnfDigest.addUNFs(beginDate);
             }

             if (spres.length > 1) {
                 //throw new UnfException("UNFUtil: Malformed unf");
             }
         }
        CharSequence[][] chseq = new CharSequence[1][beginDate.length];
        UnfDigest.setTrnps(false);
        int cnt = 0;
        for (String str : beginDate) {
            if (sdfFormat[cnt] != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(sdfFormat[cnt]);
                try {
                    Date d = sdf.parse(str);
                    UnfDateFormatter udf = new UnfDateFormatter(sdfFormat[cnt]);
                    SimpleDateFormat unfSdf = new SimpleDateFormat(udf.getUnfFormatString().toString());
                    if (udf.isTimeZoneSpecified()){
                        unfSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    }
                    str = unfSdf.format(d);
                    // remove any trailing 0s from milliseconds
                    if (sdfFormat[cnt].indexOf("S")> -1 && str.endsWith("0")){
                        while (str.endsWith("0")){
                            str = str.substring(0,str.length()-1);
                        }
                        // if all trailing milliseconds were 0s, there will now be a trailing . to remove
                        if (str.endsWith(".")) {
                            str = str.substring(0, str.length() - 1);
                        }
                    }
                    if (endDate[cnt] != null) {
                        String str2 = endDate[cnt];
                        Date endD = sdf.parse(str2);
                        str2 = unfSdf.format(endD);
                        if (sdfFormat[cnt].indexOf("S") > -1 && str2.endsWith("0")) {
                            while (str2.endsWith("0")) {
                                str2 = str2.substring(0, str.length() - 1);
                            }
                            // if all trailing milliseconds were 0s, there will now be a trailing . to remove
                            if (str2.endsWith(".")) {
                                str2 = str2.substring(0, str2.length() - 1);
                            }
                        }
                        str += "/" + str2;
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(UNFUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            chseq[0][cnt] = (CharSequence) str;
            cnt++;
        }
        String[] res = UnfDigest.unf(chseq);
        return res[0];
    }

  /**
     * Calculates unf's of two-dimensional array of double
     * along second index (columns) and add them
     * Note that if data set contains number and String this method
     * cannot be used. Instead used the one-dimensional arrays of double and
     * combine with the String unfs.
     *
     * @param numb double bi-dimensional array
     * to obtain unf along columns second index
     * @return String unf for data set
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final double[][] numb)
            throws NumberFormatException, UnfException, IOException {
        int ncol = numb[0].length;
        List<double[]> lst = Arrays.asList(numb);
        int nrw = lst.size();
        Number[][] pass = new Number[nrw][ncol];
        for (int r = 0; r < nrw; ++r) {
            for (int c = 0; c < ncol; ++c) {
                pass[r][c] = numb[r][c];
            }
        }
        String[] unfs = UnfDigest.unf(pass);
        return calculateUNF(unfs);
    }

    /**
     * Calculates unf's of two-dimensional array of String
     * along second index (columns) and add them
     * Note that if data set contains number and String this method
     * cannot be used. Instead used the one-dimensional arrays and
     * combine with the numeric unfs.
     *
     * @param str String bi-dimensional array
     * to obtain unf along columns second index
     * @return String unf for data set
     * @throws NumberFormatException
     * @throws IOException
     */
    public static String calculateUNF(final String[][] str)
            throws NumberFormatException, UnfException, IOException {
        int ncol = str[0].length;
        List<String[]> lst = Arrays.asList(str);
        int nrw = lst.size();
        CharSequence[][] pass = new CharSequence[nrw][ncol];
        for (int r = 0; r < nrw; ++r) {
            for (int c = 0; c < ncol; ++c) {
                pass[r][c] = str[r][c];
            }
        }
        String[] unfs = UnfDigest.unf(pass);
        return calculateUNF(unfs);
    }

    public static String calculateUNF(final Number[] numb) throws IOException, UnfException {
        Double mat[][] = new Double[1][numb.length];
        for (int n = 0; n < numb.length; ++n) {
            mat[0][n] = numb[n] != null ? Double.valueOf(numb[n].doubleValue()) : null;
        }
        UnfDigest.setTrnps(false);
        String[] res = UnfDigest.unf(mat);

        return res[0];
    }

    public static String calculateUNF(final BitString[] numb) throws IOException {
        UnfDigest.setTrnps(false);
        String[] res = UnfDigest.unf(numb);

        return res[0];
    }
}

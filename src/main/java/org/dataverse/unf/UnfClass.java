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
 * Description: Holds relevant parameters and values from calculating digest
 * in a data set with specified number of rows and columns. 
 * The Collection with the fingerprints, the arrays with 
 * base64 encoding, and the hexadecimal strings representations each of these  
 * values have being calculated for every column of data set. Following the 
 * definitation of class unf given by Micah Altman in his C code
 * 
 * @author evillalon
 * {@link evillalon@iq.harvard.edu} 
 * rtreacy adapted evillalon code for unf version 5
 */
package org.dataverse.unf;

import java.util.ArrayList;
import java.util.List;

public class UnfClass {

    /** approximate with cdigits number of characters */
    private int cdigits = UnfCons.DEF_CDGTS;
    /** approximate with (ndigits-1) after decimal point*/
    private int ndigits = UnfCons.DEF_NDGTS;
    private int hsize = UnfCons.DEF_HSZ;
    /**
     * contains the fingerprint (byte array) from MessageDigest
     * for every column of data matrix
     * */
    private List<Integer[]> fingerprints = new ArrayList<Integer[]>();
    /** the unf version*/
    /**
     * the hexadecimal string for columns of data matrix as obtained
     * from the byte arrays of every column
     */
    private List<String> hexvalue = new ArrayList<String>();
    /**
     * array with strings after encoding with Base64 the
     * byte arrays of the messageDigest for
     * every column of data matrix
     */
    private List<String> b64 = new ArrayList<String>();
    private String extensions = "";

    /**
     * Constructor
     */
    public UnfClass() {
    }

    /**
     * Constructor
     * @param cd integer with number of characters
     * @param nd integer with number decimal digits
     * @param vers String the unf version
     */
    public UnfClass(int cd, int nd) {
        cdigits = cd;
        ndigits = nd;
        if (cd != UnfCons.DEF_CDGTS) {
            addExtension("X" + cd);
        }
        if (nd != UnfCons.DEF_NDGTS) {
            addExtension("N" + nd);
        }
    }

    public UnfClass(int cd, int nd, int hsz) {
        cdigits = cd;
        ndigits = nd;
        hsize = hsz;
        if (cd != UnfCons.DEF_CDGTS) {
            addExtension("X" + cd);
        }
        if (nd != UnfCons.DEF_NDGTS) {
            addExtension("N" + nd);
        }
        if (hsz != UnfCons.DEF_HSZ) {
            addExtension("H" + hsz);
        }
    }

    /**
     *
     * @return integer with approximated of characters
     */
    public int getCdigits() {
        return cdigits;
    }

    /**
     *
     * @param d integer for number of chars
     */
    public void setCdigits(int d) {
        cdigits = d;
    }

    /**
     *
     * @return integer with number of digits including decimal point
     */
    public int getNdigits() {
        return ndigits;
    }

    /**
     *
     * @param d integer with digits including decimal point
     */
    public void setNdigits(int d) {
        ndigits = d;
    }

    /**
     *
     * @return String array with hexadecimal representation
     * of every column in data set after applying digest
     */
    public List<String> getHexvalue() {
        return hexvalue;
    }

    /**
     *
     * @param s String array with hexadecimal representation of
     * each column in data set after calculating digest
     */
    public void setHexvalue(List<String> s) {
        hexvalue = s;
    }

    /***
     *
     * @return String array with base64 encoding for every column
     * in data set obtained from bytes arrays of digest
     */
    public List<String> getB64() {
        return b64;
    }

    /**
     *
     * @param b String array with base64 encoding
     * of each column in data set
     */
    public void setB64(List<String> b) {
        b64 = b;
    }

    /**
     *
     * @return Collection of fingerprints from digest
     */
    public List<Integer[]> getFingerprints() {
        return fingerprints;
    }

    /**
     *
     * @param fg List of Integer arrays with fingerprints
     * of data set as obtained from digest
     */
    public void setFingerprints(List<Integer[]> fg) {
        fingerprints = fg;
    }

    /**
     * @return the extensions
     */
    public String getExtensions() {
        return extensions;
    }

    /**
     * @param extensions the extensions to set
     */

    public void setExtensions(String extensions) {
        this.extensions = extensions;
    }

    public void addExtension(String ext){
        if (getExtensions().length()>0){
            setExtensions(getExtensions()  + "," + ext);
        } else{
            setExtensions(getExtensions() + ext);
        }
    }

    /**
     * @return the hsize
     */
    public int getHsize() {
        return hsize;
    }

    /**
     * @param hsize the hsize to set
     */
    public void setHsize(int hsize) {
        this.hsize = hsize;
    }

}
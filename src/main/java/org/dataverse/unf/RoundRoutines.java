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
 * Description: Generalized Rounding Routines
 * 
 *              Implements Micah Altman code for rounding Numbers
 *              The implementation is in method Genround
 * Input: int with number of digits including the  decimal point, 
 *        Object obj to apply the rounding routine;  
 *        obj is of class Number and any of its derived sub-classes.
 *        
 * Output: String representation of Object obj in canonical form. 
 * Example :/*
 * Canonical form:
 *                -leading + or -
 *                -leading digit
 *                -decimal point
 *                -up to digits-1 no trailing 0
 *                -'e'
 *                -sign either + or -
 *                -exponent digits no leading 0
 * Number -2.123498e+22, +1.56e+1, -1.3456e-, +3.4222e+
 * mantissa= digits after the decimal point & decimal point
 * exponent= digits after 'e' and the sign that follows
 *
 * Usage: For Number, e.g  Double number and int digits
 *  	roundRoutines<Double> rout = new roundRoutines<Double>();
 *      rout.Genround(number,digits);
 * 	    For BigDecimal number,
 * 	    roundRoutines<BigDecimal> routb = new roundRoutines<BigDecimal>();
 *      routb.Genround(new BigDecimal(number),digits);
 *
 * For String of chars, e.g. String ss = "news from ado";
 *      roundRoutines.Genround(ss,digits);
 *
 * @Author: Elena Villalon
 * <a heref= email: evillalon@iq.harvard.edu/>
 *       
 */
package org.dataverse.unf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.math.MathContext;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoundRoutines<T extends Number> implements UnfCons {

    public static final long serialVersionUID = 1111L;
    private static Logger mLog = Logger.getLogger(RoundRoutines.class.getName());
    /**number of digits with decimal point*/
    private int digits;
    /**the Locale language and country*/
    private Locale loc;
    /**some formatting for special numbers*/
    private FormatNumbSymbols symb = new FormatNumbSymbols();
    /**no leading and trailing 0 digits in exponent and mantissa*/
    private static final boolean nozero = true; //default is true
    /** radix for numbers*/
    private int radix = 10;
    /**unicode characters*/
    private static final char dot = Ucnt.dot.getUcode();//decimal separator "."
    private static final char plus = Ucnt.plus.getUcode(); //"+" sign
    private static final char min = Ucnt.min.getUcode(); //"-"
    private static final char e = Ucnt.e.getUcode(); //"e"
    private static final char percntg = Ucnt.percntg.getUcode(); //"%"
    private static final char pndsgn = Ucnt.pndsgn.getUcode(); //"#"
    private static final char zero = Ucnt.zero.getUcode();
    private static final char s = Ucnt.s.getUcode();//"s"
    private static final char ffeed = Ucnt.frmfeed.getUcode();
    private static final char creturn = Ucnt.psxendln.getUcode();
    /** whether to append the null byte ('\0') the end of string */
    private static boolean nullbyte = !UnfCons.nullbyte;
    /** check conversion from string to numeric for mix 
     * columns values (i.e. column can have chars and numbers)
     * */
    private static boolean convertToNumber = false;

    /**
     * Default constructor 
     */
    public RoundRoutines() {
        if (!DEBUG) {
            mLog.setLevel(Level.WARNING);
        }
        this.digits = DEF_NDGTS;
        this.symb = new FormatNumbSymbols();

    }

    /**
     *
     * @param no boolean whether to append null bytes at end of Strings
     */
    public RoundRoutines(boolean no) {
        this();
        nullbyte = no;
    }

    /**
     *
     * @param digits integer with number of decimal digits for mantissa calculations
     * @param  no boolean whether to append null bytes at end of Strings
     * Number of decimal digits including the decimal point
     */
    public RoundRoutines(int digits, boolean no) {
        this(no);
        if (digits < 1) {
            digits = 1; //count for decimal separator
        }		//upper value is limited
        this.digits = digits <= INACCURATE_SPRINTF_DIGITS ? digits : INACCURATE_SPRINTF_DIGITS;

    }

    /**
     *
     * @param digits integer number of decimal digits for mantissa calculations
     * @param loc the default locale
     * @param no boolean whether to append null bytes at end of Strings
     */
    public RoundRoutines(int digits, boolean no, Locale loc) {
        this(digits, no);
        this.loc = loc;
    }

    /**
     *
     * @return boolean indicating if null byte
     * is appended end of String
     */
    public boolean getNullbyte() {
        return nullbyte;
    }

    /**
     *
     * @param b boolean set nullbyte
     */
    public void setNullbyte(boolean b) {
        nullbyte = b;
    }

    /**
     *
     * @param obj Object of class Number and sub-classes
     * @param digits integer total decimal digits including decimal point
     * @return String with canonical formatting
     */
    public String Genround(T obj, int digits) throws UnfException {
        return Genround(obj, digits, nullbyte);
    }

    /**
     * It obtains the string representation of numeric values according
     * to Micah Altman specs (IEEE 754)
     *
     * @param numberValue Object of class *Number and sub-classes*
     * (why not declare it as such - Number numberValue? -- L.A.)
     * @param digits integer Number of decimal digits with decimal point
     * @param nullByte boolean indicating whether null byte ('\0') is appended
     * @return String with the numeric value represented using IEEE 754
     */
    public String Genround(T numberValue, int digits, boolean nullByte) throws UnfException {
        RoundRoutines.nullbyte = nullByte;
        
        if (numberValue == null) {
            throw new UnfException ("Missing (Null) value passed to Genround(Number)!");
        }

        //the decimal separator symbol:
        char sep = symb.getDecimalSep();

         if (sep != dot) {
            mLog.warning("RoundRoutines: Decimal separator is not " +
                    "'\u002E' or a dot:.");
            sep = '.';
            // TODO: ?
        }
         
        if (digits < 0) {
            digits = this.digits;
        }

        Double doubleValue = numberValue.doubleValue();

        // Special handling for Zero, positive and negative:
        
        if (doubleValue.doubleValue() == 0.0d) {
            // For Java's primitive type double the above 
            // expression (... == 0.0d) evaluates to TRUE for both the positive 
            // and negative zero!
            
            StringBuffer nullStringBuffer = new StringBuffer(); 

            // However, the .equals() method supplied by the class 
            // Double does differentiate between the two:
            if (doubleValue.equals(new Double(0.0d))) {
                nullStringBuffer.append(plus);
            } else if (doubleValue.equals(new Double(-0.0d))) {
                nullStringBuffer.append(min);
            } else {
                throw new UnfException("The zero value supplied is neither positive, nor negative... what?");
            }
            nullStringBuffer.append(zero);
            nullStringBuffer.append(sep);
            nullStringBuffer.append(e);
            nullStringBuffer.append(plus);
            nullStringBuffer.append(creturn);
            return nullStringBuffer.toString();
        }
        
        
        // Special handling for the special floating point values, 
        // Double.NaN, Inf or -Inf:
        
        String specialValueToken = null; 
        
        if ((specialValueToken = RoundRoutinesUtils.specialNumb(doubleValue)) != null) {
            StringBuffer specialValueBuffer = new StringBuffer();
            /*
             * Important: 
             * Just like regular numeric values, and unlike missing values, 
             * string representations of special values must be terminated with 
             * new lines. -- L.A.  
             */
            specialValueBuffer.append(specialValueToken);
            specialValueBuffer.append(creturn);
           
            return specialValueBuffer.toString();
        } 
        
        BigDecimal bigDecimalValue = null;
        
        if (numberValue instanceof BigDecimal) {
            bigDecimalValue = (BigDecimal) numberValue;
        } else if (numberValue instanceof BigInteger) {
            bigDecimalValue = new BigDecimal((BigInteger) numberValue, MathContext.DECIMAL64);
        } else {
            try {
                bigDecimalValue = new BigDecimal(Double.toString(numberValue.doubleValue()), MathContext.DECIMAL64);
            } catch (NumberFormatException ex) {
                //mLog.fine("Caught an exception when trying to make a BigDecimal out of a .doubleValue() of a Number object; (most likely because it's a special value - ignoring)");
                throw new UnfException("Caught an exception when trying to make a BigDecimal out of a .doubleValue() of a Number object; (an undetected special IEEE value perhaps?)");
            }
        }
        
        if (bigDecimalValue == null) {
            throw new UnfException("Failed to convert the supplied Number value "+numberValue+" to BigDecimal.");
        }

        String fmt, fmtu;


        char[] str = {percntg, plus, pndsgn, sep}; //{'%','+', '#', '.'}

        int dgt = (INACCURATE_SPRINTF) ? INACCURATE_SPRINTF_DIGITS : (digits - 1);

        fmt = new String("%+#." + dgt + "e");
        StringBuilder stringBuilder = new StringBuilder();

        
        //using the Unicode character symbols
        stringBuilder.append(str);
        stringBuilder.append(dgt);
        stringBuilder.append(e);
        fmtu = stringBuilder.toString();
        stringBuilder = null;
        
        if (!fmtu.equalsIgnoreCase(fmt) && loc == new Locale("en", "US")) {
            mLog.severe("RoundRoutines: Unicode & format strings do not agree");
            // TODO:
            // throw an exception here - ?
            // what is this check for anyway? - could we just connclude that 
            // it's irrelevant, now that we are using UTF8?
            // -- L.A. Oct. 2014
        }

        // TODO: 
        // Figure out why proper rounding is only applied when the supplied value 
        // is NOT a native BigDecimal - ?? 
        // (this is case is not really used in real life, as of now... -- but 
        // it needs to be figured out nevertheless.)
        // (or, maybe the assumption is that the BigDecimal was created with 
        // the correct MathContext, which already specifies the rounding 
        // algorithm that needs to be used?)
        // -- L.A. Aug. 2014
        // OK, ot rid of the different cases that were defined for handling
        // BigDecimals and doubleValues differently... -- L.A. Oct. 2014
        // TODO: 
        // Document why we need RoundingMode.HALF_EVEN below, and not just 
        // the default String representation of the value, below. 
        // -- L.A. Aug. 2014
        
        
        String tmp = String.format(loc, fmtu, bigDecimalValue.round(new MathContext(digits, RoundingMode.HALF_EVEN))); 
        
        String atoms[] = tmp.split(e + "");

        stringBuilder = calcMantissa(atoms[0], sep);
        stringBuilder.append(e);
        stringBuilder.append(atoms[1].charAt(0)); //sign of exponent
        stringBuilder.append(calcExponent(atoms[1]));
        return stringBuilder.toString();
    }

    /**
     *
     * @param obj Object of class Number and sub-classes
     * @param digits integer number of decimal digits to keep
     * @param charset String with optional encoding of bytes
     * @return byte array encoded with charset
     */
    public byte[] GenroundBytes(T obj, int digits, String... charset) throws UnfException {
        String str = Genround(obj, digits);
        if (str == null || str.equals("")) {
            return null;
        }
        Charset original = Charset.defaultCharset();
        Charset to = original;
        if (charset.length > 0 && Charset.isSupported(charset[0])) {
            to = Charset.forName(charset[0]);
            if (!to.canEncode()) {
                to = original;
            }
        }
        return str.getBytes(to);
    }

    /**
     *
     * @param atom String with the exponent including the sign
     * @return StringBuffer representing exponent with no leading 0
     *         and appending the end of line.
     */
    private StringBuffer calcExponent(String atom) {

        StringBuffer build = new StringBuffer();

        String expnt = atom.substring(1);
        long lngmant = Long.parseLong(expnt);//remove leading 0's

        if (lngmant > 0 || (lngmant == 0 && !nozero)) {
            build.append(lngmant);
        }

        /**
         * per specs append the end of line "\n" and
         * null terminator
         */
        build.append(creturn);
        if (nullbyte) {
            build.append(nil);
        }
        return build;
    }

    /**
     *
     * @param atom String with the mantissa
     * @param sep char the decimal point
     * @param f boolean for number between (-1,1)
     * @return StringBuilder with mantissa after removing trailing 0
     */
    private StringBuilder calcMantissa(String atom, char sep) {
        StringBuilder build = new StringBuilder();
        //sign and leading digit before decimal separator
        char mag[] = {atom.charAt(0), atom.charAt(1)};
        //canon[] :double check you have correct results
        String canon[] = atom.split("\\" + sep);
        if (!canon[0].equalsIgnoreCase(new String(mag))) {
            mLog.severe("RoundRoutines:decimal separator no in right place");
        }
        build.append(mag);//sign and leading digit
        build.append(sep);//decimal separator
        String dec = atom.substring(3);//decimal part
        if (!dec.equalsIgnoreCase(canon[1])) {
            mLog.severe("RoundRoutines: decimal separator not right");
        }

        String tmp = new StringBuffer(dec).reverse().toString();
        long tmpl = Long.parseLong(tmp); //remove trailing 0's
        tmp = new StringBuffer((Long.toString(tmpl))).reverse().toString();

        //removing trailing 0
        if (tmpl == 0 && nozero) {
            return build;
        }

        return build.append(tmp);

    }

    /**
     * @param cobj CharSequence to format
     * @param digits integer with number of characters  to keep
     * @return String formatted
     */
    public String Genround(CharSequence cobj, int digits) throws UnfException {
        return Genround(cobj, digits, nullbyte);
    }

    /**
     * @param cobj CharSequence to format
     * @param digits integer with number of characters  to keep
     * @param no boolean if to append nullbyte
     * @return String formatted
     */
    public static String Genround(CharSequence cobj, int digits, boolean no) throws UnfException {
        
        // A special case for a character string made up entirely of 
        // "blank space" characters - i.e., spaces, tabs and assorted newlines:
        if ((((String) cobj).trim()).equals("")) {
            // (or an empty string, for that matter... - except this method
            // is NOT called on empty strings.)
            // Current approach - if this "all-blank" is not longer than
            // the cutoff limit ("digits" characters long), it is normalized
            // to an empty string. If it is longer than digits, it is normalized
            // to the first (digits - 1) characters (why not digits - ?)
            // TODO: 
            // VERIFY/FINALIZE THIS WITH MICAH ASAP!  
            //  -- L.A. AUG. 17 2014
            String res = "";
            if (cobj.length() > digits) {
                res = (String) cobj.subSequence(0, digits - 1);
            }
            res += creturn;
            if (no) {
                res += nil;
            }
            return res;
        }
        
        boolean numeric = false;
        if (convertToNumber) {
            numeric = RoundRoutinesUtils.checkNumeric(cobj);
        }

        if (numeric) {
            //only digits in obj use a BigInteger representation
            BigInteger bg = new BigInteger(cobj.toString());
            RoundRoutines<BigInteger> rout = new RoundRoutines<BigInteger>();
            return rout.Genround(bg, digits, no);
        }

        //if is not digits

        return (new RoundString().Genround((String) cobj, digits, no));
    }
}



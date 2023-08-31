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
 * Description For special numbers (infinity, -infinity,NaN)
 * represents them as in Micah's code
 * @author evillalon
 */
package org.dataverse.unf;

import java.util.*;
import java.text.*;

public class FormatNumbSymbols {

    private final Locale currentLocale;
    private final DecimalFormatSymbols decimalFmtSymb;

    public FormatNumbSymbols() {
        currentLocale = new Locale("en", "US");
        decimalFmtSymb = new DecimalFormatSymbols(currentLocale);
    }

    public FormatNumbSymbols(Locale currentLocale) {
        this.currentLocale = currentLocale;
        decimalFmtSymb = new DecimalFormatSymbols(currentLocale);

    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Micah algor defining +infinity
     * @return String
     */
    public String getPlusInf() {
        return "+inf";
    }

    /**
     * Micah algor defining -infinity
     * @return String
     */
    public String getMinusInf() {
        return "-inf";
    }

    /**
     * Micah algor defining nan
     * @return String
     */
    public String getNan() {
        return "+nan";
    }

    /**
     * Micah decimal separator
     * @return char
     */
    public char getDecSep() {
        return '.';
    }

    public String getPlusInfinity() {

        return decimalFmtSymb.getInfinity();
    }

    public String getMinusInfinity() {

        return new String(decimalFmtSymb.getMinusSign() +
                decimalFmtSymb.getInfinity());
    }

    public String getNaN() {

        return (decimalFmtSymb.getNaN());

    }

    public char getDecimalSep() {
        return (decimalFmtSymb.getDecimalSeparator());
    }

    public void setDecimalSep(char dot) {
        decimalFmtSymb.setDecimalSeparator(dot);
    }
}

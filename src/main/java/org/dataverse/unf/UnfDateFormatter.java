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

/**
 *
 * @author roberttreacy
 */
public class UnfDateFormatter {

    private boolean isValidUnfDate;
    private StringBuffer unfFormatString;
    private boolean timeZoneSpecified;

    public UnfDateFormatter(String formatString) {
        getDateTimeRepresentation(formatString);
        if (!isValidUnfDate){
            getTimeRepresentation(formatString);
        }
    }

    private void getDateTimeRepresentation(String formatString) {
        if (hasYear(formatString)) {
            unfFormatString = new StringBuffer();
            isValidUnfDate = true;
            unfFormatString.append("yyyy");
            if (hasMonth(formatString)) {
                unfFormatString.append("-MM");
                if (hasDay(formatString)) {
                    unfFormatString.append("-dd");
                    getTimeRepresentation(formatString);
                }
            } else if (formatString.indexOf('d') > -1) {
                isValidUnfDate = false;
            }
            if (!isValidUnfDate){
                unfFormatString = null;
            }
        }
    }

    private void getTimeRepresentation(String formatString) {
        if (hasHour(formatString)) {
            if (isValidUnfDate){
                unfFormatString.append("'T'");
            } else {
                if (unfFormatString == null){
                    unfFormatString = new StringBuffer();
                }
            }
            unfFormatString.append("HH");
            if (hasTimeZone(formatString)) {
                timeZoneSpecified = true;
            }
            if (hasMinute(formatString)) {
                unfFormatString.append(":mm");
                if (hasSecond(formatString)) {
                    unfFormatString.append(":ss");
                    if (hasMillisecond(formatString)) {
                        unfFormatString.append(".SSS");
                    }
                }
            }
            if (timeZoneSpecified) {
                unfFormatString.append("'Z'");
            }
        }
    }

    private boolean hasYear(String formatString){
        return formatString.indexOf('y') > -1;
    }

    private boolean hasMonth(String formatString){
        return formatString.indexOf('M') > -1;
    }

    private boolean hasDay(String formatString){
        return formatString.indexOf('d') > -1;
    }

    private boolean hasHour(String formatString){
        return (formatString.indexOf('k') > -1) || (formatString.indexOf('h') > -1) || (formatString.indexOf('K') > -1) || (formatString.indexOf('H') > -1);
    }

    private boolean hasMinute(String formatString){
        return formatString.indexOf('m') > -1;
    }

    private boolean hasSecond(String formatString){
        return formatString.indexOf('s') > -1;
    }

    private boolean hasMillisecond(String formatString){
        return formatString.indexOf('S') > -1;
    }

    private boolean hasTimeZone(String formatString){
        return (formatString.indexOf('z') > -1) || (formatString.indexOf('Z') > -1);
    }

    private boolean hasEra(String formatString){
        return formatString.indexOf('G') > -1;
    }

    private boolean hasWeekInYear(String formatString){
        return formatString.indexOf('w') > -1;
    }

    private boolean hasDayInYear(String formatString){
        return formatString.indexOf('D') > -1;
    }

    private boolean hasDayOfWeekInMonth(String formatString){
        return formatString.indexOf('F') > -1;
    }

    private boolean hasDayOfWeek(String formatString){
        return formatString.indexOf('E') > -1;
    }

    /**
     * @return the unfFormatString
     */
    public StringBuffer getUnfFormatString() {
        return unfFormatString;
    }

    /**
     * @param unfFormatString the unfFormatString to set
     */
    public void setUnfFormatString(StringBuffer unfFormatString) {
        this.unfFormatString = unfFormatString;
    }

    /**
     * @return the timeZoneSpecified
     */
    public boolean isTimeZoneSpecified() {
        return timeZoneSpecified;
    }

    /**
     * @param timeZoneSpecified the timeZoneSpecified to set
     */
    public void setTimeZoneSpecified(boolean timeZoneSpecified) {
        this.timeZoneSpecified = timeZoneSpecified;
    }

}

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
 * Description: Unicodes characters 
 * @Author Elena Villalon
 *  email:evillalon@iq.harvard.edu
 *  
 */
package org.dataverse.unf;

public enum Ucnt {

    dot('\u002E'), //decimal separator "."
    plus('\u002b'),//"+" sign
    min('\u002d'),//"-"
    e('\u0065'), //"e"
    percntg('\u0025'),//"%"
    pndsgn('\u0023'), //"#"
    zero('\u0030'), //'0'
    s('\u0073'),//"s"
    nil('\u0000'), //'\0' for null terminator
    frmfeed('\u000C'), //form feed
    ls('\u2028'),//line separator
    nel('\u0085'),//next line
    psxendln('\n'); //posix end-of-line
    private final char ucode;

    Ucnt(char c) {
        this.ucode = c;
    }

    public char getUcode() {
        return ucode;
    }
}

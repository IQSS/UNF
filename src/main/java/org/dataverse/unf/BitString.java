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
public class BitString implements CharSequence{

    private String bits;

    public BitString(){
        
    }

    public BitString(String bitString){
        if (validate(bitString)){
            setBits(bitString);
        }
    }

    public BitString(Long l){
        setBits(Long.toBinaryString(l));
    }

    public void setBits(Long l){
        setBits(Long.toBinaryString(l));
    }

    private boolean validate(String bitstring){
        boolean ok = true;
        for (int i=0;i<bitstring.length(); i++){
            if (!(bitstring.charAt(i)=='0' || bitstring.charAt(i)=='1')){
                ok = false;
                break;
            }
        }
        return ok;
    }

    private void alignToByteBoundary(){
        if (bits != null){
            int padding = 8 - (bits.length() % 8);
            if (padding > 0){
                StringBuilder bitsBuilder = new StringBuilder(bits);
                for (int i = 0; i < padding; i++){
                    bitsBuilder.insert(0, '0');
                }
                bits = bitsBuilder.toString();
            }
        }
    }

    private void truncateLeadingEmptyBits(){
        bits = bits.substring(bits.indexOf('1'));
    }

    private void normalize(){
        truncateLeadingEmptyBits();
        alignToByteBoundary();
    }

    @Override
    public int length() {
        return bits.length();
    }

    @Override
    public char charAt(int index) {
        return bits.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return bits.subSequence(start, end);
    }

    /**
     * @return the bits
     */
    public String getBits() {
        return bits;
    }

    /**
     * @param bits the bits to set
     */
    public void setBits(String bits) {
        if (validate(bits)) {
            this.bits = bits;
            normalize();
        }
    }
}

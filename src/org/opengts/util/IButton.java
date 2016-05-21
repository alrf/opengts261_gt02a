// ----------------------------------------------------------------------------
// Copyright 2007-2016, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Change History:
//  2014/11/30  Martin D. Flynn
//     -Initial release
//  2015/05/03  Martin D. Flynn
//     -Support for handling IDs in reverse/reverse-byte order
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.lang.*;
import java.util.*;
import java.math.*;

/**
*** iButton container
**/

public class IButton
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** DisplayFormat Enumeration
    **/
    public enum DisplayFormat implements EnumTools.StringLocale, EnumTools.IntValue {
        DECIMAL    (  0, I18N.getString(IButton.class,"IButton.DisplayFormat.decimal.be","Decimal (Big-Endian)"   )),
        HEX64      (  1, I18N.getString(IButton.class,"IButton.DisplayFormat.hex64.be"  ,"Hex-64 (Big-Endian)"    )),
        HEX48      (  2, I18N.getString(IButton.class,"IButton.DisplayFormat.hex48.be"  ,"Hex-48 (Big-Endian)"    )),
        DECIMAL_LE ( 10, I18N.getString(IButton.class,"IButton.DisplayFormat.decimal.le","Decimal (Little-Endian)")),
        HEX64_LE   ( 11, I18N.getString(IButton.class,"IButton.DisplayFormat.hex64.le"  ,"Hex-64 (Little-Endian)" )),
        HEX48_LE   ( 12, I18N.getString(IButton.class,"IButton.DisplayFormat.hex48.le"  ,"Hex-48 (Little-Endian)" ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        DisplayFormat(int v, I18N.Text a)           { vv = v; aa = a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isFormate(int fmt)           { return this.getIntValue() == fmt; }
    };

    /**
    *** Gets the DisplayFormat enumeration value from the specified name
    **/
    public static DisplayFormat getDisplayFormatFromName(String stn, DisplayFormat dft)
    {
        if (!StringTools.isBlank(stn)) {
            // -- Big-Endian
            if (stn.equalsIgnoreCase("DECIMAL"   )) { return DisplayFormat.DECIMAL;    } 
            if (stn.equalsIgnoreCase("HEX"       )) { return DisplayFormat.HEX64;      } 
            if (stn.equalsIgnoreCase("HEX64"     )) { return DisplayFormat.HEX64;      } 
            if (stn.equalsIgnoreCase("HEX48"     )) { return DisplayFormat.HEX48;      }
            // -- Big-Endian
            if (stn.equalsIgnoreCase("DECIMAL_BE")) { return DisplayFormat.DECIMAL;    } 
            if (stn.equalsIgnoreCase("HEX_BE"    )) { return DisplayFormat.HEX64;      } 
            if (stn.equalsIgnoreCase("HEX64_BE"  )) { return DisplayFormat.HEX64;      } 
            if (stn.equalsIgnoreCase("HEX48_BE"  )) { return DisplayFormat.HEX48;      }
            // -- Little-Endian
            if (stn.equalsIgnoreCase("DECIMAL_LE")) { return DisplayFormat.DECIMAL_LE; } 
            if (stn.equalsIgnoreCase("HEX_LE"    )) { return DisplayFormat.HEX64_LE;   } 
            if (stn.equalsIgnoreCase("HEX64_LE"  )) { return DisplayFormat.HEX64_LE;   } 
            if (stn.equalsIgnoreCase("HEX48_LE"  )) { return DisplayFormat.HEX48_LE;   }
        }
        return dft;
    }

    /**
    *** Gets the String representation of the specified DisplayFormat.
    *** Returns "Unknown" id the specified DisplayFormat is null.
    **/
    public static String GetDisplayFormatDescription(DisplayFormat df, Locale locale)
    {
        if (df != null) {
            return df.toString(locale);
        } else {
            I18N i18n = I18N.getI18N(IButton.class, locale);
            return i18n.getString("IButton.DisplayFormat.unknown", "Unknown");
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static String ConvertToBigEndianID(String value)
    {
        String Vs = StringTools.trim(value);
        if (!StringTools.isHex(Vs,true)) {
            Print.logError("Invalid iButton-ID hex value: " + Vs);
            return value;
        } else
        if (Vs.length() != 16) {
            Print.logError("Invalid iButton-ID hex length: " + Vs);
            return value;
        } else {
            long LE = StringTools.parseHexLong(Vs,0L); // in Little-Endian format
            long BE = Payload.reverseByteOrder(LE,8); // convert to Big-Endian
            return StringTools.toHexString(BE,64);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* value stored in 64-bit Little-Endian format as provided by the device */
    private long valueLE = 0L; 

    /**
    *** Constructor.
    *** Values provided by iButton devices are in Little-Endian format.
    *** As such, these values are stored within this container in Little-Endian format.
    *** @param value  The 64-bit iButton value (in Little-Endian format)
    **/
    public IButton(long value)
    {
        // -- value assumed to be in Little-Endian format
        this(value, false/*Little-Endian*/);
    }

    /**
    *** Constructor.
    *** Values provided by iButton devices are in Little-Endian format.
    *** As such, these values are stored within this container in Little-Endian format.
    *** If the specified value will be in Big-Endian format, then the second parameter
    *** should specify "true" so that this specified value will be converted into
    *** the internal Little-Endian format.
    *** @param value      The 64-bit iButton value.
    *** @param bigEndian  True if the specified value is in Big-Endian format, false if Little-Endian.
    **/
    public IButton(long value, boolean bigEndian)
    {
        if (bigEndian) {
            // -- convert 64-bit Big-Endian to Little-Endian format
            this.valueLE = Payload.reverseByteOrder(value,8); // 64-bit
        } else {
            // -- save as provided in Little-Endian format
            this.valueLE = value;
        }
    }

    /**
    *** Constructor.
    *** IButton values are typically provided by the device as a Little-Endian 64-bit integer String.
    *** This value is printed on the physical iButton as a hex value with the LSB byte first.
    *** This constructor attempts to convert the specified Little-Endian hex String into a numeric value.
    *** The String is expected to be exactly 16 characters in length, containing the full iButton ID
    *** in Little-Endian format.  After instantiating this iButton instance, "<code>isValid()</code>"
    *** should be called to verify that this iButton value is valid.
    **/
    public IButton(String value)
    {
        String Vs = StringTools.trim(value);
        if (!StringTools.isHex(Vs,true)) {
            Print.logError("Invalid iButton-ID hex value: " + Vs);
            this.valueLE = 0L;
        } else
        if (Vs.length() != 16) {
            Print.logError("Invalid iButton-ID hex length: " + Vs);
            this.valueLE = 0L;
        } else {
            this.valueLE = StringTools.parseHexLong(Vs,0L); // Assume already in Little-Endian format
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this instance contains a valid iButton tag value.
    *** (hi-order byte must not be zero)
    **/
    public boolean isValid()
    {
        long V = this.getValue(); // Little-Endian
        return ((V & 0x00000000000000FFL) != 0L)? true : false; // LE hi-order byte
    }

    /**
    *** Gets the iButton tag value (Little-Endian format)
    **/
    public long getValue()
    {
        return this.valueLE;
    }

    /**
    *** Gets the iButton tag value
    *** @param bigEndian True to return as a Big-Endian value
    **/
    public long getValue(boolean bigEndian)
    {
        long V = this.getValue();
        if (bigEndian) {
            return Payload.reverseByteOrder(V,8);
        } else {
            return V;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Converts the iButton tag value to a 64-bit decimal String
    **/
    public String toDecimalString(boolean bigEndian) 
    {
        return String.valueOf(this.getValue(bigEndian));
    }

    /**
    *** Converts the iButton tag value to a 64-bit decimal String (Little-Endian format)
    **/
    public String toDecimalString() 
    {
        return this.toDecimalString(false);
    }

    // --------------------------------

    /**
    *** Converts the iButton tag value to a 64-bit hex String (Big-Endian format)
    *** (this format typically matches the hex value displayed on the iButton device)
    **/
    public String toHexString(boolean bigEndian) 
    {
        long BE = this.getValue(bigEndian);
        return StringTools.toHexString(BE,64).toUpperCase();
    }

    /**
    *** Converts the iButton tag value to a 64-bit hex String (Big-Endian format)
    *** (this format typically matches the hex value displayed on the iButton device)
    **/
    public String toHexString() 
    {
        return this.toHexString(true/*Big-Endian*/);
    }

    // --------------------------------

    /**
    *** Extract the middle 48-bits of the iButton tag value and returns it
    *** as a 48-bit hex String (Big-Endian format).
    **/
    public String toHexString48() 
    {
        return this.toHexString48(true);
    }

    /**
    *** Extract the middle 48-bits of the iButton tag value and returns it
    *** as a 48-bit hex String.
    **/
    public String toHexString48(boolean bigEndian) 
    {
        String H = this.toHexString(bigEndian);
        if (H.length() == 16) {
            // -- expected length, extract middle 48-bit hex
            return H.substring(2,14);
        } else {
            // -- should not occur
            return H; // return as-is
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the 64-bit hex String iButton tag value
    **/
    public String toString() 
    {
        return this.toHexString();
    }

    /**
    *** Returns the String representation of this iButton value in the specified
    *** DisplayFormat.  If the DisplayFormat is null, returns the 64-bit hex representation.
    **/
    public String toString(DisplayFormat df) 
    {
        if (df != null) {
            switch (df) {
                case DECIMAL : 
                    // -- decimal (Big-Endian) 
                    return this.toDecimalString();
                case HEX64   : // preferred
                    // -- 64-bit hex (converted to Big-Endian): "310000179D3A2A01"
                    return this.toHexString();
                case HEX48   : // not recommended
                    // -- 48-bit hex (converted to Big-Endian): "0000179D3A2A" (strip hi/lo bytes)
                    return this.toHexString48();
                case DECIMAL_LE : 
                    // -- decimal (Little-Endian) as provided by device
                    return this.toDecimalString(false);
                case HEX64_LE   : // not recommended
                    // -- 64-bit hex (Little-Endian): "012A3A9D17000031"
                    return this.toHexString(false);
                case HEX48_LE   : // not recommended
                    // -- 48-bit hex (Little-Endian): "A3A9D170000" (strip hi/lo bytes)
                    return this.toHexString48(false);
            }
        }
        return this.toHexString(); // default: 64-bit hex (Big-Endian)
    }

    /** 
    *** Converts the specified iButton value into a String value
    *** @param value The iButton value
    *** @param st    The display format
    *** @return The value converted to the specified output format, or null if the 
    ***         specified display format is null.
    **/
    public static String toString(long value, DisplayFormat st)
    {
        return (new IButton(value)).toString(st);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        IButton decBtn = new IButton(RTConfig.getLong(  "dec",0L));
        IButton strBtn = new IButton(RTConfig.getString("str",""));
        Print.sysPrintln("Decimal: " + decBtn.toString() + " : " + decBtn.getValue());
        Print.sysPrintln("String : " + strBtn.toString() + " : " + strBtn.getValue());
    }

}

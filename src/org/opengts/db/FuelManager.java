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
//  2011/04/01  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.tables.*;

public abstract class FuelManager
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Fuel Type

    // http://www.epa.gov/cleanenergy/energy-resources/refs.html
    // http://www.eia.gov/tools/faqs/faq.cfm?id=307&t=11
    /*
    public enum FuelType implements EnumTools.StringLocale, EnumTools.IntValue {
        DIESEL,         // 10.180kg per Gallon
        ETHENOL_20,     //  8.596kg per Gallon
        ETHENOL_100,    //  5.774kg per Gallon
        BIODIESEL_20,   // 10.006kg per Gallon
        BIODIESEL_100,  //  9.421kg per Gallon
        GASOLINE,       //  8.887kg per Gallon
        PROPANE,        //  0.00133333333333kg per pound
        NATURAL_GAS     //  5.302kg per Therm
    }
    */

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Fuel Level Change 

    public enum LevelChangeType implements EnumTools.StringLocale, EnumTools.IntValue {
        NONE        (  0, I18N.getString(FuelManager.class,"FuelManager.LevelChangeType.none"    ,"None"    )), // default
        INCREASE    (  1, I18N.getString(FuelManager.class,"FuelManager.LevelChangeType.increase","Increase")),
        DECREASE    (  2, I18N.getString(FuelManager.class,"FuelManager.LevelChangeType.decrease","Decrease")),
        UNKNOWN     ( 99, I18N.getString(FuelManager.class,"FuelManager.LevelChangeType.unknown" ,"Unknown" ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        LevelChangeType(int v, I18N.Text a)         { vv = v; aa = a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isType(int type)             { return this.getIntValue() == type; }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public abstract LevelChangeType insertFuelLevelChange(EventData event);

    // ------------------------------------------------------------------------

}

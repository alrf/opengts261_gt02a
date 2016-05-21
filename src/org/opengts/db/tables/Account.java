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
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/04/09  Martin D. Flynn
//     -Integrate DBException
//  2007/01/25  Martin D. Flynn
//     -Moved to "OpenGTS"
//     -Various new fields added
//  2007/02/28  Martin D. Flynn
//     -Default to GeocoderMode=GEOZONE when creating new accounts
//  2007/03/11  Martin D. Flynn
//     -Added 'FLD_distanceUnits' & 'FLD_temperatureUnits'
//  2007/03/25  Martin D. Flynn
//     -Moved to 'org.opengts.db.tables'
//  2007/05/20  Martin D. Flynn
//     -Added 'FLD_privateLabelName'
//  2007/06/13  Martin D. Flynn
//     -Added BLANK_PASSWORD to explicitly support blank passwords
//  2007/07/27  Martin D. Flynn
//     -Added ability to list a specific AccountID
//  2007/08/09  Martin D. Flynn
//     -Set 'accountExists' to true when creating a new account.
//  2007/09/16  Martin D. Flynn
//     -Added PrivateLabel access methods
//     -Integrated DBSelect
//     -Added "getGeocoderModeString" method
//  2007/11/28  Martin D. Flynn
//     -Added '-editall' command-line option to display all fields.
//  2007/12/13  Martin D. Flynn
//     -Added methods to allow customizing "Device", "Device Group", and "Entity"
//      titles (not yet fully implemented).
//  2008/02/04  Martin D. Flynn
//     -Added Volume/Economy conversion methods
//     -Added column 'FLD_volumeUnits'
//     -Custom "Device"/"Device Group"/"Entity" title now supported via AccountString
//  2008/02/11  Martin D. Flynn
//     -Added column 'FLD_autoAddDevices'
//     -Added AccountString support for ID_DEVICE_NEW_DESCRIPTION
//  2008/05/14  Martin D. Flynn
//     -Encorporated 'enum' types for various display units and other enumerated types.
//     -"acct.getGeocoderModeString()" has been replaced with "Account.getGeocoderMode(acct).toString()"
//  2008/05/20  Martin D. Flynn
//     -Displayed text for enumerated types can now be localized.
//  2008/06/20  Martin D. Flynn
//     -Added column 'FLD_economyUnits'
//  2008/09/19  Martin D. Flynn
//     -Added column 'FLD_defaultUser'
//  2009/01/01  Martin D. Flynn
//     -Added command-line option "-desc=" for setting the description when creating Account.
//  2009/01/28  Martin D. Flynn
//     -Unless overridden by PrivateLabel/Runtime configs, the default 'privateLabelName' field
//      value for new Accounts is BasicPrivateLabel.ALL_HOSTS ("*")
//  2009/04/02  Martin D. Flynn
//     -Added 'FLD_retainedEventAge'.
//     -Added check for invalid ID during Account '-list'
//  2010/01/29  Martin D. Flynn
//     -Added "PressureUnits"
//     -Added "hasDeviceLastNotifySince" method
//  2010/04/11  Martin D. Flynn
//     -Added FLD_pressureUnits
//  2010/07/18  Martin D. Flynn
//     -Added "KPG" Economy units
//  2011/01/28  Martin D. Flynn
//     -Added FLD_maximumDevices, FLD_isAccountManager, FLD_managerID
//  2011/03/08  Martin D. Flynn
//     -Added "getFieldValueString"
//  2011/05/13  Martin D. Flynn
//     -Added FLD_totalPingCount, FLD_maxPingCount
//  2011/10/03  Martin D. Flynn
//     -Added "getAddressTitles(...)"
//     -Added FLD_dcsPropertiesID
//  2011/12/06  Martin D. Flynn
//     -Updated "getFieldValueString" to also search for matching table fields.
//  2012/02/03  Martin D. Flynn
//     -Added EconomyUnits "L/100km"
//  2012/04/03  Martin D. Flynn
//     -Added "setCurrentUser"/"getCurrentUser"
//     -Renamed "getFieldValueString" to "getKeyFieldValue", and added title support.
//  2012/05/27  Martin D. Flynn
//     -Fixed NPE in "_getKeyFieldString"
//  2012/06/29  Martin D. Flynn
//     -Check for divide-by-zero in EconomyUnits, when units are "L/100km"
//  2012/08/01  Martin D. Flynn
//     -Added "-findEmail" command-line option
//  2012/03/01  Martin D. Flynn
//     -Changed "-confirm" to "-confirmDelete" for "deleteOldEvents".  Removed
//      "-confirm" from "countOldEvents".
//     -Added "-account=acct1,acct2,..." support for "deleteOldEvents"/"countOldEvents".
//     -Added expiration date/days account replacement variables. (KEY_EXPIREDATE)
//  2013/05/28  Martin D. Flynn
//     -Changed "-findEMail" option to make email address searches case-insentitive.
//  2013/08/06  Martin D. Flynn
//     -Added FLD_allowNotify
//     -Added "-setPassword=" command-line option.
//  2013/09/23  Martin D. Flynn
//     -Changed sample data "demo" event date from "2010/03/12" to "2013/08/26".
//  2014/03/03  Martin D. Flynn
//     -Added FLD_allowWebService
//  2014/09/16  Martin D. Flynn
//     -Limit upper value for "totalPingCount"/"maxPingCount" to 0xFFFF [2.5.7-B11]
//  2014/12/19  Martin D. Flynn
//     -Added support for global "alwaysAllowWebService" property settings. [2.5.8-B36]
//  2015/05/03  Martin D. Flynn
//     -Fixed "${gtsDiskUsage} to properly display free space.
// ----------------------------------------------------------------------------
package org.opengts.db.tables;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;
import java.security.*;

import org.opengts.util.*;
import org.opengts.dbtypes.*;
import org.opengts.dbtools.*;

import org.opengts.db.*;

public class Account
    extends AccountRecord<Account>
    implements UserInformation
{

    // ------------------------------------------------------------------------

    private static      int     NotifyEmailColumnLength         = -1; // FLD_notifyEmail

    // ------------------------------------------------------------------------

    /* optional columns */
    public static final String  OPTCOLS_AddressFieldInfo        = "startupInit.Account.AddressFieldInfo";
    public static final String  OPTCOLS_MapLegendFieldInfo      = "startupInit.Account.MapLegendFieldInfo";
    public static final String  OPTCOLS_AccountManagerInfo      = "startupInit.Account.AccountManagerInfo";
    public static final String  OPTCOLS_DataPushInfo            = "startupInit.Account.DataPushInfo";
    public static final String  OPTCOLS_ELogHOSInfo             = "startupInit.Account.ELogHOSInfo";

    // ------------------------------------------------------------------------

    /* common ACL keys (see also 'private.xml') */
    public static final String  ACL_CHANGE_PASSWORD             = "acl.admin.password";
    public static final String  ACL_CHANGE_ACCOUNT              = "acl.admin.account";
    public static final String  ACL_CHANGE_USER                 = "acl.admin.user";

    // ------------------------------------------------------------------------

    /* "Account" title (ie. "Account", "Company", etc) */
    public static String[] GetTitles(Locale loc) 
    {
        I18N i18n = I18N.getI18N(Account.class, loc);
        return new String[] {
            i18n.getString("Account.title.singular", "Account"),
            i18n.getString("Account.title.plural"  , "Accounts"),
        };
    }

    // ------------------------------------------------------------------------

    public static String SUPER_ACCOUNT_SEPARATOR    = ":";

    /* extract account display ID */
    public static String getAccountDisplayID(String accountID)
    {
        if (accountID != null) {
            int p = accountID.indexOf(SUPER_ACCOUNT_SEPARATOR);
            if (p >= 0) {
                return accountID.substring(p+1);
            }
        }
        return accountID;
    }

    // ------------------------------------------------------------------------
    // Demo account information
    // This is the section that specifies the date ranges for the sample data found
    // in the "sampleData" directory.
    // Default properties:
    //   DemoAccount.accountName=demo
    //   DemoAccount.deviceNames=demo,demo2
    //   DemoAccount.demo.dateRange=2015/05/27,2015/05/27
    //   DemoAccount.demo2.dateRange=2015/05/27,2015/05/27

    public  static String           PROP_DemoAccount_                   = "DemoAccount.";
    public  static String           PROP_DemoAccount_accountName        = PROP_DemoAccount_ + "accountName";
    public  static String           PROP_DemoAccount_deviceNames        = PROP_DemoAccount_ + "deviceNames";
    public  static String           _PROP_DemoAccount_device_dateRange  = "dateRange";

    private static String           DEFAULT_DEMO_ACCOUNT_ID             = "demo";
    private static String           DEFAULT_DEMO_DEVICE_IDS[]           = new String[] { "demo", "demo2" };
    private static String           DEFAULT_DEMO_DEVICE_DATE_RANGE[]    = new String[] { "2015/05/27", "2015/05/27" };
    // demo : 2015/05/27,15:07:01 to 2015/05/27,16:54:10
    // demo2: 2015/05/27,06:47:01 to 2015/05/27,13:36:42

    public static String GetDemoAccountID()
    { 
        String da = RTConfig.getString(PROP_DemoAccount_accountName, DEFAULT_DEMO_ACCOUNT_ID);
        //Print.logInfo("Demo Account: " + da);
        return da;
    }
    
    public static boolean IsDemoAccount(String accountID)
    {
        if (!StringTools.isBlank(accountID)) {
            return accountID.equals(Account.GetDemoAccountID());
        } else {
            return false;
        }
    }

    public static String[] GetDemoAccountDeviceIDs()
    { 
        String dd[] = RTConfig.getStringArray(PROP_DemoAccount_deviceNames, DEFAULT_DEMO_DEVICE_IDS);
        Print.logInfo("Demo Devices: " + StringTools.join(dd,"|"));
        return dd;
    }
    
    public static boolean IsDemoDevice(String accountID, String deviceID)
    {
        if (Account.IsDemoAccount(accountID) && !StringTools.isBlank(deviceID)) {
            String did[] = Account.GetDemoAccountDeviceIDs();
            if (ListTools.size(did) > 0) {
                for (int i = 0; i < did.length; i++) {
                    if (deviceID.equals(did[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String[] GetDemoDeviceDateRange(String accountID, String deviceID)
    { 
        if (Account.IsDemoDevice(accountID,deviceID)) {
            String key = PROP_DemoAccount_ + deviceID + _PROP_DemoAccount_device_dateRange;
            String dr[] = RTConfig.getStringArray(key, DEFAULT_DEMO_DEVICE_DATE_RANGE);
            Print.logInfo("Demo Device: " + accountID + "/" + deviceID + " Date Rage " + StringTools.join(dr,","));
            return dr;
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // Default SMS enabled state

    public enum SMSDefaultState {
        FALSE,
        TRUE,
        ACCOUNT
    };

    public static SMSDefaultState GetDefaultSmsEnabledState()
    {
        String smsState = StringTools.trim(RTConfig.getString(DBConfig.PROP_Account_smsEnabled,""));
        if (smsState.equalsIgnoreCase("account")) {
            return SMSDefaultState.ACCOUNT;
        } else
        if (StringTools.isBoolean(smsState,false)) {
            return StringTools.parseBoolean(smsState,false)? 
                SMSDefaultState.TRUE : 
                SMSDefaultState.FALSE;
        } else {
            return SMSDefaultState.ACCOUNT;
        }
    }
    
    public static boolean IsFixedSmsEnabledState()
    {
        switch (Account.GetDefaultSmsEnabledState()) {
            case FALSE:
            case TRUE:
                return true;
            default:
                return false;
        }
    }

    // ------------------------------------------------------------------------
    // Temporary account attributes

    public  static final long       DFT_EXPIRATION_SEC      = DateTime.DaySeconds(7);
    public  static final long       MAX_EXPIRATION_SEC      = DateTime.DaySeconds(60);
    public  static final long       MAX_UNCONFIRMED_SEC     = DateTime.HourSeconds(12);
    private static Object           TempAccountLock         = new Object();

    // ------------------------------------------------------------------------
    // Password attributes

    public static final int         TEMP_PASSWORD_LENGTH    = 8;
    public static final String      BLANK_PASSWORD          = "*blank*";

    private static PasswordHandler  passwordHandler         = null;

    public static void setDefaultPasswordHandler(PasswordHandler ph)
    {
        Account.passwordHandler = ph;
    }

    public static PasswordHandler getDefaultPasswordHandler()
    {
        if (Account.passwordHandler == null) {
            Account.passwordHandler = new GeneralPasswordHandler();
        }
        return Account.passwordHandler;
    }

    public static PasswordHandler getPasswordHandler(BasicPrivateLabel bpl)
    {
        if (bpl != null) {
            return bpl.getPasswordHandler();
        } else {
            return Account.getDefaultPasswordHandler();
        }
    }

    // ------------------------------------------------------------------------
    // Timezone

    /**
    *** Gets the default Account timezone
    **/
    public static String GetDefaultTimeZone()
    {
        String tmz = StringTools.trim(RTConfig.getString(DBConfig.PROP_Account_defaultTimeZone,""));
        return !StringTools.isBlank(tmz)? tmz : DateTime.GMT_TIMEZONE;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Account type [FLD_accountType]

    public enum AccountType implements EnumTools.StringLocale, EnumTools.IntValue {
        TYPE_000    (  0, I18N.getString(Account.class,"Account.type.type000"  ,"Type000"  )), // default
        TYPE_001    (  1, I18N.getString(Account.class,"Account.type.type001"  ,"Type001"  )),
        TYPE_002    (  2, I18N.getString(Account.class,"Account.type.type002"  ,"Type002"  )),
        TYPE_003    (  3, I18N.getString(Account.class,"Account.type.type003"  ,"Type003"  )),
        TYPE_004    (  4, I18N.getString(Account.class,"Account.type.type004"  ,"Type004"  )),
        TYPE_005    (  5, I18N.getString(Account.class,"Account.type.type005"  ,"Type005"  )),
        TYPE_006    (  6, I18N.getString(Account.class,"Account.type.type006"  ,"Type006"  )),
        TYPE_007    (  7, I18N.getString(Account.class,"Account.type.type007"  ,"Type007"  )),
        TYPE_008    (  8, I18N.getString(Account.class,"Account.type.type008"  ,"Type008"  )),
        TYPE_009    (  9, I18N.getString(Account.class,"Account.type.type009"  ,"Type009"  )),
        TYPE_010    ( 10, I18N.getString(Account.class,"Account.type.type010"  ,"Type010"  )),
        TYPE_011    ( 11, I18N.getString(Account.class,"Account.type.type011"  ,"Type011"  )),
        TYPE_020    ( 20, I18N.getString(Account.class,"Account.type.type020"  ,"Type020"  )),
        TYPE_021    ( 21, I18N.getString(Account.class,"Account.type.type021"  ,"Type021"  )),
        TYPE_030    ( 30, I18N.getString(Account.class,"Account.type.type030"  ,"Type030"  )),
        TYPE_031    ( 31, I18N.getString(Account.class,"Account.type.type031"  ,"Type031"  )),
        TEMPORARY   (900, I18N.getString(Account.class,"Account.type.temporary","Temporary")),
        DEMO        (920, I18N.getString(Account.class,"Account.type.demo"     ,"Demo"     )),
        SYSTEM      (999, I18N.getString(Account.class,"Account.type.system"   ,"System"   ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        AccountType(int v, I18N.Text a)             { vv = v; aa = a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isDefault()                  { return this.equals(TYPE_000); }
        public boolean isTemporary()                { return this.equals(TEMPORARY); }
        public boolean isSystem()                   { return this.equals(SYSTEM); }
        public boolean isType(int type)             { return this.getIntValue() == type; }
    };

    /**
    *** Returns the defined AccountType for the specified account.
    *** @param a  The account from which the AccountType will be obtained.  
    ***           If null, the default AccountType will be returned.
    *** @return The AccountType
    **/
    public static AccountType getAccountType(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(AccountType.class,a.getAccountType()) : 
            EnumTools.getDefault(AccountType.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Geocode modes: (RG = "Reverse-Geocode) [FLD_geocoderMode]

    public enum GeocoderMode implements EnumTools.StringLocale, EnumTools.IntValue {
        NONE        (0, I18N.getString(Account.class,"Account.geocoder.none"   ,"none"   )),
        GEOZONE     (1, I18N.getString(Account.class,"Account.geocoder.geozone","geozone")),
        PARTIAL     (2, I18N.getString(Account.class,"Account.geocoder.partial","partial")),
        FULL        (3, I18N.getString(Account.class,"Account.geocoder.full"   ,"full"   ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        GeocoderMode(int v, I18N.Text a)       { vv = v; aa = a; }
        public int     getIntValue()           { return vv; }
        public String  toString()              { return aa.toString(); }
        public String  toString(Locale loc)    { return aa.toString(loc); }
        public boolean isNone()                { return this.equals(NONE); } 
        public boolean isGeozone()             { return this.equals(GEOZONE); } 
      //public boolean okGeozone()             { return (this.getIntValue() >= 1); }
        public boolean isPartial()             { return this.equals(PARTIAL); } 
      //public boolean okPartial()             { return (this.getIntValue() >= 2); }
        public boolean isFull()                { return this.equals(FULL); } 
      //public boolean okFull()                { return (this.getIntValue() >= 3); }
        public boolean equals(GeocoderMode gm) { return ((gm != null) && (this.getIntValue() == gm.getIntValue())); }
    };

    /**
    *** Returns the defined GeocoderMode for the specified account.
    *** @param a  The account from which the GeocoderMode will be obtained.  
    ***           If null, the default GeocoderMode will be returned.
    *** @return The GeocoderMode
    **/
    public static GeocoderMode getGeocoderMode(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(GeocoderMode.class,a.getGeocoderMode()) : 
            EnumTools.getDefault(GeocoderMode.class);
    }

    /**
    *** Gets the GeocoderMode enum value for the specified name
    *** @param code The name of the GeocoderMode (one of "none", "geozone", "partial", "full")
    *** @return The GeocoderMode, or GeocoderMode.NONE if the name is invalid
    **/
    public static GeocoderMode getGeocoderMode(String code)
    {
        return Account.getGeocoderMode(code, GeocoderMode.NONE);
    }

    /**
    *** Gets the GeocoderMode enum value for the specified name
    *** @param code The name of the GeocoderMode (one of "none", "geozone", "partial", "full")
    *** @param dft  The default GeocoderMode if the specified name is invalid.
    *** @return The GeocoderMode, or the specified default if the name is invalid
    **/
    public static GeocoderMode getGeocoderMode(String code, GeocoderMode dft)
    {
        return EnumTools.getValueOf(GeocoderMode.class, code, dft);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Speed units & conversion [FLD_speedUnits]

    public enum SpeedUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        MPH         ( 0, I18N.getString(Account.class,"Account.speed.mph"     ,"mph"  ), GeoPoint.MILES_PER_KILOMETER           ),
        KPH         ( 1, I18N.getString(Account.class,"Account.speed.kph"     ,"km/h" ), 1.0                                    ),
        KNOTS       ( 2, I18N.getString(Account.class,"Account.speed.knots"   ,"knots"), GeoPoint.NAUTICAL_MILES_PER_KILOMETER  );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        SpeedUnits(int v, I18N.Text a, double m)    { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; }
        public double  convertFromKPH(double v)     { return v * mm; } // ie. MPH: km/h * mi/km = mi/h
        public double  convertToKPH(double v)       { return v / mm; }
    };

    /**
    *** Returns the defined SpeedUnits for the specified account.
    *** @param a  The account from which the SpeedUnits will be obtained.  
    ***           If null, the default SpeedUnits will be returned.
    *** @return The SpeedUnits
    **/
    public static SpeedUnits getSpeedUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(SpeedUnits.class,a.getSpeedUnits()) : 
            EnumTools.getDefault(SpeedUnits.class);
    }

    /**
    *** Returns the defined SpeedUnits for the specified user.
    *** @param u  The user from which the SpeedUnits will be obtained.  
    ***           If null, the default SpeedUnits will be returned.
    *** @return The SpeedUnits
    **/
    public static SpeedUnits getSpeedUnits(User u)
    {
        return (u != null)? 
            EnumTools.getValueOf(SpeedUnits.class,u.getSpeedUnits()) : 
            EnumTools.getDefault(SpeedUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Distance units & conversion [FLD_distanceUnits]

    public enum DistanceUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        MILES       (0, I18N.getString(Account.class,"Account.distance.miles","Miles"), GeoPoint.MILES_PER_KILOMETER           ),
        KM          (1, I18N.getString(Account.class,"Account.distance.km"   ,"Km"   ), 1.0                                    ),
        NM          (2, I18N.getString(Account.class,"Account.distance.nm"   ,"Nm"   ), GeoPoint.NAUTICAL_MILES_PER_KILOMETER  );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        DistanceUnits(int v, I18N.Text a, double m) { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; } // km to unit
        public boolean isKM()                       { return this.equals(KM); }
        public boolean isMiles()                    { return this.equals(MILES); }
        public boolean isKnots()                    { return this.equals(NM); }
        public double  convertFromKM(double v)      { return v * mm; }   // MILES: km * mi/km = mi
        public double  convertToKM(double v)        { return v / mm; }
    };

    /**
    *** Returns the defined DistanceUnits for the specified account.
    *** @param a  The account from which the DistanceUnits will be obtained.  
    ***           If null, the default DistanceUnits will be returned.
    *** @return The DistanceUnits
    **/
    public static DistanceUnits getDistanceUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(DistanceUnits.class,a.getDistanceUnits()) : 
            EnumTools.getDefault(DistanceUnits.class);
    }

    /**
    *** Returns the defined DistanceUnits for the specified user.
    *** @param u  The user from which the DistanceUnits will be obtained.  
    ***           If null, the default DistanceUnits will be returned.
    *** @return The DistanceUnits
    **/
    public static DistanceUnits getDistanceUnits(User u)
    {
        return (u != null)? 
            EnumTools.getValueOf(DistanceUnits.class,u.getDistanceUnits()) : 
            EnumTools.getDefault(DistanceUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Altitude units 

    public enum AltitudeUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        FEET        (0, I18N.getString(Account.class,"Account.altitude.feet"  ,"feet"  ), GeoPoint.FEET_PER_METER      ),
        METERS      (1, I18N.getString(Account.class,"Account.altitude.meters","meters"), 1.0                          ),
        MILES       (2, I18N.getString(Account.class,"Account.altitude.miles" ,"miles" ), GeoPoint.MILES_PER_METER     ),
        KM          (3, I18N.getString(Account.class,"Account.altitude.km"    ,"km"    ), GeoPoint.KILOMETERS_PER_METER),
        CUBITS      (4, I18N.getString(Account.class,"Account.altitude.cubits","cubits"), 2.187226597                  );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        AltitudeUnits(int v, I18N.Text a, double m) { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; }
        public boolean isMeters()                   { return this.equals(METERS); }
        public boolean isFeet()                     { return this.equals(FEET); }
        public double  convertFromMeters(double v)  { return v * mm; }   // FEET: m * m/ft = ft
        public double  convertToMeters(double v)    { return v / mm; }
    };

    /**
    *** Returns the defined AltitudeUnits for the specified account.
    *** @param a  The account from which the AltitudeUnits will be obtained.  
    ***           If null, the default AltitudeUnits will be returned.
    *** @return The AltitudeUnits
    **/
    public static AltitudeUnits getAltitudeUnits(Account a)
    {
        DistanceUnits distUnits = Account.getDistanceUnits(a);
        return (distUnits.isMiles() || distUnits.isKnots())?
            AltitudeUnits.FEET :
            AltitudeUnits.METERS;
    }

    /**
    *** Returns the defined AltitudeUnits for the specified user.
    *** @param u  The user from which the AltitudeUnits will be obtained.  
    ***           If null, the default AltitudeUnits will be returned.
    *** @return The AltitudeUnits
    **/
    public static AltitudeUnits getAltitudeUnits(User u)
    {
        DistanceUnits distUnits = Account.getDistanceUnits(u);
        return (distUnits.isMiles() || distUnits.isKnots())?
            AltitudeUnits.FEET :
            AltitudeUnits.METERS;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Temeprature units & conversion [FLD_temperatureUnits]

    public enum TemperatureUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        F (0, I18N.getString(Account.class,"Account.temperature.f","F")),  // Fahrenheit
        C (1, I18N.getString(Account.class,"Account.temperature.c","C"));  // Celsius (default)
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        TemperatureUnits(int v, I18N.Text a) { vv=v; aa=a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isC()                        { return this.equals(C); }
        public boolean isF()                        { return this.equals(F); }
        public double  convertFromC(double c)       { return this.isF()? ((c * 9.0 / 5.0) + 32.0) : c; }
        public double  convertToC(double c)         { return this.isF()? ((c - 32.0) * 5.0 / 9.0) : c; }
    };

    /**
    *** Returns the defined TemperatureUnits for the specified account.
    *** @param a  The account from which the TemperatureUnits will be obtained.  
    ***           If null, the default TemperatureUnits will be returned.
    *** @return The TemperatureUnits
    **/
    public static TemperatureUnits getTemperatureUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(TemperatureUnits.class,a.getTemperatureUnits()) : 
            EnumTools.getDefault(TemperatureUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Volume units & conversion [FLD_volumeUnits]

    public static final double  LITERS_PER_US_GALLON    = 3.785411784;
    public static final double  US_GALLONS_PER_LITER    = 1.0 / LITERS_PER_US_GALLON; // 0.264172052
    public static final double  LITERS_PER_UK_GALLON    = 4.546090; // Weights and Measures Act of 1985 (Imperial Gallon)
    public static final double  UK_GALLONS_PER_LITER    = 1.0 / LITERS_PER_UK_GALLON; // 0.2199692483
    public static final double  CUBIC_FEET_PER_LITER    = 0.0353146667;

    // ----------

    // Base Unit: Liters
    public enum VolumeUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        US_GALLONS  (0, I18N.getString(Account.class,"Account.volume.usgal"  ,"gal"  ), Account.US_GALLONS_PER_LITER  ),
        LITERS      (1, I18N.getString(Account.class,"Account.volume.liter"  ,"Liter"), 1.0                           ),  // default
        UK_GALLONS  (2, I18N.getString(Account.class,"Account.volume.ukgal"  ,"IG"   ), Account.UK_GALLONS_PER_LITER  ),
        CUBIC_FEET  (3, I18N.getString(Account.class,"Account.volume.cubicFt","ft^3" ), Account.CUBIC_FEET_PER_LITER  );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        VolumeUnits(int v, I18N.Text a, double m)   { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; }
        public double  convertFromLiters(double v)  { return v * mm; } // ie. US_GALLONS: L * g/L = g
        public double  convertToLiters(double v)    { return v / mm; }
        public boolean isUSGallons()                { return this.equals(US_GALLONS); }
    };

    /**
    *** Returns the defined VolumeUnits for the specified account.
    *** @param a  The account from which the VolumeUnits will be obtained.  
    ***           If null, the default VolumeUnits will be returned.
    *** @return The VolumeUnits
    **/
    public static VolumeUnits getVolumeUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(VolumeUnits.class,a.getVolumeUnits()) : 
            EnumTools.getDefault(VolumeUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Currency [ISO-4217] 

    public enum Currency implements EnumTools.StringLocale {
        USD/*"usd"*/("$"  ,"Dollar","USA"               ),
        AUD/*"aud"*/("$"  ,"Dollar","Australia"         ),
        NZD/*"nzd"*/("$"  ,"Dollar","New Zealand"       ),
        CAD/*"cad"*/("$"  ,"Dollar","Canada"            ),
        GBP/*"gbp"*/("?"  ,"Pound" ,"United Kingdom"    ), // 
        DOP/*"dop"*/("RD$","Peso"  ,"Dominican Republic"),
        EUR/*"eur"*/("?"  ,"Euro"  ,"Europe"            ), // France/Germany/Ireland/Italy/Luxembourg/Spain
        INR/*"inr"*/("?"  ,"Rupee" ,"India"             ),
        MXN/*"mxn"*/("$"  ,"Peso"  ,"Mexico"            ),
        RUB/*"rub"*/("R"  ,"Ruble" ,"Russia"            ),
        SAR/*"sar"*/("SR" ,"Riyal" ,"Saudi Arabia"      ),
        NAD/*"nad"*/("$"  ,"Dollar","Namibia"           );
        // ---
        private String      ss = null;
        private String      dd = null;
        private String      cc = null;
        Currency(String s, String d, String c)  { ss=s; dd=d; cc=c; }
        public String  getCode()                { return this.name().toLowerCase(); }
        public boolean hasSymbol()              { return (!StringTools.isBlank(ss) && !ss.equals("?")); }
        public String  getSymbol()              { return ss; }
        public String  getDescription()         { return dd; }
        public String  getCountry()             { return cc; }
        public String  toString()               { return this.getCode(); }
        public String  toString(Locale loc)     { return this.getCode(); }
    };

    public static Currency GetCurrency(String code)
    {
        return EnumTools.getValueOf(Currency.class, code, Currency.USD);
    }

    // --------------------------------

    /**
    *** Returns the default currency
    *** @return The default currency
    **/
    public static String GetDefaultCurrency()
    {
        String curr = RTConfig.getString(DBConfig.PROP_Account_defaultCurrency, null);
        if (!StringTools.isBlank(curr)) {
            return curr;
        } else {
            return Account.Currency.USD.getCode();
        }
    }

    /**
    *** Returns the currency units for the specified account
    *** @param a  The account for which the currency units is returned
    *** @return The currency units for the specified account
    **/
    public static String getCurrency(Account a)
    {
        if (a != null) {
            return a.getCurrencyUnits();
        } else {
            return Account.GetDefaultCurrency();
        }
    }

    // --------------------------------

    /**
    *** Returns the default currency symbol
    *** @return The default currency symbol
    **/
    public static String GetDefaultCurrencySymbol()
    {

        /* symbol from property */
        String dftSym = RTConfig.getString(DBConfig.PROP_Account_defaultCurrencySymbol, null);
        if (!StringTools.isBlank(dftSym)) {
            return dftSym;
        }

        /* from default currency */
        String code = Account.GetDefaultCurrency();
        Currency curr = Account.GetCurrency(code);
        String sym = ((curr != null) && curr.hasSymbol())? curr.getSymbol() : null;
        if (!StringTools.isBlank(sym)) {
            return sym;
        } else {
            return code;
        }

    }

    /**
    *** Returns the currency symbol for the specified account
    *** @param a The account for which the currency symbol is returned
    *** @return The currency symbol for the specified account
    **/
    public static String getCurrencySymbol(Account a)
    {
        return Account.GetDefaultCurrencySymbol();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Economy units & conversion [FLD_economyUnits]
    // mpg     = km/L * mi/km * L/g
    // km/L    = (L/100km) * 100((km/L)^2)
    // 100km/L = (km/L) * (1/100)
    // km/L    = 100km/L / (1/100)
    // L/100km = 1 / ((km/L) * (1/100)) = 100 / (km/L)
    // km/L    = (1 / (L/100km)) / (1/100)) = 100 / (L/100km)

    public enum EconomyUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        MPG     (0, I18N.getString(Account.class,"Account.economy.mpg"    ,"mpg"    ), GeoPoint.MILES_PER_KILOMETER * Account.LITERS_PER_US_GALLON,false),
        KPL     (1, I18N.getString(Account.class,"Account.economy.kpl"    ,"km/L"   ), 1.0                                                        ,false),
        KPG     (2, I18N.getString(Account.class,"Account.economy.kpg"    ,"kpg"    ), Account.LITERS_PER_US_GALLON                               ,false),
        LP100KM (3, I18N.getString(Account.class,"Account.economy.lp100km","L/100km"), 1.0 / 100.0                                                ,true );
        // ---
        private int         vv = 0;     // index
        private I18N.Text   aa = null;  // description
        private double      mm = 1.0;   // conversion
        private boolean     ii = false; // L/100km (inverse)
        EconomyUnits(int v, I18N.Text a, double m, boolean i)  { vv=v; aa=a; mm=m; ii=i; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
      //public double  getMultiplier()              { return mm; }
        public double  convertFromKPL(double v)     { return (v == 0.0)? 0.0 : (ii? (1.0 / (v * mm)) : (v * mm)); }
        public double  convertToKPL(double v)       { return (v == 0.0)? 0.0 : (ii? ((1.0 / v) / mm) : (v / mm)); }
    };

    /**
    *** Returns the defined EconomyUnits for the specified account.
    *** @param a  The account from which the EconomyUnits will be obtained.  
    ***           If null, the default EconomyUnits will be returned.
    *** @return The EconomyUnits
    **/
    public static EconomyUnits getEconomyUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(EconomyUnits.class,a.getEconomyUnits()) : 
            EnumTools.getDefault(EconomyUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Pressure units & conversion [FLD_oilPressure]
    // http://www.unitconversion.org/unit_converter/pressure-ex.html

    public static final double  PA_PER_KPA          = 1000.0;                   // Pascals per KiloPascals
    public static final double  PSF_PER_KPA         = 20.885434233;             // Lbs per sq ft
    public static final double  PSI_PER_KPA         = 0.14503773773020923;      // Lbs per sq in
    public static final double  TORR_PER_KPA        = 7.500616827;  // 7.5028   // Torr
    public static final double  MMHG_PER_KPA        = 7.500637554;              // mm mercury
    public static final double  ATM_PER_KPA         = 0.009869233;              // Standard Atmosphere
    public static final double  AT_PER_KPA          = 0.010197162;              // Technical Atmosphere
    public static final double  BAR_PER_PA          = 0.00001;
    public static final double  BAR_PER_KPA         = BAR_PER_PA * PA_PER_KPA;
    public static final double  KPA_PER_BAR         = 1.0 / BAR_PER_KPA;
    public static final double  KPA_PER_PSI         = 1.0 / PSI_PER_KPA;

    public enum PressureUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        KPA         (0, I18N.getString(Account.class,"Account.pressure.kPa" ,"kPa"  ), 1.0                 ),
        PSI         (1, I18N.getString(Account.class,"Account.pressure.psi" ,"psi"  ), Account.PSI_PER_KPA ),
        MMHG        (2, I18N.getString(Account.class,"Account.pressure.mmHg","mmHg" ), Account.MMHG_PER_KPA),
        BAR         (3, I18N.getString(Account.class,"Account.pressure.bar" ,"bar"  ), Account.BAR_PER_KPA );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        PressureUnits(int v, I18N.Text a, double m) { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; }
        public double  convertFromKPa(double v)     { return v * mm; }
        public double  convertToKPa(double v)       { return v / mm; }
    };

    /**
    *** Returns the defined PressureUnits for the specified account.
    *** @param a  The account from which the PressureUnits will be obtained.  
    ***           If null, the default PressureUnits will be returned.
    *** @return The PressureUnits
    **/
    public static PressureUnits getPressureUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(PressureUnits.class,a.getPressureUnits()) : 
            EnumTools.getDefault(PressureUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Mass/Weight units & conversion [FLD_massUnits]

    public static final double  LBS_PER_KG          = 2.20462262185;        // Pounds per Kilogram
    public static final double  KG_PER_LBS          = 1.0 / LBS_PER_KG;     // Kilograms per Pound [0.45359236999975]

    public enum MassUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        KG          (0, I18N.getString(Account.class,"Account.mass.kg" ,"kg"  ), 1.0                ),
        LB          (1, I18N.getString(Account.class,"Account.mass.lb" ,"lb"  ), Account.LBS_PER_KG );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        MassUnits(int v, I18N.Text a, double m) { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; }
        public double  convertFromKG(double v)      { return v * mm; }
        public double  convertToKG(double v)        { return v / mm; }
    };

    /**
    *** Returns the defined MassUnits for the specified account.
    *** @param a  The account from which the MassUnits will be obtained.  
    ***           If null, the default MassUnits will be returned.
    *** @return The MassUnits
    **/
    public static MassUnits getMassUnits(Account a)
    {
        //return (a != null)? 
        //    EnumTools.getValueOf(MassUnits.class,a.getMassUnits()) : 
        //    EnumTools.getDefault(MassUnits.class);
        if (a != null) {
            VolumeUnits vu = Account.getVolumeUnits(a);
            MassUnits massUnits = vu.isUSGallons()?  MassUnits.LB : MassUnits.KG;
            return EnumTools.getValueOf(MassUnits.class, massUnits);
        } else {
            return EnumTools.getDefault(MassUnits.class);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Force/Acceleration units & conversion

    public static final double METERS_PER_SEC_SQ_PER_G      = Accelerometer.METERS_PER_SEC_SQ_PER_G;
    public static final double MPSS_PER_G_FORCE             = Accelerometer.METERS_PER_SEC_SQ_PER_G;
    public static final double G_PER_MPSS_FORCE             = Accelerometer.G_PER_MPSS_FORCE;
    public static final double MPH_PER_SEC_PER_MPSS         = Accelerometer.MPH_PER_SEC_PER_MPSS;
    public static final double MPH_PER_SEC_PER_G            = Accelerometer.MPH_PER_SEC_PER_G;

    public enum ForceUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        MPSS     ( 0, I18N.getString(Account.class,"Account.force.metersPerSecSquared","m/ss" ), 1.0                  ),
        CMPSS    ( 1, I18N.getString(Account.class,"Account.force.centimPerSecSquared","cm/ss"), 100.0                ),
        G        ( 5, I18N.getString(Account.class,"Account.force.gForce"             ,"G"    ), G_PER_MPSS_FORCE     ),
        MPHPS    (10, I18N.getString(Account.class,"Account.force.milesPerHourPerSec" ,"mph/s"), MPH_PER_SEC_PER_MPSS );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        ForceUnits(int v, I18N.Text a, double m)         { vv=v; aa=a; mm=m; }
        public int     getIntValue()                     { return vv; }
        public String  toString()                        { return aa.toString(); }
        public String  toString(Locale loc)              { return aa.toString(loc); }
        public double  getMultiplier()                   { return mm; }
        public double  convertFromMetersPerSS(double v)  { return v * mm; }
        public double  convertToMetersPerSS(double v)    { return v / mm; }
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Area units & conversion

    public static final double SQUARE_METERS_PER_KILOMETER  = 1000000.0;
    public static final double SQUARE_KILOMETERS_PER_METER  = 1.0 / SQUARE_METERS_PER_KILOMETER; // 0.000001
    public static final double SQUARE_METERS_PER_MILE       = 2589988.110336;
    public static final double SQUARE_MILES_PER_METER       = 1.0 / SQUARE_METERS_PER_MILE;      // 0.000000386102159
    public static final double SQUARE_METERS_PER_ACRE       = 4046.8564224;
    public static final double ACRES_PER_SQUARE_METER       = 1.0 / SQUARE_METERS_PER_ACRE;      // 0.000247105381467
    public static final double SQUARE_METERS_PER_FOOT       = 0.09290304;
    public static final double SQUARE_FEET_PER_METER        = 1.0 / SQUARE_METERS_PER_FOOT;      // 10.763910416709722

    public enum AreaUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        SQUARE_METERS ( 0, I18N.getString(Account.class,"Account.area.squareMeters","Sq.m" ), 1.0                    ),
        SQUARE_FEET   ( 1, I18N.getString(Account.class,"Account.area.squareFeet"  ,"Sq.ft"), SQUARE_FEET_PER_METER  ),
        SQUARE_MILES  (10, I18N.getString(Account.class,"Account.area.squareMiles" ,"Sq.mi"), SQUARE_MILES_PER_METER ),
        ACRES         (20, I18N.getString(Account.class,"Account.area.acres"       ,"Acres"), ACRES_PER_SQUARE_METER );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        AreaUnits(int v, I18N.Text a, double m)          { vv=v; aa=a; mm=m; }
        public int     getIntValue()                     { return vv; }
        public String  toString()                        { return aa.toString(); }
        public String  toString(Locale loc)              { return aa.toString(loc); }
        public double  getMultiplier()                   { return mm; }
        public double  convertFromSquareMeters(double v) { return v * mm; }
        public double  convertToSquareMeters(double v)   { return v / mm; }
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Latitude/Longitude display format [FLD_latLonFormat]

    public enum LatLonFormat implements EnumTools.StringLocale, EnumTools.IntValue {
        DEG         (0, I18N.getString(Account.class,"Account.latlon.degrees","Degrees"    )),
        DMS         (1, I18N.getString(Account.class,"Account.latlon.dms"    ,"Deg:Min:Sec")),
        DM          (2, I18N.getString(Account.class,"Account.latlon.dm"     ,"Deg:Min"    ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        LatLonFormat(int v, I18N.Text a)            { vv=v; aa=a; }
        public int     getIntValue()                { return vv; }
        public boolean isDegrees()                  { return (vv == DEG.getIntValue()); }
        public boolean isDegMinSec()                { return (vv == DMS.getIntValue()); }
        public boolean isDegMin()                   { return (vv == DM.getIntValue());  }
        public String  getFormatType()              { return this.isDegMinSec()?GeoPoint.SFORMAT_DMS:this.isDegMin()?GeoPoint.SFORMAT_DM:GeoPoint.SFORMAT_DEC_5; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        //public String  formatLatitude( double v, Locale loc) { return GeoPoint.formatLatitude( v, this.getFormatType(), loc); }
        //public String  formatLongitude(double v, Locale loc) { return GeoPoint.formatLongitude(v, this.getFormatType(), loc); }
    };

    /**
    *** Returns the defined LatLonFormat for the specified account.
    *** @param a  The account from which the LatLonFormat will be obtained.  
    ***           If null, the default LatLonFormat will be returned.
    *** @return The LatLonFormat
    **/
    public static LatLonFormat getLatLonFormat(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(LatLonFormat.class,a.getLatLonFormat()) : 
            EnumTools.getDefault(LatLonFormat.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Global Account specification
    // - Used for Device auto-add

    /**
    *** Gets the global AccountID to which new devices will be automatically added.
    **/
    public static String getGlobalAccountID()
    {
        return RTConfig.getString(DBConfig.PROP_Account_globalAccountID,null);
    }

    /**
    *** Gets the global Account to which new devices will be automatically added.
    **/
    public static Account getGlobalAccount()
    {
        String acctID = Account.getGlobalAccountID();
        if (!StringTools.isBlank(acctID)) {
            try {
                Account acct = Account.getAccount(acctID);
                return acct;
            } catch (DBException dbe) {
                return null;
            }
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    private static final String _TABLE_NAME                 = "Account";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    // -- Account fields
    public static final String FLD_accountType              = "accountType";
    public static final String FLD_notifyEmail              = "notifyEmail";
    public static final String FLD_allowNotify              = "allowNotify";
    public static final String FLD_speedUnits               = "speedUnits";
    public static final String FLD_distanceUnits            = "distanceUnits";
    public static final String FLD_volumeUnits              = "volumeUnits";
    public static final String FLD_pressureUnits            = "pressureUnits";
    public static final String FLD_economyUnits             = "economyUnits";
    public static final String FLD_temperatureUnits         = "temperatureUnits";
    public static final String FLD_currencyUnits            = "currencyUnits";
    public static final String FLD_fuelCostPerLiter         = "fuelCostPerLiter";
    public static final String FLD_latLonFormat             = "latLonFormat";
    public static final String FLD_geocoderMode             = "geocoderMode";
    public static final String FLD_privateLabelName         = "privateLabelName";
    public static final String FLD_privateLabelJsp          = "privateLabelJsp";
    public static final String FLD_isBorderCrossing         = "isBorderCrossing";
    public static final String FLD_retainedEventAge         = "retainedEventAge";
    public static final String FLD_maximumDevices           = "maximumDevices";
    public static final String FLD_totalPingCount           = "totalPingCount";        // total ping count
    public static final String FLD_maxPingCount             = "maxPingCount";          // maximum allowed ping count
    public static final String FLD_autoAddDevices           = "autoAddDevices";        // EXPERIMENTAL (not fully implemented)
    public static final String FLD_dcsPropertiesID          = "dcsPropertiesID";
    public static final String FLD_smsEnabled               = "smsEnabled";
    public static final String FLD_smsProperties            = "smsProperties";
    public static final String FLD_emailProperties          = "emailProperties";
    public static final String FLD_expirationTime           = "expirationTime";
    public static final String FLD_suspendUntilTime         = "suspendUntilTime";
    public static final String FLD_allowWebService          = "allowWebService";
    // -- User fields
    public static final String FLD_defaultUser              = "defaultUser";
    public static final String FLD_password                 = "password";
    public static final String FLD_contactName              = "contactName";
    public static final String FLD_contactPhone             = "contactPhone";
    public static final String FLD_contactEmail             = "contactEmail";
    public static final String FLD_timeZone                 = "timeZone";
    public static final String FLD_passwdChangeTime         = "passwdChangeTime";
    public static final String FLD_passwdQueryTime          = "passwdQueryTime";
    public static final String FLD_lastLoginTime            = "lastLoginTime";
  //public static final String FLD_newUserRoleID            = "newUserRoleID";
    public static final DBField FieldInfo[] = {
        // -- Account fields
        newField_accountID(true), // key
        new DBField(FLD_accountType         , Integer.TYPE  , DBField.TYPE_UINT16      , "Account Type"              , "edit=2 enum=Account$AccountType"),
        new DBField(FLD_notifyEmail         , String.class  , DBField.TYPE_EMAIL_LIST(), "Notification EMail Address", "edit=2"),
        new DBField(FLD_allowNotify         , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "Allow Notification"        , "edit=2"),
        new DBField(FLD_speedUnits          , Integer.TYPE  , DBField.TYPE_UINT8       , "Speed Units"               , "edit=2 enum=Account$SpeedUnits"),
        new DBField(FLD_distanceUnits       , Integer.TYPE  , DBField.TYPE_UINT8       , "Distance Units"            , "edit=2 enum=Account$DistanceUnits"),
        new DBField(FLD_volumeUnits         , Integer.TYPE  , DBField.TYPE_UINT8       , "Volume Units"              , "edit=2 enum=Account$VolumeUnits"),
        new DBField(FLD_pressureUnits       , Integer.TYPE  , DBField.TYPE_UINT8       , "Pressure Units"            , "edit=2 enum=Account$PressureUnits"),
        new DBField(FLD_economyUnits        , Integer.TYPE  , DBField.TYPE_UINT8       , "Economy Units"             , "edit=2 enum=Account$EconomyUnits"),
        new DBField(FLD_temperatureUnits    , Integer.TYPE  , DBField.TYPE_UINT8       , "Temperature Units"         , "edit=2 enum=Account$TemperatureUnits"),
        new DBField(FLD_currencyUnits       , String.class  , DBField.TYPE_STRING(8)   , "Currency Units"            , "edit=2 enum=Account$Currency"),
        new DBField(FLD_fuelCostPerLiter    , Double.TYPE   , DBField.TYPE_DOUBLE      , "Fuel Cost Per Liter"       , "edit=2"),
        new DBField(FLD_latLonFormat        , Integer.TYPE  , DBField.TYPE_UINT8       , "Latitude/Longitude Format" , "edit=2 enum=Account$LatLonFormat"),
        new DBField(FLD_geocoderMode        , Integer.TYPE  , DBField.TYPE_UINT8       , "Geocoder Mode"             , "edit=2 enum=Account$GeocoderMode"),
        new DBField(FLD_privateLabelName    , String.class  , DBField.TYPE_STRING(32)  , "PrivateLabel Name"         , "edit=2 editor=privateLabel"),
      //new DBField(FLD_privateLabelJsp     , String.class  , DBField.TYPE_STRING(32)  , "PrivateLabel JSP"          , "edit=2"),
        new DBField(FLD_isBorderCrossing    , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "BorderCrossing Enabled"    , "edit=2"),
        new DBField(FLD_retainedEventAge    , Long.TYPE     , DBField.TYPE_UINT32      , "Retained Event Age (sec)"  , "edit=2"),
        new DBField(FLD_maximumDevices      , Long.TYPE     , DBField.TYPE_INT32       , "Maximum number of devices" , "edit=2"),
        new DBField(FLD_totalPingCount      , Integer.TYPE  , DBField.TYPE_UINT16      , "Total 'Ping' Count"        , ""),
        new DBField(FLD_maxPingCount        , Integer.TYPE  , DBField.TYPE_UINT16      , "Maximum 'Ping' Count"      , "edit=2"),
        new DBField(FLD_autoAddDevices      , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "AutoAdd Devices"           , "edit=2"),
        new DBField(FLD_dcsPropertiesID     , String.class  , DBField.TYPE_STRING(32)  , "DCS Properties ID"         , "edit=2"),
        new DBField(FLD_smsEnabled          , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "SMS Enabled"               , "edit=2"),
        new DBField(FLD_smsProperties       , String.class  , DBField.TYPE_STRING(200) , "SMS Properties"            , "edit=2"),
        new DBField(FLD_emailProperties     , String.class  , DBField.TYPE_STRING(250) , "EMail Properties"          , "edit=2"),
        new DBField(FLD_expirationTime      , Long.TYPE     , DBField.TYPE_UINT32      , "Expiration Time"           , "format=time"),
        new DBField(FLD_suspendUntilTime    , Long.TYPE     , DBField.TYPE_UINT32      , "Suspend Until Time"        , "format=time"),
        new DBField(FLD_allowWebService     , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "Allow Web-Service"         , "edit=2"),
        // -- User fields
        new DBField(FLD_defaultUser         , String.class  , DBField.TYPE_USER_ID()   , "Default User ID"           , "edit=2"),
        new DBField(FLD_password            , String.class  , DBField.TYPE_STRING(32)  , "Password"                  , "edit=2 editor=password"),
        new DBField(FLD_contactName         , String.class  , DBField.TYPE_STRING(64)  , "Contact Name"              , "edit=2 utf8=true"),
        new DBField(FLD_contactPhone        , String.class  , DBField.TYPE_STRING(32)  , "Contact Phone"             , "edit=2"),
        new DBField(FLD_contactEmail        , String.class  , DBField.TYPE_STRING(128) , "Contact EMail Address"     , "edit=2 altkey=email"),
        new DBField(FLD_timeZone            , String.class  , DBField.TYPE_STRING(32)  , "Time Zone"                 , "edit=2 editor=timeZone"),
        new DBField(FLD_passwdChangeTime    , Long.TYPE     , DBField.TYPE_UINT32      , "Last Password Change Time" , "format=time"),
        new DBField(FLD_passwdQueryTime     , Long.TYPE     , DBField.TYPE_UINT32      , "Last Password Query Time"  , "format=time"),
        new DBField(FLD_lastLoginTime       , Long.TYPE     , DBField.TYPE_UINT32      , "Last Login Time"           , "format=time"),
      //new DBField(FLD_newUserRoleID       , String.class  , DBField.TYPE_ROLE_ID()   , "Default New User Role"     , "edit=2"),
        // -- Common fields
        newField_isActive(), // getIsActive
        newField_displayName(),
        newField_description(),
        newField_notes(),
        newField_lastUpdateTime(),
        newField_lastUpdateAccount(true),
        newField_lastUpdateUser(true),
        newField_creationTime(),
    };
    
    // -- Address fields
    // -  startupInit.Account.AddressFieldInfo=true
    public static final String FLD_addressLine1             = "addressLine1";           // address line 1
    public static final String FLD_addressLine2             = "addressLine2";           // address line 2
    public static final String FLD_addressLine3             = "addressLine3";           // address line 3
    public static final String FLD_addressCity              = "addressCity";            // address city
    public static final String FLD_addressState             = "addressState";           // address state/province
    public static final String FLD_addressPostalCode        = "addressPostalCode";      // address postal code
    public static final String FLD_addressCountry           = "addressCountry";         // address country
    public static final DBField AddressFieldInfo[] = {
        new DBField(FLD_addressLine1        , String.class      , DBField.TYPE_STRING(70)   , "Address Line 1"              , "edit=2 utf8=true"),
        new DBField(FLD_addressLine2        , String.class      , DBField.TYPE_STRING(70)   , "Address Line 2"              , "edit=2 utf8=true"),
        new DBField(FLD_addressLine3        , String.class      , DBField.TYPE_STRING(70)   , "Address Line 3"              , "edit=2 utf8=true"),
        new DBField(FLD_addressCity         , String.class      , DBField.TYPE_STRING(50)   , "Address City"                , "edit=2 utf8=true"),
        new DBField(FLD_addressState        , String.class      , DBField.TYPE_STRING(50)   , "Address State/Province"      , "edit=2 utf8=true"),
        new DBField(FLD_addressPostalCode   , String.class      , DBField.TYPE_STRING(20)   , "Address Postal Code"         , "edit=2 utf8=true"),
        new DBField(FLD_addressCountry      , String.class      , DBField.TYPE_STRING(20)   , "Address Country"             , "edit=2 utf8=true"),
    };

    // -- Map Legend fields
    // -  startupInit.Account.MapLegendFieldInfo=true
    public static final String FLD_mapLegendDevice          = "mapLegendDevice";        // Device Map Legend
    public static final String FLD_mapLegendGroup           = "mapLegendGroup";         // DeviceGroup Map Legend
    public static final DBField MapLegendFieldInfo[] = {
        new DBField(FLD_mapLegendDevice     , String.class      , DBField.TYPE_TEXT         , "Device Map Legend"           , "edit=2 utf8=true"),
        new DBField(FLD_mapLegendGroup      , String.class      , DBField.TYPE_TEXT         , "DeviceGroup Map Legend"      , "edit=2 utf8=true"),
    };

    // -- Account Manager Fields
    // -  startupInit.Account.AccountManagerInfo=true
    public static final String FLD_isAccountManager         = "isAccountManager";
    public static final String FLD_managerID                = "managerID";
    public static final DBField AccountManagerInfo[]        = {
        new DBField(FLD_isAccountManager     , Boolean.TYPE      , DBField.TYPE_BOOLEAN     , "Is Account Manager"          , "edit=2"),
        new DBField(FLD_managerID            , String.class      , DBField.TYPE_ID()        , "Manager ID"                  , "edit=2 altkey=manager"),
    };
    
    // -- Data Request/Push Fields
    // -  startupInit.Account.DataPushInfo=true
    public static final String FLD_requestPassCode          = "requestPassCode";        // data request passcode
    public static final String FLD_requestIPAddress         = "requestIPAddress";       // valid request IP address block
    public static final String FLD_dataPushURL              = "dataPushURL";            // data push URL
    public static final String FLD_lastDataRequestTime      = "lastDataRequestTime";    // timestamp of last data request
    public static final String FLD_lastDataPushTime         = "lastDataPushTime";       // timestamp of last data push
    public static final DBField DataPushInfo[]              = {
        new DBField(FLD_requestPassCode      , String.class      , DBField.TYPE_STRING(32)  , "Request Passcode"            , "edit=2"),
        new DBField(FLD_requestIPAddress     , DTIPAddrList.class, DBField.TYPE_STRING(128) , "Valid Request IP Addresses"  , "edit=2"),
        new DBField(FLD_dataPushURL          , String.class      , DBField.TYPE_STRING(240) , "Data Push URL (destination)" , "edit=2"),
        new DBField(FLD_lastDataRequestTime  , Long.TYPE         , DBField.TYPE_UINT32      , "Last Data Request Time"      , "format=time"),
        new DBField(FLD_lastDataPushTime     , Long.TYPE         , DBField.TYPE_UINT32      , "Last Data Push Time (millis)", "format=time"),
    };

    // -- ELog/HOS fields
    // -  startupInit.Account.ELogHOSInfo=true
    public static final String FLD_eLogEnabled              = "eLogEnabled";           // ELog/HOS enabled 
    public static final String FLD_eLogUsername             = "eLogUsername";          // ELog/HOS username
    public static final String FLD_eLogPassword             = "eLogPassword";          // ELog/HOS password
    public static final DBField ELogHOSInfo[]               = {
        new DBField(FLD_eLogEnabled          , Boolean.TYPE      , DBField.TYPE_BOOLEAN     , "ELog/HOS Enabled"            , "edit=2"),
        new DBField(FLD_eLogUsername         , String.class      , DBField.TYPE_STRING(32)  , "ELog/HOS Username"           , "edit=2"),
        new DBField(FLD_eLogPassword         , String.class      , DBField.TYPE_STRING(32)  , "ELog/HOS Password"           , "edit=2 editor=password"),
    };

    /* key class */
    public static class Key
        extends AccountKey<Account>
    {
        public Key() {
            super();
        }
        public Key(String acctId) {
            super.setKeyValue(FLD_accountID, ((acctId != null)? acctId.trim().toLowerCase() : ""));
        }
        public DBFactory<Account> getFactory() {
            return Account.getFactory();
        }
    }

    /* factory constructor */
    protected static DBFactory<Account> factory = null;
    public static DBFactory<Account> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                Account.TABLE_NAME(), 
                Account.FieldInfo, 
                DBFactory.KeyType.PRIMARY,
                Account.class, 
                Account.Key.class,
                true/*editable*/, true/*viewable*/);
            // -- FLD_notifyEmail max length
            DBField emailFld = factory.getField(FLD_notifyEmail);
            Account.NotifyEmailColumnLength = (emailFld != null)? emailFld.getStringLength() : 0;
        }
        return factory;
    }

    /* Bean instance */
    public Account()
    {
        super();
    }

    /* database record */
    public Account(Account.Key key)
    {
        super(key);
    }

    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(Account.class, loc);
        return i18n.getString("Account.description", 
            "This table defines " +
            "the top level Account specific information."
            );
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below

    /**
    *** Gets the default login user-id
    **/
    public String getDefaultUser()
    {
        String v = (String)this.getFieldValue(FLD_defaultUser);
        return (v != null)? v : "";
    }

    /**
    *** Sets the default login user-id
    **/
    public void setDefaultUser(String v)
    {
        this.setFieldValue(FLD_defaultUser, StringTools.trim(v));
    }

    /**
    *** Gets the default login user-id for the specified account
    *** @param acct     The Account
    *** @param rtnAdmin True to return the "admin" user-id if the account default user-id is not defined
    *** @return The default account user-id.
    **/
    public static String getDefaultUser(Account acct, boolean rtnAdmin)
    {
        String userID = (acct != null)? acct.getDefaultUser() : null;
        if (!StringTools.isBlank(userID)) {
            return userID;
        } else
        if (rtnAdmin) {
            return User.getAdminUserID();
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------

    /* Gets the password of this account */
    public String getPassword()
    {
        String p = (String)this.getFieldValue(FLD_password);
        return (p != null)? p : "";
    }

    /* Sets the password for this account */
    public void setPassword(String p)
    {
        this.setFieldValue(FLD_password, ((p != null)? p : ""));
        this.setPasswdChangeTime(DateTime.getCurrentTimeSec());
    }

    /* Gets the password of this account */
    public String getEncodedPassword()
    {
        return this.getPassword();
    }

    /* Sets the password for this account */
    public void setEncodedPassword(String p)
    {
        this.setPassword(p);
    }

    // --------

    /* get decoded password */
    public String getDecodedPassword(BasicPrivateLabel bpl)
    {
        if (bpl == null) {
            bpl = this.getPrivateLabel();
        }
        String pass = Account.decodePassword(bpl, this.getEncodedPassword());
        // it is possible that this password cannot be decoded
        return pass; // 'null' if password cannot be decoded
    }

    /* encode and set password */
    public void setDecodedPassword(BasicPrivateLabel bpl, String enteredPass)
    {
        if (bpl == null) {
            bpl = this.getPrivateLabel();
        }
        String pass = Account.encodePassword(bpl, enteredPass);
        if (!this.getEncodedPassword().equals(pass)) {
            this.setEncodedPassword(pass);
        }
    }

    // --------

    /* reset the password */
    // does not save the record!
    public String resetPassword(BasicPrivateLabel bpl)
    {
        String pass = Account.createRandomPassword(Account.TEMP_PASSWORD_LENGTH);
        this.setDecodedPassword(bpl, pass);
        return pass; // record not yet saved!
    }

    /* check that the specified password is a match for this account */
    public boolean checkPassword(BasicPrivateLabel bpl, String enteredPass, boolean suspend)
    {
        if (bpl == null) { 
            bpl = this.getPrivateLabel(); 
        }
        boolean ok = Account.checkPassword(bpl,enteredPass,this.getEncodedPassword());
        if (!ok && suspend) {
            Account acct = this.getAccount();
            if (acct != null) {
                acct.suspendOnLoginFailureAttempt();
            }
        }
        return ok;
    }

    /* check that the specified password is a match for this account */
    public static boolean checkPassword(BasicPrivateLabel bpl, String enteredPass, String tablePass)
    {
        PasswordHandler pwh = Account.getPasswordHandler(bpl); // non-null
        //((GeneralPasswordHandler)pwh).setDebugCheckPassword(true);
        return pwh.checkPassword(enteredPass, tablePass);
    }

    // --------

    /* encode password */
    // Convert a clear-text password into a table-encoded password 
    // (this may be a one-way encoding).
    public static String encodePassword(BasicPrivateLabel bpl, String enteredPass)
    {
        // all passwords are encodable (even if it's a 1-1 encoding)
        PasswordHandler pwh = Account.getPasswordHandler(bpl);
        return pwh.encodePassword(enteredPass);
    }

    /* decode password */
    // Convert a table-encoded password into a clear-text password
    public static String decodePassword(BasicPrivateLabel bpl, String tablePass)
    {
        // this method must return 'null' if table-encoded passwords cannot be decoded
        PasswordHandler pwh = Account.getPasswordHandler(bpl);
        return pwh.decodePassword(tablePass);
    }

    // ------------------------------------------------------------------------

    /* return the account type (default AccountType.DEFAULT) */
    public int getAccountType()
    {
        Integer v = (Integer)this.getFieldValue(FLD_accountType);
        return (v != null)? v.intValue() : EnumTools.getDefault(AccountType.class).getIntValue();
    }

    /* set the account type */
    public void setAccountType(int v)
    {
        this.setFieldValue(FLD_accountType, EnumTools.getValueOf(AccountType.class,v).getIntValue());
    }

    /* set the account type */
    public void setAccountType(AccountType v)
    {
        this.setFieldValue(FLD_accountType, EnumTools.getValueOf(AccountType.class,v).getIntValue());
    }

    /* set the account type */
    public void setAccountType(String v, Locale locale)
    {
        this.setFieldValue(FLD_accountType, EnumTools.getValueOf(AccountType.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* return the contact name for this account */
    public String getContactName()
    {
        String v = (String)this.getFieldValue(FLD_contactName);
        return StringTools.trim(v);
    }

    /* set the contact name for this account */
    public void setContactName(String v)
    {
        this.setFieldValue(FLD_contactName, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return the contact phone# for this account */
    public String getContactPhone()
    {
        String v = (String)this.getFieldValue(FLD_contactPhone);
        return StringTools.trim(v);
    }

    /* set the contact phone# for this account */
    public void setContactPhone(String v)
    {
        this.setFieldValue(FLD_contactPhone, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return the contact email address for this account */
    public String getContactEmail()
    {
        String v = (String)this.getFieldValue(FLD_contactEmail);
        return StringTools.trim(v);
    }

    /* set the contact email address for this account */
    public void setContactEmail(String v)
    {
        this.setFieldValue(FLD_contactEmail, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return the notification email address for this account */
    public String getNotifyEmail()
    {
        String v = (String)this.getFieldValue(FLD_notifyEmail);
        return StringTools.trim(v);
    }

    /* set the notification email address for this account */
    public void setNotifyEmail(String v)
    {
        String ne = StringTools.trim(v);
        if ((Account.NotifyEmailColumnLength > 0)           &&
            (ne.length() >= Account.NotifyEmailColumnLength)  ) {
            int newLen = Account.getMaximumNotifyEmailLength();
            ne = ne.substring(0, newLen).trim();
            // Note: MySQL will refuse to insert the record if the data length
            // is greater than the table column length.
        }
        this.setFieldValue(FLD_notifyEmail, ne);
    }

    /**
    *** Gets the maximum Notify Email length
    **/
    public static int getMaximumNotifyEmailLength()
    {
        // -1 so we are not so close to the edge of the cliff
        return Account.NotifyEmailColumnLength - 1;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if notifications are allowed for this Account
    *** @return True if notifications are allowed for this Account
    **/
    public boolean getAllowNotify()
    {
        Boolean v = (Boolean)this.getOptionalFieldValue(FLD_allowNotify);
        return (v != null)? v.booleanValue() : false;
    }

    /**
    *** Sets the "Allow Notification" state for this Account
    *** @param v The "Allow Notification" state for this Account
    **/
    public void setAllowNotify(boolean v)
    {
        this.setOptionalFieldValue(FLD_allowNotify, v);
    }

    // ------------------------------------------------------------------------

    private TimeZone timeZone = null;
    
    /* get TimeZone */
    public static TimeZone getTimeZone(Account account, TimeZone dft)
    {
        return (account != null)? account.getTimeZone(dft) : dft;
    }

    /* get the TimeZone instance for this account */
    // return the specified default if no timezone have been specified
    // does not return null
    public TimeZone getTimeZone(TimeZone dft)
    {
        if (this.timeZone == null) {
            this.timeZone = DateTime.getTimeZone(this.getTimeZone(), null);
            if (this.timeZone == null) {
                this.timeZone = (dft != null)? dft : DateTime.getGMTTimeZone();
            }
        }
        return this.timeZone;
    }

    /* get the string representation of the TimeZone for this account */
    public String getTimeZone()
    {
        String v = (String)this.getFieldValue(FLD_timeZone);
        return !StringTools.isBlank(v)? v.trim() : Account.GetDefaultTimeZone();
    }

    /* set the string representation of the timezone for this account */
    public void setTimeZone(String v)
    {
        String tz = StringTools.trim(v);
        if (!StringTools.isBlank(tz)) {
            // validate timezone value?
        }
        this.timeZone = null;
        this.setFieldValue(FLD_timeZone, !StringTools.isBlank(tz)? tz : Account.GetDefaultTimeZone());
    }

    /* return current DateTime (relative the Account TimeZone) */
    public DateTime getCurrentDateTime()
    {
        return new DateTime(this.getTimeZone(null));
    }

    // ------------------------------------------------------------------------

    /* return the last time the password was changed for this account */
    public long getPasswdChangeTime()
    {
        Long v = (Long)this.getFieldValue(FLD_passwdChangeTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the time the password was changed for this account */
    public void setPasswdChangeTime(long v)
    {
        this.setFieldValue(FLD_passwdChangeTime, v);
    }

    /* password expired? */
    public boolean hasPasswordExpired()
    {
        BasicPrivateLabel bpl = this.getPrivateLabel();
        PasswordHandler pwh = Account.getPasswordHandler(bpl);
        return pwh.hasPasswordExpired(this.getPasswdChangeTime());
    }

    // ------------------------------------------------------------------------

    /* return the last time the password was queried for this account */
    public long getPasswdQueryTime()
    {
        Long v = (Long)this.getFieldValue(FLD_passwdQueryTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the time the password was queried for this account */
    public void setPasswdQueryTime(long v)
    {
        this.setFieldValue(FLD_passwdQueryTime, v);
    }

    // ------------------------------------------------------------------------

    /* return true if the retained-event-age has been defined */
    public boolean hasRetainedEventAge()
    {
        return (this.getRetainedEventAge() > 0L)? true : false;
    }

    /* return the maximum age of retained events (in seconds) */
    public long getRetainedEventAge()
    {
        Long v = (Long)this.getFieldValue(FLD_retainedEventAge);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the maximum age of retained events (in seconds) */
    public void setRetainedEventAge(long v)
    {
        this.setFieldValue(FLD_retainedEventAge, v);
    }
    
    /* adjust retained event age date */
    public long adjustRetainedEventTime(long oldTimeSec)
    {
        long retainedAgeSec = this.getRetainedEventAge();
        if (retainedAgeSec > 0L) {
            // -- retainedEventAge is defined
            long retainedTimeSec = DateTime.getCurrentTimeSec() - retainedAgeSec;
            if (retainedTimeSec < oldTimeSec) {
                // -- retained-time is prior to delete-time
                oldTimeSec = retainedTimeSec;
                if (oldTimeSec < 1L) {
                    // -- must not be less than "1"
                    oldTimeSec = 1L;
                }
            }
        }
        return oldTimeSec;
    }

    // ------------------------------------------------------------------------

    /**
    *** Return true if this account is at the maximum number of allowed devices
    **/
    public boolean isAtMaximumDevices(boolean zeroIsUnlimited)
    {
        long maxCnt = this.getMaximumDevices();
        if (maxCnt < 0L) {
            // no limit on number of devices
            return false;
        } else
        if ((maxCnt == 0L) && zeroIsUnlimited) {
            // no limit on number of devices
            return false;
        } else {
            // check device count against maximum limit
            long devCnt = this.getDeviceCount();
            return (devCnt >= maxCnt)? true : false;
        }
    }

    /**
    *** Return true if the specified value exceeds the allow maximum number of devices
    **/
    public boolean exceedsMaximumDevices(long devCnt, boolean zeroIsUnlimited)
    {
        long maxCnt = this.getMaximumDevices();
        if (maxCnt < 0L) {
            return false;
        } else
        if ((maxCnt == 0L) && zeroIsUnlimited) {
            return false;
        } else {
            return (devCnt > maxCnt)? true : false;
        }
    }

    /* return the maximum number of allowed devices */
    public long getMaximumDevices()
    {
        Long v = (Long)this.getFieldValue(FLD_maximumDevices);
        return (v != null)? v.intValue() : -1L;
    }

    /* set the maximum number of allowed devices */
    public void setMaximumDevices(long v)
    {
        this.setFieldValue(FLD_maximumDevices, v);
    }

    // ------------------------------------------------------------------------

    public int getTotalPingCount()
    {
        Integer v = (Integer)this.getFieldValue(FLD_totalPingCount);
        return (v != null)? v.intValue() : 0;
    }

    public void setTotalPingCount(int v)
    {
        v = (v > 0xFFFF)? 0xFFFF : (v > 0)? v : 0; // limit to 16-bit
        this.setFieldValue(FLD_totalPingCount, v);
    }

    public boolean incrementPingCount(long pingTime, boolean reload, boolean update)
    {

        /* refresh current totalPingCount */
        if (reload) {
            // in case another Device 'ping' has changed this value already
            this.reload(Account.FLD_totalPingCount);
        }

        /* increment totalPingCount */
        this.setTotalPingCount(this.getTotalPingCount() + 1);
        if (pingTime > 0L) {
            //this.setLastPingTime(pingTime);   TODO: add this method
        }

        /* update record */
        if (update) {
            try {
                this.update( // may throw DBException
                  //Account.FLD_lastPingTime,
                    Account.FLD_totalPingCount
                    );
            } catch (DBException dbe) {
                Print.logException("Unable to update 'ping' count", dbe);
                return false;
            }
        }

        return true;
    }

    public boolean resetTotalPingCount(boolean update)
    {

        /* reset */
        this.setTotalPingCount(0);
        //this.setLastPingTime(0L);   TODO: add this method

        /* update record */
        if (update) {
            try {
                this.update( // may throw DBException
                  //Account.FLD_lastPingTime,
                    Account.FLD_totalPingCount
                    );
            } catch (DBException dbe) {
                Print.logException("Unable to update 'ping' count", dbe);
                return false;
            }
        }

        return true;

    }

    // ------------------------------------------------------------------------

    public int getMaxPingCount()
    {
        Integer v = (Integer)this.getFieldValue(FLD_maxPingCount);
        return (v != null)? v.intValue() : 0;
    }

    public void setMaxPingCount(int v)
    {
        v = (v > 0xFFFF)? 0xFFFF : (v > 0)? v : 0; // limit to 16-bit
        this.setFieldValue(FLD_maxPingCount, v);
    }

    // ------------------------------------------------------------------------

    /* return the time this account expires */
    public long getExpirationTime()
    {
        Long v = (Long)this.getFieldValue(FLD_expirationTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the time this account expires */
    public void setExpirationTime(long v)
    {
        this.setFieldValue(FLD_expirationTime, v);
    }

    /* return true if this account has expired */
    public boolean isExpired()
    {

        /* not active? (assume expired if not active) */
        if (!this.isActive()) {
            return true;
        }

        /* expired? */
        long expireTime = this.getExpirationTime();
        if ((expireTime > 0L) && (expireTime < DateTime.getCurrentTimeSec())) {
            return true;
        }

        /* not expired */
        return false;

    }
    
    /* return true if this account has an expiry date */
    public boolean doesExpire()
    {
        long expireTime = this.getExpirationTime();
        return (expireTime > 0L);
    }

    /* return true if this account will expire within the specified # of seconds */
    public boolean willExpire(long withinSec)
    {

        /* will account expire? */
        long expireTime = this.getExpirationTime();
        if ((expireTime > 0L) && 
            ((withinSec < 0L) || (expireTime < (DateTime.getCurrentTimeSec() + withinSec)))) {
            return true;
        }

        /* will not expired */
        return false;

    }

    // ------------------------------------------------------------------------

    /* return the account suspend time */
    public long getSuspendUntilTime()
    {
        Long v = (Long)this.getFieldValue(FLD_suspendUntilTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the time this account expires */
    public void setSuspendUntilTime(long v)
    {
        this.setFieldValue(FLD_suspendUntilTime, v);
    }

    /* return true if this account has expired */
    public boolean isSuspended()
    {

        /* account suspended? */
        long suspendTime = this.getSuspendUntilTime();
        if ((suspendTime > 0L) && (suspendTime >= DateTime.getCurrentTimeSec())) {
            return true;
        }

        /* not suspended */
        return false;

    }

    /* login failure, check for suspension */
    public void suspendOnLoginFailureAttempt()
    {
        String accountID = this.getAccountID();
        long failedInterval = RTConfig.getLong(DBConfig.PROP_Account_failedLoginInterval,DateTime.MinuteSeconds(3));
        int  failedAttempts = RTConfig.getInt( DBConfig.PROP_Account_failedLoginAttempts,6);
        if ((failedInterval > 0L) && (failedAttempts > 0)) {
            long nowTime   = DateTime.getCurrentTimeSec();
            long sinceTime = nowTime - failedInterval;
            int count = Audit.getFailedLoginAttampts(accountID, sinceTime);
            if (count >= failedAttempts) {
                // -- too many failed login attempts, suspend account
                long suspendTime = nowTime + failedInterval;
                this.setSuspendUntilTime(suspendTime);
                try {
                    this.update(Account.FLD_suspendUntilTime);
                } catch (DBException dbe) {
                    Print.logError("Unable to set suspendUntilTime for account ("+accountID+"): " + dbe);
                }
            }
        }
    }

    // ------------------------------------------------------------------------

    /* default value for "Account.alwaysAllowWebService" */
    private static final boolean DEFAULT_alwaysAllowWebService = true;

    /**
    *** Returns true if web-service access is always allowed for all Accounts
    *** @return True if web-service access is always allowed for all Accounts
    **/
    public static boolean IsAlwaysAllowWebService()
    {
        if (RTConfig.getBoolean(DBConfig.PROP_Account_alwaysAllowWebService,DEFAULT_alwaysAllowWebService)) {
            return true;
        } else {
            return false;
        }
    }

    /**
    *** Returns true if web-service access is allowed for the specified Account
    *** @return True if web-service access is allowed for the specified Account
    **/
    public static boolean GetAllowWebService(Account acct)
    {
        if (acct == null) {
            return false;
        } else
        if (IsAlwaysAllowWebService()) {
            return true;
        } else {
            return acct.getAllowWebService();
        }
    }

    /**
    *** Returns true if web-service access is allowed for this Account
    *** @return True if web-service access is allowed for this Account
    **/
    public boolean getAllowWebService()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_allowWebService);
        return (v != null)? v.booleanValue() : false;
    }

    /**
    *** Sets the "Allow Web-Service" state for this Account
    *** @param v The "Allow Web-Service" state for this Account
    **/
    public void setAllowWebService(boolean v)
    {
        this.setFieldValue(FLD_allowWebService, v);
    }

    // ------------------------------------------------------------------------

    public String getAddressLine1()
    {
        String v = (String)this.getFieldValue(FLD_addressLine1);
        return StringTools.trim(v);
    }
    
    public String getAddressLine2()
    {
        String v = (String)this.getFieldValue(FLD_addressLine2);
        return StringTools.trim(v);
    }
    
    public String getAddressLine3()
    {
        String v = (String)this.getFieldValue(FLD_addressLine3);
        return StringTools.trim(v);
    }
    
    public String[] getAddressLines()
    {
        return new String[] {
            this.getAddressLine1(),
            this.getAddressLine2(),
            this.getAddressLine3()
        };
    }
    
    public String getAddressCity()
    {
        String v = (String)this.getFieldValue(FLD_addressCity);
        return StringTools.trim(v);
    }
    
    public String getAddressState()
    {
        String v = (String)this.getFieldValue(FLD_addressState);
        return StringTools.trim(v);
    }
   
    public String getAddressPostalCode()
    {
        String v = (String)this.getFieldValue(FLD_addressPostalCode);
        return StringTools.trim(v);
    }
   
    public String getAddressCountry()
    {
        String v = (String)this.getFieldValue(FLD_addressCountry);
        return StringTools.trim(v);
    }

    public void setAddressLine1(String v)
    {
        this.setFieldValue(FLD_addressLine1, StringTools.trim(v));
    }

    public void setAddressLine2(String v)
    {
        this.setFieldValue(FLD_addressLine2, StringTools.trim(v));
    }

    public void setAddressLine3(String v)
    {
        this.setFieldValue(FLD_addressLine3, StringTools.trim(v));
    }
    
    public void setAddressLines(String lines[])
    {
        if ((lines != null) && (lines.length > 0)) {
            int n = 0;
            while ((n < lines.length) && ((lines[n] == null) || lines[n].trim().equals(""))) { n++; }
            this.setAddressLine1((n < lines.length)? lines[n++].trim() : "");
            while ((n < lines.length) && ((lines[n] == null) || lines[n].trim().equals(""))) { n++; }
            this.setAddressLine2((n < lines.length)? lines[n++].trim() : "");
            while ((n < lines.length) && ((lines[n] == null) || lines[n].trim().equals(""))) { n++; }
            this.setAddressLine3((n < lines.length)? lines[n++].trim() : "");
        } else {
            this.setAddressLine1("");
            this.setAddressLine2("");
            this.setAddressLine3("");
        }
    }

    public void setAddressCity(String v)
    {
        this.setFieldValue(FLD_addressCity, StringTools.trim(v));
    }

    public void setAddressState(String v)
    {
        this.setFieldValue(FLD_addressState, StringTools.trim(v));
    }

    public void setAddressPostalCode(String v)
    {
        this.setFieldValue(FLD_addressPostalCode, StringTools.trim(v));
    }

    public void setAddressCountry(String v)
    {
        this.setFieldValue(FLD_addressCountry, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* get the speed-units for this account */
    public int getSpeedUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_speedUnits);
        return (v != null)? v.intValue() : EnumTools.getDefault(SpeedUnits.class).getIntValue();
    }

    /* set the speed-units */
    public void setSpeedUnits(int v)
    {
        this.setFieldValue(FLD_speedUnits, EnumTools.getValueOf(SpeedUnits.class,v).getIntValue());
    }

    /* set the speed-units */
    public void setSpeedUnits(SpeedUnits v)
    {
        this.setFieldValue(FLD_speedUnits, EnumTools.getValueOf(SpeedUnits.class,v).getIntValue());
    }

    /* set the string representation of the speed-units */
    public void setSpeedUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_speedUnits, EnumTools.getValueOf(SpeedUnits.class,v,locale).getIntValue());
    }

    /* return a formatted speed string */
    public String getSpeedString(double speedKPH, boolean inclUnits, Locale locale)
    {
        return this.getSpeedString(speedKPH, "0", null, inclUnits, locale);
    }

    /* return a formatted speed string */
    public String getSpeedString(double speedKPH, String format, boolean inclUnits, Locale locale)
    {
        return this.getSpeedString(speedKPH, format, null, inclUnits, locale);
    }

    /* return a formatted speed string */
    public String getSpeedString(double speedKPH, String format, SpeedUnits speedUnitsEnum, boolean inclUnits, Locale locale)
    {
        if (speedUnitsEnum == null) { speedUnitsEnum = Account.getSpeedUnits(this); }
        double speed = speedUnitsEnum.convertFromKPH(speedKPH);
        String speedFmt = StringTools.format(speed, format);
        if (speed <= 0.0) {
            return speedFmt;
        } else {
            if (inclUnits) {
                return speedFmt + " " + speedUnitsEnum.toString(locale);
            } else {
                return speedFmt;
            }
        }
    }

    // ------------------------------------------------------------------------

    /* get the distance units for this account */
    public int getDistanceUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_distanceUnits);
        return (v != null)? v.intValue() : EnumTools.getDefault(DistanceUnits.class).getIntValue();
    }

    /* set the distance units */
    public void setDistanceUnits(int v)
    {
        this.setFieldValue(FLD_distanceUnits, EnumTools.getValueOf(DistanceUnits.class,v).getIntValue());
    }

    /* set the distance units */
    public void setDistanceUnits(DistanceUnits v)
    {
        this.setFieldValue(FLD_distanceUnits, EnumTools.getValueOf(DistanceUnits.class,v).getIntValue());
    }

    /* set the string representation of the distance units */
    public void setDistanceUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_distanceUnits, EnumTools.getValueOf(DistanceUnits.class,v,locale).getIntValue());
    }

    /* return a formatted distance string */
    public String getDistanceString(double distKM, boolean inclUnits, Locale locale)
    {
        DistanceUnits units = Account.getDistanceUnits(this);
        String distUnitsStr = units.toString(locale);
        double dist         = units.convertFromKM(distKM);
        String distStr      = StringTools.format(dist, "0");
        return inclUnits? (distStr + " " + distUnitsStr) : distStr;
    }

    // ------------------------------------------------------------------------

    /* get the volume units for this account */
    public int getVolumeUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_volumeUnits);
        if (v != null) {
            return v.intValue();
        } else {
            switch (Account.getDistanceUnits(this)) {
                case MILES : return VolumeUnits.US_GALLONS.getIntValue();
                default    : return VolumeUnits.LITERS.getIntValue();
            }
        }
    }

    /* set the volume units */
    public void setVolumeUnits(int v)
    {
        this.setFieldValue(FLD_volumeUnits, EnumTools.getValueOf(VolumeUnits.class,v).getIntValue());
    }

    /* set the volume units */
    public void setVolumeUnits(VolumeUnits v)
    {
        this.setFieldValue(FLD_volumeUnits, EnumTools.getValueOf(VolumeUnits.class,v).getIntValue());
    }

    /* set the string representation of the volume units */
    public void setVolumeUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_volumeUnits, EnumTools.getValueOf(VolumeUnits.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Fuel cost per Liter
    *** @return The Fuel cost per Liter
    **/
    public double getFuelCostPerLiter()
    {
        Double v = (Double)this.getFieldValue(FLD_fuelCostPerLiter);
        return (v != null)? v.doubleValue() : 0.0;
    }

    /**
    *** Sets the Fuel cost per Liter
    *** @param v The Fuel cost per Liter
    **/
    public void setFuelCostPerLiter(double v)
    {
        this.setFieldValue(FLD_fuelCostPerLiter, (v >= 0.0)? v : 0.0);
    }

    // ------------------------------------------------------------------------

    /* get the volume units for this account */
    public int getPressureUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_pressureUnits);
        if (v != null) {
            return v.intValue();
        } else {
            switch (Account.getVolumeUnits(this)) {
                case US_GALLONS : return PressureUnits.PSI.getIntValue();
                default         : return PressureUnits.KPA.getIntValue();
            }
        }
    }

    /* set the pressure units */
    public void setPressureUnits(int v)
    {
        this.setFieldValue(FLD_pressureUnits, EnumTools.getValueOf(PressureUnits.class,v).getIntValue());
    }

    /* set the pressure units */
    public void setPressureUnits(PressureUnits v)
    {
        this.setFieldValue(FLD_pressureUnits, EnumTools.getValueOf(PressureUnits.class,v).getIntValue());
    }

    /* set the string representation of the pressure units */
    public void setPressureUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_pressureUnits, EnumTools.getValueOf(PressureUnits.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* get the economy units for this account */
    public int getEconomyUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_economyUnits);
        if (v != null) {
            return v.intValue();
        } else {
            switch (Account.getVolumeUnits(this)) {
                case US_GALLONS : return EconomyUnits.MPG.getIntValue();
                default         : return EconomyUnits.KPL.getIntValue();
            }
        }
    }

    /* set the economy units */
    public void setEconomyUnits(int v)
    {
        this.setFieldValue(FLD_economyUnits, EnumTools.getValueOf(EconomyUnits.class,v).getIntValue());
    }

    /* set the economy units */
    public void setEconomyUnits(EconomyUnits v)
    {
        this.setFieldValue(FLD_economyUnits, EnumTools.getValueOf(EconomyUnits.class,v).getIntValue());
    }

    /* set the economy units */
    public void setEconomyUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_economyUnits, EnumTools.getValueOf(EconomyUnits.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* get the temperature units of the account */
    public int getTemperatureUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_temperatureUnits);
        return (v != null)? v.intValue() : EnumTools.getDefault(TemperatureUnits.class).getIntValue();
    }

    /* set the temperature units */
    public void setTemperatureUnits(int v)
    {
        this.setFieldValue(FLD_temperatureUnits, EnumTools.getValueOf(TemperatureUnits.class,v).getIntValue());
    }

    /* set the temperature units */
    public void setTemperatureUnits(TemperatureUnits v)
    {
        this.setFieldValue(FLD_temperatureUnits, EnumTools.getValueOf(TemperatureUnits.class,v).getIntValue());
    }

    /* set the string representation of the temperature units */
    public void setTemperatureUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_temperatureUnits, EnumTools.getValueOf(TemperatureUnits.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* get the currency units for this account */
    public String getCurrencyUnits()
    {
        String v = (String)this.getFieldValue(FLD_currencyUnits);
        if (!StringTools.isBlank(v)) {
            return v;
        } else {
            return Account.GetDefaultCurrency(); // "usd"
        }
    }

    /* set the currency units */
    public void setCurrencyUnits(String v)
    {
        v = StringTools.trim(v);
        if (StringTools.isBlank(v)) {
            v = Account.GetDefaultCurrency();
        }
        this.setFieldValue(FLD_currencyUnits, v);
    }

    /* set the currency units */
    public void setCurrencyUnits(Currency v)
    {
        this.setFieldValue(FLD_currencyUnits, EnumTools.getValueOf(Currency.class,v).getCode());
    }

    // ------------------------------------------------------------------------

    /* get the Lat/Lon format of the account */
    public int getLatLonFormat()
    {
        Integer v = (Integer)this.getFieldValue(FLD_latLonFormat);
        return (v != null)? v.intValue() : EnumTools.getDefault(LatLonFormat.class).getIntValue();
    }

    /* set the Lat/Lon format */
    public void setLatLonFormat(int v)
    {
        this.setFieldValue(FLD_latLonFormat, EnumTools.getValueOf(LatLonFormat.class,v).getIntValue());
    }

    /* set the Lat/Lon format */
    public void setLatLonFormat(LatLonFormat v)
    {
        this.setFieldValue(FLD_latLonFormat, EnumTools.getValueOf(LatLonFormat.class,v).getIntValue());
    }

    /* set the string representation of the Lat/Lon format */
    public void setLatLonFormat(String v, Locale locale)
    {
        this.setFieldValue(FLD_latLonFormat, EnumTools.getValueOf(LatLonFormat.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* return the geocoder mode for this account */
    public int getGeocoderMode()
    {
        Integer v = (Integer)this.getFieldValue(FLD_geocoderMode);
        return (v != null)? v.intValue() : EnumTools.getDefault(GeocoderMode.class).getIntValue();
    }

    /* set the geocoder mode */
    public void setGeocoderMode(int v)
    {
        this.setFieldValue(FLD_geocoderMode, EnumTools.getValueOf(GeocoderMode.class,v).getIntValue());
    }

    /* set the geocoder mode */
    public void setGeocoderMode(GeocoderMode v)
    {
        this.setFieldValue(FLD_geocoderMode, EnumTools.getValueOf(GeocoderMode.class,v).getIntValue());
    }

    /* set the geocoder mode based on the specified string */
    public void setGeocoderMode(String v, Locale locale)
    {
        this.setFieldValue(FLD_geocoderMode, EnumTools.getValueOf(GeocoderMode.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* get the PrivateLabel name assigned to this account */
    public String getPrivateLabelName()
    {
        String v = (String)this.getFieldValue(FLD_privateLabelName);
        return StringTools.trim(v);
    }

    /* set the PrivateLabel name assigned to this account */
    public void setPrivateLabelName(String v)
    {
        this.setFieldValue(FLD_privateLabelName, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* get the PrivateLabel jsp assigned to this account */
    public String getPrivateLabelJsp()
    {
        String v = (String)this.getOptionalFieldValue(FLD_privateLabelJsp);
        return StringTools.trim(v);
    }

    /* set the PrivateLabel jsp assigned to this account */
    public void setPrivateLabelJsp(String v)
    {
        this.setOptionalFieldValue(FLD_privateLabelJsp, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return the last account login time */
    public long getLastLoginTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastLoginTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the last account login time */
    public void setLastLoginTime(long v)
    {
        this.setFieldValue(FLD_lastLoginTime, v);
    }

    // ------------------------------------------------------------------------

    /* return true if BorderCrossing detection is enabled for this account */
    public boolean getIsBorderCrossing()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_isBorderCrossing);
        return (v != null)? v.booleanValue() : false;
    }

    /* set the 'borderCrossing' enabled state for this account */
    public void setIsBorderCrossing(boolean v)
    {
        this.setFieldValue(FLD_isBorderCrossing, v);
    }

    /* return true if BorderCrossing detection is enabled for this account [see "getIsBorderCrossing()"] */
    public boolean isBorderCrossing()
    {
        return this.getIsBorderCrossing();
    }
    
    private static int borderCrossingExists = -1;
    public static boolean SupportsBorderCrossing()
    {
        if (borderCrossingExists < 0) {
            // NOTE: test may fail if table name translation is enabled
            //borderCrossingExists = (DBFactory.getFactoryByName("BorderCrossing") != null)? 1 : 0;
            try {
                Class.forName(DBConfig.PACKAGE_BCROSS_TABLES_ + "BorderCrossing");
                borderCrossingExists = 1;
            } catch (Throwable th) {
                borderCrossingExists = 0;
            }
        }
        return (borderCrossingExists == 1);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this Account can "auto-add" at least one new devices 
    **/
    public boolean okToAutoAddDevice()
    {
        if (!this.getAutoAddDevices()) {
            return false;
        } else {
            return !this.isAtMaximumDevices(true);
        }
    }

    /**
    *** Gets the 'auto-add' devices enabled state for this account 
    **/
    public boolean getAutoAddDevices()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_autoAddDevices);
        return (v != null)? v.booleanValue() : false;
    }

    /**
    *** Sets the 'auto-add' devices enabled state for this account 
    **/
    public void setAutoAddDevices(boolean v)
    {
        this.setFieldValue(FLD_autoAddDevices, v);
    }

    // ------------------------------------------------------------------------

    /* get the DCS Properties ID assigned to this account */
    public String getDcsPropertiesID()
    {
        String v = (String)this.getFieldValue(FLD_dcsPropertiesID);
        return StringTools.trim(v);
    }

    /* set the DCS Properties ID assigned to this account */
    public void setDcsPropertiesID(String v)
    {
        this.setFieldValue(FLD_dcsPropertiesID, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return true if SMS is enabled for this account */
    public boolean getSmsEnabled()
    {
        switch (Account.GetDefaultSmsEnabledState()) {
            case FALSE  : 
                return false;
            case TRUE   : 
                return true;
            case ACCOUNT:
                Boolean v = (Boolean)this.getFieldValue(FLD_smsEnabled);
                return (v != null)? v.booleanValue() : false;
            default:
                return false;
        }
    }

    /* set the 'smsEnabled' state for this account */
    public void setSmsEnabled(boolean v)
    {
        this.setFieldValue(FLD_smsEnabled, v);
    }

    // ------------------------------------------------------------------------

    /* gets the SMS Gateway Properties assigned to this account */
    public String getSmsProperties()
    {
        String v = (String)this.getFieldValue(FLD_smsProperties);
        return StringTools.trim(v);
    }

    /* sets the SMS Gateway Properties assigned to this account */
    public void setSmsProperties(String v)
    {
        this.setFieldValue(FLD_smsProperties, StringTools.trim(v));
    }

    /* gets the SMS Gateway Properties assigned to this account */
    public RTProperties getSmsRTProperties()
    {
        String v = this.getSmsProperties();
        return new RTProperties(v);
    }

    /* sets the SMS Gateway Properties assigned to this account */
    public void setSmsRTProperties(RTProperties v)
    {
        if (v == null) {
            this.setSmsProperties("");
        } else {
            this.setSmsProperties(v.toString());
        }
    }

    // ------------------------------------------------------------------------

    /* gets the Email Properties assigned to this account */
    public String getEmailProperties()
    {
        String v = (String)this.getFieldValue(FLD_emailProperties);
        return StringTools.trim(v);
    }

    /* sets the Email Properties assigned to this account */
    public void setEmailProperties(String v)
    {
        this.setFieldValue(FLD_emailProperties, StringTools.trim(v));
    }

    /* gets the Email Properties assigned to this account */
    public RTProperties getEmailRTProperties()
    {
        String v = this.getEmailProperties();
        return new RTProperties(v);
    }

    /* sets the Email Properties assigned to this account */
    public void setEmailRTProperties(RTProperties v)
    {
        if (v == null) {
            this.setEmailProperties("");
        } else {
            this.setEmailProperties(v.toString());
        }
    }

    // ------------------------------------------------------------------------

    public String getMapLegendDevice()
    {
        if (this.hasField(FLD_mapLegendDevice)) {
            String v = (String)this.getFieldValue(FLD_mapLegendDevice);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    public void setMapLegendDevice(String v)
    {
        if (this.hasField(FLD_mapLegendDevice)) {
            this.setFieldValue(FLD_mapLegendDevice, ((v != null)? v : ""));
        }
    }

    public String getMapLegendGroup()
    {
        if (this.hasField(FLD_mapLegendGroup)) {
            String v = (String)this.getFieldValue(FLD_mapLegendGroup);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    public void setMapLegendGroup(String v)
    {
        if (this.hasField(FLD_mapLegendGroup)) {
            this.setFieldValue(FLD_mapLegendGroup, ((v != null)? v : ""));
        }
    }

    public void setMapLegend(boolean isFleet, String legend)
    {
        if (isFleet) {
            this.setMapLegendGroup(legend);
        } else {
            this.setMapLegendDevice(legend);
        }
    }

    public String getMapLegend(boolean isFleet)
    {
        if (isFleet) {
            return this.getMapLegendGroup();
        } else {
            return this.getMapLegendDevice();
        }
    }

    // ------------------------------------------------------------------------

    /* return true if AccountManager is supported */
    public static boolean SupportsAccountManager()
    {
        DBFactory<Account> dbFact = Account.getFactory();
        if (!dbFact.hasField(FLD_isAccountManager)) {
            return false;
        } else
        if (!dbFact.hasField(FLD_managerID)) {
            return false;
        } else {
            return true;
        }
    }

    /* return true if this is an account manager */
    public boolean getIsAccountManager()
    {
        if (this.hasField(FLD_isAccountManager)) {
            Boolean v = (Boolean)this.getFieldValue(FLD_isAccountManager);
            return (v != null)? v.booleanValue() : false;
        } else {
            return false;
        }
    }

    /* set the 'accountManager' state for this account */
    public void setIsAccountManager(boolean v)
    {
        if (this.hasField(FLD_isAccountManager)) {
            this.setFieldValue(FLD_isAccountManager, v);
        }
    }

    /* return true if this account is an account manager [see "getIsAccountManager()"] */
    public boolean isAccountManager()
    {
        return this.getIsAccountManager();
    }

    // ------------------------------------------------------------------------

    /* get the manager id */
    public String getManagerID()
    {
        if (this.hasField(FLD_managerID)) {
            String v = (String)this.getFieldValue(FLD_managerID);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    /* set the manager id */
    public void setManagerID(String v)
    {
        if (this.hasField(FLD_managerID)) {
            this.setFieldValue(FLD_managerID, StringTools.trim(v));
        }
    }
    
    /* return true if ManagerID is defined */
    public boolean hasManagerID() 
    {
        return !StringTools.isBlank(this.getManagerID());
    }

    // ------------------------------------------------------------------------

    /* get the data request passcode */
    public String getRequestPassCode()
    {
        if (this.hasField(FLD_requestPassCode)) {
            String v = (String)this.getFieldValue(FLD_requestPassCode);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    /* set the data request passcode */
    public void setRequestPassCode(String v)
    {
        if (this.hasField(FLD_requestPassCode)) {
            this.setFieldValue(FLD_requestPassCode, StringTools.trim(v));
        }
    }

    // ------------------------------------------------------------------------

    /* gets the valid data request IP address */
    public DTIPAddrList getRequestIPAddress()
    {
        if (this.hasField(FLD_requestIPAddress)) {
            DTIPAddrList v = (DTIPAddrList)this.getFieldValue(FLD_requestIPAddress);
            return v; // May return null!!
        } else {
            return null;
        }
    }

    /* sets the valid data request IP address */
    public void setRequestIPAddress(DTIPAddrList v)
    {
        if (this.hasField(FLD_requestIPAddress)) {
            this.setFieldValue(FLD_requestIPAddress, v);
        }
    }

    /* sets the valid data request IP address */
    public void setRequestIPAddress(String v)
    {
        if (this.hasField(FLD_requestIPAddress)) {
            this.setRequestIPAddress((v != null)? new DTIPAddrList(v) : null);
        }
    }

    /* returns true if the specified IP address matches the saved valid IP address */
    public boolean isValidRequestIPAddress(String ipAddr)
    {
        DTIPAddrList ipList = this.getRequestIPAddress();
        if ((ipList == null) || ipList.isEmpty()) {
            return true;
        } else
        if (!ipList.isMatch(ipAddr)) {
            return false;
        } else {
            return true;
        }
    }

    // ------------------------------------------------------------------------

    /* returns true if the dataPushURL field is supported */
    public static boolean SupportsDataPushURL()
    {
        DBFactory<Account> dbFact = Account.getFactory();
        return dbFact.hasField(FLD_dataPushURL);
    }
    
    /* get the data push URL */
    public String getDataPushURL()
    {
        if (this.hasField(FLD_dataPushURL)) {
            String v = (String)this.getFieldValue(FLD_dataPushURL);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    /* set the data push URL */
    public void setDataPushURL(String v)
    {
        if (this.hasField(FLD_dataPushURL)) {
            this.setFieldValue(FLD_dataPushURL, StringTools.trim(v));
        }
    }

    // ------------------------------------------------------------------------

    public long getLastDataRequestTime()
    {
        if (this.hasField(FLD_lastDataRequestTime)) {
            Long v = (Long)this.getFieldValue(FLD_lastDataRequestTime);
            return (v != null)? v.longValue() : 0L;
        } else {
            return 0L;
        }
    }

    public void setLastDataRequestTime(long v)
    {
        if (this.hasField(FLD_lastDataRequestTime)) {
            this.setFieldValue(FLD_lastDataRequestTime, v);
        }
    }

    // ------------------------------------------------------------------------

    public long getLastDataPushTime()
    {
        if (this.hasField(FLD_lastDataPushTime)) {
            Long v = (Long)this.getFieldValue(FLD_lastDataPushTime);
            return (v != null)? v.longValue() : 0L;
        } else {
            return 0L;
        }
    }

    public void setLastDataPushTime(long v)
    {
        if (this.hasField(FLD_lastDataPushTime)) {
            this.setFieldValue(FLD_lastDataPushTime, v);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if ELog/HOS is enabled for the specified Account
    *** @return True if ELog/HOS is enabled for the specified Account
    **/
    public static boolean IsELogEnabled(Account acct)
    {
        return Account.IsELogEnabled(acct, false);
    }

    /**
    *** Returns true if ELog/HOS is enabled for the specified Account
    *** @return True if ELog/HOS is enabled for the specified Account
    **/
    public static boolean IsELogEnabled(Account acct, boolean showReason)
    {

        /* has ELog/HOS package */ 
        if (!DBConfig.hasELogPackage()) {
            if (showReason) { Print.logDebug("ELog/HOS not supported"); }
            return false;
        }

        /* check account */
        if (acct == null) {
            if (showReason) { Print.logDebug("Account is null"); }
            return false;
        } else
        if (!acct.isActive()) {
            if (showReason) { Print.logDebug("Account is inactive: " + acct.getAccountID()); }
            return false;
        } else
        if (acct.isExpired()) {
            if (showReason) { Print.logDebug("Account is expired: " + acct.getAccountID()); }
            return false;
        } else
        if (!acct.getELogEnabled()) {
            if (showReason) { Print.logDebug("Account ELog is not enabled: " + acct.getAccountID()); }
            return false;
        }

        /* enabled */
        return true;

    }

    /**
    *** Returns true if ELog/HOS is enabled for this Account
    *** @return True if ELog/HOS is enabled for this Account
    **/
    public boolean getELogEnabled()
    {
        Boolean v = (Boolean)this.getOptionalFieldValue(FLD_eLogEnabled);
        return (v != null)? v.booleanValue() : false;
    }

    /**
    *** Sets the "ELog/HOS Enabled" state for this Account
    *** @param v The "ELog/HOS Enabled" state for this Account
    **/
    public void setELogEnabled(boolean v)
    {
        this.setOptionalFieldValue(FLD_eLogEnabled, v);
    }

    // --------------------------------

    /**
    *** Gets the ELog/HOS username
    **/
    public String getELogUsername()
    {
        String v = (String)this.getOptionalFieldValue(FLD_eLogUsername);
        return (v != null)? v : "";
    }

    /**
    *** Sets the ELog/HOS username
    **/
    public void setELogUsername(String v)
    {
        this.setOptionalFieldValue(FLD_eLogUsername, StringTools.trim(v));
    }

    // --------------------------------

    /**
    *** Gets the ELog/HOS password 
    **/
    public String getELogPassword()
    {
        String p = (String)this.getOptionalFieldValue(FLD_eLogPassword);
        return (p != null)? p : "";
    }

    /**
    *** Sets the ELog/HOS password 
    **/
    public void setELogPassword(String p)
    {
        this.setOptionalFieldValue(FLD_eLogPassword, ((p != null)? p : ""));
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* overridden to set default values */
    public void setCreationDefaultValues()
    {

        /* always active */
        this.setIsActive(true);

        /* Description and default geocoder mode */
        if (this.isSystemAdmin()) {
            this.setDescription("System Administrator");
            this.setPrivateLabelName(BasicPrivateLabel.ALL_HOSTS);
            this.setGeocoderMode(GeocoderMode.FULL);
            this.setIsAccountManager(true);
        } else {
            this.setDescription("New Account [" + this.getAccountID() + "]");
            this.setGeocoderMode(GeocoderMode.FULL); // <-- default to FULL for now
            this.setIsBorderCrossing(Account.SupportsBorderCrossing());
            String plk[] = this.getDefaultFieldValueKey(Account.FLD_privateLabelName);
            this.setPrivateLabelName(RTConfig.hasProperty(plk,false)? RTConfig.getString(plk,"") : BasicPrivateLabel.ALL_HOSTS);
            this.setIsAccountManager(false);
        }

        /* Notify enabled */
        //this.setAllowNotify(RTConfig.getBoolean(DBConfig.PROP_Device_checkAccountAllowNotify,false));
        this.setAllowNotify(true);

        /* SMS enabled? */
        switch (Account.GetDefaultSmsEnabledState()) {
            case FALSE:
                this.setSmsEnabled(false);
                break;
            case TRUE:
                this.setSmsEnabled(true);
                break;
        }

        /* clear account manager */
        this.setManagerID("");

        /* password */
        String newPass = Account.createRandomPassword(16);
        this.setDecodedPassword(null, newPass); // setPassword

        /* allow overriding values from runtime configuration */
        super.setRuntimeDefaultValues();

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this Account has an "admin" user 
    *** @return True if this Account has an "admin" user 
    **/
    public boolean hasAdminUser()
    {
        try {
            return User.exists(this.getAccountID(), User.getAdminUserID());
        } catch (DBException dbe) {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    private User currentUser = null;

    /**
    *** Sets the current context user 
    *** @param user  The current context user
    **/
    public void setCurrentUser(User user)
    {
        this.currentUser = user;
    }

    /**
    *** Gets the current context user 
    *** @return  The current context user
    **/
    public User getCurrentUser()
    {
        return this.currentUser; // may be null
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the email address to which reports should be emailed
    *** @param account  The account to test for email address, if user not specified
    *** @param user     The overriding user to test for email address first
    *** @return The report email address, or null if no address was found
    **/
    public static String getReportEmailAddress(Account account, User user)
    {
        return (account != null)? account.getReportEmailAddress(user) : null;
    }

    /**
    *** Returns the email address to which reports should be emailed
    *** @param user  The overriding user to test for email address first
    *** @return The report email address, or null if no address was found
    **/
    public String getReportEmailAddress(User user)
    {

        /* try user email address */
        if ((user != null) && user.getAccountID().equals(this.getAccountID())) {
            // Notification email address
            String notifyEmail = user.getNotifyEmail();
            if (!StringTools.isBlank(notifyEmail)) {
                return notifyEmail;
            }
            // Contact email address
            String contactEmail = user.getContactEmail();
            if (!StringTools.isBlank(contactEmail)) {
                return contactEmail;
            }
        }

        /* try account "admin" email address */
        try {
            User adminUser = User.getUser(this, User.getAdminUserID());
            if (adminUser != null) {
                // Notification email address
                String notifyEmail = adminUser.getNotifyEmail();
                if (!StringTools.isBlank(notifyEmail)) {
                    return notifyEmail;
                }
                // Contact email address
                String contactEmail = adminUser.getContactEmail();
                if (!StringTools.isBlank(contactEmail)) {
                    return contactEmail;
                }
            }
        } catch (DBException dbe) {
            Print.logError("Error retrieving Admin user: " + dbe);
        }

        /* Account */
        // Notification email address
        String notifyEmail = this.getNotifyEmail();
        if (!StringTools.isBlank(notifyEmail)) {
            return notifyEmail;
        }
        // Contact email address
        String contactEmail = this.getContactEmail();
        if (!StringTools.isBlank(contactEmail)) {
            return contactEmail;
        }

        /* still not found */
        return null;

    }
    
    // ------------------------------------------------------------------------

    /* get device */
    // Note: may return null if device was not found
    public Device getDevice(String devID)
        throws DBException
    {
        return Device.getDevice(this, devID);
    }

    /* get number of devices for this account */
    public long getDeviceCount()
    {
        try {
            //Print.logInfo("Retrieving count: " + dsel);
            DBWhere dwh = new DBWhere(Device.getFactory());
            String where = dwh.WHERE_(
                dwh.EQ(Device.FLD_accountID,this.getAccountID())
                );
            return DBRecord.getRecordCount(Device.getFactory(), where);
        } catch (DBException dbe) {
            Print.logException("Unable to retrieve DeviceList count", dbe);
            return -1L;
        }
    }

    /* return true if any device has a recent 'lastNotifyTime' ["Save/Alert" action] */
    public boolean hasDeviceLastNotifySince(long sinceTime, User user)
        throws DBException
    {

        /* simple case */
        if (sinceTime < 0L) {
            return true;
        }

        /* invalid User? */
        if ((user != null) && !user.getAccountID().equals(this.getAccountID())) {
            // User account does not match device account (unlikely to occur)
            Print.logError("User Account does not match Device Account");
            return false;
        }

        /* read devices for account with (lastNotifyTime > sinceTime) */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        boolean    found = false;
        try {

            /* select */
            // DBSelect: SELECT * FROM Device WHERE (accountID='acct') ORDER BY accountID,deviceID
            DBSelect<Device> dsel = new DBSelect<Device>(Device.getFactory());
            dsel.setSelectedFields(
                Device.FLD_accountID,
                Device.FLD_deviceID,
                Device.FLD_lastNotifyTime,
                Device.FLD_lastNotifyCode);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.EQ(Device.FLD_accountID,this.getAccountID()),
                    dwh.GT(Device.FLD_lastNotifyTime,sinceTime),
                    dwh.NE(Device.FLD_isActive,0)
                )
            ));
            dsel.setOrderByFields(
                Device.FLD_accountID,
                Device.FLD_deviceID);
            dsel.setLimit(1L);

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String accountID  = rs.getString(Device.FLD_accountID);
                String deviceID   = rs.getString(Device.FLD_deviceID);
                long   notifyTime = rs.getLong(Device.FLD_lastNotifyTime);
                int    notifyCode = rs.getInt(Device.FLD_lastNotifyCode);
                if ((user != null) && !user.isAuthorizedDevice(deviceID)) {
                    // User is not authorized for this device
                    Print.logDebug("User '"+accountID+"/"+user.getUserID()+"' not authorized to device: " + deviceID);
                    continue;
                }
                Print.logDebug("Found Device with recent notification: "+accountID+"/"+deviceID+" ==> "+notifyTime+":"+StatusCodes.GetHex(notifyCode));
                found = true;
                break;
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account Device 'lastNotifyTime'", sqe);
        } finally {
            DBConnection.release(dbc, stmt, rs);
        }

        /* return results */
        return found;

    }

    /* return true if any device has a recent 'lastNotifyTime' */
    // Note: for optimum lookup, the following property should be specified:
    //  Device.keyedLastNotifyTime=true
    public static boolean hasAnyDeviceLastNotifySince(long sinceTime)
        throws DBException
    {

        /* simple case */
        if (sinceTime < 0L) {
            return true;
        }

        /* read devices for account with (lastNotifyTime > sinceTime) */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        boolean    found = false;
        try {

            /* select */
            // DBSelect: SELECT * FROM Device WHERE (accountID='acct') ORDER BY accountID,deviceID
            DBSelect<Device> dsel = new DBSelect<Device>(Device.getFactory());
            dsel.setSelectedFields(
                Device.FLD_accountID,
                Device.FLD_deviceID,
                Device.FLD_lastNotifyTime,
                Device.FLD_lastNotifyCode);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.GT(Device.FLD_lastNotifyTime,sinceTime),
                    dwh.NE(Device.FLD_isActive,0)
                )
            ));
            dsel.setOrderByFields(
                Device.FLD_accountID,
                Device.FLD_deviceID);
            dsel.setLimit(1L);

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                found = true;
                String accountID  = rs.getString(Device.FLD_accountID);
                String deviceID   = rs.getString(Device.FLD_deviceID);
                long   notifyTime = rs.getLong(Device.FLD_lastNotifyTime);
                int    notifyCode = rs.getInt(Device.FLD_lastNotifyCode);
                Print.logInfo("Found Device with recent notification: "+accountID+"/"+deviceID+" ==> "+notifyTime+":"+StatusCodes.GetHex(notifyCode));
                break;
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account Device 'lastNotifyTime'", sqe);
        } finally {
            DBConnection.release(dbc, stmt, rs);
        }

        /* return results */
        return found;

    }

    // ------------------------------------------------------------------------
    
    /* convert field value units */
    public Object convertFieldUnits(DBField field, Object value, boolean inclUnits, Locale locale)
    {

        /* unconvertable */
        if ((field == null) || (value == null)) {
            return value;
        }
        String fldName = field.getName();

        /* field unit type */
        String unitAtt = field.getStringAttribute(DBField.ATTR_UNITS, null);
        if (StringTools.isBlank(unitAtt)) {
            return value;
        }

        /* DBFactory */
        //DBFactory dbFact = field.getFactory();
        //if (dbFact == null) {
        //    return value;
        //}
        //String tblName = dbFact.getTableName();

        /* speed */
        if (unitAtt.equalsIgnoreCase("speed")) {
            SpeedUnits units = Account.getSpeedUnits(this);
            double val = units.convertFromKPH(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* distance */
        if (unitAtt.equalsIgnoreCase("distance")) {
            DistanceUnits units = Account.getDistanceUnits(this);
            double val = units.convertFromKM(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* volume */
        if (unitAtt.equalsIgnoreCase("volume")) {
            VolumeUnits units = Account.getVolumeUnits(this);
            double val = units.convertFromLiters(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* temperature */
        if (unitAtt.equalsIgnoreCase("temp")) {
            TemperatureUnits units = Account.getTemperatureUnits(this);
            double val = units.convertFromC(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* economy */
        if (unitAtt.equalsIgnoreCase("econ")) {
            EconomyUnits units = Account.getEconomyUnits(this);
            double val = units.convertFromKPL(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* percent */
        if (unitAtt.equalsIgnoreCase("percent")) {
            double dval = StringTools.parseDouble(value,0.0);
            double val = (dval < 0.0)? 0.0 : (dval > 1.50)? dval : (dval * 100.0);
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + "%";
            } else {
                return new Double(val);
            }
        }

        /* default to specified field value */
        return value;
        
    }

    // ------------------------------------------------------------------------

    /* return the AccountID */
    public String toString()
    {
        return this.getAccountID();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private BasicPrivateLabel privateLabel = null;
    private boolean           foundPrivateLabel = false;

    /* get PrivateLabel instance for the specified account */
    public static BasicPrivateLabel getPrivateLabel(Account acct)
    {
        return (acct != null)? acct.getPrivateLabel() : null;
    }

    /* get PrivateLabel instance for this account */
    // does not return null;
    public BasicPrivateLabel getPrivateLabel()
    {
        if (this.privateLabel == null) {
            String bplName = this.getPrivateLabelName();
            this.privateLabel = BasicPrivateLabelLoader.getPrivateLabel(bplName);
            if (this.privateLabel == null) {
                Print.logWarn("PrivateLabel not defined! [" + bplName + "]");
                this.privateLabel = new BasicPrivateLabel("null");
                this.foundPrivateLabel = false;
            } else {
                this.foundPrivateLabel = true;
            }
        }
        return this.privateLabel;
    }

    /* return true if this Account has a vlid PrivateLabel */
    public boolean hasPrivateLabel()
    {
        this.getPrivateLabel();
        return this.foundPrivateLabel;
    }

    /**
    *** Gets the Locale for the current Account.
    **/
    public Locale getLocale()
    {
        BasicPrivateLabel bpl = this.getPrivateLabel();
        if (bpl != null) {
            return bpl.getLocale();
        } else {
            // --- will not occur
            return null;
        }
    }

    /**
    *** Gets the Locale for the specified Account.
    *** Returns null if the Account is null
    **/
    public static Locale GetLocale(Account acct)
    {
        return (acct != null)? acct.getLocale() : null;
    }

    // ------------------------------------------------------------------------

    /* get date format */
    public String getDateFormat()
    {
        BasicPrivateLabel privLabel = this.getPrivateLabel();
        if (privLabel != null) {
            return privLabel.getDateFormat();
        } else {
            return BasicPrivateLabel.getDefaultDateFormat();
        }
    }

    /* get date format */
    public static String GetDateFormat(Account acct)
    {
        return (acct != null)? acct.getDateFormat() : BasicPrivateLabel.getDefaultDateFormat();
    }

    // --------------------------------

    /* get time format */
    public String getTimeFormat()
    {
        BasicPrivateLabel privLabel = this.getPrivateLabel();
        if (privLabel != null) {
            return privLabel.getTimeFormat();
        } else {
            return BasicPrivateLabel.getDefaultTimeFormat();
        }
    }

    /* get time format */
    public static String GetTimeFormat(Account acct)
    {
        return (acct != null)? acct.getTimeFormat() : BasicPrivateLabel.getDefaultTimeFormat();
    }

    // --------------------------------

    /* get date/time format */
    public String getDateTimeFormat()
    {
        return this.getDateFormat() + " " + this.getTimeFormat();
    }

    /* get date/time format */
    public static String GetDateTimeFormat(Account acct)
    {
        return Account.GetDateFormat(acct) + " " + Account.GetTimeFormat(acct);
    }

    // --------------------------------

    /* return formated date */
    public String formatDate(DateTime dt)
    {
        if (dt != null) {
            TimeZone tz = this.getTimeZone(null);
            return dt.format(this.getDateFormat(), tz);
        } else {
            return "";
        }
    }

    /* return formated date */
    public String formatTime(DateTime dt)
    {
        if (dt != null) {
            TimeZone tz = this.getTimeZone(null);
            return dt.format(this.getTimeFormat(), tz);
        } else {
            return "";
        }
    }

    /* return formated date */
    public String formatDateTime(DateTime dt)
    {
        if (dt != null) {
            TimeZone tz = this.getTimeZone(null);
            return dt.format(this.getDateTimeFormat(), tz);
        } else {
            return "";
        }
    }
    public String formatDateTime(long dt)
    {
        TimeZone tz = this.getTimeZone(null);
        return (new DateTime(dt,tz)).format(this.getDateTimeFormat(),tz);
    }

    // ------------------------------------------------------------------------

    /* return the "Device" titles */
    public String[] getDeviceTitles(Locale loc)
    {
        return this.getDeviceTitles(loc, Device.GetTitles(loc));
    }

    /* return the "Device" titles */
    public String[] getDeviceTitles(Locale loc, String dft[])
    {
        return AccountString.getStringsArray(this, AccountString.ID_DEVICE, dft);
    }
    
    /* set device title string */
    public void setDeviceTitle(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_DEVICE;
            String desc  = "Device Title"; // no need to localize here
            if (StringTools.isBlank(plural)) { plural = singular; }
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save Device: " + dbe);
        }
    }

    // ------------------------------------------------------------------------

    /* return the default new device description */
    public String getNewDeviceDescription()
    {
        return this.getNewDeviceDescription(null, "");
    }
    
    /* return the default new device description */
    public String getNewDeviceDescription(Locale loc, String dftDesc)
    {
        //I18N i18n = I18N.getI18N(Account.class, loc);
        try {
            AccountString str = AccountString.getAccountString(this, AccountString.ID_DEVICE_NEW_DESCRIPTION);
            return ((str != null) && str.hasSingularTitle())? str.getSingularTitle() : dftDesc;
        } catch (DBException dbe) {
            return dftDesc;
        }
    }

    /* set the default new device description */
    public void setNewDeviceDescription(String singular)
    {
        this.setNewDeviceDescription(singular, null);
    }
    
    /* set the default new device description */
    public void setNewDeviceDescription(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_DEVICE_NEW_DESCRIPTION;
            String desc  = "New Device Description"; // no need to localize here
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save Device: " + dbe);
        }
    }

    // ------------------------------------------------------------------------

    /* return the "Device Group" titles */
    public String[] getDeviceGroupTitles(Locale loc)
    {
        return this.getDeviceGroupTitles(loc, DeviceGroup.GetTitles(loc));
    }

    /* return the "Device Group" titles */
    public String[] getDeviceGroupTitles(Locale loc, String dft[])
    {
        return AccountString.getStringsArray(this, AccountString.ID_DEVICE_GROUP, dft);
    }

    /* set device group title string */
    public void setDeviceGroupTitle(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_DEVICE_GROUP;
            String desc  = "Device Group Title"; // no need to localize here
            if (StringTools.isBlank(plural)) { plural = singular; }
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save DeviceGroup title: " + dbe);
        }
    }

    // ------------------------------------------------------------------------

    /* return the "Entity" title (ie "Trailer") */
    public String[] getEntityTitles(Locale loc)
    {
        String T[] = this.getEntityTitles(loc, null);
        return (T != null)? T : (new String[] { "", "" });
    }

    /* return the "Entity" title (ie "Trailer") */
    public String[] getEntityTitles(Locale loc, String dft[])
    {
        return AccountString.getStringsArray(this, AccountString.ID_ENTITY, dft);
    }

    /* set entity title string */
    public void setEntityTitle(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_ENTITY;
            String desc  = "Entity Title"; // no need to localize here
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save Entity title: " + dbe);
        }
    }

    // ------------------------------------------------------------------------

    /* return the "Address" titles (ie "Address", "Landmark", etc.) */
    public String[] getAddressTitles(Locale loc)
    {
        String T[] = this.getAddressTitles(loc, null);
        return (T != null)? T : (new String[] { "", "" });
    }

    /* return the "Address" titles (ie "Address", "Landmark", etc.) */
    public String[] getAddressTitles(Locale loc, String dft[])
    {
        return AccountString.getStringsArray(this, AccountString.ID_ADDRESS, dft);
    }

    /* set address title string */
    public void setAddressTitle(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_ADDRESS;
            String desc  = "Address Title"; // no need to localize here
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save Address title: " + dbe);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* return list of all Account IDs (NOT SCALABLE) */
    public static Collection<String> getAllAccounts()
        throws DBException
    {
        return Account.getAllAccounts(null);
    }

    /* return list of all Account IDs which have a non-blank dataPushURL (NOT SCALABLE) */
    public static Collection<String> getDataPushAccountIDs()
        throws DBException
    {
        DBFactory<Account> acctFact = Account.getFactory();
        DBSelect<Account> dsel = new DBSelect<Account>(acctFact);
        DBWhere dwh = new DBWhere(acctFact);
        dwh.append(dwh.NE(Account.FLD_dataPushURL, ""));
        dsel.setWhere(dwh);
        dsel.setSelectedFields(Account.FLD_accountID);
        dsel.setOrderByFields(Account.FLD_accountID);
        return Account.getAllAccounts(dsel);
    }

    /* return list of all Account IDs (NOT SCALABLE) */
    public static Collection<String> getAllAccounts(DBSelect<Account> dsel)
        throws DBException
    {

        /* default selection? */
        if (dsel == null) {
            // DBSelect: SELECT accountID FROM Account 
            dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(Account.FLD_accountID);
            dsel.setOrderByFields(Account.FLD_accountID);
        }

        /* read accounts */
        Collection<String> acctList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(Account.FLD_accountID);
                acctList.add(acctId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return acctList;

    }

    /* return list of all Account IDs (NOT SCALABLE) */
    public static Collection<String> getAuthorizedAccounts(Account account)
        throws DBException
    {

        /* invalid account */
        if (account == null) {
            return new Vector<String>(); // TODO: should be immutable
        }

        /* SysAdmin Account? */
        if (account.isSystemAdmin()) {
            // -- all accounts
            return Account.getAllAccounts();
        }

        /* inactive account? */
        if (!account.getIsActive()) {
            // -- not active, not even authorized to self
            return new Vector<String>(); // TOD: should be immutable
        }

        /* manager account? */
        if (account.isAccountManager()) {
            String managerID = account.getManagerID();
            if (!StringTools.isBlank(managerID)) {
                // -- guaranteed to have at least 'account' in the list
                DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
                dsel.setSelectedFields(Account.FLD_accountID, Account.FLD_managerID);
                dsel.setOrderByFields(Account.FLD_accountID);
                DBWhere dwh = dsel.createDBWhere();
                dsel.setWhere(dwh.WHERE(dwh.EQ(Account.FLD_managerID, managerID)));
                return Account.getAllAccounts(dsel);
            }
        }

        /* only authorized to self */
        Collection<String> acctList = new Vector<String>();
        acctList.add(account.getAccountID());
        return acctList;

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified Account-ID exists
    *** @param acctID  The Account-ID to test for existance
    *** @return True if the specified account exists, false otherwise.
    **/
    public static boolean exists(String acctID)
        throws DBException // if error occurs while testing existence
    {
        if (acctID != null) {
            Account.Key actKey = new Account.Key(acctID);
            return actKey.exists();
        }
        return false;
    }

    /**
    *** Gets an Account with the specified Account-ID.  Returns null if the 
    *** Account ID does not exist
    *** @param acctID  The Account-ID to retrieve
    *** @return The retrieved Account, or null if the Account does not exist.
    **/
    public static Account getAccount(String acctID)
        throws DBException // if error occurs while getting record
    {
        if (StringTools.isBlank(acctID)) {
            // invalid AccountID specified
            //Print.logError("Account-ID is null/blank");
            return null;
        } else {
            Account.Key key = new Account.Key(acctID);
            if (key.exists()) {
                return key.getDBRecord(true);
            } else {
                // Account does not exist
                return null;
            }
        }
    }

    /** 
    *** Gets or creates an Account with the specified Account-ID
    *** @param acctID  The Account ID to get or create.
    *** @param create  True to create a nee account, false to get an existing account
    *** @return The created/retrieved Account (does not return null, not yet saved)
    *** @throws DBException if the account already exists and 'create' was specified,
    ***         or if the account does not exist and 'create' was not specified.
    **/
    public static Account getAccount(String acctID, boolean create)
        throws DBException
    {

        /* account-id specified? */
        if (StringTools.isBlank(acctID)) {
            // always throw an exception
            throw new DBNotFoundException("Account-ID not specified.");
        }

        /* get/create account */
        Account acct = null;
        Account.Key acctKey = new Account.Key(acctID);
        if (!acctKey.exists()) { // may throw DBException
            if (create) {
                acct = acctKey.getDBRecord();
                acct.setCreationDefaultValues();
                return acct; // not yet saved!
            } else {
                throw new DBNotFoundException("Account-ID does not exists '" + acctKey + "'");
            }
        } else
        if (create) {
            // we've been asked to create the account, and it already exists
            throw new DBAlreadyExistsException("Account-ID already exists '" + acctKey + "'");
        } else {
            acct = Account.getAccount(acctID); // may throw DBException
            if (acct == null) {
                throw new DBException("Unable to read existing Account-ID '" + acctKey + "'");
            }
            return acct;
        }

    }

    /**
    *** Creates a new Account with the specified ID and password
    *** @param acctMgr The creating account
    *** @param acctID  The account ID to create
    *** @param passwd  The account password
    *** @return The created account
    *** @throws DBException if an error occurs, or if account already exists
    **/
    public static Account createNewAccount(Account acctMgr, String acctID, String passwd)
        throws DBException
    {

        /* validate account id */
        if (StringTools.isBlank(acctID)) {
            throw new DBException("Invalid AccountID specified");
        }

        /* create account */
        Account acct = Account.getAccount(acctID, true); // not yet saved

        /* set password */
        if (passwd != null) { // empty string allowed
            acct.setDecodedPassword(null, passwd);
        }

        /* account manager */
        if (acctMgr != null) {
            if (!Account.isSystemAdmin(acctMgr) && !Account.isAccountManager(acctMgr)) {
                throw new DBNotAuthorizedException("Not Authorized to create accounts");
            }
            acct.setPrivateLabelName(acctMgr.getPrivateLabelName());
            acct.setManagerID(acctMgr.getManagerID());
            acct.setGeocoderMode(acctMgr.getGeocoderMode());
            acct.setIsBorderCrossing(acctMgr.getIsBorderCrossing());
            acct.setDcsPropertiesID(acctMgr.getDcsPropertiesID());
            acct.setSmsEnabled(acctMgr.getSmsEnabled());
            acct.setSmsProperties(acctMgr.getSmsProperties());
        }

        /* save and return */
        acct.save();
        return acct;

    }
    
    /**
    *** Creates a temporary account
    *** @param accountID         The AccountID used for the temporary account. If null/blank a random accountid will be assigned.
    *** @param expireDays        The number of days the account will be available.
    *** @param contactName       The account contact name
    *** @param contactEmail      The account contact email address
    *** @param privateLabelName  The assigned PrivateLabel name
    *** @return The created/saved account
    **/
    public static Account createTemporaryAccount(
        String accountID, int expireDays, String encPass,
        String contactName, String contactEmail, 
        String privateLabelName)
        throws DBException
    {
        Account account = null;
        long nowTime    = DateTime.getCurrentTimeSec();
        long expireSec  = (expireDays > 0L)? DateTime.DaySeconds(expireDays) : Account.DFT_EXPIRATION_SEC;
        if (expireSec > Account.MAX_EXPIRATION_SEC) { expireSec = Account.MAX_EXPIRATION_SEC; }
        long expireTime = (new DateTime((nowTime + expireSec),DateTime.getGMTTimeZone())).getDayEnd();

        // -- make sure we're creating only one account at a time
        synchronized (TempAccountLock) {
            if (!StringTools.isBlank(accountID)) {
                Account.Key acctKey = new Account.Key(accountID);
                if (!acctKey.exists()) { // may throw DBException
                    account = acctKey.getDBRecord();
                }
            } else {
                // -- This temporary account number has granularity of 3 seconds and 
                // -  only repeats every 34 days.
                long tval = (nowTime % DateTime.DaySeconds(34)) / 3; // 0..979200
                // -- we make 3 attempts to create a unique account name
                for (int i = 0; i < 3; i++) {
                    String acctID = "T" + StringTools.format(tval+i,"000000"); // T106052
                    Account.Key acctKey = new Account.Key(acctID);
                    if (!acctKey.exists()) { // may throw DBException
                        account = acctKey.getDBRecord();
                        break;
                    }
                }
            }
        }

        /* set/save account */
        if (account != null) {
            account.setCreationDefaultValues();
            account.setDescription(contactName);
            account.setContactEmail(contactEmail);
            account.setContactName(contactName);
            account.setMaximumDevices(1L);
            account.setPasswdQueryTime(nowTime);
            account.setEncodedPassword(encPass);
            account.setExpirationTime(expireTime);
            account.setPrivateLabelName(privateLabelName);
            account.save();
        }

        return account;
    }

    /**
    *** A list of characters from which the random password is generated
    *** (vowels omitted to avoid spelling words :-)
    **/
    private static final String RANDOM_PASSWORD_ALPHABET = "0123456789bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ.-_@";

    /**
    *** Creates a random password with the specified number of characters
    *** @param length  The length of the created password
    *** @return The created password.
    **/
    public static String createRandomPassword(int length)
    {
        return StringTools.createRandomString(length, RANDOM_PASSWORD_ALPHABET);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns an array of Account-IDs managed by the specified contact email address
    *** @param emailAddr  The contact email address
    *** @return And array of Accounts managed by the specifgied contact email address
    **/
    public static java.util.List<String> getAccountIDsForContactEmail(String emailAddr)
        throws DBException
    {

        /* EMailAddress specified? */
        if (StringTools.isBlank(emailAddr)) {
            throw new DBException("Contact EMail address not specified");
        }

        /* read accounts for contact email */
        java.util.List<String> acctList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT * FROM <TableName> WHERE (contactEmail='email')
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(Account.FLD_accountID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE(dwh.EQ(Account.FLD_contactEmail,emailAddr)));
            // Note: The index on the column FLD_contactEmail is not unique
            // (since null/empty values are allowed and needed)
    
            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(FLD_accountID);
                acctList.add(acctId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Get Account ContactEmail", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        return acctList;
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Returns an array of Account-IDs that have been created more than 11 hours ago,
    *** and have not yet logged in to the system.
    *** @return An array of Account-IDs which have been created, but no-one has yet logged into them.
    **/
    public static String[] getUnconfirmedAccounts()
        throws DBException
    {
        return getUnconfirmedAccounts(Account.MAX_UNCONFIRMED_SEC);
    }

    /**
    *** Returns an array of Account-IDs that have been created more than 'ageSec' seconds ago,
    *** and have not yet logged in to the system.
    *** @param ageSec  The specified 'age' of an existing account, in seconds
    *** @return An array of Account-IDs which have been created, but no-one has yet logged into them.
    **/
    public static String[] getUnconfirmedAccounts(long ageSec)
        throws DBException
    {
        // Return temporary accounts that no one has logged into, and the creation time is greater than 'ageSec' seconds ago.
        
        /* read unconfirmed accounts */
        java.util.List<String> acctList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            long unconfirmedTime = DateTime.getCurrentTimeSec() - ageSec;

            /* select */
            // DBSelect: SELECT accountID FROM Account WHERE ((expirationTime > 0) AND (lastLoginTime = 0) AND (creationTime < time))
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(Account.FLD_accountID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.GT(FLD_expirationTime,0L),              // temporary account (has expiration)
                    dwh.EQ(FLD_lastLoginTime,0L),               // never logged-in
                    dwh.LT(FLD_creationTime,unconfirmedTime)    // > 'ageSec' old
                )
            ));

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(Account.FLD_accountID);
                acctList.add(acctId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Get Unconfirmed Account List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return acctList.toArray(new String[acctList.size()]);

    }
    
    // ------------------------------------------------------------------------

    /**
    *** Returns an array of currently expired Account-IDs
    *** @return An array of currently expired Account-IDs
    **/
    public static String[] getExpiredAccounts()
        throws DBException
    {
        // return active, currently expired, accounts (for purposes of deactivation)
        return getExpiredAccounts(0L, true);
        // the returned accounts can be passed to 'deactivateAccounts(...)' for deactivation
    }

    /**
    *** Returns an array of account-ids which are expired.
    *** @param deltaSec  The number of seconds specifying a range which represents the
    ***                 Accounts which are due to expire within the next 'deltaSec' seconds.
    *** @return An array of Account IDs matching the expiration criteria
    **/
    public static String[] getExpiredAccounts(long deltaSec)
        throws DBException
    {
        // return inactive, past expired (more than deltaSec seconds ago), accounts (for purposes of deletion)
        return getExpiredAccounts(deltaSec, false);
        // the returned accounts can be passed to 'deleteAccounts(...)' for deletion
    }
    
    /**
    *** Returns an array of account-ids which are expired, or active, depending on the
    *** value specified for 'activeState'.
    *** @param deltaSec  The number of seconds specifying a range which represents the
    ***                 Accounts which are due to expire within the next 'deltaSec' seconds.
    *** @param activeState  Accounts matching the specified 'active' state will be returned.
    *** @return An array of Account IDs matching the expiration criteria
    **/
    public static String[] getExpiredAccounts(long deltaSec, boolean activeState)
        throws DBException
    {
        
        /* read unconfirmed accounts */
        java.util.List<String> acctList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            long expTime = DateTime.getCurrentTimeSec() - deltaSec;

            /* select */
            // DBSelect: SELECT accountID FROM Account WHERE ((expirationTime > 0) AND (expirationTime < time))
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(Account.FLD_accountID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.GT(FLD_expirationTime,0L),          // temporary account (has expiration)
                    dwh.LT(FLD_expirationTime,expTime),     // never logged-in
                    dwh.EQ(FLD_isActive,activeState)        // active/inactive
                )
            ));

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(Account.FLD_accountID);
                acctList.add(acctId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Get Expired Account List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return acctList.toArray(new String[acctList.size()]);

    }

    // ------------------------------------------------------------------------

    /**
    *** Deactivates the specified list of Accounts.  A deactivated account is not
    *** able to log-in to the system.
    *** @param acctID  An array of account-ids to deactivate
    **/
    public static void deactivateAccounts(String acctID[])
        throws DBException
    {
        if (acctID != null) {
            for (int i = 0; i < acctID.length; i++) {
                Account account = Account.getAccount(acctID[i]); // may return null
                if (account != null) {
                    Print.logInfo("Deactivating account: " + acctID[i]);
                    if (account.getIsActive()) {
                        account.setIsActive(false);
                        account.save();
                    } else {
                        // already inactive
                    }
                } else {
                    Print.logWarn("[Deactivate] Account not found: " + acctID[i]);
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    
    /**
    *** Deletes the specified Accounts, including all owned Devices, Users, Events, etc.
    *** @param acctID  An array of Account-IDs to delete
    **/
    public static void deleteAccounts(String acctID[])
        throws DBException
    {
        if (acctID != null) {
            for (int i = 0; i < acctID.length; i++) {
                Account.Key acctKey = new Account.Key(acctID[i]);
                Print.logWarn("Deleting Account: " + acctID[i]);
                acctKey.delete(true); // will also delete dependencies
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Count old events for all devices within this account.<br>
    *** Note: Will return -1 if EventData table is InnoDB.
    *** @param oldTimeSec  The Epoch time before which all events will be counted.
    *** @param log True to print the progress to the logging output.
    **/
    public long countOldEvents(long oldTimeSec, boolean log)
        throws DBException
    {
        String acctID  = this.getAccountID();
        String groupID = DeviceGroup.DEVICE_GROUP_ALL;
        if (log) Print.sysPrintln("Counting old events for account "+acctID+" prior to "+(new DateTime(oldTimeSec)));
        return DeviceGroup.countOldEvents(this, groupID, oldTimeSec, log); // -1 for InnoDB
    }

    // ------------------------------------------------------------------------

    /**
    *** Delete old events for all devices within this account.<br>
    *** Note: Will return -1 if EventData table is InnoDB.  
    ***       Old events will still be deleted, however it will still go through the
    ***       motions of attempting to delete events, event if the range is empty.
    *** @param oldTimeSec  The EPoch time before which all events will be deleted.
    ***          If there are no events for a device after this specified time, then 
    ***          the most recent event prior to the specified time will be retained.
    *** @param log True to print the progress to the logging output.
    **/
    public long deleteOldEvents(long oldTimeSec, boolean log)
        throws DBException
    {
        String acctID  = this.getAccountID();
        String groupID = DeviceGroup.DEVICE_GROUP_ALL;
        if (log) Print.sysPrintln("Deleting old events for account "+acctID+" prior to "+(new DateTime(oldTimeSec)));
        return DeviceGroup.deleteOldEvents(this, groupID, oldTimeSec, log); // -1 for InnoDB?
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Accumulate total Events-Per-Second from values stored in each individual Device
    **/
    public static double getEventsPerSecond()
    {
        try {
            return Account._getEventsPerSecond();
        } catch (DBException dbe) {
            return -1.0; // unable to calculate
        }
    }

    /**
    *** Accumulate total Events-Per-Second from values stored in each individual Device
    **/
    public static double _getEventsPerSecond()
        throws DBException
    {
        double accumEPS = 0.0;
        Collection<String> acctList = Account.getAllAccounts();
        long nowTimeMS = System.currentTimeMillis();
        for (String acctID : acctList) {

            /* get account */
            Account account = Account.getAccount(acctID); // may return null
            if (account == null) { // unlikely to be null
                // this should never occur if we started with a valid list
                continue;
            }

            /* devices for account */
            OrderedSet<String> devIDList = Device.getDeviceIDsForAccount(acctID, null/*User*/, false/*inclInactv*/);
            for (String devID : devIDList) {
                Device device = account.getDevice(devID);
                if (device != null) { // unlikely to be null
                    accumEPS += device.getAgedEventsPerSecond(nowTimeMS);
                }
            }

        }
        return accumEPS;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // This section support a method for obtaining human readable information from
    // the EventData record for reporting, or email purposes. (currently this is
    // used by the 'rules' engine when generating notification emails).

    private static final String KEY_ACCOUNT[]       = EventData.KEY_ACCOUNT;
    private static final String KEY_DEVICE_COUNT[]  = EventData.KEY_DEVICE_COUNT;
    private static final String KEY_DATETIME[]      = EventData.KEY_DATETIME;
    private static final String KEY_DATE_YEAR[]     = EventData.KEY_DATE_YEAR;
    private static final String KEY_DATE_MONTH[]    = EventData.KEY_DATE_MONTH;
    private static final String KEY_DATE_DAY[]      = EventData.KEY_DATE_DAY;
    private static final String KEY_DATE_DOW[]      = EventData.KEY_DATE_DOW;
    private static final String KEY_TIME[]          = EventData.KEY_TIME;

    private static final String KEY_EXPIREDATE[]    = new String[] { "accountExpireDate"  , "acctExpireDate" };  // 
    private static final String KEY_EXPIREDAYS[]    = new String[] { "accountExpireDays"  , "acctExpireDays" };  // 

    private static final String KEY_DISKUSAGE[]     = new String[] { "gtsHomeDiskUsage"   , "gtsDiskUsage"   };
    private static final String KEY_DISKFOLDER[]    = new String[] { "gtsHomeDiskFolder"  , "gtsDiskFolder"  };
    private static final String KEY_DISKTOTAL[]     = new String[] { "gtsHomeDiskTotal"   , "gtsDiskTotal"   };
    private static final String KEY_DISKFREE[]      = new String[] { "gtsHomeDiskFree"    , "gtsDiskFree"    };
    private static final String KEY_DISKUSED[]      = new String[] { "gtsHomeDiskUsed"    , "gtsDiskUsed"    };
    private static final String KEY_DISKFREE_PCT[]  = new String[] { "gtsHomeDiskFreePct" , "gtsDiskFreePct" , "gtsDiskFreePercent" };
    private static final String KEY_DISKUSED_PCT[]  = new String[] { "gtsHomeDiskUsedPct" , "gtsDiskUsedPct" , "gtsDiskUsedPercent" };

    public static String getKeyFieldTitle(String key, String arg, Locale locale)
    {
        return Account._getKeyFieldString(
            true/*title*/, key, arg, 
            locale, null/*BasicPrivateLabel*/, null/*Account*/);
    }

    // getFieldValueString
    public String getKeyFieldValue(String key, String arg, BasicPrivateLabel bpl)
    {
        Locale locale = (bpl != null)? bpl.getLocale() : null;
        return this._getKeyFieldString(
            false/*value*/, key, arg, 
            locale, bpl, this);
    }

    public static String _getKeyFieldString(
        boolean getTitle, String key, String arg, 
        Locale locale, BasicPrivateLabel bpl, Account acct)
    {

        /* check for valid field name */
        if (key == null) {
            return null;
        } else
        if ((acct == null) && !getTitle) {
            return null; // account require for value (not for title)
        }
        if ((locale == null) && (bpl != null)) { locale = bpl.getLocale(); }
        I18N i18n = I18N.getI18N(Account.class, locale);
        long now = DateTime.getCurrentTimeSec();

        /* Account/Device fields */
        if (EventData._keyMatch(key,Account.KEY_ACCOUNT)) {
            if (getTitle) {
                return i18n.getString("Account.key.accountDescription", "Account");
            } else {
                return acct.getDescription();
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DEVICE_COUNT)) {
            if (getTitle) {
                return i18n.getString("Account.key.deviceCount", "Device Count");
            } else {
                return String.valueOf(acct.getDeviceCount());
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DATETIME)) {
            if (getTitle) {
                return i18n.getString("Account.key.dateTime", "Date/Time");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampString(now, acct, tmz, bpl);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DATE_YEAR)) {
            if (getTitle) {
                return i18n.getString("Account.key.dateYear", "Year");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampYear(now, tmz);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DATE_MONTH)) {
            if (getTitle) {
                return i18n.getString("Account.key.dateMonth", "Month");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampMonth(now, false, tmz, locale);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DATE_DAY)) {
            if (getTitle) {
                return i18n.getString("Account.key.dateDay", "Day");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampDayOfMonth(now, tmz);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DATE_DOW)) {
            if (getTitle) {
                return i18n.getString("Account.key.dayOfWeek", "Day Of Week");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampDayOfWeek(now, false, tmz, locale);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_TIME)) {
            if (getTitle) {
                return i18n.getString("Account.key.time", "Time");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampString(now, acct, tmz, bpl);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_EXPIREDATE)) {
            if (getTitle) {
                return i18n.getString("Account.key.expireDate", "Expiration Date");
            } else {
                long expTime = acct.getExpirationTime();
                if (expTime > 0L) {
                    if (expTime <= now) {
                        return i18n.getString("Account.key.expireDate.expired", "Expired");
                    } else {
                        TimeZone tmz = acct.getTimeZone(null); // non-null
                        return EventData.getTimestampString(expTime, acct, tmz, bpl);
                    }
                } else {
                    return i18n.getString("Account.key.expireDate.noExpire", "n/a");
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_EXPIREDAYS)) {
            if (getTitle) {
                return i18n.getString("Account.key.expireDays", "Expiration Days");
            } else {
                long expTime = acct.getExpirationTime();
                if (expTime > 0L) {
                    if (expTime <= now) {
                        return i18n.getString("Account.key.expireDays.expired", "Expired");
                    } else {
                        long expSec = expTime - now;
                        String D = String.valueOf(expSec / DateTime.DaySeconds(1));
                        String H = String.valueOf((expSec % DateTime.DaySeconds(1)) / DateTime.HourSeconds(1));
                        return i18n.getString("Account.key.expireDays.daysHours", "{0} Days {1} Hours", new String[]{D,H});
                    }
                } else {
                    return i18n.getString("Account.key.expireDays.noExpire", "n/a");
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKUSAGE)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskUsage", "Disk Usage");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // -- 
                        String folder  = diskDir.toString();
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        double usedMb  = totalMb - freeMb;
                        double freePct = (totalMb > 0.0)? (freeMb / totalMb) : 0.0;
                        // -- Dir=/Volumes/WS/mdflynn/gts  Total=20480mb  Free=13280mb  [65% free]
                        StringBuffer sb = new StringBuffer();
                        sb.append(i18n.getString("Account.key.gtsHomeDiskUsage.directory","Dir"));
                        sb.append("=");
                        sb.append(folder);
                        sb.append("  ");
                        sb.append(i18n.getString("Account.key.gtsHomeDiskUsage.total","Total"));
                        sb.append("=");
                        sb.append(StringTools.format(totalMb,"0"));
                        sb.append("mb  ");
                        sb.append(i18n.getString("Account.key.gtsHomeDiskUsage.free","Free"));
                        sb.append("=");
                        sb.append(StringTools.format(freeMb,"0"));
                        sb.append("mb  [");
                        sb.append(StringTools.format(freePct*100.0,"0"));
                        sb.append("% ");
                        sb.append(i18n.getString("Account.key.gtsHomeDiskUsage.free","Free").toLowerCase());
                        sb.append("]");
                        return sb.toString();
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKFOLDER)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskFolder", "Disk Folder");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        return diskDir.toString();
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKTOTAL)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskTotal", "Disk Total");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        return StringTools.format(totalMb,"0") + "mb";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKFREE)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskFree", "Disk Free");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        return StringTools.format(freeMb,"0") + "mb";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKUSED)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskUsed", "Disk Used");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        double usedMb  = totalMb - freeMb;
                        return StringTools.format(usedMb,"0") + "mb";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKFREE_PCT)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskFreePct", "Disk Free %");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        double freePct = (totalMb > 0.0)? (freeMb / totalMb) : 0.0;
                        return StringTools.format(freePct*100.0,"0") + "%";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKUSED_PCT)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskUsedPct", "Disk Used %");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        double usedMb  = totalMb - freeMb;
                        double usedPct = (totalMb > 0.0)? (usedMb / totalMb) : 0.0;
                        return StringTools.format(usedPct*100.0,"0") + "%";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        }

        /* Account fields */
        if (getTitle) {
            DBField dbFld = Account.getFactory().getField(key);
            if (dbFld != null) {
                return dbFld.getTitle(locale);
            }
            // -- field not found
        } else {
            String fldName = acct.getFieldName(key); // this gets the field name with proper case
            DBField dbFld = (fldName != null)? acct.getField(fldName) : null;
            if (dbFld != null) {
                Object val = acct.getFieldValue(fldName); // straight from table
                if (val == null) { val = dbFld.getDefaultValue(); }
                Account account = acct;
                if (account != null) {
                    val = account.convertFieldUnits(dbFld, val, true/*inclUnits*/, locale);
                    return StringTools.trim(val);
                } else {
                    return dbFld.formatValue(val);
                }
            }
            // -- field not found
        }

        /* try temporary properties */
        if (acct.hasTemporaryProperties()) {
            RTProperties rtp = acct.getTemporaryProperties();
            Object text = (rtp != null)? rtp.getProperty(key,null) : null;
            if (text instanceof I18N.Text) {
                if (getTitle) {
                    // -- all we have is the key name for the title
                    return key;
                } else {
                    // -- return Localized version of value
                    return ((I18N.Text)text).toString(locale);
                }
            }
        }

        // ----------------------------
        // Account key not found

        /* not found */
        //Print.logWarn("Account key not found: " + key);
        return null;

    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below
    
    private static final String ARG_MANAGER[]    = new String[] { "manager"   , "am"    , "m" };
    private static final String ARG_ACCOUNT[]    = new String[] { "account"   , "acct"  , "a", "list", "report" };
    private static final String ARG_DEVICE[]     = new String[] { "device"    , "dev"   , "d" };
    private static final String ARG_DELETE[]     = new String[] { "delete"             };
    private static final String ARG_CREATE[]     = new String[] { "create"             };
    private static final String ARG_NOPASS[]     = new String[] { "nopass"             };
    private static final String ARG_PASSWORD[]   = new String[] { "password"  , "passwd", "pass" };
    private static final String ARG_SET_PASSWD[] = new String[] { "setPasswd" , "setPass", "setPassword" };
    private static final String ARG_DESC[]       = new String[] { "desc"      , "description" };
    private static final String ARG_EDIT[]       = new String[] { "edit"               };
    private static final String ARG_EDITALL[]    = new String[] { "editall"            };
    private static final String ARG_LIST[]       = new String[] { "list"               };
    private static final String ARG_REPORT[]     = new String[] { "report"             };
    private static final String ARG_PRUNE[]      = new String[] { "prune"              };
    private static final String ARG_XML[]        = new String[] { "xml"                };
    private static final String ARG_PRIVLABEL[]  = new String[] { "privLabel" , "pl"   };
    private static final String ARG_LISTTYPE[]   = new String[] { "listType"  , "type" };
    private static final String ARG_EVENTCOUNT[] = new String[] { "eventCount", "ec"   };
    private static final String ARG_CNT_OLD_EV[] = new String[] { "countOldEvents"     };
    private static final String ARG_DEL_OLD_EV[] = new String[] { "deleteOldEvents"    };
    private static final String ARG_CONFIRM_DEL[]= new String[] { "confirmDelete"      };
    private static final String ARG_FIND_EMAIL[] = new String[] { "findEMail"          };

    private static void usage()
    {
        Print.sysPrintln("Usage:");
        Print.sysPrintln("  java ... " + Account.class.getName() + " {options}");
        Print.sysPrintln("Options:");
        Print.sysPrintln("  -account=<AccountID>         Specific AccountID to list/report/delete/create/edit");
        Print.sysPrintln("");
        Print.sysPrintln("  -delete                      Delete specified Account and owned Devices (USE WITH CAUTION!)");
        Print.sysPrintln("  -create                      To create a new Account");
        Print.sysPrintln("  -pass=<pass>                 Set password on Account creation");
        Print.sysPrintln("  -nopass                      Set blank password on Account creation");
        Print.sysPrintln("  -edit                        To edit an existing (or newly created) Account");
        Print.sysPrintln("");
        Print.sysPrintln("  -list[=<AccountID>]          List all Accounts and owned Devices");
        Print.sysPrintln("  -report[=<AccountID>]        Report on Accounts total/active Devices");
        Print.sysPrintln("  -manager=<ManagerID>         Confine '-list' and '-report' to specified ManagerID");
        Print.sysPrintln("");
        Print.sysPrintln("  -findEMail=<EmailAddr>       Find all references to specified email address");
        Print.sysPrintln("");
        Print.sysPrintln("  -countOldEvents=<EpochTime>  Count events (for all Devices) before specified Epoch time");
        Print.sysPrintln("  -deleteOldEvents=<EpochTime> Delete events (for all Devices) before specified Epoch time (requires '-confirm')");
        Print.sysPrintln("  -confirm                     Confirms countOldEvents/deleteOldEvents");
        Print.sysPrintln("");
        Print.sysPrintln("  -prune                       Deactivate/Delete expired accounts");
        System.exit(1);
    }
    
    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);  // main
        String acctID = RTConfig.getString(ARG_ACCOUNT, "");
        if (acctID == null) { acctID = ""; }
        boolean hasAccountID = !StringTools.isBlank(acctID);

        /* "-device" specified? */
        if (RTConfig.hasProperty(ARG_DEVICE)) {
            // Checks the case where "admin Account ..." was specified when "admin Device ..." was intended.
            Print.logError("Cannot specify '-device' on Account admin command");
            System.exit(99);
        }

        /* account exists? */
        boolean accountExists = false;
        if (hasAccountID) {
            try {
                accountExists = Account.exists(acctID);
            } catch (DBException dbe) {
                Print.logError("Error determining if Account exists: " + acctID);
                System.exit(99);
            }
        } else {
            //Print.logWarn("Account-ID not specified");
            accountExists = false;
        }

        /* option count */
        int opts = 0;

        /* delete */
        if (RTConfig.getBoolean(ARG_DELETE, false) && hasAccountID) {
            opts++;
            if (!accountExists) {
                Print.logWarn("Account-ID does not exist: " + acctID);
                Print.logWarn("Continuing with delete process ...");
            } 
            try {
                Account.deleteAccounts(new String[] { acctID });
                Print.logInfo("Account-ID deleted: " + acctID);
                accountExists = false;
            } catch (DBException dbe) {
                Print.logError("Error deleting Account-ID: " + acctID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* create default account */
        if (RTConfig.getBoolean(ARG_CREATE, false) && hasAccountID) {
            opts++;
            if (accountExists) {
                Print.logWarn("Account-ID already exists: " + acctID);
            } else {
                try {
                    String passwd = null;
                    if (RTConfig.getBoolean(ARG_NOPASS,false)) {
                        passwd = Account.BLANK_PASSWORD;
                    } else
                    if (RTConfig.hasProperty(ARG_PASSWORD)) {
                        passwd = RTConfig.getString(ARG_PASSWORD,"");
                    }
                    Account acct = Account.createNewAccount(null, acctID, passwd);
                    if (RTConfig.hasProperty(ARG_DESC)) {
                        try {
                            acct.setDescription(RTConfig.getString(ARG_DESC,""));
                            acct.save();
                        } catch (DBException dbe) {
                            // ignore errors generated when updating the description
                        }
                    }
                    Print.logInfo("Created Account-ID: " + acctID);
                    accountExists = true;
                } catch (DBException dbe) {
                    Print.logError("Error creating Account-ID: " + acctID);
                    dbe.printException();
                    System.exit(99);
                }
            }
        }

        /* xml */
        if (RTConfig.getBoolean(ARG_XML,false) && hasAccountID) {
            opts++;
            if (!accountExists) {
                Print.logError("Account-ID does not exist: " + acctID);
            } else {
                try {
                    Account account = Account.getAccount(acctID,false);
                    DBRecord.printXML(new PrintWriter(System.out), account);
                } catch (DBException dbe) {
                    Print.logError("Error displaying Account-ID: " + acctID);
                    dbe.printException();
                    System.exit(99);
                }
            }
            System.exit(0);
        }

        /* edit */
        if ((RTConfig.getBoolean(ARG_EDIT,false) || RTConfig.getBoolean(ARG_EDITALL,false)) && hasAccountID) {
            opts++;
            if (!accountExists) {
                Print.logError("Account-ID does not exist: " + acctID);
            } else {
                boolean adminExists = false;
                try {
                    adminExists = User.exists(acctID, User.getAdminUserID());
                    if (adminExists) {
                        Print.sysPrintln("(Note: This account has an '"+User.getAdminUserID()+"' user)");
                    }
                } catch (DBException dbe) {
                    Print.logError("Error determining if User exists: "+acctID+","+User.getAdminUserID());
                }
                try {
                    boolean allFlds = RTConfig.getBoolean(ARG_EDITALL,false);
                    Account account = Account.getAccount(acctID,false); // does NOT return null
                    DBEdit  editor  = new DBEdit(account);
                    editor.edit(allFlds); // may throw IOException
                } catch (IOException ioe) {
                    if (ioe instanceof EOFException) {
                        Print.logError("End of input");
                    } else {
                        Print.logError("IO Error");
                    }
                } catch (DBException dbe) {
                    Print.logError("Error editing Account-ID: " + acctID);
                    dbe.printException();
                    System.exit(99);
                }
            }
            System.exit(0);
        }

        /* set password */
        if (RTConfig.hasProperty(ARG_SET_PASSWD)) {
            opts++;
            // account exists
            if (!accountExists) {
                Print.logError("Account-ID does not exist: " + acctID);
                System.exit(99);
            }
            // get password
            String passwd = RTConfig.getString(ARG_SET_PASSWD,null);
            if (StringTools.isBlank(passwd)) {
                Print.logError("Password not specified");
                System.exit(99);
            }
            // set password
            try {
                Account account = Account.getAccount(acctID,false); // does NOT return null
                account.setPassword(passwd);
                account.update(Account.FLD_password);
                Print.logInfo("Set Account password: " + acctID);
            } catch (DBException dbe) {
                Print.logError("Error setting Account-ID password: " + acctID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* report */
        if (RTConfig.hasProperty(ARG_REPORT)) {
            String  plName  = RTConfig.getString(ARG_PRIVLABEL, null);
            String  manager = RTConfig.getString(ARG_MANAGER  , null);
          //String  rptFmt  = "{C:4}{0:-3} {C:+6}{1:-5} {C:+8}{2:-5} {C:+8}{3:18} {C:+20}{4}";
            String  rptFmt  = "{C:4}{0:-3} {C:+6}{1:-5} {C:+8}{3:18} {C:+20}{4}";
            opts++;
            try {
                int acctCount      = 0;
                int tdevCountTotal = 0;
                int tdevCountActiv = 0;
                int tdevCountHasEv = 0;
                Collection<String> acctList = Account.getAllAccounts();
                Print.sysPrintln("");
                for (String acctListID : acctList) {

                    /* specific Account? */
                    if (hasAccountID && !acctID.equals(acctListID)) {
                        // specific account requested, and accountID doesn't match
                        continue;
                    }

                    /* get account */
                    Account account = Account.getAccount(acctListID); // may return null
                    if (account == null) {
                        // this should never occur if we started with a valid list
                        continue;
                    }

                    /* specific PrivateLabel name? */
                    String privLabelName = account.getPrivateLabelName();
                    if (!StringTools.isBlank(plName) && !plName.equalsIgnoreCase(privLabelName)) {
                        // specified PrivateLabel name does not match
                        continue;
                    }

                    /* specific account manager */
                    String managerID = account.getManagerID();
                    if (!StringTools.isBlank(manager)) {
                        if (!managerID.equals(manager)) {
                            // specific account manager does not match
                            continue;
                        }
                        Print.sysPrintln("AccountManager: " + manager);
                        Print.sysPrintln("");
                    }

                    /* count accounts */
                    acctCount++;

                    /* Account information */
                    boolean acctActive = account.getIsActive();
                    String  acctDesc   = account.getDescription();
                    Print.sysPrintln("  Account: " + acctListID + " - " + acctDesc + " [active=" + acctActive + "]");

                    /* Device row header */
                    Print.sysPrintln(StringTools.formatLine(rptFmt, 
                        " ## ", "Active", "Events", "DeviceID"           , "Description"
                        ));
                    Print.sysPrintln(StringTools.formatLine(rptFmt, 
                        "----", "------", "------", "------------------", "-----------------------------"
                        ));

                    /* Device list */
                    OrderedSet<Device> devList = new OrderedSet<Device>();
                    OrderedSet<String> devIDList = Device.getDeviceIDsForAccount(acctListID, null/*User*/, true/*inclInactv*/);
                    int devCntTotal = 0;
                    int devCntActiv = 0;
                    int devCntHasEv = 0;
                    for (String devID : devIDList) {
                        Device device = account.getDevice(devID);
                        if (device != null) {
                            boolean   devActive = acctActive && device.getIsActive();
                            boolean   hasEvents = (device.getLastEventTimestamp() > 0L)? true : false;
                            String    devDesc   = device.getDescription();
                            EventData lastEvent = device.getLastEvent(false);
                            if (!hasEvents && (lastEvent != null)) {
                                hasEvents = true;
                            }

                            /* tally */
                            devList.add(device);
                            devCntTotal++;
                            tdevCountTotal++;
                            if (devActive) {
                                devCntActiv++;
                                tdevCountActiv++;
                            }
                            if (hasEvents || (lastEvent != null)) {
                                devCntHasEv++;
                                tdevCountHasEv++;
                            }

                            /* print */
                            Print.sysPrintln(StringTools.formatLine(rptFmt,
                                String.valueOf(devCntTotal),
                                (devActive? "yes"/*String.valueOf(devCntActiv)*/ : "--"),
                                (hasEvents? "yes" : ""),
                                devID,
                                devDesc));

                        }
                    }

                    /* totals */
                    Print.sysPrintln(StringTools.formatLine(rptFmt,
                        "----",               
                        "------",               
                        "",
                        "",
                        ""));
                    Print.sysPrintln(StringTools.formatLine(rptFmt,
                        String.valueOf(devCntTotal), 
                        String.valueOf(devCntActiv), 
                        "", 
                        "Active Count", 
                        ""));
                    Print.sysPrintln("");

                }
                Print.sysPrintln("---------------------------------");
                Print.sysPrintln("Total  Device Count: " + tdevCountTotal);
                Print.sysPrintln("Active Device Count: " + tdevCountActiv);
                Print.sysPrintln("");
            } catch (DBException dbe) {
                Print.logException("Error listing Accounts", dbe);
                System.exit(99);
            }
            System.exit(0);
        }

        /* list */
        if (RTConfig.hasProperty(ARG_LIST)) {
            String  type     = RTConfig.getString( ARG_LISTTYPE  , "all");
            String  plName   = RTConfig.getString( ARG_PRIVLABEL , null);
            String  manager  = RTConfig.getString( ARG_MANAGER   , null);
            boolean eventCnt = RTConfig.getBoolean(ARG_EVENTCOUNT, false);
            long    nowMS    = System.currentTimeMillis();
            opts++;
            try {
                int    acctCount        = 0;
                int    tdevCountTotal   = 0;
                int    tdevCountActiv   = 0;
                double tdevEventsPerSec = 0.0;
                Collection<String> acctList = Account.getAllAccounts();
                for (String acctListID : acctList) {

                    /* specific Account? */
                    if (hasAccountID && !acctID.equals(acctListID)) {
                        // specific account requested, and accountID doesn't match
                        continue;
                    }

                    /* get account */
                    Account account = Account.getAccount(acctListID); // may return null
                    if (account == null) {
                        // this should never occur if we started with a valid list
                        continue;
                    }

                    /* specific PrivateLabel name? */
                    String privLabelName = account.getPrivateLabelName();
                    if (!StringTools.isBlank(plName) && !plName.equalsIgnoreCase(privLabelName)) {
                        // specified PrivateLabel name does not match
                        continue;
                    }

                    /* specific account manager */
                    String managerID = account.getManagerID();
                    if (!StringTools.isBlank(manager) && !managerID.equals(manager)) {
                        // specific account manager does not match
                        continue;
                    }

                    /* count accounts */
                    acctCount++;

                    /* display account info */
                    if (StringTools.isBlank(type)       || 
                        type.equalsIgnoreCase("all")    ||
                        type.equalsIgnoreCase("active") ||
                        type.equalsIgnoreCase("count")    ) {
                        boolean fullList = !type.equalsIgnoreCase("active");
                        Print.sysPrintln("");

                        /* Account */
                        StringBuffer asb = new StringBuffer();
                        asb.append("Account: " + acctListID + " - " + account.getDescription());
                        asb.append(" [active=").append(account.getIsActive());
                        if (fullList) {
                            asb.append(", hasAdminUser=" + account.hasAdminUser());
                            if (account.isSystemAdmin()) { 
                                asb.append(", isSysAdmin=true"); 
                            }
                        }
                        asb.append("]");
                        // invalid ID
                        if (!AccountRecord.isValidID(acctListID)) {
                            asb.append(" (ID may contain invalid characters)");
                        }
                        Print.sysPrintln(asb.toString());

                        /* private label */
                        Print.sysPrintln("  > PrivateLabel: " + privLabelName);

                        /* Account manager */
                        if (!StringTools.isBlank(managerID)) {
                            Print.sysPrintln("  > Account Manager: " + managerID);
                        }

                        /* last login */
                        long lastLoginTime = account.getLastLoginTime();
                        String lastLoginDt = (lastLoginTime > 0L)? new DateTime(lastLoginTime).toString() : "never";
                        Print.sysPrintln("  > Last Login: " + lastLoginDt);

                        /* expiration */
                        long expireTime = account.getExpirationTime();
                        if (expireTime > 0L) {
                            String expireDt = new DateTime(expireTime).toString();
                            boolean expired = (expireTime < DateTime.getCurrentTimeSec());
                            Print.sysPrintln("  > Expires: " + expireDt + (expired?" [expired]":""));
                        }

                        /* rules */
                        //try {
                        //    String ruleList[] = Rule.getRuleIDs(acctListID, false/*activeOnly*/, true/*cronRules*/, false/*sysRules*/);
                        //    for (int r = 0; r < ruleList.length; r++) {
                        //        Rule rule = Rule.getRule(account, ruleList[r]);
                        //        if (rule != null) {
                        //            Print.sysPrintln("  Rule: " + ruleList[r] + " - " + rule.getDescription() + (rule.isActive()?"":" [inactive]"));
                        //        }
                        //    }
                        //} catch (Throwable th) {
                        //    // ignore
                        //}

                        /* default device authorization */
                        boolean groupALL = DBConfig.GetDefaultDeviceAuthorization(acctListID);
                        if (!groupALL) {
                            Print.sysPrintln("  > Group 'ALL' Allowed: " + groupALL);
                        }

                        /* device count */
                        OrderedSet<Device> devList = new OrderedSet<Device>();
                        OrderedSet<String> devIDList = Device.getDeviceIDsForAccount(acctListID, null/*User*/, true/*inclInactv*/);
                        int devCntTotal = 0;
                        int devCntActiv = 0;
                        for (String devID : devIDList) {
                            Device device = account.getDevice(devID);
                            if (device != null) {
                                devList.add(device);
                                devCntTotal++;
                                tdevCountTotal++;
                                if (account.getIsActive() && device.getIsActive()) {
                                    devCntActiv++;
                                    tdevCountActiv++;
                                }
                                tdevEventsPerSec += device.getAgedEventsPerSecond(nowMS);
                            }
                        }
                        Print.sysPrintln("  > Active Device Count: " + devCntActiv + 
                            ((devCntTotal > devCntActiv)? " [total=" + devCntTotal + "]" : ""));

                        /* devices */
                        if (!type.equalsIgnoreCase("count")) {
                            for (Device device : devList) {
                                String  uniq = device.getDataTransport().getUniqueID(); // default UniqueID
                                boolean actv = account.getIsActive() && device.getIsActive();
                                StringBuffer dsb = new StringBuffer();
                                dsb.append("  ");
                                if (fullList) { dsb.append("Device: "); }
                                dsb.append("[ID=").append(device.getDeviceID());
                                dsb.append(", uniq=").append(uniq);
                                dsb.append(", active=").append(actv);
                                dsb.append("] ");
                                dsb.append(device.getDescription());
                                Print.sysPrintln(dsb.toString());
                                long lastConnectTime = device.getLastConnectTime();
                                String lastConnectDt = (lastConnectTime > 0L)? new DateTime(lastConnectTime).toString() : "never";
                                if (fullList) { 
                                    Print.sysPrintln("    > Last Connect: " + lastConnectDt);
                                    long    epst = device.getLastEventsPerSecondMS();
                                    double  eps  = device.getAgedEventsPerSecond(nowMS);
                                    double  eph  = eps * 60.0 * 60.0; // events per hour
                                    String  ephs = (eph >= 0.01)? StringTools.format(eph,"0.00") : "n/a";
                                    if (eventCnt) {
                                        long evCnt = device.getEventCount(); // -1 for InnoDB?
                                        if (evCnt > 0L) {
                                            EventData lastEv[] = device.getLatestEvents(1L,false);
                                            DateTime lastEventDt = new DateTime(lastEv[0].getTimestamp());
                                            Print.sysPrintln("    > Events: " + evCnt + "  [" + lastEventDt + "] ev/h=" + ephs);
                                        } else 
                                        if (evCnt < 0L) {
                                            Print.sysPrintln("    > Events: unknown (InnoDB?)");
                                        }
                                    } else {
                                        EventData lastEv[] = device.getLatestEvents(1L,false);
                                        if (!ListTools.isEmpty(lastEv)) {
                                            long lastEventTime = lastEv[0].getTimestamp();
                                            String lastEventDt = new DateTime(lastEventTime).toString();
                                            Print.sysPrintln("    > Last Event: [" + lastEventDt + "] ev/h=" + ephs);
                                        } else {
                                            Print.sysPrintln("    > Last Event: n/a");
                                        }
                                    }
                                }
                                /*
                                Collection<String> ruleNames = null;
                                try {
                                    ruleNames = RuleList.getRulesForDevice(acctListID,devId,-1,true);
                                } catch (Throwable th) {
                                    // ignore
                                }
                                if (!ListTools.isEmpty(ruleNames)) {
                                    StringBuffer rsb = new StringBuffer();
                                    for (String ruleListID : ruleNames) {
                                        if (rsb.length() > 0) { rsb.append(", "); }
                                        rsb.append(ruleListID);
                                    }
                                    Print.sysPrintln("    > Rules: " + rsb);
                                }
                                */
                            }
                        }

                    } else
                    if (type.equalsIgnoreCase("comma")) {

                        // comma separated
                        if (acctCount > 1) { Print.sysPrint(","); }
                        Print.sysPrint(acctListID);

                    }

                } // for (String acctID : acctList)
                Print.sysPrintln("");
                Print.sysPrintln("Total Active Device Count: " + tdevCountActiv + 
                    ((tdevCountTotal > tdevCountActiv)? " [total=" + tdevCountTotal + "]" : ""));
                if (tdevEventsPerSec > 0.0) {
                    String eps = StringTools.format(tdevEventsPerSec, "0.000");
                    String eph = StringTools.format(tdevEventsPerSec * 3600.0, "0");
                    Print.sysPrintln("Average Rate of Events   : "+eps+"/second ["+eph+"/hour]");
                }
                Print.sysPrintln("");
            } catch (DBException dbe) {
                Print.logException("Error listing Accounts", dbe);
                System.exit(99);
            }
            System.exit(0);
        }

        /* prune */
        if (RTConfig.getBoolean(ARG_PRUNE, false)) {
            opts++;
            try {
                // delete temporary accounts that no-one has ever logged into
                Account.deleteAccounts(Account.getUnconfirmedAccounts());
                // delete temporary accounts that had expired 3 days ago (and were previously marked inactive)
                Account.deleteAccounts(Account.getExpiredAccounts(DateTime.DaySeconds(3L),false));
                // deactivate temporary accounts that have expired
                Account.deactivateAccounts(Account.getExpiredAccounts(0L,true));
            } catch (DBException dbe) {
                Print.logException("Error pruning Accounts", dbe);
                System.exit(99);
            }
            System.exit(0);
        }

        /* reload test */
        if (RTConfig.getBoolean("reloadTest",false)) {
            opts++;
            if (!accountExists) {
                Print.logError("Account-ID does not exist: " + acctID);
            } else {
                try {
                    Account account = Account.getAccount(acctID,false);
                    Print.sysPrintln("Original TimeZone   : " + account.getTimeZone());
                    Print.sysPrintln("Original AccountType: " + account.getAccountType());
                    account.setTimeZone("US/Hawaii");
                    account.setAccountType(20);
                    Print.sysPrintln("Before   TimeZone   : " + account.getTimeZone());
                    Print.sysPrintln("Before   AccountType: " + account.getAccountType());
                    account.reload(Account.FLD_accountType);
                    Print.sysPrintln("After 1  TimeZone   : " + account.getTimeZone());
                    Print.sysPrintln("After 1  AccountType: " + account.getAccountType());
                    account.setAccountType(3);
                    account.reload(Account.FLD_timeZone);
                    Print.sysPrintln("After 2  TimeZone   : " + account.getTimeZone());
                    Print.sysPrintln("After 2  AccountType: " + account.getAccountType());
                } catch (DBException dbe) {
                    Print.logError("Error displaying Account-ID: " + acctID);
                    dbe.printException();
                    System.exit(99);
                }
            }
            System.exit(0);
        }

        /* count/delete old events */
        if (RTConfig.hasProperty(ARG_CNT_OLD_EV) || 
            RTConfig.hasProperty(ARG_DEL_OLD_EV)   ) {
            opts++;
            boolean deleteEvents = RTConfig.hasProperty(ARG_DEL_OLD_EV);
            String actionText = deleteEvents? "Deleting" : "Counting";
            TimeZone acctTMZ = DateTime.GMT;
            String   argTime = deleteEvents?
                RTConfig.getString(ARG_DEL_OLD_EV,"") :
                RTConfig.getString(ARG_CNT_OLD_EV,"");
            // arg time
            DateTime oldTime = null;
            if (StringTools.isBlank(argTime)) {
                Print.logError("Invalid time specification: " + argTime);
                System.exit(98);
            } else
            if (argTime.equalsIgnoreCase("current")) {
                oldTime = new DateTime(acctTMZ);
            } else {
                try {
                    oldTime = DateTime.parseArgumentDate(argTime,acctTMZ,true); // end of day time
                } catch (DateTime.DateParseException dpe) {
                    oldTime = null;
                }
                if (oldTime == null) {
                    Print.sysPrintln("Invalid Time specification: " + argTime);
                    System.exit(98);
                } else
                if (oldTime.getTimeSec() > DateTime.getCurrentTimeSec()) {
                    Print.sysPrintln(actionText + " future events not allowed");
                    System.exit(98);
                }
            }
            // Accounts
            Collection<String> acctList = null;
            if (accountExists) {
                // ie. "-account=jones"
                acctList = new Vector<String>();
                acctList.add(acctID);
            } else
            if (acctID.equalsIgnoreCase("all")) {
                // ie. "-account=all"
                try {
                    acctList = Account.getAllAccounts();
                } catch (DBException dbe) {
                    acctList = null;
                }
            } else
            if (acctID.endsWith("*") || acctID.endsWith("%")) {
                // ie. "-account=falcon*"
                String partAcctID = acctID.substring(0,acctID.length()-1); // remove trailing '*'/'%'
                if (!StringTools.isBlank(partAcctID) && (partAcctID.indexOf(",") < 0)) {
                    acctList = new Vector<String>();
                    try {
                        Collection<String> alist = Account.getAllAccounts();
                        for (String aid : alist) {
                            if (aid.startsWith(partAcctID)) {
                                acctList.add(aid);
                            }
                        }
                    } catch (DBException dbe) {
                        acctList = null;
                    }
                }
            } else 
            if (acctID.indexOf(",") >= 0) {
                // ie. "-account=smith,jones,max"
                acctList = ListTools.toList(StringTools.split(acctID,','));
            }
            // remove accounts that should not be deleted
            for (String A : acctList) {
                //
            }
            // stop if no Account in list
            if (ListTools.isEmpty(acctList)) {
                Print.sysPrintln("No Accounts specified");
                System.exit(1);
            }
            // count/delete events
            long    oldTimeSec = oldTime.getTimeSec();
            boolean confirmDel = RTConfig.getBoolean(ARG_CONFIRM_DEL,false);
            try {
                if (deleteEvents) {
                    Print.sysPrintln("Deleting events prior to: " + (new DateTime(oldTimeSec)));
                    if (!confirmDel) {
                        Print.sysPrintln("ERROR: Missing '-"+ARG_CONFIRM_DEL[0]+"', aborting delete ...");
                        System.exit(1);
                    }
                    for (String A : acctList) {
                        if (!StringTools.isBlank(A)) {
                            Account account = Account.getAccount(A);
                            if (account != null) {
                                account.deleteOldEvents(oldTimeSec, true); // InnoDB
                            } else {
                                Print.sysPrintln("WARN: Skipping non-existent Account: " + A);
                            }
                        }
                    }
                } else {
                    Print.sysPrintln("Counting events prior to: " + (new DateTime(oldTimeSec)));
                    for (String A : acctList) {
                        if (!StringTools.isBlank(A)) {
                            Account account = Account.getAccount(A);
                            if (account != null) {
                                account.countOldEvents(oldTimeSec, true); // InnoDB?
                            } else {
                                Print.sysPrintln("WARN: Skipping non-existent Account: " + A);
                            }
                        }
                    }
                }
                System.exit(0);
            } catch (DBException dbe) {
                Print.logError("Error " + actionText + " old events: " + dbe);
                System.exit(99);
            }
        }

        /* find email address */
        if (RTConfig.hasProperty(ARG_FIND_EMAIL)) {
            opts++;
            String emailAddr = RTConfig.getString(ARG_FIND_EMAIL,"");
            Print.sysPrintln("Searching for email address: " + emailAddr);
            // -- Iterate through accounts
            int findCount = 0;
            try {
                Collection<String> AIDList = Account.getAllAccounts();
                for (String AID : AIDList) {
                    Account A = Account.getAccount(AID); // may return null
                    if (A == null) { continue; /* should not occur */ }
                    String AHdr = " Account " + AID;
                    if (A.hasManagerID()) { AHdr += "[" + A.getManagerID() + "]"; }
                    //Print.sysPrintln("  Searching " + AHdr + " ...");
                    // -- Account Contact Email Address
                    String ace = A.getContactEmail();
                    if (StringTools.indexOfIgnoreCase(ace,emailAddr) >= 0) {
                        Print.sysPrintln(AHdr + ": Contact Email [Account."+Account.FLD_contactEmail+"] " + ace);
                        findCount++;
                    }
                    // -- Account Notify Email Address
                    String ane = A.getNotifyEmail();
                    if (StringTools.indexOfIgnoreCase(ane,emailAddr) >= 0) {
                        Print.sysPrintln(AHdr + ": Notify Email [Account."+Account.FLD_notifyEmail+"] " + ane);
                        findCount++;
                    }
                    // -- Users
                    String UIDList[] = User.getUsersForAccount(AID);
                    for (String UID : UIDList) {
                        User U = User.getUser(A, UID);
                        if (U == null) { continue; /* should not occur */ }
                        String UHdr = " User " + AID + "/" + UID;
                        //Print.sysPrintln("    Searching " + UHdr + " ...");
                        // -- Contact Email Address
                        String uce = U.getContactEmail();
                        if (StringTools.indexOfIgnoreCase(uce,emailAddr) >= 0) {
                            Print.sysPrintln(UHdr + ": Contact Email [User."+User.FLD_contactEmail+"] " + uce);
                            findCount++;
                        }
                    }
                    // -- Devices
                    OrderedSet<String> DIDList = Device.getDeviceIDsForAccount(AID, null/*User*/, true/*inclInactv*/);
                    for (String DID : DIDList) {
                        Device D = Device._getDevice(A, DID);
                        if (D == null) { continue; /* should not occur */ }
                        String DHdr = " Device " + AID + "/" + DID;
                        //Print.sysPrintln("    Searching " + DHdr + " ...");
                        // -- Notify Email Address
                        String dne = D.getNotifyEmail(false/*inclAcct*/,false/*inclUser*/,false/*inclGroup*/);
                        if (StringTools.indexOfIgnoreCase(dne,emailAddr) >= 0) {
                            Print.sysPrintln(DHdr + ": Notify Email [Device."+Account.FLD_notifyEmail+"] " + dne);
                            findCount++;
                        }
                        // -- SMS Email Address
                        String sme = D.getSmsEmail();
                        if (StringTools.indexOfIgnoreCase(sme,emailAddr) >= 0) {
                            Print.sysPrintln(DHdr + ": SMS Email [Device."+Account.FLD_notifyEmail+"] " + sme);
                            findCount++;
                        }
                    }
                    // -- Rules (optional - may not be present)
                    RuleFactory ruleFactory = Device.getRuleFactory();
                    if (ruleFactory != null) {
                        String RIDList[] = ruleFactory.getRuleIDs(A); // may be null
                        if (RIDList == null) { RIDList = new String[0]; }
                        for (String RID : RIDList) {
                            String RHdr = " Rule " + AID + "/" + RID;
                            //Print.sysPrintln("    Searching " + RHdr + " ...");
                            // Notify Email Address
                            String rne = StringTools.trim(ruleFactory.getRuleNotifyEmail(A,RID));
                            if (StringTools.indexOfIgnoreCase(rne,emailAddr) >= 0) {
                                Print.sysPrintln(RHdr + ": Notify Email [Rule."+FLD_notifyEmail+"] " + rne);
                                findCount++;
                            }
                        }
                    }
                    // -- ReportJob (optional - may not be present)
                    String rjClassName = DBConfig.PACKAGE_EXTRA_TABLES_ + "ReportJob";
                    try {
                        Class rjClass = Class.forName(rjClassName);
                        MethodAction getRJs = new MethodAction(rjClass, "getReportJobsForAccount", String.class);
                        Object rjList[] = (Object[])getRJs.invoke(AID);
                        if (!ListTools.isEmpty(rjList)) {
                            for (Object rj : rjList) {
                                // -- get "getReportJobID"
                                MethodAction maGetID = new MethodAction(rj,"getReportJobID");
                                String RJID = (String)maGetID.invoke();
                                String RJHdr = " ReportJob " + AID + "/" + RJID;
                                // -- get recipients
                                MethodAction maGetRecip = new MethodAction(rj,"getRecipients");
                                String rjRecip = StringTools.trim(maGetRecip.invoke());
                                if (StringTools.indexOfIgnoreCase(rjRecip,emailAddr) >= 0) {
                                    Print.sysPrintln(RJHdr + ": ReportJob Email [ReportJob.recipients] " + rjRecip);
                                    findCount++;
                                }
                            }
                        }
                    } catch (Throwable th) {
                        // skip
                    }
                    // ---
                }
                if (findCount <= 0) {
                    Print.sysPrintln("Email address not found");
                } else {
                    Print.sysPrintln("Found " + findCount + " matches");
                }
            } catch (DBException dbe) {
                Print.logException("Account error", dbe);
                System.exit(99);
            }
            System.exit(0);
        }

        /* no options specified */
        if (opts == 0) {
            Print.logWarn("Missing options ...");
            usage();
        }

    }

}

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
//  2008/07/21  Martin D. Flynn
//     -Initial release
//  2010/11/29  Martin D. Flynn
//     -Added "default=" attribute
// ----------------------------------------------------------------------------
package org.opengts.war.track.taglib;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.war.tools.*;
import org.opengts.war.track.*;

public class TrackTag 
    extends BodyTagSupport
    implements Constants
{

    // ------------------------------------------------------------------------

    public  static final String SECTION_REQUESTPROPS            = CommonServlet.SECTION_REQUESTPROPS;
    public  static final String SECTION_STYLESHEET              = CommonServlet.SECTION_STYLESHEET;
    public  static final String SECTION_JAVASCRIPT              = CommonServlet.SECTION_JAVASCRIPT;
    
    public  static final String SECTION_PAGE_NAME               = CommonServlet.SECTION_PAGE_NAME;
    public  static final String SECTION_PAGE_URL                = CommonServlet.SECTION_PAGE_URL;

    public  static final String SECTION_CSSFILE                 = CommonServlet.SECTION_CSSFILE;

    public  static final String SECTION_BODY_ONLOAD             = CommonServlet.SECTION_BODY_ONLOAD;
    public  static final String SECTION_BODY_ONUNLOAD           = CommonServlet.SECTION_BODY_ONUNLOAD;
    
    public  static final String SECTION_BANNER_IMAGE            = CommonServlet.SECTION_BANNER_IMAGE;
    public  static final String SECTION_BANNER_IMAGE_SOURCE     = CommonServlet.SECTION_BANNER_IMAGE_SOURCE;
    public  static final String SECTION_BANNER_IMAGE_WIDTH      = CommonServlet.SECTION_BANNER_IMAGE_WIDTH;
    public  static final String SECTION_BANNER_IMAGE_HEIGHT     = CommonServlet.SECTION_BANNER_IMAGE_HEIGHT;
    public  static final String SECTION_BANNER_WIDTH            = CommonServlet.SECTION_BANNER_WIDTH;
    public  static final String SECTION_BANNER_STYLE            = CommonServlet.SECTION_BANNER_STYLE;

    public  static final String SECTION_NAVIGATION              = CommonServlet.SECTION_NAVIGATION;

    public  static final String SECTION_MENU_STYLE              = "expandmenu.style";
    public  static final String SECTION_MENU_JAVASCRIPT         = "expandmenu.javascript";
    public  static final String SECTION_MENU                    = "expandmenu";

    public  static final String SECTION_CONTENT_CLASS_TABLE     = CommonServlet.SECTION_CONTENT_CLASS_TABLE;
    public  static final String SECTION_CONTENT_CLASS_CELL      = CommonServlet.SECTION_CONTENT_CLASS_CELL;
    public  static final String SECTION_CONTENT_CLASS_MESSAGE   = CommonServlet.SECTION_CONTENT_CLASS_MESSAGE;
    public  static final String SECTION_CONTENT_ID_MESSAGE      = CommonServlet.SECTION_CONTENT_ID_MESSAGE;
    public  static final String SECTION_CONTENT_MENUBAR         = CommonServlet.SECTION_CONTENT_MENUBAR;
    public  static final String SECTION_CONTENT_BODY            = CommonServlet.SECTION_CONTENT_BODY;
    public  static final String SECTION_CONTENT_MESSAGE         = CommonServlet.SECTION_CONTENT_MESSAGE;

    public  static final String SECTION_REQUEST_CONTEXT         = "request.context";
    
    // ------------------------------------------------------------------------
    
    private static final int    MIN_BANNER_WIDTH                = 860;
    
    private static final char   PropSeparator                   = StringTools.PropertySeparatorSEMIC;
    private static final char   KeyValSeparator[]               = new char[] { StringTools.KeyValSeparatorCOLON };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String section = null;

    /**
    *** Gets the "section" attribute
    *** @return The "section" attribute
    **/
    public String getSection()
    {
        return this.section;
    }
    
    /**
    *** Sets the "section" attribute
    *** @param s  The "section" attribute value
    **/
    public void setSection(String s)
    {
        //Print.sysPrintln("Taglib section=" + s);
        this.section = s;
    }
    
    // ------------------------------------------------------------------------

    private String options = null;

    /**
    *** Gets the "option" attribute
    *** @return The "option" attribute
    **/
    public String getOptions()
    {
        return this.options;
    }
    
    /**
    *** Sets the "option" attribute
    *** @param opt  The "option" attribute value
    **/
    public void setOptions(String opt)
    {
        this.options = opt;
    }
    
    /**
    *** Returns an RTProperties instance based on the supplied options
    *** @return A RTProperties instance, or null if there are no properties
    **/
    public RTProperties getProperties()
    {
        if (!StringTools.isBlank(this.options)) {
            return new RTProperties(this.options,PropSeparator,KeyValSeparator);
        } else {
            return null;
        }
    }
    
    // ------------------------------------------------------------------------

    private String arg = null;

    /**
    *** Gets the "arg" attribute
    *** @return The "arg" attribute
    **/
    public String getArg()
    {
        return this.arg;
    }
    
    /**
    *** Sets the "arg" attribute
    *** @param arg  The "arg" attribute value
    **/
    public void setArg(String arg)
    {
        this.arg = arg;
    }

    // ------------------------------------------------------------------------

    private String dft = null;

    /**
    *** Gets the "default" attribute
    *** @return The "default" attribute
    **/
    public String getDefault()
    {
        return this.dft;
    }
    
    /**
    *** Returns true if a default is defined
    *** @return True is a default is defined
    **/
    public boolean hasDefault()
    {
        return !StringTools.isBlank(this.dft);
    }
    
    /**
    *** Sets the "default" attribute
    *** @param dft  The "default" attribute value
    **/
    public void setDefault(String dft)
    {
        this.dft = dft;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String COMPARE_EQ              = "eq";         // ==
    private static final String COMPARE_NE              = "ne";         // !=
    private static final String COMPARE_GT              = "gt";         // >
    private static final String COMPARE_GE              = "ge";         // >=
    private static final String COMPARE_LT              = "lt";         // <
    private static final String COMPARE_LE              = "le";         // <=
    private static final String COMPARE_DEFINED         = "defined";    // not in set

    private static final String BOOLEAN_TRUE            = "true";
    private static final String BOOLEAN_FALSE           = "false";

    private String  ifKey           = null;
    private String  ifCompare       = null;
    private String  ifCompareType   = COMPARE_EQ;

    /**
    *** Gets the "ifKey" attribute
    *** @return The "ifKey" attribute
    **/
    public String getIfKey()
    {
        return this.getIf();
    }

    /**
    *** Gets the "if" attribute
    *** @return The "if" attribute
    **/
    public String getIf()
    {
        return this.ifKey;
    }

    /**
    *** Gets the "ifDefined" attribute
    *** @return The "ifDefined" attribute
    **/
    public String getIfDefined()
    {
        if ((this.ifCompare != null) && this.ifCompare.equalsIgnoreCase(BOOLEAN_TRUE)) {
            return this.ifKey;
        } else {
            return null;
        }
    }

    /**
    *** Gets the "ifTrue" attribute
    *** @return The "ifTrue" attribute
    **/
    public String getIfTrue()
    {
        if ((this.ifCompare != null) && this.ifCompare.equalsIgnoreCase(BOOLEAN_TRUE)) {
            return this.ifKey;
        } else {
            return null;
        }
    }

    /**
    *** Gets the "ifFalse" attribute
    *** @return The "ifFalse" attribute
    **/
    public String getIfFalse()
    {
        if ((this.ifCompare != null) && this.ifCompare.equalsIgnoreCase(BOOLEAN_FALSE)) {
            return this.ifKey;
        } else {
            return null;
        }
    }

    /**
    *** Gets the comparison value
    *** @return The comparison value
    **/
    public String getValue()
    {
        return !StringTools.isBlank(this.ifCompare)? this.ifCompare : BOOLEAN_TRUE;
    }

    /**
    *** Gets the "compare" type
    *** @return The "compare" type
    **/
    public String getCompare()
    {
        return !StringTools.isBlank(this.ifCompareType)? this.ifCompareType : COMPARE_EQ;
    }

    // --------------------------------

    /**
    *** Sets the "ifKey" attribute
    *** @param k  The "ifKey" attribute value
    **/
    public void setIfKey(String k)
    {
        this.setIf(k);
    }

    /**
    *** Sets the "if" attribute
    *** @param k  The "if" attribute value
    **/
    public void setIf(String k)
    {
        this.ifKey          = k;
        this.ifCompare      = null;
      //this.ifCompareType  = COMPARE_EQ; <-- explicitly set later
    }

    /**
    *** Sets the "ifDefined" attribute
    *** @param k  The "ifDefined" attribute value
    **/
    public void setIfDefined(String k)
    {
        this.ifKey          = k;
        this.ifCompare      = BOOLEAN_TRUE;
        this.ifCompareType  = COMPARE_DEFINED;
    }

    /**
    *** Sets the "ifTrue" attribute
    *** @param k  The "ifTrue" attribute value
    **/
    public void setIfTrue(String k)
    {
        this.ifKey          = k;
        this.ifCompare      = BOOLEAN_TRUE;
        this.ifCompareType  = COMPARE_EQ;
    }

    /**
    *** Sets the "ifFalse" attribute
    *** @param k  The "ifFalse" attribute value
    **/
    public void setIfFalse(String k)
    {
        this.ifKey          = k;
        this.ifCompare      = BOOLEAN_FALSE;
        this.ifCompareType  = COMPARE_EQ;
    }

    /**
    *** Sets the "ifPage" attribute
    *** @param p  The page name
    **/
    public void setIfPage(String p)
    {
        this.ifKey          = RequestProperties.KEY_pageName;
        this.ifCompare      = StringTools.trim(p);
        this.ifCompareType  = COMPARE_EQ;
    }

    /**
    *** Sets the "ifNotPage" attribute
    *** @param p  The page name
    **/
    public void setIfNotPage(String p)
    {
        this.ifKey          = RequestProperties.KEY_pageName;
        this.ifCompare      = StringTools.trim(p);
        this.ifCompareType  = COMPARE_NE;
    }

    /**
    *** Sets the comparison value
    *** @param val  The comparison value
    **/
    public void setValue(String val)
    {
        this.ifCompare = val;
    }

    /**
    *** Sets the "compare" type
    *** @param comp  The "compare" type
    **/
    public void setCompare(String comp)
    {
        this.ifCompareType = comp;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the Session attribute for the specified key
    *** @param key  The attribute key
    *** @param dft  The default value
    *** @return The value for the specified key
    **/
    public String getAttributeValue(String key, String dft)
    {
        if (!StringTools.isBlank(key)) {
            ServletRequest request = super.pageContext.getRequest();
            RequestProperties rp = (RequestProperties)request.getAttribute(PARM_REQSTATE);
            if (rp != null) {
                String v = rp.getKeyValue(key, null/*arg*/, null/*dft*/);
                if (v != null) {
                    return v;
                }
            } else {
                Print.logWarn("RequestProperties is null!!!");
            }
        }
        return dft;

    }

    // "StringTools.KeyValueMap" interface
    public String getKeyValue(String key, String arg, String dft)
    {
        return this.getAttributeValue(key, dft);
    }

    /**
    *** Returns true if the attribute key matches the current comparison value, based on the
    *** comparison type.
    **/
    public boolean isMatch()
    {

        /* key 'ifKey' */
        String ifKY = this.getIfKey();
        if (StringTools.isBlank(ifKY)) {
            // -- key not defined (always true)
            return true;
        }

        /* check "if" comparison */
        String  ifCT  = this.getCompare().toLowerCase();
        String  ifCV  = this.getValue();                        // constant (not null)
        String  ifKV  = this.getAttributeValue(ifKY, null);     // variable (may be null)
        boolean match = this._compare(ifCT, ifCV, ifKV);
        //Print.logInfo("Compare: "+ifKV+" "+ifCT+" "+ifCV+" ==> "+match);

        return match;

    }
    
    private boolean _compare(String ct, String cv, String kv)
    {
        // ct == CompareType
        // cv == CompareValue
        // kv == KeyValue
        boolean match = false;
        if (ct.equals(COMPARE_EQ)) {
            // -- compare equals
            match = (kv != null)?  kv.equalsIgnoreCase(cv) : false;
        } else
        if (ct.equals(COMPARE_NE)) {
            // -- compare not equals
            match = (kv != null)? !kv.equalsIgnoreCase(cv) : true;
        } else
        if (kv == null) {
            // -- no value
            match = false;
        } else
        if (ct.equals(COMPARE_GT)) {
            // -- compare greater-than
            match = (StringTools.parseDouble(kv,0.0) >  StringTools.parseDouble(cv,0.0));
        } else
        if (ct.equals(COMPARE_GE)) {
            // -- compare greater-than-or-equals-to
            match = (StringTools.parseDouble(kv,0.0) >= StringTools.parseDouble(cv,0.0));
        } else
        if (ct.equals(COMPARE_LT)) {
            // -- compare less-than
            match = (StringTools.parseDouble(kv,0.0) <  StringTools.parseDouble(cv,0.0));
        } else
        if (ct.equals(COMPARE_LE)) {
            // -- compare less-than-or-equals-to
            match = (StringTools.parseDouble(kv,0.0) <= StringTools.parseDouble(cv,0.0));
        } else
        if (ct.equals(COMPARE_DEFINED)) {
            // -- compare defined
            boolean def = StringTools.parseBoolean(cv,true);
            if (!RTConfig.hasProperty(kv)) {
                match = !def; // not defined
            } else 
            if (StringTools.isBlank(RTConfig.getString(kv,null))) {
                match = !def; // has blank value
            } else {
                match = def; // defined and non-blank
            }
        } else {
            // -- false
            match = false;
        }
        return match;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public int doStartTag()
        throws JspTagException
    {
        if (this.isMatch()) {
            return EVAL_BODY_BUFFERED;
        } else {
            // -- no-match, do not process this tag-block
            return SKIP_BODY;
        }
    }
    
    public int doEndTag()
        throws JspTagException
    {
        HttpServletRequest request = (HttpServletRequest)super.pageContext.getRequest();
        RequestProperties reqState = (RequestProperties)request.getAttribute(SECTION_REQUESTPROPS);
        PrivateLabel     privLabel = (reqState != null)? reqState.getPrivateLabel() : RequestProperties.NullPrivateLabel;
        JspWriter              out = super.pageContext.getOut();
        String                   s = this.getSection().toLowerCase();

        /* ignore blank section definitions */
        if (StringTools.isBlank(s)) {
            // -- ignore
            return EVAL_PAGE;
        }

        /* not a match? */
        if (!this.isMatch()) {
            // -- ignore
            return EVAL_PAGE;
        }

        // --------------------------------------------------------------------

        /* "onload='...'" */
        if (s.equalsIgnoreCase(SECTION_BODY_ONLOAD)) {
            String bodyOnLoad = (String)request.getAttribute(SECTION_BODY_ONLOAD);
            if (!StringTools.isBlank(bodyOnLoad)) {
                try {
                    out.print(bodyOnLoad);
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }
        
        /* "onunload='...'" */
        if (s.equalsIgnoreCase(SECTION_BODY_ONUNLOAD)) {
            String bodyOnUnload = (String)request.getAttribute(SECTION_BODY_ONUNLOAD);
            if (!StringTools.isBlank(bodyOnUnload)) {
                try {
                    out.print(bodyOnUnload);
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        // --------------------------------------------------------------------

        /* expandMenu style */
        if (s.equalsIgnoreCase(SECTION_MENU_STYLE)) {
            try {
                ExpandMenu.writeStyle(out, reqState);
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString());
            }
            return EVAL_PAGE;
        }

        /* expandMenu javascript */
        if (s.equalsIgnoreCase(SECTION_MENU_JAVASCRIPT)) {
            try {
                ExpandMenu.writeJavaScript(out, reqState);
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString());
            }
            return EVAL_PAGE;
        }

        /* expandMenu */
        if (s.equalsIgnoreCase(SECTION_MENU)) {
            try {
                ExpandMenu.writeMenu(out, reqState, 
                    null/*menuID*/, true/*expandableMenu*/, 
                    false/*showIcon*/, ExpandMenu.DESC_LONG, false/*showMenuHelp*/);
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString());
            }
            return EVAL_PAGE;
        }

        // --------------------------------------------------------------------

        /* content table class */
        if (s.equalsIgnoreCase(SECTION_CONTENT_CLASS_TABLE)) {
            HTMLOutput content = (HTMLOutput)request.getAttribute(SECTION_CONTENT_BODY);
            if (content != null) {
                try {
                    String tableClass = content.getTableClass();
                    out.write(!StringTools.isBlank(tableClass)? tableClass : "contentTableClass");
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        /* content cell class */
        if (s.equalsIgnoreCase(SECTION_CONTENT_CLASS_CELL)) {
            HTMLOutput content = (HTMLOutput)request.getAttribute(SECTION_CONTENT_BODY);
            if (content != null) {
                try {
                    String cellClass = content.getCellClass();
                    out.write(!StringTools.isBlank(cellClass)? cellClass : "contentCellClass");
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        /* content message id */
        if (s.equalsIgnoreCase(SECTION_CONTENT_ID_MESSAGE)) {
            try {
                out.write(CommonServlet.ID_CONTENT_MESSAGE);
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString());
            }
            return EVAL_PAGE;
        }

        /* content message class */
        if (s.equalsIgnoreCase(SECTION_CONTENT_CLASS_MESSAGE)) {
            try {
                out.write(CommonServlet.CSS_CONTENT_MESSAGE);
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString());
            }
            return EVAL_PAGE;
        }

        /* content menubar */
        if (s.equalsIgnoreCase(SECTION_CONTENT_MENUBAR)) {
            HTMLOutput content = (HTMLOutput)request.getAttribute(SECTION_CONTENT_BODY);
            if (content != null) {
                String contentClass = content.getTableClass();
                try {
                    if (ListTools.contains(CommonServlet.CSS_MENUBAR_OK,contentClass)) {
                        MenuBar.writeTableRow(out, reqState.getPageName(), reqState);
                    } else {
                        out.write("<!-- no menubar ['"+contentClass+"'] -->");
                    }
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        /* content message */
        if (s.equalsIgnoreCase(SECTION_CONTENT_MESSAGE)) {
            HTMLOutput content = (HTMLOutput)request.getAttribute(SECTION_CONTENT_BODY);
            String msg = (content != null)? StringTools.trim(content.getTableMessage()) : "";
            try {
                out.write(msg); // TODO: HTML encode?
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString());
            }
            return EVAL_PAGE;
        }

        // --------------------------------------------------------------------

        /* request context path */
        if (s.equalsIgnoreCase(SECTION_REQUEST_CONTEXT)) {
            try {
                out.write(request.getContextPath());
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString());
            }
            return EVAL_PAGE;
        }

        // --------------------------------------------------------------------

        /* CSS file */
        if (s.equalsIgnoreCase(SECTION_CSSFILE)) {
            String cssFilePath = this.getArg();
            if (!StringTools.isBlank(cssFilePath)) {
                try {
                    PrintWriter pw = new PrintWriter(out, out.isAutoFlush());
                    WebPageAdaptor.writeCssLink(pw, reqState, cssFilePath, null);
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        // --------------------------------------------------------------------

        /* Banner Image Height */
        if (s.equalsIgnoreCase(SECTION_BANNER_WIDTH)) {
            // key suffix
            String kSfx = StringTools.trim(this.getArg());
            // property values
            String bannerWidth = privLabel.getStringProperty(PrivateLabel.PROP_Banner_width   + kSfx, null);
            if (StringTools.isBlank(bannerWidth)) {
                bannerWidth = privLabel.getStringProperty(PrivateLabel.PROP_Banner_imageWidth + kSfx, null);
            }
            // minimum valie
            if (StringTools.isBlank(bannerWidth)) {
                bannerWidth = this.hasDefault()? this.getDefault() : "100%";
            } else
            if (!bannerWidth.endsWith("%")) {
                int W = StringTools.parseInt(bannerWidth, 0);
                bannerWidth = String.valueOf((W < MIN_BANNER_WIDTH)? MIN_BANNER_WIDTH : W);
            }
            // generate html
            try {
                out.write(bannerWidth);
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString());
            }
            return EVAL_PAGE;
        }

        /* Banner Style */
        if (s.equalsIgnoreCase(SECTION_BANNER_STYLE)) {
            // key suffix
            String kSfx = StringTools.trim(this.getArg());
            // property values
            String bannerStyle = privLabel.getStringProperty(PrivateLabel.PROP_Banner_style + kSfx, null);
            // generate html
            if (!StringTools.isBlank(bannerStyle)) {
                try {
                    out.write(bannerStyle);
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            } else
            if (this.hasDefault()) {
                try {
                    out.write(this.getDefault());
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        /* Banner Image */
        if (s.equalsIgnoreCase(SECTION_BANNER_IMAGE)) {
            // key suffix
            String kSfx      = StringTools.trim(this.getArg());
            // property values
            String imgLink   = privLabel.getStringProperty(PrivateLabel.PROP_Banner_imageLink   + kSfx, null);
            String imgSrc    = privLabel.getStringProperty(PrivateLabel.PROP_Banner_imageSource + kSfx, null);
            String imgWidth  = privLabel.getStringProperty(PrivateLabel.PROP_Banner_imageWidth  + kSfx, null);
            String imgHeight = privLabel.getStringProperty(PrivateLabel.PROP_Banner_imageHeight + kSfx, null);
            // generate html
            if (!StringTools.isBlank(imgSrc)) {
                StringBuffer sb = new StringBuffer();
                if (!StringTools.isBlank(imgLink)) { 
                    sb.append("<a href='").append(imgLink).append("' target='_blank'>"); 
                }
                sb.append("<img src='").append(imgSrc).append("' border='0'");
                if (!StringTools.isBlank(imgWidth)) {
                    sb.append(" width='").append(imgWidth).append("'");
                }
                if (!StringTools.isBlank(imgHeight)) {
                    sb.append(" height='").append(imgHeight).append("'");
                }
                sb.append(">");
                if (!StringTools.isBlank(imgLink)) {
                    sb.append("</a>");
                }
                try {
                    out.write(sb.toString());
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            } else
            if (this.hasDefault()) {
                try {
                    out.write(this.getDefault());
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        /* Banner Image */
        if (s.equalsIgnoreCase(SECTION_BANNER_IMAGE_SOURCE)) {
            // key suffix
            String kSfx      = StringTools.trim(this.getArg());
            // property values
            String imgSrc    = privLabel.getStringProperty(PrivateLabel.PROP_Banner_imageSource + kSfx, null);
            // generate html
            if (!StringTools.isBlank(imgSrc)) {
                //Print.sysPrintln("Property Image Source: " + imgSrc);
                try {
                    out.write(imgSrc);
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            } else
            if (this.hasDefault()) {
                //Print.sysPrintln("Default Image Source: " + this.getDefault());
                try {
                    out.write(this.getDefault());
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }
        
        /* Banner Image Height */
        if (s.equalsIgnoreCase(SECTION_BANNER_IMAGE_WIDTH)) {
            // key suffix
            String kSfx      = StringTools.trim(this.getArg());
            // property values
            String imgWidth  = privLabel.getStringProperty(PrivateLabel.PROP_Banner_imageWidth  + kSfx, null);
            // generate html
            if (!StringTools.isBlank(imgWidth)) {
                try {
                    out.write(imgWidth);
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            } else
            if (this.hasDefault()) {
                try {
                    out.write(this.getDefault());
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        /* Banner Image Height */
        if (s.equalsIgnoreCase(SECTION_BANNER_IMAGE_HEIGHT)) {
            // key suffix
            String kSfx      = StringTools.trim(this.getArg());
            // property values
            String imgHeight = privLabel.getStringProperty(PrivateLabel.PROP_Banner_imageHeight + kSfx, null);
            // generate html
            if (!StringTools.isBlank(imgHeight)) {
                try {
                    out.write(imgHeight);
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            } else
            if (this.hasDefault()) {
                try {
                    out.write(this.getDefault());
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        // --------------------------------------------------------------------

        /* JavaScript */
        if (s.equalsIgnoreCase(SECTION_JAVASCRIPT)) {
            try {
                // always write "utils.js"
                JavaScriptTools.writeUtilsJS(out, request);
                // check for other javascript 
                Object obj = request.getAttribute(SECTION_JAVASCRIPT);
                if (obj instanceof HTMLOutput) {
                    ((HTMLOutput)obj).write(out); 
                } else {
                    out.write("<!-- Unexpected section type '" + s + "' [" + StringTools.className(obj) + "] -->"); 
                }
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString());
            }
            return EVAL_PAGE;
        }

        // --------------------------------------------------------------------

        /* current page name */
        if (s.equalsIgnoreCase(SECTION_PAGE_NAME)) { // "pagename"
            String pageName = reqState.getPageName();
            if (!StringTools.isBlank(pageName)) {
                try {
                    out.write(pageName);
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }

        // --------------------------------------------------------------------

        /* Page URL */
        if (s.equalsIgnoreCase(SECTION_PAGE_URL)) { // "pageurl"
            String pageName = this.getArg();
            String cmd      = null;
            String cmdArg   = null;
            WebPage wp = privLabel.getWebPage(pageName);
            String url = (wp != null)? wp.encodePageURL(reqState,cmd,cmdArg) : null;
            if (!StringTools.isBlank(url)) {
                try {
                    out.write(url);
                } catch (IOException ioe) {
                    throw new JspTagException(ioe.toString());
                }
            }
            return EVAL_PAGE;
        }
 
        // --------------------------------------------------------------------

        /* HTMLOutput */
        try {
            Object obj = request.getAttribute(s);
            if (obj == null) {
                out.write("<!-- Undefined section '" + s + "' -->"); 
            } else
            if (obj instanceof HTMLOutput) {
                ((HTMLOutput)obj).write(out); 
            } else {
                out.write("<!-- Unexpected section type '" + s + "' [" + StringTools.className(obj) + "] -->"); 
            }
        } catch (IOException ioe) {
            throw new JspTagException(ioe.toString());
        }
        return EVAL_PAGE;

    }

    // ------------------------------------------------------------------------
        
    public void setBodyContent(BodyContent body)
    {
        super.setBodyContent(body);
    }
    
    /**
    *** Invoked before the body of the tag is evaluated but after body content is set
    **/
    public void doInitBody()
        throws JspException
    {
        // invoked after 'setBodyContent'
        super.doInitBody();
    }
    
    /**
    *** Invoked after body content is evaluated
    **/
    public int doAfterBody()
        throws JspException
    {
        // invoked after 'doInitBody'        
        return SKIP_BODY; // EVAL_BODY_TAG loops
    }

    // ------------------------------------------------------------------------

    public void release()
    {
        this.section = null;
        this.options = null;
    }

}

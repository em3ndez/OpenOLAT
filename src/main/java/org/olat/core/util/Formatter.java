/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.util.filter.impl.HtmlMathScanner;


/**
 * enclosing_type Description: <br>
 * A formatter to format locale-specific things (mainly dates and times)
 * 
 * @author Felix Jost
 */
public class Formatter {

	public static final int BYTE_UNIT = 1000;
	private static final DateFormat formatterDateFilesystem = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat formatterDatetimeFilesystem = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss_SSS");
	private static final DateFormat formatterDatetimeWithMinutes = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm");
	private static final DateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static final DateFormat shortFormatDateFileSystem = new SimpleDateFormat("yyyyMMdd");

	private static final Map<Locale,Formatter> localToFormatterMap = new HashMap<>();

	// Pattern to find date formats with only two year digits for dates formatted
	// with . (most languages) and with / (EN). Since the date string is converted
	// to a date the two digit dates are interpreted as first century years which is
	// most likely not what the user meant. 
	private static final Pattern twoYearDatePattern = Pattern.compile("^(\\d{1,2}[\\./]\\d{1,2}[\\./])(\\d{2})$");
	private static final String twentyXX_replacementPattern = "$120$2"; // replace xx with 20xx
	

	private final Locale locale;
	private final Translator translator;
	private final DateFormat shortDateFormat;
	private final DateFormat shortDateWithDayFormat;
	private final DateFormat longDateFormat;
	private final DateFormat shortDateTimeFormat;
	private final DateFormat longDateTimeFormat;
	private final DateFormat shortTimeFormat;
	private final DateFormat mediumTimeFormat;
	

	private final DateTimeFormatter shortTimeFormatter;
	private final DateTimeFormatter shortDateFormatter;
	private final DateTimeFormatter shortDateWithDayFormatter;
	private final DateTimeFormatter shortDateTimeFormatter;
	private final DateTimeFormatter longDateTimeFormatter;

	/**
	 * Constructor for Formatter.
	 */
	private Formatter(Locale locale) {
		this.locale = locale;
		this.translator = Util.createPackageTranslator(Formatter.class, locale);
		// Date only formats
		shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		shortDateFormat.setLenient(false);
		
		
		
		shortDateWithDayFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		shortDateWithDayFormat.setLenient(false);
		
		if (shortDateFormat instanceof SimpleDateFormat sdf && shortDateWithDayFormat instanceof SimpleDateFormat sdfwd) {
			// by default year has only two digits, however most people prefer a four digits year, even in short format
			String pattern = sdf.toPattern().replaceAll("y+","yyyy");
			sdf.applyPattern(pattern);
			sdfwd.applyPattern("EEE, " + pattern);
		}
		longDateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
		longDateFormat.setLenient(false);
		// Time only formats
		shortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		shortTimeFormat.setLenient(false);
		mediumTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
		mediumTimeFormat.setLenient(false);
		// Date and time formats
		shortDateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
		shortDateTimeFormat.setLenient(false);
		if (shortDateTimeFormat instanceof SimpleDateFormat sdf) {
			// by default year has only two digits, however most people prefer a four digits year, even in short format
			String pattern = sdf.toPattern().replaceAll("y+","yyyy");
			sdf.applyPattern(pattern); 
		}
		
		longDateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		longDateTimeFormat.setLenient(false);
		
		shortTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).localizedBy(locale);
		
		
		String shortDateWithDayPattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, null, Chronology.ofLocale(locale), locale);
		shortDateWithDayFormatter = DateTimeFormatter.ofPattern("EEE, " + enhanceYear(shortDateWithDayPattern), locale);
		String shortDatePattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, null, Chronology.ofLocale(locale), locale);
		shortDateFormatter = DateTimeFormatter.ofPattern(enhanceYear(shortDatePattern), locale);
		String shortDateTimePattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, FormatStyle.SHORT, Chronology.ofLocale(locale), locale);
		shortDateTimeFormatter = DateTimeFormatter.ofPattern(enhanceYear(shortDateTimePattern), locale);
		
		longDateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.LONG).localizedBy(locale);
	}
	
	/**
	 * Ensure 4 digits year.
	 * 
	 * @param pattern
	 * @return
	 */
	private static String enhanceYear(String pattern) {
		if(pattern.contains("yyyy")) {
			return pattern;
		}
		if(pattern.contains("yyy")) {
			return pattern.replace("yyy","yyyy");
		}
		if(pattern.contains("yy")) {
			return pattern.replace("yy","yyyy");
		}
		if(pattern.contains("y+")) {
			return pattern.replace("y+","yyyy");
		}
		return pattern;
	}

	/**
	 * get an instance of the Formatter given the locale
	 * 
	 * @param locale the locale which the formatter should use in its operations
	 * @return the instance of the Formatter
	 */
	public static Formatter getInstance(Locale locale) {
		Formatter formatter;
		if(localToFormatterMap.containsKey(locale)) {
			formatter = localToFormatterMap.get(locale);
		} else {
			formatter = new Formatter(locale);
			localToFormatterMap.put(locale, formatter);
		}
		return formatter;
	}
	
	public String dayOfWeek(Date date) {
		DayOfWeek dayOfWeek = getDayOfWeek(date);
		return  dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, locale);
	}
	
	public String dayOfWeekShort(Date date) {
		DayOfWeek dayOfWeek = getDayOfWeek(date);
		return  dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale);
	}
	
	public static DayOfWeek getDayOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		DayOfWeek dayOfWeek;
		switch(cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY: dayOfWeek = DayOfWeek.MONDAY; break;
			case Calendar.TUESDAY: dayOfWeek = DayOfWeek.TUESDAY; break;
			case Calendar.WEDNESDAY: dayOfWeek = DayOfWeek.WEDNESDAY; break;
			case Calendar.THURSDAY: dayOfWeek = DayOfWeek.THURSDAY; break;
			case Calendar.FRIDAY: dayOfWeek = DayOfWeek.FRIDAY; break;
			case Calendar.SATURDAY: dayOfWeek = DayOfWeek.SATURDAY; break;
			case Calendar.SUNDAY: dayOfWeek = DayOfWeek.SUNDAY; break;
			default: dayOfWeek = DayOfWeek.MONDAY;
		}
		return  dayOfWeek;
	}

	/**
	 * Formats the given date in a short format, e.g. 05.12.2015 or 12/05/2015
	 * 
	 * @param date the date
	 * @return a String with the formatted date
	 */
	public String formatDate(Date date) {
		if (date == null) return null;
		synchronized (shortDateFormat) {
			return shortDateFormat.format(date);
		}
	}
	
	public String formatDate(ZonedDateTime date) {
		if (date == null) return null;
		return shortDateFormatter.format(date);
	}
	
	public String formatDateWithDay(Date date) {
		if (date == null) return null;
		synchronized (shortDateWithDayFormat) {
			return shortDateWithDayFormat.format(date);
		}
	}
	
	public String formatDateWithDay(ZonedDateTime date) {
		if (date == null) return null;
		return shortDateWithDayFormatter.format(date);
	}

	/**
	 * Formats the given date in a medium sized format, e.g. 12. Dezember 2015 or December 12, 2015
	 * 
	 * @param date the date
	 * @return a String with the formatted date
	 */
	public String formatDateLong(Date date) {
		if (date == null) return null;
		synchronized (longDateFormat) {
			return longDateFormat.format(date);
		}
	}

	public Date parseDate(String val) throws ParseException {
		synchronized (shortDateFormat) {
			return shortDateFormat.parse(val);
		}
	}
	
	/**
	 * formats the given time period so it is friendly to read
	 * 
	 * @param date the date
	 * @return a String with the formatted time
	 */
	public String formatTime(Date date) {
		if (date == null) return null;
		synchronized (mediumTimeFormat) {
			return mediumTimeFormat.format(date);
		}
	}

	/**
	 * Formats the given date in a short size with date and time, e.g.
	 * 05.12.2015 14:35
	 * 
	 * @param date
	 *            the date
	 * @return a String with the formatted date and time
	 */
	public String formatDateAndTime(Date date) {
		if (date == null) return null;
		synchronized (shortDateTimeFormat) {
			return shortDateTimeFormat.format(date);
		}
	}
	
	public String formatDateAndTime(ZonedDateTime date) {
		if (date == null) return null;
		return shortDateTimeFormatter.format(date);
	}
	

	/**
	 * Formats the given date in a long size with date and time, e.g. Tuesday,
	 * 10. September 2015, 3:48 PM
	 * 
	 * @param date
	 *            the date
	 * @return a String with the formatted date and time
	 */
	public String formatDateAndTimeLong(Date date) {
		if (date == null) return null;
		synchronized (longDateTimeFormat) {
			return longDateTimeFormat.format(date);
		}
	}
	
	public String formatDateAndTimeLong(ZonedDateTime date) {
		if (date == null) return null;
		return longDateTimeFormatter.format(date);
	}
	
	
	/**
	 * Generate a simple date pattern that formats a date using the locale of the
	 * formatter
	 * 
	 * @return
	 */
	public String getSimpleDatePatternForDate() {
		Calendar cal = new GregorianCalendar();
		cal.set( 1999, Calendar.MARCH, 1, 0, 0, 0 );  		
		Date testDate = cal.getTime();
		String formattedDate = formatDate(testDate);
		formattedDate = formattedDate.replace("1999", "%Y");
		formattedDate = formattedDate.replace("99", "%Y");
		formattedDate = formattedDate.replace("03", "%m");
		formattedDate = formattedDate.replace("3", "%m");
		formattedDate = formattedDate.replace("01", "%d");
		formattedDate = formattedDate.replace("1", "%d");
		return formattedDate;
	}
	
	/**
	 * Formats the given date with the ISO 8601 standard also known as 'datetime'
	 * See http://www.w3.org/TR/NOTE-datetime.html for more info.
	 * 
	 * @param d the date to be formatted
	 * @return a String with the formatted date and time
	 */
	public static String formatDatetime(Date d) {
		synchronized (formatDateTime) {
			return formatDateTime.format(d);
		}
	}
	
	/**
	 * Parse the given date with the ISO 8601 standard also known as 'datetime'
	 * See http://www.w3.org/TR/NOTE-datetime.html for more info.
	 * 
	 * @param d the date as string to be parsed
	 * @return The date
	 */
	public static Date parseDatetime(String d) throws ParseException {
		synchronized (formatDateTime) {
			return formatDateTime.parse(d);
		}
	}
	
	/**
	 * Use this for naming files or directories with a timestamp. 
	 * As windows does not like ":" in filenames formatDateAndTime(d) does not work
	 * 
	 * @param d the date to be formatted
	 * @return a String with the formatted date and time
	 */
	public static String formatDatetimeFilesystemSave(Date d) {
		synchronized (formatterDatetimeFilesystem) {
			return formatterDatetimeFilesystem.format(d);
		}
	}
	
	/**
	 * Use this for naming files or directories with a date.
	 * 
	 * @param d The date to be formatted
	 * @return a String with the formatted date
	 */
	public static String formatDateFilesystemSave(Date d) {
		synchronized (formatterDateFilesystem) {
			return formatterDateFilesystem.format(d);
		}
	}
	
	/**
	 * Use this for naming files or directories with a timestamp. No Seconds and millis!
	 * As windows does not like ":" in filenames formatDateAndTime(d) does not work
	 * 
	 * @param d the date to be formatted
	 * @return a String with the formatted date and time
	 */
	public static String formatDatetimeWithMinutes(Date d) {
		synchronized (formatterDatetimeWithMinutes) {
			return formatterDatetimeWithMinutes.format(d);
		}
	}
	
	public static Date parseDatetimeFilesystemSave(String d) throws ParseException {
		synchronized (formatterDatetimeFilesystem) {
			return formatterDatetimeFilesystem.parse(d);
		}
	}
	
	public static String formatShortDateFilesystem(Date d) {
		synchronized (shortFormatDateFileSystem) {
			return shortFormatDateFileSystem.format(d);
		}
	}
	

	/**
	 * formats the given time period so it is friendly to read
	 * 
	 * @param d the date
	 * @return a String with the formatted time
	 */
	public String formatTimeShort(Date d) {
		synchronized (shortTimeFormat) {
			return shortTimeFormat.format(d);
		}
	}
	
	public String formatTimeShort(ZonedDateTime d) {
		if(d == null) return "";
		return shortTimeFormatter.format(d);
	}
	
	public String dayOfWeekName(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int day = c.get(Calendar.DAY_OF_WEEK);

		DateFormatSymbols symbols = new DateFormatSymbols(locale);
		String[] dayNames = symbols.getWeekdays();
		return dayNames[day];
	}
	
	public String formatDateRelative(Date d) {
		Date now = new Date();
		if (DateUtils.isSameDay(d, now)) {
			return translator.translate("today");
		}
		Date yesterday = DateUtils.addDays(now, -1);
		if (DateUtils.isSameDay(d, yesterday)) {
			return translator.translate("yesterday");
		}
		long days = DateUtils.countDays(d, now);
		if (days <= 7) {
			return translator.translate("days.ago", String.valueOf(days));
		}
		return formatDate(d);
	}

	/**
	 * Formats a duration in millis to "XXh YYm ZZs"
	 * @param millis
	 * @return formatted string
	 */
	public static String formatDuration(long millis) {
		long h = millis / (1000*60*60);
		long hmins = h*1000*60*60;
		long m = (millis - hmins)/(1000*60);
		long s = (millis - hmins - m*1000*60)/1000;
		return h + "h " + m + "m " + s + "s";
	}
	
	public static String formatDurationCompact(long millis) {
		long h = millis / (1000*60*60);
		long hmins = h*1000*60*60;
		long m = (millis - hmins)/(1000*60);
		long s = (millis - hmins - m*1000*60)/1000;
		return h + "h " + (m > 0 || s > 0 ? m + "m " : "") + (s > 0 ? s + "s" : "");
	}

	/**
	 * Formats a duration in millis to "XX:YY:ZZ". Removes the hours if zero
	 * 
	 * @param timecode in milliseconds
	 * @return formatted timecode
	 */
	public static String formatTimecode(long timecode) {
		String result =  DurationFormatUtils.formatDuration(timecode, "H:mm:ss", true);
		if (result.startsWith("0:")) {
			// remove empty hours
			result = result.substring(2);
		}
		if (result.startsWith("0")) {
			// remove zero from from 10x minutes (02:23 -> 2:23, 00:14 -> 0:14)
			result = result.substring(1);
		}
		return result;
	}
	
	/**
	 * Formats a duration in millis to "XX:YY".
	 * 
	 * @param timecode in milliseconds
	 * @return formatted timecode
	 */
	public static String formatHourAndSeconds(long timecode) {
		return DurationFormatUtils.formatDuration(timecode, "H:mm", true);
	}
	
	public String formatHrsMin(int minutes) {
		int hours = minutes / 60;
		int remainingMinutes = minutes % 60;
		
		if (hours > 0) {
			return translator.translate("duration.hrs.min", String.valueOf(hours), String.valueOf(remainingMinutes));
		}
		if (remainingMinutes > 0) {
			return translator.translate("duration.min", String.valueOf(remainingMinutes));
		}
		return "";
	}

	public static String formatKBytes(long kBytes) {
		return formatBytes(BYTE_UNIT * kBytes);
	}
	
	/**
	 * Format the given bytes to human readable format
	 * @param bytes the byte count
	 * @return human readable formatted bytes
	 */
	public static String formatBytes(long bytes) {
	    long unit = BYTE_UNIT;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = "kMGTPE".charAt(exp-1) + "";
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	/**
	 * Escape " with &quot; in strings
	 * @param source
	 * @return escaped string
	 */
	public static StringBuilder escapeDoubleQuotes(String source) {
		StringBuilder sb = new StringBuilder(300);
		if (source != null) {
			int len = source.length();
			char[] cs = source.toCharArray();
			for (int i = 0; i < len; i++) {
				char c = cs[i];
				switch (c) {
					case '"':
						sb.append("&quot;");
						break;
					default:
						sb.append(c);
				}
			}
		}
		return sb;
	}
	
	/**
	 * Escape " with \" and ' with \' in strings
	 * @param source
	 * @return escaped string
	 * @deprecated use org.apache.commons.lang.StringEscapeUtils.escapeJavaScript() instead.
	 */
	@Deprecated
	public static StringBuilder escapeSingleAndDoubleQuotes(String source) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(300);
		int len = source.length();
		char[] cs = source.toCharArray();
		for (int i = 0; i < len; i++) {
			char c = cs[i];
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\'':
					sb.append("\\\'");
					break;
				default:
					sb.append(c);
			}
		}
		return sb;
	}

	/**
	 * replace a given String with a different String
	 * 
	 * @param source the source
	 * @param delim the String to replace
	 * @param replacement the replacement String
	 * @return StringBuilder
	 */
	public StringBuilder replace(String source, String delim, String replacement) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(300);
		StringTokenizer st = new StringTokenizer(source, delim);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			sb.append(tok);
			sb.append(replacement);
		}
		return sb;
	}

	/**
	 * truncates the supplied string to len-3 and replaces the last three positions
	 * with ...
	 * 
	 * @param source
	 * @param len
	 * @return truncated string
	 */
	public static String truncate(String source, int len) {
		if (source == null) return null;
		if (source.length() > len && len > 3) return truncate(source, len - 3, "...");
		else return source;
	}
	
	/**
	 * This returns a substring of a len length from the input string, as opposite to the 
	 * <code> truncate </code> method. This should be used for processing strings before writing.
	 * @param source
	 * @param len
	 * @return
	 */
	public static String truncateOnly(String source, int len) {
		if (source == null) return null;
		if (source.length() > len) return source.substring(0, len) ;
		else return source;
	}

	/**
	 * replaces all non ASCII characters in a string and also the most common special characters like by urlencode it
	 * "/" "\" ":" "*" "?" """ ' "<" ">" "|"
	 * @param source
	 * @return a string which is OS independant and save for using on any filesystem
	 */
	public static String makeStringFilesystemSave(String source) {
		try {
			source = removeUndesirableSubstrings(source);
			return URLEncoder.encode(source, "utf-8");
		} catch (UnsupportedEncodingException e) {
			//utf-8 should be supported
		}
		return "";
	}
	
	/**
	 * 
	 * @param source
	 * @return
	 */
	private static String removeUndesirableSubstrings(String source) {
		String returnString = source;
		
		//replace successive dots with only one, for now.
		while(returnString!=null && returnString.indexOf("..")>-1) {				
		  returnString = returnString.replaceAll("\\.\\.", "."); //replace recursive 2 dots with one
		} 
		
		return returnString;
	}
	
		/**
	 * truncates a String: useful to limit in GUI
	 * 
	 * @param source
	 * @param len length of the returned string; if negative, return n chars from
	 *          the end of the string, otherwise from the beginning of the string
	 * @param delim
	 * @return truncated string
	 */
	public static String truncate(String source, int len, String delim) {
		if (source == null) return null;
		int start;
		int stop;
		int alen = source.length();
		if (len > 0) {
			if (alen <= len) return source;
			start = 0;
			stop = (len > alen ? alen : len);
			StringBuilder sb = new StringBuilder(source.substring(start, stop));
			if (alen > len) sb.append(delim);
			return sb.toString();
		}
		start = (len < -alen ? 0 : alen + len);
		stop = alen;
		StringBuilder sb = new StringBuilder(source.substring(start, stop));
		if (-alen <= len) sb.insert(0, delim);
		return sb.toString();
	}

	/**
	 * @return locale of this formatter
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param source
	 * @return escaped string
	 */
	public static StringBuilder escWithBR(String source) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(300);
		int len = source.length();
		char[] cs = source.toCharArray();
		for (int i = 0; i < len; i++) {
			char c = cs[i];
			switch (c) {
				case '"':
					sb.append("&quot;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '&':
					// check on &# first (entities -> don't escape)
					if (i < len - 1 && cs[i + 1] == '#') { // we have # as next char
						sb.append("&");
					} else {
						sb.append("&amp;");
					}
					break;
				case '\n':
					sb.append("<br>");
					break;
				default:
					sb.append(c);
			}
		}
		return sb;
	}
	

	/**
	 * @param source
	 * @return stripped string
	 */
	public static StringBuilder stripTabsAndReturns(String source) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(source.length() + (source.length() / 10));
		int len = source.length();
		char[] cs = source.toCharArray();
		for (int i = 0; i < len; i++) {
			char c = cs[i];
			switch (c) {
				case '\t':
					sb.append("    ");
					break;
				case '\n':
					sb.append("<br>");
					if(cs.length > i + 1 && cs[i + 1] == '\r') {
						i++;
					}
					break;
				case '\r':
					sb.append("<br>");
					if(cs.length > i + 1 && cs[i + 1] == '\n') {
						i++;
					}
					break;
				case '\u2028': // unicode linebreak
					sb.append("<br>");
					break;
				default:
					sb.append(c);
			}
		}
		return sb;
	}

	/**
	 * Wrapp given html code with a wrapper an add code to transform latex
	 * formulas to nice visual characters on the client side. The latex formulas
	 * must be within an HTML element that has the class 'math' attached.
	 * 
	 * @param htmlFragment A html element that might contain an element that has a
	 *          class 'math' with latex formulas
	 * @return
	 */
	public static String formatLatexFormulas(String htmlFragment) {
		if (htmlFragment == null) return "";
		// optimize, reduce MathJax calls on client
		if (new HtmlMathScanner().scan(htmlFragment)) {
			// add math wrapper
			String domid = "mw_" + CodeHelper.getRAMUniqueID();
			String elem = htmlFragment.contains("<div") || htmlFragment.contains("<p") ? "div" : "span";
			StringBuilder sb = new StringBuilder(htmlFragment.length() + 200);
			sb.append("<").append(elem).append(" id=\"").append(domid).append("\">");
			sb.append(htmlFragment);
			sb.append("</").append(elem).append(">");
			sb.append(elementLatexFormattingScript());
			return sb.toString();
		}
		return htmlFragment;
	}

	/**
	 * Html code script to render the latex formulas of a given element id 
	 * @param domid Id of the DOM node containing the elements to render.
	 */
	public static String elementLatexFormattingScript() {
		return "<script>o_info.latexit=true;</script>";
	}
	
	public static String mathJaxConfiguration() {
		StringBuilder sb = new StringBuilder(768);
		sb.append("window.MathJax = {\n")
		  .append("  tex: {\n")
		  .append("    inlineMath: [['$$', '$$'], ['\\\\(', '\\\\)']],\n")
		  .append("    macros: {\n")
		  .append("      exponentialE: \"\\\\mathrm{e}\",\n")
		  .append("      imaginaryI: \"\\\\mathrm{i}\",\n")
		  .append("      differentialD: \"\\\\mathrm{d}\",\n")
		  .append("      larr: \"\\\\leftarrow\",\n")
		  .append("      lrArr: \"\\\\leftrightarrow\",\n")
		  .append("      rarr: \"\\\\rightarrow\"\n")
		  .append("    }\n")
		  .append("  },\n")
		  .append("  options: {\n")
		  .append("    enableMenu: false,\n")
		  .append("    processHtmlClass: 'tex2jax_process',\n")
		  .append("    renderActions: {\n")
		  .append("      findScripts: [11, function (doc) {\n")
		  .append("        var nodes = document.querySelectorAll('.math');\n")
		  .append("        for (var i=0; i<nodes.length; i++) {\n")
		  .append("          var node = nodes[i];\n")
		  .append("          var math = new doc.options.MathItem(node.textContent, doc.inputJax[0], (node.tagName !== 'SPAN'));\n")
		  .append("          var text = document.createTextNode('');\n")
		  .append("          node.parentNode.replaceChild(text, node);\n")
		  .append("          math.start = {node: text, delim: '', n: 0};\n")
		  .append("          math.end = {node: text, delim: '', n: 0};\n")
		  .append("          doc.math.push(math);\n")
		  .append("        }\n")
		  .append("      }, '']\n")
		  .append("    }\n")
		  .append("  }\n")
		  .append("};\n");
		return sb.toString();
	}
	
	
	// Pattern to find URL's in text
	private static final Pattern urlPattern = Pattern.compile("((mailto\\:|(news|(ht|f)tp(s?))\\://|www\\.)[-A-Za-z0-9+&@#/%?=~_|!:,\\.;]+[-A-Za-z0-9+&@#/%=~_|]*)");

	/**
	 * Search in given text fragment for URL's and surround them with clickable
	 * HTML link objects.
	 * 
	 * @param textFragment
	 * @param addIcon TODO
	 * @return text with clickable links
	 */
	public static String formatURLsAsLinks(String textFragment, boolean addIcon) {
		if(textFragment == null) return "";
		Matcher matcher = urlPattern.matcher(textFragment); 		
		
		StringBuilder sb = new StringBuilder(128);
		int pos = 0;
		while (matcher.find()) {
			// Add text since last match and set end of current patch as new end
			// of this match
			sb.append(textFragment.substring(pos, matcher.start()));
			pos = matcher.end();
			// The URL is in group1, the other groups are ignored
			String url = matcher.group(1);
			// Fix URL's without protocol, assume http
			if (url.startsWith("www")) {
				url = "http://" + url; 
			}
			// Fix URL's at end of a sentence
			if (url.endsWith(",") || url.endsWith(".") || url.endsWith(":")) {
				url = url.substring(0, url.length()-1);
				pos--;
			}
			sb.append("<a href=\"");
			sb.append(url);
			sb.append("\"");
			if (url.startsWith("mailto")) {
				sb.append(" target=\"_blank\"");				
			}
			// OpenOLAT URL's are opened in same window, all other URL's in separate window
			else if (!url.startsWith(Settings.getServerContextPathURI())) {
				sb.append(" target=\"_blank\"")
				  .append(" rel=\"noopener noreferrer\"");
			}
			sb.append(">");
			if (addIcon) {
				if (url.startsWith("mailto")) {
					sb.append("<i class='o_icon o_icon_mail'> </i> ");					
				} else if (!url.startsWith(Settings.getServerContextPathURI())) {
					sb.append("<i class='o_icon o_icon_link_extern'> </i> ");				
				} else {
					sb.append("<i class='o_icon o_icon_star'> </i> ");
				}
			}
			
			sb.append(url);
			sb.append("</a>");
		}
		// Add rest of text
		sb.append(textFragment.substring(pos));
		//
		return sb.toString();
	}
	
	private static final Pattern mailPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
	
	/**
	 * Search in given text fragment for mail addresses and surround them with clickable links
	 * 
	 * @param textFragment
	 * @return
	 */
	public static String formatMailsAsLinks(String textFragment, boolean addIcon) {
		if(textFragment == null) return "";
		
		Matcher matcher = mailPattern.matcher(textFragment); 		
				
		return matcher.replaceAll("<a href=\"mailto:$0\">" + (addIcon ? "<i class='o_icon o_icon_mail'> </i> $0" : "$0") + "</a>");
	}
	

	/* emoticon patterns */
	private static final Pattern angelPattern = Pattern.compile("(O\\:-*(\\)|3))");
	private static final Pattern angryPattern = Pattern.compile("(\\:-*(\\|\\||@))");
	private static final Pattern confusedPattern = Pattern.compile("(%-*\\))");
	private static final Pattern coolPattern = Pattern.compile("(8-*\\))");
	private static final Pattern grinPattern = Pattern.compile("(;-*\\))");
	private static final Pattern kissPattern = Pattern.compile("(\\:(\\^)*\\*)");
	private static final Pattern ohohPattern = Pattern.compile("(\\:-*O)");
	private static final Pattern sadPattern = Pattern.compile("(\\:-*\\()");
	private static final Pattern smilePattern = Pattern.compile("(\\:-*\\))");
	private static final Pattern tonguePattern = Pattern.compile("(\\:-*P)");
	private static final Pattern upPattern = Pattern.compile("((^\\s*(\\+)\\s*$)|(\\(\\+\\)))");
	private static final Pattern downPattern = Pattern.compile("((^\\s*(-)\\s*$)|(\\(\\-\\)))");
	
	private static final StringOutput emptyGifUrl = new StringOutput();
	static {
		StaticMediaDispatcher.renderStaticURI(emptyGifUrl, "images/transparent.gif");
	}
	/**
	 * Search in textFragment for emoticons such as :-) :-( etc and replace them
	 * with image tags that render a nice icon.
	 * 
	 * @param textFragment
	 * @return replaced text
	 */
	public static String formatEmoticonsAsImages(String textFragment) {
		
		Matcher matcher;
		matcher = confusedPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_confused' />");
		matcher = coolPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_cool' />");
		matcher = angryPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_angry' />");
		matcher = grinPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_grin' />");
		matcher = kissPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_kiss' />");
		matcher = ohohPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_ohoh' />");
		matcher = angelPattern.matcher(textFragment); // must be before smile pattern
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_angel' />");
		matcher = smilePattern.matcher(textFragment); 
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_smile' />");
		matcher = sadPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_sad' />");
		matcher = tonguePattern.matcher(textFragment); 
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_tongue' />");
		matcher = upPattern.matcher(textFragment); 
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_up' />");
		matcher = downPattern.matcher(textFragment); 
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_down' />");
				
		return textFragment;
	}
	
	/**
	 * Round a double value to a double value with given number of 
	 * figures after comma
	 * @param value
	 * @param decimalPlace
	 * @return rounded double value
	 */
	public static double round(double value, int decimalPlace) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
    value = bd.doubleValue();
    return value;
	}

	/**
	 * Round a float value to a float value with given number of 
	 * figures after comma
	 * @param value
	 * @param decimalPlace
	 * @return rounded float value
	 */
	public static float round(float value, int decimalPlace) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP); 
    value = bd.floatValue();
    return value;
	}
	
	/**
	 * Format a float as string with given number of figures after
	 * comma
	 * @param value
	 * @param decimalPlace
	 * @return formatted string
	 */
	public static String roundToString(float value, int decimalPlace) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.toString();
	}
	
	/**
	 * Format a double as string with given number of figures after
	 * comma
	 * @param value
	 * @param decimalPlace
	 * @return formatted string
	 */
	public static String roundToString(double value, int decimalPlace) {
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.toString();
	}
	
	/**
	 * Format a number to a better readable format
	 * @param value
	 */
	public static String makeReadable(Long value) {
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);
        
        return decimalFormat.format(value);
	}

	/**
	 * Format a string containing a date (without the time, just the date) that uses
	 * a two digits form for the year as a date with a four digits year assuming
	 * that the 21st century is meant. Only the year is modified, all other aspects of
	 * the date remain untouched except for trailing whitespace which is removed.
	 * 
	 * 2.3.22 -> 2.3.2022
	 * 02.03.22 -> 02.03.2022
	 * 3/2/22 -> 3/2/2022
	 * 03/02/22 -> 03/02/2022
	 * 
	 * @param dateString
	 * @return
	 */
	public static String formatTwoDigitsYearsAsFourDigitsYears(String dateString) {
		if (StringHelper.containsNonWhitespace(dateString)) {
			dateString = dateString.trim();
			dateString = twoYearDatePattern.matcher(dateString).replaceAll(twentyXX_replacementPattern);
		}
		return dateString;
	}

	public static String addReference(Translator translator, String reference) {
		return addReference(translator, null, reference, null);
	}
	
	public static String addReference(Translator translator, String value, String reference) {
		return addReference(translator, value, reference, null);
	}
	
	public static String addReference(Translator translator, String value, String reference, String iconCss) {
		String result = "";
		if (StringHelper.containsNonWhitespace(iconCss)) {
			result = "<i class='"
					+ iconCss
					+ "'></i> ";
		}
		if (StringHelper.containsNonWhitespace(value)) {
			result += value;
		}
		if (StringHelper.containsNonWhitespace(reference)) {
			result = result
					+ " "
					+ translator.translate("reference.separator")
					+ " <small>"
					+ reference
					+ "</small>";
		}
		return result;
	}
	
}

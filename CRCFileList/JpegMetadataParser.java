import java.io.*;
import com.drew.imaging.*;
import com.drew.metadata.*;

public class JpegMetadataParser {
	
	public File jpegFile;
	public final String date_original = "Date/Time Original";
	private boolean isFileSet = false;

	public static void main(String args[]) {
		if(args.length<1) {
			System.out.println("Usage:  java  JpegMetadataParser  jpeg_filename");
			System.exit(3);
		}
		JpegMetadataParser zzz = new JpegMetadataParser(args[0], true);
	}

	JpegMetadataParser(String filename, boolean test) {
		String found_tag;
		String parsed_tag;
		try {
			isFileSet = setJpegFile(filename);
			if (test) {
				found_tag = find_tag(date_original);
				p("tag value " + found_tag);
				parsed_tag = filenameFormatDateTag(found_tag);
				p("target filename " + parsed_tag + ".jpg");
			}

		} catch(Throwable t) { t.printStackTrace(); }

	} // constructor

	JpegMetadataParser(String filename) {
		try {
			isFileSet = setJpegFile(filename);
		} catch(Throwable t) { t.printStackTrace(); }
	} // constructor
	
	JpegMetadataParser() {
		isFileSet = false;
		jpegFile = null;
	} // constructor

	boolean setJpegFile(String filename) {
		// check the existence of file
		try {
			jpegFile = new File(filename);
			if (!jpegFile.exists()) {
				p("File not found");
				return false;
			}
			String extension = filename.substring(filename.length()-4);
			p("Extension " + extension);
			if (extension.compareToIgnoreCase(".jpg") != 0) {
				p("Not a jpeg file");
				return false;
			}
		} catch(Throwable t) { 
			t.printStackTrace(); 
			return false;
		}
		// check if the file is a jpeg
		return true;
	}

	String find_tag(String tagname) {
		if (!isFileSet) {
			p("The jpeg file is not set");
			return "";
		}

		boolean found_date_original_tag = false;
		String tag_value = "";
		
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);
			for (Directory directory : metadata.getDirectories()) {
				for (Tag tag : directory.getTags()) {
					if (tag.getTagName().compareTo(tagname) == 0) {
						found_date_original_tag = true;
						tag_value = new String(tag.getDescription());
						p("found!");
						return tag_value;
					}
				}
			}
		} catch(Throwable t) { t.printStackTrace(); }
		return tag_value;
	}

	String filenameFormatDateTag(String tagformat) {
		String retvalue = "";
		try {
			String []elements = tagformat.split(":");
			for (int i = 0; i<elements.length; i++) {
				p(elements[i]);
			}
			if (elements.length != 5) {
				return "";
			}
			if (elements[0].length() != 4) {
				return "";
			}
			retvalue = new String(elements[0]);
			p("retvalue1 " + retvalue);
			if (elements[1].length() != 2) {
				return "";
			}
			retvalue = retvalue.concat("_" + elements[1]);
			p("retvalue2 " + retvalue);
			String []second_split = elements[2].split(" ");
			if (second_split.length != 2) {
				return "";
			}
			if ((second_split[0].length()!=2) || (second_split[1].length()!=2)) {
				return "";
			}
			retvalue = retvalue.concat("_" + second_split[0]);
			retvalue = retvalue.concat("_" + second_split[1]);
			p("retvalue3 " + retvalue);

			if ((elements[3].length() != 2) || (elements[4].length() != 2)) {
				return "";
			}
			retvalue = retvalue.concat(elements[3]);
			retvalue = retvalue.concat(elements[4]);
			p("retvalue4 " + retvalue);
			return retvalue;
		} catch(Throwable t) { t.printStackTrace(); }
		return "";
	}
	
//	protected long getTime() { return System.currentTimeMillis(); }


	protected void p(String s) { System.out.println(s); }
	protected void pp(String s) { System.out.print(s); }


	protected String leftPad(int i, int length) {
		return leftPadInt(i, length, ' '); }

	protected String leftPad(double d, int length) {
		return leftPadDouble(d, length, 0, ' '); }

	//	leftPad("12", 5)          -->    "   12"
	//	leftPad("a8", 6, '0')     -->   "0000a8"
	//	leftPad("hello", 7, '-')  -->  "--hello"
	protected String leftPad(String s, int length) {
		return leftPadString(s, length); }

	protected String leftPadString(String s, int length) {
		return leftPadString(s, length, ' '); }

	protected String leftPadString(String s, int length, char padChar) {
		String temp = "";
		int i;
		for(i=0; i<length; i++)
			temp = temp + padChar; 
		temp = temp + s;
		i = temp.length();
		return(temp.substring(i-length));
	}
	
	protected String leftPadInt(int val, int length) {
		return(leftPadString(Integer.toString(val), length, ' ')); }

	protected String leftPadInt(int val, int length, char padChar) {
		return(leftPadString(Integer.toString(val), length, padChar)); }

	protected String leftPadIntHex(int val, int length, char padChar) {
		return(leftPadString(Integer.toHexString(val), length, padChar)); }


	private String leftPadDouble(double val, int integerDigits, int fractionalDigits) {
		return leftPadDouble(val, integerDigits, fractionalDigits, ' ', true); }

	private String leftPadDouble(double val, int integerDigits, int fractionalDigits, char leftPadChar) {
		return leftPadDouble(val, integerDigits, fractionalDigits, leftPadChar, true); }

	private String leftPadDouble(
			double val,
			int integerDigits,
			int fractionalDigits,
			char leftPadChar,
			boolean roundValueAtLastFractionDigit ) {

		boolean lessThanZero = false;
		int lessThanZeroInt = 0;

		if(val<0) {
			lessThanZero = true;
			val = - val;
			lessThanZeroInt = 1;
		}

		if(integerDigits>18)
			integerDigits = 18;

		if(fractionalDigits>18)
			fractionalDigits = 18;

		long fm = 1; // fraction multiplier

		for(int i=0; i<fractionalDigits; i++)
			fm = fm * 10;

		double rounding = 0.5;
		if(val<0)
			rounding = -rounding;

		if(!roundValueAtLastFractionDigit)
			rounding = 0;

		long pi = (long) (val + rounding / fm);   // part integer
		long pf = (long) ((val-((double)pi)) * fm + rounding); // part fractional       1.e+17

		String spi = "";

		if( (""+pi).length() + lessThanZeroInt > integerDigits )
			spi = "############################################".substring(0, integerDigits);
		else {
			if( lessThanZero && (leftPadChar == ' ') )
				spi = "                             ".substring(0, integerDigits - ((""+pi).toString().length()+lessThanZeroInt) ) + "-" + pi ;
			else
				spi = "" + ( lessThanZero ? "-" : "" ) + leftPadString(""+pi, integerDigits - lessThanZeroInt, leftPadChar);
		}

		if( fractionalDigits==0 )
			return("" + spi);

		return("" + spi + "." + leftPadString(Long.toString(pf), fractionalDigits, '0'));

	} // leftPadDouble

	protected void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) { e.printStackTrace(); }
	}

} // class


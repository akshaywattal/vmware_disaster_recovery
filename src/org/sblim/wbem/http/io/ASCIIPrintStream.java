/**
 * ASCIIPrintStream.java
 *
 * (C) Copyright IBM Corp. 2005, 2009
 * 
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author: Chung-hao Tan, IBM ,chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 *   18045    2005-08-10  pineiro5     Some code clean up in multiple points
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.http.io;

import java.io.BufferedWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class ASCIIPrintStream extends FilterOutputStream {
//	private static boolean ASCIIPlatform = true;
//	private String eol;
    private boolean iAutoFlush = false;
    private Exception iTrouble = null;

    private BufferedWriter iTextOut;
//    private OutputStreamWriter iCharOut;

    private ASCIIPrintStream(boolean autoFlush, OutputStream pOut) {
		super(pOut);
		if (pOut == null)
		    throw new NullPointerException("Null output stream");
		iAutoFlush = autoFlush;
//		AccessController.doPrivileged(new PrivilegedAction() {
//			public Object run() {
//				ASCIIPrintStream.ASCIIPlatform 
//					= System.getProperty("platform.notASCII", "false").equalsIgnoreCase("false");
//				return null;
//			}
//		});
//
//		eol = System.getProperty("line.separator");
    }
    public ASCIIPrintStream(OutputStream outputstream) {
        this(outputstream, false);

        init(new OutputStreamWriter(this));
    }

    public ASCIIPrintStream(OutputStream outputstream, boolean autoFlush) {
        this(autoFlush, outputstream);

        init(new OutputStreamWriter(out));
    }
    
    public ASCIIPrintStream(OutputStream outputstream, boolean autoFlush, String encoding) {
    	this(outputstream, autoFlush);
    }

    private void init(OutputStreamWriter osw) {
//        iCharOut = osw;
        iTextOut = new BufferedWriter(osw);
    }
    
    private void write(String str) {
//        if (ASCIIPlatform) {
//        	if (str == null) str = "null";
//            _write(str);
//            return;
//        }
        int stringLength = str.length();
        char charArray[] = new char[stringLength];
        byte asciiArray[] = new byte[stringLength];
        str.getChars(0, stringLength, charArray, 0);
        for (int i = 0; i < stringLength; i++)
            asciiArray[i] = charArray[i] >= '\u0100' ? 63 : (byte)charArray[i];

        write(asciiArray, 0, stringLength);
    }

//    private void _write(String s) {
//    	try {
//    	    synchronized (this) {
//    		ensureOpen();
//    		iTextOut.write(s);
//
//    		if (iAutoFlush && (s.indexOf('\n') >= 0))
//    		    out.flush();
//    	    }
//    	}
//    	catch (InterruptedIOException x) {
//    	    Thread.currentThread().interrupt();
//    	}
//    	catch (IOException x) {
//    	    iTrouble = x;
//    	}
//        }

    public void print(boolean flag) {
        write(flag ? "true" : "false");
    }

    public void print(char c) {
        write(String.valueOf(c));
    }

    public void print(int i) {
        write(String.valueOf(i));
    }

    public void print(long l) {
        write(String.valueOf(l));
    }

    public void print(float f) {
        write(String.valueOf(f));
    }

    public void print(double d) {
        write(String.valueOf(d));
    }

    public void print(char ac[]) {
        write(String.valueOf(ac));
    }

    public void print(String s) {
        if(s == null) s = "null";
        write(s);
    }

    public void print(Object obj) {
        write(String.valueOf(obj));
    }

    public void println() {
        newLine();
    }

    public void println(boolean flag) {
        synchronized(this) {
            print(flag);
            newLine();
        }
    }

    public void println(char c) {
        synchronized(this) {
            print(c);
            newLine();
        }
    }

    public void println(int i) {
        synchronized(this) {
            print(i);
            newLine();
        }
    }

    public void println(long l) {
        synchronized(this) {
            print(l);
            newLine();
        }
    }

    public void println(float f) {
        synchronized(this) {
            print(f);
            newLine();
        }
    }

    public void println(double d) {
        synchronized(this) {
            print(d);
            newLine();
        }
    }

    public void println(char ac[]) {
        synchronized(this) {
            print(ac);
            newLine();
        }
    }

    public void println(String s) {
        synchronized(this) {
            print(s);
            newLine();
        }
    }

    public void println(Object obj) {
        synchronized(this) {
            print(obj);
            newLine();
        }
    }
    private void newLine() {
    	try {
    	    synchronized (this) {
    		ensureOpen();
    		iTextOut.newLine();
//    		textOut.flushBuffer();
//    		charOut.flushBuffer();
    		if (iAutoFlush)
    		    out.flush();
    	    }
    	}
    	catch (InterruptedIOException x) {
    	    Thread.currentThread().interrupt();
    	}
    	catch (IOException x) {
    	    iTrouble = x;
    	}
        }
//    private void write(char buf[]) {
//    	try {
//    	    synchronized (this) {
//	    		ensureOpen();
//	    		iTextOut.write(buf);
//	    		iTextOut.flush();
//	    		iCharOut.flush();
//	    		if (iAutoFlush) {
//   		    		out.flush();
//	    		}
//    	    }
//    	}
//    	catch (InterruptedIOException x) {
//    	    Thread.currentThread().interrupt();
//    	}
//    	catch (IOException x) {
//    	    iTrouble = x;
//    	}
//    }

//	private void write(String s) {
//    	try {
//    	    synchronized (this) {
//    		ensureOpen();
//    		textOut.write(s);
//    		textOut.flushBuffer();
//    		charOut.flushBuffer();
//    		if (autoFlush && (s.indexOf('\n') >= 0))
//    		    out.flush();
//    	    }
//    	}
//    	catch (InterruptedIOException x) {
//    	    Thread.currentThread().interrupt();
//    	}
//    	catch (IOException x) {
//    	    trouble = x;
//    	}
//	}
	
//    int i = 0;
    public void write(byte buf[], int off, int len) {
    	try {
    	    synchronized (this) {
    		ensureOpen();
    		out.write(buf, off, len);
//    		i ++;
    		if (iAutoFlush)
    		    out.flush();
    	    }
//			System.out.println("TOTAL:"+i);
    	}
    	catch (InterruptedIOException x) {
    	    Thread.currentThread().interrupt();
    	}
    	catch (IOException x) {
    	    iTrouble = x;
    	}
	}
    
    public void write(int b) {
    	try {
    	    synchronized (this) {
    		ensureOpen();
    		out.write(b);
    		if ((b == '\n') && iAutoFlush)
    		    out.flush();
    	    }
    	}
    	catch (InterruptedIOException x) {
    	    Thread.currentThread().interrupt();
    	}
    	catch (IOException x) {
    	    iTrouble = x;
    	}
	}
    
    protected void setError() {
//    	trouble = x;
    }

    public Exception checkError() {
    	if (out != null)
    	    flush();
    	return iTrouble;
    }
    
    public void close() {
    	synchronized (this) {
    	    if (! closing) {
    		closing = true;
    		try {
    		    iTextOut.close();
    		    out.close();
    		}
    		catch (IOException x) {
    		    iTrouble = x;
    		}
    		iTextOut = null;
//    		iCharOut = null;
    		out = null;
    	    }
    	}
	}
    public void flush() {
    	synchronized (this) {
    	    try {
    		ensureOpen();
    		out.flush();
    	    }
    	    catch (IOException x) {
    		iTrouble = x;
    	    }
    	}
    }

	private boolean closing = false;
    private void ensureOpen() throws IOException {
    	if (out == null)
    	    throw new IOException("Stream closed");
	}
}
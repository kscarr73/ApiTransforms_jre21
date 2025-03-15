package com.progbits.api.json;

import com.progbits.api.exception.ApiException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple JSON Streaming Parser
 *
 * @author scarr
 */
public class UTF8JsonParser implements Closeable, AutoCloseable {

    /**
     * The Token Event
     */
    public static enum Event {
        /**
         * Start of a JSON array. The position of the parser is after '['.
         */
        START_ARRAY,
        /**
         * Start of a JSON object. The position of the parser is after '{'.
         */
        START_OBJECT,
        /**
         * Name in a name/value pair of a JSON object. The position of the
         * parser is after the key name. The method {@link #getString} returns
         * the key name.
         */
        KEY_NAME,
        /**
         * String value in a JSON array or object. The position of the parser is
         * after the string value. The method {@link #getString} returns the
         * string value.
         */
        VALUE_STRING,
        /**
         * Number value in a JSON array or object. The position of the parser is
         * after the number value. {@code JsonParser} provides the following
         * methods to access the number value: {@link #getInt},
         * {@link #getLong}
         */
        VALUE_NUMBER_INT,
        VALUE_NUMBER_FLOAT,
        /**
         * {@code true} value in a JSON array or object. The position of the
         * parser is after the {@code true} value.
         */
        VALUE_TRUE,
        /**
         * {@code false} value in a JSON array or object. The position of the
         * parser is after the {@code false} value.
         */
        VALUE_FALSE,
        /**
         * {@code null} value in a JSON array or object. The position of the
         * parser is after the {@code null} value.
         */
        VALUE_NULL,
        /**
         * End of a JSON object. The position of the parser is after '}'.
         */
        END_OBJECT,
        /**
         * End of a JSON array. The position of the parser is after ']'.
         */
        END_ARRAY,
        
        /**
         * End of File found
         */
        EOF
    }

    private Reader _reader;
    private final StringBuilder _textBuffer = new StringBuilder();
    private String _currentName;
    private Event _currentEvent;
    private final byte[] currStateLst = new byte[256];
    private int currStateCnt = 0;
    
    private byte currState = -1;
    
    private char iRead;
    private boolean usePrev = false;

    final static char STRUCT_BEGIN_ARRAY = '['; // [
    final static char STRUCT_END_ARRAY = ']'; // ]
    final static char STRUCT_BEGIN_OBJECT = '{'; // {
    final static char STRUCT_END_OBJECT = '}'; // }
    final static char STRUCT_COLON = ':'; // :
    final static char STRUCT_COMMA = ','; // ,

    final static int STRUCT_QUOTES = '"'; // "

    final static char INT_SPACE = ' ';
    final static char INT_TAB = '\t';
    final static char INT_SLASH = '/';
    final static char INT_BACKSLASH = '\\';
    final static char INT_NEWLINE = '\n';
    final static char INT_LINEFEED = '\f';
    final static char INT_CARRIAGERETURN = '\r';

    int iCurrPtr = 0;
    int iEnd = 0;
    char[] charBuff = new char[4000];

    /**
     * Create a JsonParser using String
     * 
     * @param subject The String to Parse
     */
    public UTF8JsonParser(String subject) {
        _reader = new StringReader(subject);
    }

    /**
     * Create a JsonParser using a Reader
     * 
     * @param reader The Reader To Parse
     */
    public UTF8JsonParser(Reader reader) {
        _reader = reader;
    }

    /**
     * Create a JsonParser using InputStream
     * 
     * @param is The InputStream to Parse
     */
    public UTF8JsonParser(InputStream is) {
        _reader = new InputStreamReader(is);
    }

    /**
     * Close the Parser
     * 
     * @throws IOException 
     */
    @Override
    public void close() throws IOException {
        _reader = null;
    }

    /**
     * Get the Current Token Event
     * 
     * @return The Current Token Event
     */
    public Event currentEvent() {
        return _currentEvent;
    }

    private char getNextChar() throws ApiException {
        if (iCurrPtr >= iEnd) {
            loadMore();
        }

        return charBuff[iCurrPtr++];
    }

    private char getNextCharIgnoreWhitespace() throws ApiException {
        char respChar = 0;
        boolean bContinue = true;
        
        while (bContinue) {
            respChar = getNextChar();
            
            switch (respChar) {
                case INT_SPACE, INT_NEWLINE, INT_TAB, INT_CARRIAGERETURN -> bContinue = true;
                default -> bContinue = false;
            }
        }
        
        return respChar;
    }
    
    private boolean loadMore() throws ApiException {
        try {
            if (_reader != null) {
                int count = _reader.read(charBuff, 0, charBuff.length);

                if (count > 0) {
                    iCurrPtr = 0;
                    iEnd = count;

                    return true;
                }

                iCurrPtr = iEnd = 0;

                return false;
            }
        } catch (IOException io) {
            throw new ApiException(710, io.getMessage());
        }

        return false;
    }

    /**
     * Get the next Token Event
     * 
     * @return The Token Event for the new Current Token
     * 
     * @throws ApiException The function ran into an exception
     */
    public Event nextToken() throws ApiException {
        Event event = null;

        _textBuffer.setLength(0);
        _currentName = null;
        
        while (true) {
            if (!usePrev) {
                iRead = getNextCharIgnoreWhitespace();
            } else {
                usePrev = false;
            }

            if (iRead == -1) {
                event = Event.EOF;
                break;
            }

            event = isStruct();

            if (event != null) {
                break;
            }

            if (_currentName == null && currState != 2) {
                if (iRead == STRUCT_QUOTES) {
                    readToEndQuote();
                    _currentName = getString();
                    event = Event.KEY_NAME;
                    break;
                }
            }

            if (iRead == STRUCT_COLON) {
                event = readValue();
                if (event != null) {
                    break;
                }
            }

            // If we are in a new Array, look for Scalar values
            if (currState == 2) {
                // Outside specific codes
                usePrev = true;
                
                event = readValue();
                if (event != null) {
                    break;
                }
            }

        }

        _currentEvent = event;
        return event;
    }

    private void readToEndQuote() throws ApiException {

        boolean inBackslash = false;

        OUTER:
        while (true) {
            iRead = getNextChar();
            if (inBackslash) {
                switch (iRead) {
                    case 110 -> {
                        _textBuffer.append(INT_NEWLINE);
                    }

                    case 116 -> {
                        _textBuffer.append(INT_TAB);
                    }

                    case 34 -> {
                        _textBuffer.append(STRUCT_QUOTES);
                    }

                    case 102 -> {
                        _textBuffer.append(INT_LINEFEED);
                    }

                    case 114 -> {
                        _textBuffer.append(INT_CARRIAGERETURN);
                    }

                    default ->
                        _textBuffer.append("");
                }

                inBackslash = false;
            } else {
                switch (iRead) {
                    case STRUCT_QUOTES -> {
                        break OUTER;
                    }
                    case INT_BACKSLASH -> inBackslash = true;
                    default -> _textBuffer.append(iRead);
                }
            }
        }

    }

    private Event readValue() throws ApiException {
        Event event;

            int iType = 0;

            boolean bContinue = true;
            
            while (bContinue) {
                if (usePrev) {
                    usePrev = false;
                } else {
                    iRead = getNextChar();
                }

                if (iRead == STRUCT_QUOTES) {
                    iType = 1;
                    _textBuffer.setLength(0);
                    readToEndQuote();
                    
                    bContinue = readToControlChar(false);
                } else if (((iRead >= 48 && iRead <= 57) || iRead == 45 || iRead == 46 || iRead == 101) && (iType == 0 || iType == 2)) {
                    // Number Parsing
                    iType = 2;
                    bContinue = readToControlChar(true);
                } else if ((iRead == 110 || iRead == 117 || iRead == 108) && (iType == 0 || iType == 3)) {
                    // Null Value
                    iType = 3;
                    bContinue = readToControlChar(false);
                } else if ((iRead == 116 || iRead == 102) && (iType == 0 || iType == 4 || iType == 5)) {
                    // true/false parsing
                    if (iRead == 116) {
                        iType = 4;
                    } else {
                        iType = 5;
                    }
                    
                    bContinue = readToControlChar(false);
                } else if (iRead == STRUCT_BEGIN_ARRAY || iRead == STRUCT_BEGIN_OBJECT ) {
                    usePrev = true;
                    bContinue = false;
                }
            }

            switch (iType) {
                case 1 ->
                    event = Event.VALUE_STRING;
                case 2 -> {
                    if (_textBuffer.indexOf(".") > -1) {
                        event = Event.VALUE_NUMBER_FLOAT;
                    } else {
                        event = Event.VALUE_NUMBER_INT;
                    }
                }
                case 3 ->
                    event = Event.VALUE_NULL;
                case 4 ->
                    event = Event.VALUE_TRUE;
                case 5 ->
                    event = Event.VALUE_FALSE;

                default ->
                    event = null;
            }

        return event;
    }

    private boolean readToControlChar(boolean useTextBuffer) throws ApiException {
        boolean bRet = true;
        
        if (useTextBuffer) _textBuffer.append(iRead);
        
        while (bRet) {
            iRead = getNextChar();
            
            switch (iRead) {
                case STRUCT_BEGIN_ARRAY, STRUCT_BEGIN_OBJECT, 
                    STRUCT_END_OBJECT, STRUCT_END_ARRAY  -> {
                    usePrev = true;
                    bRet = false;
                }
                    
                case STRUCT_COMMA -> {
                    bRet = false;
                }
                    
                default -> {
                    if (useTextBuffer) _textBuffer.append(iRead);
                }
            }
        }
        
        return bRet;
    }
    
    private Event isStruct() throws ApiException {
        switch (iRead) {
            case STRUCT_BEGIN_ARRAY -> {
                currStateCnt++;
                currStateLst[currStateCnt] = 2;
                currState = 2;
                return Event.START_ARRAY;
            }
            case STRUCT_BEGIN_OBJECT -> {
                currStateCnt++;
                currStateLst[currStateCnt] = 1;
                currState = 1;
                return Event.START_OBJECT;
            }
            case STRUCT_END_OBJECT -> {
                if (currState == 1) {
                    currStateCnt--;
                    
                    if (currStateCnt > -1) {
                        currState = currStateLst[currStateCnt];
                    } else {
                        currState = -1;
                    }
                } else {
                    throw new ApiException(520, "State Incorrect");
                }
                
                return Event.END_OBJECT;
            }
            case STRUCT_END_ARRAY -> {
                if (currState == 2) {
                    currStateCnt--;
                    
                    if (currStateCnt > -1) {
                        currState = currStateLst[currStateCnt];
                    } else {
                        currState = -1;
                    }
                } else {
                    throw new ApiException(520, "State Incorrect");
                }
                
                return Event.END_ARRAY;
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Get Current Token Buffer as String
     * 
     * @return String Representation of the Buffer
     */
    public String getString() {
        return _textBuffer.toString();
    }

    /**
     * Get Current Token Buffer as Integer
     * 
     * @return Integer Representation of the Buffer
     */
    public Integer getInt() {
        return Integer.valueOf(_textBuffer.toString().trim());
    }

    /**
     * Get Current Token Buffer as Long
     * 
     * @return Long Representation of the Buffer
     */
    public Long getLong() {
        return Long.valueOf(_textBuffer.toString().trim());
    }

    /**
     * Get Current Token Buffer as Float
     * 
     * @return Float Representation of the Buffer
     */
    public Float getFloat() {
        return Float.valueOf(_textBuffer.toString().trim());
    }

    /**
     * Get Current Token Buffer as Double
     * 
     * @return Double Representation of the Buffer
     */
    public Double getDouble() {
        return Double.valueOf(_textBuffer.toString().trim());
    }
}

package com.quartetfs.fwk.format.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.IVariableParser;
import com.quartetfs.fwk.types.impl.PluginValue;
import com.quartetfs.fwk.util.MessageUtil;

public abstract class AVectorParser<K> extends PluginValue
  implements IVariableParser<K>
{
  private static final long serialVersionUID = -845693134195758019L;
  private static final String EMPTY_OBJECT = "";
  static final String DELIMITER_PROPERTY = "vectorDelimiter";
  static final char DEFAULT_DELIMITER = ';';
  static final IParser<?> DEFAULT_PARSER = new StringParser();

  
  public static final Integer VARIABLE_LENGTH = null;
  protected char delimiter;
  protected final String prefix;
  protected Integer vectorSize = VARIABLE_LENGTH;

  public AVectorParser(String prefix, Integer vectorSize, char delimiter) {
    this.prefix = prefix;
    this.delimiter = delimiter;
    this.vectorSize = vectorSize;
  }

  public static char getDefaultDelimiter()
  {
    String prop = System.getProperty("vectorDelimiter");
    if ((prop == null) || (prop.length() == 0)) {
      return ';';
    }
    return prop.charAt(0);
  }

  protected void setVectorSize(Integer vectorSize)
  {
    this.vectorSize = vectorSize;
  }

  protected void setDelimiter(char delimiter) {
    this.delimiter = delimiter;
  }

  protected abstract K newVector(int paramInt);
  
  @Override
  public K parse(CharSequence sequence)
  {
    int length = sequence.length();
    if (length == 0) return null;
    int actualVectorSize;
      
    if (this.vectorSize == null)
    {
       actualVectorSize = 1;
      for (int c = 0; c < length; c++) {
        if (this.delimiter == sequence.charAt(c))
          actualVectorSize++;
      }
    }
    else
    {
      actualVectorSize = this.vectorSize.intValue();
    }

    final K vector = newVector(actualVectorSize);

    SubSequence seq = new SubSequence(sequence);
    try
    {
      int vectorIndex = 0;

      for (int c = 0; c < length; c++) {
        if ((this.delimiter == sequence.charAt(c)) && (c == length - 1))
        {
          seq.from = (seq.to == 0 ? 0 : seq.to + 1);
          seq.to = c; 
          fillVector( vector, vectorIndex++, seq);

          fillVector(vector, vectorIndex++, "");
        } else if (this.delimiter == sequence.charAt(c))
        {
          seq.from = (seq.to == 0 ? 0 : seq.to + 1);
          seq.to = c;
          fillVector(vector, vectorIndex++, seq); } else {
          if (c != length - 1)
            continue;
          seq.from = (seq.to == 0 ? 0 : seq.to + 1);
          seq.to = length;
          fillVector(vector, vectorIndex++, seq);
        }
      }

      /*if (vectorIndex != actualVectorSize) {
        String m = MessageUtil.formMessage("EXC_PARSE_VECTOR_LENGTH", "EXC_PARSE_VECTOR_LENGTH", new Object[] { Integer.valueOf(vectorIndex), Integer.valueOf(actualVectorSize) });
        throw new QuartetException(m);
      }*/
    }
    catch (Exception e) {
      String m = MessageUtil.formMessage("composer", "EXC_PARSING", new Object[] { sequence });
      throw new QuartetRuntimeException(m, e);
    }

    return vector;
  }

  public IVariableParser<K> parseKey(String description)
  {
    if (description == null) return null;

    Pattern p = Pattern.compile(this.prefix + "\\[(\\d+)?\\](?:\\[(.+)\\])?", 2);
    Matcher match = p.matcher(description);
    if (match.matches()) {
      String vectorSize = match.group(1);
      String delimiter = match.group(2);
      try
      {
        AVectorParser result = (AVectorParser)getClass().newInstance();
        if (vectorSize != null)
          result.setVectorSize(Integer.valueOf(Integer.parseInt(vectorSize)));
        if (delimiter != null)
          result.setDelimiter(delimiter.charAt(0));
        return result;
      } catch (Exception e) {
        throw new QuartetRuntimeException("Vector parser " + key() + " cannot parse description: " + description, e);
      }
    }
    throw new QuartetRuntimeException("Vector parser " + key() + " cannot match description: " + description);
  }

  protected String buildKey(Integer length, Character delimiter)
  {
    if (length == null) {
      if ((delimiter == null) || (delimiter.equals(Character.valueOf(';')))) {
        return this.prefix + "[]";
      }
      return this.prefix + "[][" + delimiter.toString() + "]";
    }

    if ((delimiter == null) || (delimiter.equals(Character.valueOf(';')))) {
      return this.prefix + "[" + length.toString() + "]";
    }
    return this.prefix + "[" + length.toString() + "][" + delimiter.toString() + "]";
  }

  public String description()
  {
    return this.prefix + " vector field parser";
  }

  public Object key()
  {
    return buildKey(this.vectorSize, Character.valueOf(this.delimiter));
  }
  
  protected abstract void fillVector(K paramK, int paramInt, CharSequence paramCharSequence);
}
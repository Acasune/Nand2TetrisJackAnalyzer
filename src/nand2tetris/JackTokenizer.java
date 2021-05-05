package nand2tetris;

import java.io.BufferedReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JackTokenizer {

  private static final Pattern symbolsPattern = Pattern.compile("([^\\{\\}\\(\\)\\[\\]\\.,;\\+\\-\\*\\/&\\|<>=~]*)([\\{\\}\\(\\)\\[\\]\\.,;\\+\\-\\*\\/&\\|<>=~])([^\\{\\}\\(\\)\\[\\]\\.,;\\+\\-\\*\\/&\\|<>=~]*)");
  private static final Pattern stringLiteralPattern = Pattern.compile("\".*?\"");
  private static final Pattern commentPattern = Pattern.compile("(.*)(\\/\\/.*)");
  private static final Pattern isDigit = Pattern.compile("^\\d+$");

  private static final Set<String> keywordsSet = Stream.of(
      "class","constructor","function", "method","field","static",
      "var","int","char","boolean","void","true","false","null",
      "this","let","do","if","else","while","return"
  ).collect(Collectors.toSet());

  private static final Set<String> symbolsSet = Stream.of(
      "{","}","(", ")","[","]",
      ".",",",";","+","-","*","/","&", "|","<",">","=","~"
  ).collect(Collectors.toSet());


  int currentPos = -1;

  List<String> tokenized;

  JackTokenizer(List<String> input) {

    String tokenizedCode = "";
    for (String line : input) {
      line = preProcessor(line);
      if (line.isBlank() || line.isEmpty()) {
        continue;
      }
      tokenizedCode += lineProcesssor(line);
    }
    this.tokenized = Arrays.asList(tokenizedCode.split("\n"));

  }

  public boolean hasMoreTokens() {
    return currentPos < tokenized.size();
  }

  public int advance() {
    this.currentPos+=1;
    return this.currentPos;
  }

  public TokenType tokenType(String token) {
    if (keywordsSet.contains(token)) {
      return TokenType.KEY_WORD;
    }
    else if (symbolsSet.contains(token)) {
      return TokenType.SYMBOL;
    }
    else if (isDigit.matcher(token).find()) {
      return TokenType.INT_CONST;
    }
    else if (token.startsWith("\"")&&token.endsWith("\"")) {
      return TokenType.STRING_CONST;
    }
    else {
      return TokenType.IDENTIFIER;
    }
  }

  public String getSymbol() throws Exception {
    if (tokenType(this.tokenized.get(currentPos)) !=TokenType.SYMBOL) {
      throw new Exception("This token is not a symbol!!");
    }
    return this.tokenized.get(currentPos);
  }

  public String getIdentifier() throws Exception {
    if (tokenType(this.tokenized.get(currentPos)) !=TokenType.IDENTIFIER) {
      throw new Exception("This token is not an identifier!!");
    }
    return this.tokenized.get(currentPos);
  }

  public int getIntVal() throws Exception {
    if (tokenType(this.tokenized.get(currentPos)) !=TokenType.INT_CONST) {
      throw new Exception("This token is not an integer value!!");
    }
    return Integer.parseInt(this.tokenized.get(currentPos));
  }

  public String getStringVal() throws Exception {
    if (tokenType(this.tokenized.get(currentPos)) !=TokenType.INT_CONST) {
      throw new Exception("This token is not a string value!!");
    }
    return this.tokenized.get(currentPos);
  }


  private String preProcessor(String line) {
    Matcher matcher = commentPattern.matcher(line);
    if (!matcher.find()) {
      return line;
    }
    return matcher.group(1).trim();
  }


  private String lineProcesssor(String line) {
    String out = "";

    Matcher stringConstMatcher = stringLiteralPattern.matcher(line);
    if (stringConstMatcher.find()) {
      int start = stringConstMatcher.start();
      int end = stringConstMatcher.end();
      out += lineProcesssor(line.substring(0, start));
      out += line.substring(start, end - 1);
      out += lineProcesssor(line.substring(end - 1));
      return out;
    }

    String[] words = line.split("\\s+");
    for (String word : words) {
      out += wordProcessor(word);
    }
    return out;
  }

  private String wordProcessor(String word) {
    if (word == null || word.isBlank()) {
      return "";
    }
    Matcher symbolsMatcher = symbolsPattern.matcher(word);
    if (!symbolsMatcher.find()) {
      return word + "\n";
    }

    String out = "";
    do {
      out += wordProcessor(symbolsMatcher.group(1));
      out += symbolsMatcher.group(2) + "\n";
      out += wordProcessor(symbolsMatcher.group(3));

    } while (symbolsMatcher.find());
    return out;
  }
}
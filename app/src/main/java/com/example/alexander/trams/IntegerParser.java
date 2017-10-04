package com.example.alexander.trams;

import org.jsoup.nodes.Element;
import java.util.regex.*;

public class IntegerParser {
    public static Integer getIntFromString(Element e, String word) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(word);
        int start = 0;
        Integer result = 0;
        while (matcher.find(start)) {
            String value = word.substring(matcher.start(), matcher.end());
            result = Integer.parseInt(value);
            start = matcher.end();
        }
        return result;
    }
}

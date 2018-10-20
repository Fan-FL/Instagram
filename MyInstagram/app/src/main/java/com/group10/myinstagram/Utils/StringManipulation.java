package com.group10.myinstagram.Utils;

public class StringManipulation {
    public static String expandUsername(String username){
        return username.replace("."," ");
    }
    public static String condenseUsername(String username){
        return username.replace(" ", ".");
    }
    public static String getTags(String string){
        if(string.indexOf("#") > 0){
            StringBuilder stringBuilder = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundWord = false;
            for(char c : charArray){
                if(c == '#'){
                    foundWord =true;
                    stringBuilder.append(c);
                }else{
                    if(foundWord){
                        stringBuilder.append(c);
                    }
                }
                if (c == ' ') {
                    foundWord =false;
                }
            }
            String tagString;
            tagString = stringBuilder.toString().replace(" ", "").replace("#", ",#");
            return tagString.substring(1,tagString.length());
        }
        return string;
    }
}

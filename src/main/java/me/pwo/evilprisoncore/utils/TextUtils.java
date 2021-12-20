package me.pwo.evilprisoncore.utils;

import me.lucko.helper.text3.Text;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {
    public static List<String> colorize(List<String> paramList) {
        ArrayList<String> arrayList = new ArrayList<>(paramList.size());
        paramList.forEach(paramString -> arrayList.add(Text.colorize(paramString)));
        return arrayList;
    }
}

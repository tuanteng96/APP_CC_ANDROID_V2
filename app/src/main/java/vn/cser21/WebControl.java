package vn.cser21;

import java.util.Map;

public class WebControl {
    public static boolean IsNullEmpty(String s) {
        return s == null || "".equals(s);
    }

    public static String toUrlWithsParams(String url, Map<String, String> params) {
        String j = url.indexOf('?') > 0 ? "&" : "?";
        String c = "";
        String s = url;

        for (Map.Entry<String, String> x : params.entrySet()) {
            if (!"".equals(j)) {
                s += j;
                j = "";
            }
            s += c + x.getKey() + '=' + x.getValue();
            c = "&";
        }

        return s;
    }

    public static int getMapInt(Map<String, String> map, String name, int df) {
        try {
            if (map.containsKey(name)) {
                String v = map.get(name);
                if (v == null || "".equals(v)) return df;
                return Integer.getInteger(v, df);
            }

        } catch (Exception e) {
            //
        }
        return df;
    }
}

package vn.cser21;

import java.lang.reflect.Field;

public class Result {
    public boolean success;
    public String error;
    public Object data;

    public String sub_cmd;
    public int sub_cmd_id;
    public String params;

    /**
     * copy
     */
    public Result copy() {
        Result _r = new Result();

        for (Field f : Result.class.getFields()) {
            try {
                f.set(_r, f.get(this));
            } catch (Exception ex) {

            }
        }

        return _r;
    }
}

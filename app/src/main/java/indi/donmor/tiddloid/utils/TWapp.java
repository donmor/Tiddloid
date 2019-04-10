package indi.donmor.tiddloid.utils;

import java.io.Serializable;

public class TWapp implements Serializable {

    private static final long serialVersionUID = 2761089286310489750L;

    public String name, path, dir, filename, favicon;

    public Boolean backup;

    public TWapp(String file, String title) {
        for (int i = file.length(); i > 0; i--) {
            if (file.substring(i - 1, i).equals("/")) {
                dir = file.substring(0, i);
                filename = file.substring(i, file.length());
                break;
            }
        }
        path = file;
        name = title;
        backup = false;
        favicon = path + "favicon.ico";
    }

    public TWapp(String file, String title, Boolean bk) {
        for (int i = file.length(); i > 0; i--) {
            if (file.substring(i - 1, i).equals("/")) {
                path = file.substring(0, i);
                filename = file.substring(i, file.length());
                break;
            }
        }
        name = title;
        backup = bk;
        favicon = path + "favicon.ico";
    }
}

package com.tobbetu.en4s.helpers;

import com.tobbetu.en4s.R;

/**
 * Kategoriler APIden string olarak geldigi icin bunlari kolayca tercume
 * edebilmek icin bu sinifi yazdim. Basitce string comparison yapip uygun
 * tercumenin IDsini donuyor.
 * 
 * @author mustafa
 * 
 */
public class CategoryI18n {

    public static int getID(String cat) {
        if (cat.equalsIgnoreCase("Disability Rights")) {
            return R.string.cat_dr;
        }

        if (cat.equalsIgnoreCase("Infrastructure")) {
            return R.string.cat_inf;
        }

        if (cat.equalsIgnoreCase("Environment")) {
            return R.string.cat_env;
        }

        if (cat.equalsIgnoreCase("Traffic")) {
            return R.string.cat_tra;
        }

        // You are fucked up now
        throw new RuntimeException("Category Name Error");
    }

    public static String getEnglishName(int index) {
        switch (index) {
        case 1:
            return "Disability Rights";

        case 2:
            return "Infrastructure";

        case 3:
            return "Environment";

        case 4:
            return "Traffic";

        default:
            // Don't fuck with me or I throw an Exception
            throw new RuntimeException("Category ID Error");
        }
    }
}

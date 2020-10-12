package com.sty.ne.modularapt.test;

import com.sty.ne.modularapt.MainActivity;

/**
 * @Author: tian
 * @UpdateDate: 2020/10/12 9:48 PM
 */
public class XActivity$$ARouter {
    public static Class<?> findTargetClass(String path) {
        if(path.equalsIgnoreCase("app/MainActivity")) {
            return MainActivity.class;
        }
        return null;
    }
}

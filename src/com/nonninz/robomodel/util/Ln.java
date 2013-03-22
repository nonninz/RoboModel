/**
 * Copyright 2012 Francesco Donadon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nonninz.robomodel.util;

import android.util.Log;

/**
 * @author Francesco Donadon <francesco.donadon@gmail.com>
 * 
 */
public class Ln {

    private Ln() {
    }

    public static void d(String message, Object... args) {
        final String str = String.format(message, args);
        Log.d("RoboModel", str);
    }

    public static void d(Throwable t, String message, Object... args) {
        final String str = String.format(message, args);
        Log.d("RoboModel", str, t);
    }

    public static void v(String message, Object... args) {
        final String str = String.format(message, args);
        Log.v("RoboModel", str);
    }

    public static void w(Throwable t, String message, Object... args) {
        final String str = String.format(message, args);
        Log.w("RoboModel", str, t);
    }

}

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
package com.nonninz.robomodel;

import java.util.List;

import android.content.Context;

/**
 * @author Francesco Donadon <francesco.donadon@gmail.com>
 * 
 */
public class RoboManager<T extends RoboModel> {
    protected Context mContext;

    public RoboManager(Context context) {
        mContext = context;
    }

    public List<T> all() {
        return null; // TODO
    }

    public void clear() {
        // TODO
    }

    public T create() {
        return null; // TODO
    }

    public T find(long id) {
        return null; // TODO
    }

    public List<T> where(String clause) {
        return where(clause, null);
    }

    public List<T> where(String clause, String[] params) {
        return null; // TODO
    }
}

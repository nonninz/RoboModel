package com.nonninz.robomodel;

import java.lang.reflect.Field;
import java.util.Collection;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.nonninz.robomodel.util.Ln;

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

/**
 * @author Francesco Donadon <francesco.donadon@gmail.com>
 * 
 */
@JsonAutoDetect(creatorVisibility = Visibility.NONE, fieldVisibility = Visibility.PUBLIC_ONLY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public class RoboModelCollection<T extends RoboModel> {

    public void save() {
        Field[] fields = getClass().getFields();
        for (Field f : fields) {
            try {
                if (f.getType().isAssignableFrom(Collection.class)) {
                    @SuppressWarnings("unchecked")
                    Collection<? extends RoboModel> list = (Collection<? extends RoboModel>) f
                                    .get(this);
                    for (RoboModel model : list) {
                        model.save();
                    }
                }
            } catch (IllegalAccessException e) {
                Ln.d(e, "Error while accessing field %s", f.getName());
            }
        }
    }

    void setContext(Context context) {
        Field[] fields = getClass().getFields();
        for (Field f : fields) {
            try {
                if (f.getType().isAssignableFrom(Collection.class)) {
                    @SuppressWarnings("unchecked")
                    Collection<? extends RoboModel> list = (Collection<? extends RoboModel>) f
                                    .get(this);
                    for (RoboModel model : list) {
                        model.setContext(context);
                    }
                }
            } catch (IllegalAccessException e) {
                Ln.d(e, "Error while accessing field %s", f.getName());
            }
        }
    }

}

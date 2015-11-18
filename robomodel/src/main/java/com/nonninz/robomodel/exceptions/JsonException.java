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
package com.nonninz.robomodel.exceptions;

/**
 * @author Francesco Donadon <francesco.donadon@gmail.com>
 * 
 */
public class JsonException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 6842213750832011823L;

    /**
     * 
     */
    public JsonException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param detailMessage
     */
    public JsonException(String detailMessage) {
        super(detailMessage);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param throwable
     */
    public JsonException(Throwable throwable) {
        super(throwable);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param detailMessage
     * @param throwable
     */
    public JsonException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}

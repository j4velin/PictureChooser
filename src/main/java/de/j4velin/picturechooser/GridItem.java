/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.j4velin.picturechooser;

class GridItem {
    final String name;
    final String path;

    /**
     * Creates a new GridItem
     *
     * @param n the name of the item
     * @param p the path to the item
     */
    public GridItem(final String n, final String p) {
        if (p == null) throw new IllegalArgumentException("Path for " + n + " must not be null");
        name = n;
        path = p;
    }
}

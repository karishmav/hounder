/*
Copyright 2008 Flaptor (flaptor.com) 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

    http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/
package com.flaptor.hounder.crawler.pagedb;

import com.flaptor.util.sort.Comparator;

/**
 * @author Flaptor Development Team
 */
public class PageComparator implements Comparator {

    public int compare(Object a, Object b) {
        if (!(a instanceof PageRecord && b instanceof PageRecord)) {
            throw new RuntimeException("Trying to compare objects that are not of type PageRecord.");
        }
        Page pa = ( (PageRecord) a ).getPage();
        Page pb = ( (PageRecord) b ).getPage();
        return pa.getUrlHash().compareTo(pb.getUrlHash());
    }

}


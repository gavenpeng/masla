package com.msw.masla.common.util;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CollectionUtil {
	private static final Logger logger = LoggerFactory.getLogger(CollectionUtil.class);


   @SuppressWarnings("unchecked")
   public static final boolean isEmpty(Collection c) {
       return null == c || 0 == c.size()? true : false;
   }
}

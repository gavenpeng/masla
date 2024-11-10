package com.msw.masla.common.util;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * 资源的唯一key获取器
 *
 * @param <K>
 * @param <V>
 * @param <E>
 */
public interface ResourcePrimaryLockHold<K> {
	
	ReadWriteLock getLock(K k);
	
	void removeLock(K k);
	
}

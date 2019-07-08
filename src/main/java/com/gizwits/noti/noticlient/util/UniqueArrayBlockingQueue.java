package com.gizwits.noti.noticlient.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 唯一数组阻塞队列
 * <p>
 * 队列内没有重复元素
 *
 * @author Jcxcc
 * @since 1.0
 */
@Slf4j
public class UniqueArrayBlockingQueue<E> extends ArrayBlockingQueue<E> {

    //put锁, 保证不放进重复的元素
    private final ReentrantLock putLock = new ReentrantLock();

    public UniqueArrayBlockingQueue(int capacity) {
        super(capacity);
    }

    public UniqueArrayBlockingQueue(int capacity, boolean fair) {
        super(capacity, fair);
    }

    public UniqueArrayBlockingQueue(int capacity, boolean fair, Collection<? extends E> c) {
        super(capacity, fair, c);
    }

    @Override
    public void put(E it) throws InterruptedException {
        putLock.lock();

        try {
            if (Objects.nonNull(it) && !contains(it)) {
                //元素不为空并且队列内不包含此元素, 则放进队列
                super.put(it);
            } else {
                log.warn("队列元素存在重复元素不处理. item[{}]", it);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            putLock.unlock();
        }
    }
}

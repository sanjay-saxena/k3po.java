/**
 * Copyright (c) 2007-2013, Kaazing Corporation. All rights reserved.
 */

package org.kaazing.netty.channel;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.DefaultChannelFuture;

/**
 * A {@link ChannelFuture} of {@link ChannelFuture}s. It is useful when you want to get notified when all
 * {@link ChannelFuture}s are complete.
 * @param <E> the type of the child futures.
 */
public class CompositeChannelFuture<E extends ChannelFuture> extends DefaultChannelFuture {

    private final NotifyingListener listener = new NotifyingListener();
    private final AtomicInteger unnotified = new AtomicInteger();
    private volatile boolean constructionFinished;

    // XXX Look into have a special-purpose constructor which only takes
    // two ChannelFutures, rather than an Iterable (whose length cannot be
    // known in advance). One of the ChannelFutures might already be a
    // CompositeChannelFuture itself, which is fine. By knowing that
    // we only have two components, we can initialize the AtomicInteger
    // to 2, and call it good.

    public CompositeChannelFuture(Channel channel, boolean cancellable, Iterable<E> kids) {
        super(channel, cancellable);

        for (E k : kids) {
            k.addListener(listener);
            unnotified.incrementAndGet();
        }

        constructionFinished = true;
        if (unnotified.get() == 0) {
            setSuccess();
        }
    }

    public CompositeChannelFuture(Channel channel, Iterable<E> kids) {
        this(channel, false, kids);
    }

    /**
     * Convenience constructor which takes a single {@link ChannelFuture}.
     */
    public CompositeChannelFuture(Channel channel, ChannelFuture kid) {
        super(channel, false);

        kid.addListener(listener);
        unnotified.incrementAndGet();

        constructionFinished = true;
        if (unnotified.get() == 0) {
            setSuccess();
        }
    }

    private class NotifyingListener implements ChannelFutureListener, ChannelFutureProgressListener {

        @Override
        public void operationProgressed(ChannelFuture future, long amount, long current, long total) throws Exception {

            if (constructionFinished) {
                setProgress(amount, current, total);
            }
        }

        @Override
        public void operationComplete(ChannelFuture future) {
            if (unnotified.decrementAndGet() == 0 && constructionFinished) {

                if (future.isSuccess()) {
                    setSuccess();

                } else {
                    setFailure(future.getCause());
                }
            }
        }
    }
}
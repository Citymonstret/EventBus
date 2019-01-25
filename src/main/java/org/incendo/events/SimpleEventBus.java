//
// MIT License
//
// Copyright (c) 2019 Alexander Söderberg
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package org.incendo.events;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Simple {@link EventBus} implementation using a cached thread pool to handle
 * asynchronous events
 * {@inheritDoc}
 */
public final class SimpleEventBus extends EventBus {

    private final Object lock = new Object();

    private final ExecutorService executorService;
    @SuppressWarnings("UnstableApiUsage") private final Multimap<String, ListenerMethod> listenerMethodMultimap =
        MultimapBuilder.hashKeys().hashSetValues().build();

    @SuppressWarnings("WeakerAccess") public SimpleEventBus() {
        super(true);
        this.executorService = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setDaemon(false).setNameFormat("kvantum-events-%s").build());
    }

    @Override protected void registerListenersInternally(
        @NotNull @NonNull Collection<ListenerMethod> listenerMethods) {
        synchronized (this.lock) {
            for (final ListenerMethod listenerMethod : listenerMethods) {
                if (listenerMethodMultimap
                    .containsEntry(listenerMethod.getEventType(), listenerMethod)) {
                    return;
                }
                listenerMethodMultimap
                    .put(getClassName(listenerMethod.getEventType()), listenerMethod);
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public final Collection<ListenerMethod> getMethods(@NonNull final String eventType) {
        final Collection<ListenerMethod> methods;
        synchronized (this.lock) {
            methods = this.listenerMethodMultimap.get(eventType);
        }
        return methods;
    }

    @NotNull @Contract(pure = true)
    private <T> Callable<T> createRunnable(@NonNull final Collection<ListenerMethod> methods,
        @NonNull final T event) {
        return () -> {
            for (final ListenerMethod method : methods) {
                method.invoke(event);
            }
            return event;
        };
    }

    private String getClassName(@NotNull @NonNull final Class clazz) {
        return clazz.getName();
    }

    @NotNull @Override protected <T> Future<T> throwAsync(@NotNull @NonNull T event) {
        final Collection<ListenerMethod> methods = getMethods(getClassName(event.getClass()));
        return this.executorService.submit(createRunnable(methods, event));
    }

    @Override protected <T> T throwSync(@NonNull T event) {
        try {
            final Collection<ListenerMethod> methods = getMethods(getClassName(event.getClass()));
            return this.createRunnable(methods, event).call();
        } catch (final Throwable throwable) {
            throwable.printStackTrace();
        }
        return event;
    }
}

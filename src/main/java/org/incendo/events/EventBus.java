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

import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.incendo.events.reflection.ReflectionUtils;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Event bus responsible for distributing submitted events, and
 * handling {@link Listener} registrations
 */
@SuppressWarnings({"unused", "WeakerAccess"}) public abstract class EventBus {

    private final String name;
    private final boolean supportsAsync;

    public EventBus(final boolean supportsAsync) {
        this.name = this.getClass().getSimpleName();
        this.supportsAsync = supportsAsync;
    }

    /**
     * Check whether or not the event bus implementation supports
     * asynchronous event distribution
     *
     * @return True if the event bus supports asynchronous event distribution,
     * False if not
     */
    @Contract(pure = true) public final boolean supportsAsync() {
        return this.supportsAsync;
    }

    /**
     * Scan a class for methods annotated with {@link Listener} annotations,
     * and register them in the event bus
     *
     * @param listenerInstance Instance to scan
     */
    public void registerListeners(@NonNull final Object listenerInstance) {
        Preconditions.checkNotNull(listenerInstance, "Listener instance may not be null");
        final Class<?> clazz = listenerInstance.getClass();
        try {
            //
            // Find all methods annotated with @Listener
            //
            final Collection<ReflectionUtils.AnnotatedMethod<Listener>> annotatedMethods =
                ReflectionUtils.getAnnotatedMethods(Listener.class, clazz);
            //
            // Create a collection of listener methods
            //
            final List<ListenerMethod> listenerMethods = new ArrayList<>(annotatedMethods.size());
            for (final ReflectionUtils.AnnotatedMethod<Listener> annotatedMethod : annotatedMethods) {
                final Method method = annotatedMethod.getMethod();
                if (method.getParameterCount() != 1) {
                    continue;
                }
                final Class eventType = method.getParameterTypes()[0];
                final ListenerMethod listenerMethod =
                    new ListenerMethod(method, listenerInstance, eventType);
                listenerMethods.add(listenerMethod);
            }
            //
            // Register each annotation internally
            //
            this.registerListenersInternally(listenerMethods);
        } catch (final Throwable throwable) {
            throw new RuntimeException(String.format("Failed to register listener of type %s",
                listenerInstance.getClass().getSimpleName()), throwable);
        }
    }

    protected abstract void registerListenersInternally(
        @NonNull final Collection<ListenerMethod> listenerMethods);

    protected abstract <T> Future<T> throwAsync(@NonNull final T event);

    protected abstract <T> T throwSync(@NonNull final T event);

    /**
     * Submit an event for distribution
     *
     * @param event The event to submit
     * @param async Whether or not the event can be handled asynchronously
     * @return Future containing the distributed event, or an exception if the event
     * could not be handled properly
     */
    public final <T> Future<T> throwEvent(@NonNull final T event, final boolean async) {
        Preconditions.checkNotNull(event, "Event may not be null");
        if (async && !this.supportsAsync()) {
            throw new IllegalStateException("This event bus does not support asynchronous events");
        }
        try {
            if (async) {
                return this.throwAsync(event);
            } else {
                return CompletableFuture.completedFuture(this.throwSync(event));
            }
        } catch (final Throwable throwable) {
            final RuntimeException exception = new RuntimeException(String
                .format("Failed to handle event of type %s", event.getClass().getSimpleName()),
                throwable);
            exception.printStackTrace();
            final CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(exception);
            return future;
        }
    }

    @Override public final int hashCode() {
        return this.toString().hashCode();
    }

    @Contract(value = "null -> false", pure = true) @Override
    public final boolean equals(final Object obj) {
        return obj != null && obj.getClass().equals(getClass()) && ((EventBus) obj).name
            .equalsIgnoreCase(this.name);
    }

    @Override public final String toString() {
        return String.format("EventBus{%s}", this.name);
    }
}

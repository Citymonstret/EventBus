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

import com.hervian.lambda.Lambda;
import com.hervian.lambda.LambdaFactory;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@Getter @SuppressWarnings({"WeakerAccess"}) public final class ListenerMethod {

    private final Lambda lambda;
    private final Class eventType;
    private final Object instance;

    public ListenerMethod(@NonNull final Method method, @NonNull final Object instance,
        @NonNull final Class eventType) throws Throwable {
        this.eventType = eventType;
        this.instance = instance;
        this.lambda = LambdaFactory.create(method);
    }

    public void invoke(@NotNull @NonNull final Object instance) {
        if (!instance.getClass().equals(eventType)) {
            throw new IllegalArgumentException(String.
                format("Mis-matched event types. Requires '%s', but was given '%s'",
                    eventType.getSimpleName(), instance.getClass().getSimpleName()));
        }
        this.lambda.invoke_for_void(this.instance, instance);
    }

    @Contract(value = "null -> false", pure = true) @Override
    public boolean equals(final Object obj) {
        return (obj != null && obj.getClass().equals(this.getClass()) && obj.toString()
            .equals(this.toString()));
    }

    @Override public int hashCode() {
        return this.toString().hashCode();
    }

    @Override public String toString() {
        // type-event_type
        return String
            .format("%s-%s", instance.getClass().getSimpleName(), eventType.getSimpleName());
    }

}

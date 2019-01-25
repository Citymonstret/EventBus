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

package org.incendo.events.reflection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for dealing with reflection
 */
@UtilityClass final public class ReflectionUtils {

    /**
     * Generate a list of {@code @Annotated} methods from a class
     *
     * @param a     Annotation to search for
     * @param clazz Class in which the annotations are to be searched for
     * @return List containing the found annotations
     */
    @NotNull public static <A extends Annotation> List<AnnotatedMethod<A>> getAnnotatedMethods(
        @NotNull @NonNull final Class<A> a, @NotNull @NonNull final Class<?> clazz) {
        final List<AnnotatedMethod<A>> annotatedMethods = new ArrayList<>();
        Class<?> c = clazz;
        while (c != Object.class) {
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(c.getDeclaredMethods()));

            allMethods.stream().filter(method -> method.isAnnotationPresent(a)).forEach(
                method -> annotatedMethods
                    .add(new AnnotatedMethod<>(method.getAnnotation(a), method)));

            c = c.getSuperclass();
        }

        return annotatedMethods;
    }

    /**
     * Value class for {@code @Annotated} methods
     */
    @Getter @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AnnotatedMethod<A extends Annotation> {

        @NonNull private final A annotation;
        @NonNull private final Method method;

    }

}

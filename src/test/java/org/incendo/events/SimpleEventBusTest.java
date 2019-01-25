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

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class SimpleEventBusTest {

    private static boolean indicateSuccess = false;

    private SimpleEventBus createEventBus() {
        return new SimpleEventBus();
    }

    @Test public void createListeners() {
        final SimpleEventBus eventBus = createEventBus();
        eventBus.registerListeners(new TestListenerClass());
        Assertions.assertEquals(1, eventBus.getMethods(String.class.getName()).size());
    }

    @Test public void throwAsync() {
        try {
            final EventBus bus = createEventBus();
            bus.registerListeners(new TestListenerClass());
            Assertions.assertDoesNotThrow(() -> {
                bus.throwAsync("Hello World").get();
            });
            Assertions.assertTrue(indicateSuccess);
        } finally {
            indicateSuccess = false;
        }
    }

    @Test public void throwSync() {
        try {
            final EventBus bus = createEventBus();
            bus.registerListeners(new TestListenerClass());
            final String event = "Hello World!";
            Assertions.assertEquals(event, bus.throwSync(event));
            Assertions.assertTrue(indicateSuccess);
        } finally {
            indicateSuccess = false;
        }
    }

    @SuppressWarnings("unused") public static final class TestListenerClass {
        @Listener public void onString(final String string) {
            new RuntimeException("Found a string: " + string).printStackTrace();
            indicateSuccess = true;
        }

    }
}

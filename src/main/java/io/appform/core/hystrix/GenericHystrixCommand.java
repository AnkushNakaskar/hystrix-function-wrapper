/*
 * Copyright (c) 2016 Santanu Sinha <santanu.sinha@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appform.core.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandProperties;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.Map;
import org.slf4j.MDC;

/**
 * Command that returns the single element
 */
public class GenericHystrixCommand<R> {

    private static final String TRACE_ID = "TRACE-ID";

    private final HystrixCommand.Setter setter;

    private final String traceId;

    private final String command;

    public GenericHystrixCommand(HystrixCommand.Setter setter, String traceId, String command) {
        this.setter = setter;
        this.traceId = traceId;
        this.command = command;
    }

    public HystrixCommand<R> executor(HandlerAdapter<R> function) {
        final Map parentMDCContext = MDC.getCopyOfContextMap();
        final Tracer tracer = TracingHandler.getTracer();
        final Span parentActiveSpan = TracingHandler.getParentActiveSpan(tracer);
        return new HystrixCommand<R>(setter) {
            @Override
            protected R run() throws Exception {
                if (parentMDCContext != null) {
                    MDC.setContextMap(parentMDCContext);
                }
                final Span span = TracingHandler.startChildSpan(tracer, parentActiveSpan, command);
                final Scope scope = TracingHandler.activateSpan(tracer, span);

                MDC.put(TRACE_ID, traceId);
                try {
                    return function.run();
                } finally {
                    TracingHandler.closeScopeAndSpan(span, scope);
                    HystrixCommandProperties.ExecutionIsolationStrategy isolationStrategy =
                            getProperties().executionIsolationStrategy().get();
                    if (isolationStrategy == HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE) {
                        MDC.remove(TRACE_ID);
                    } else {
                        MDC.clear();
                    }
                }
            }
        };
    }
}

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

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;

/**
 * Builds hystrix command
 */
public class CommandFactory {

    public static <ReturnType> GenericHystrixCommand<ReturnType> create(String group,
                                                                       String command) {
        return new GenericHystrixCommand<>(
                HystrixObservableCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(group))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey(group, command))));

    }

    public static <ReturnType> GenericHystrixCollectionCommand<ReturnType> createCollectionCommand(String group,
                                                                                  String command) {
        return new GenericHystrixCollectionCommand<>(
                HystrixObservableCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(group))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey(group, command))));

    }

    private static String commandKey(final String group, final String command) {
        return String.format("%s.%s", group, command);
    }
}

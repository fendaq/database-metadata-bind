package com.github.jinahya.database.metadata.bind;

/*-
 * #%L
 * database-metadata-bind
 * %%
 * Copyright (C) 2011 - 2019 Jinahya, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.Arrays;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Jin Kwon &lt;onacit at gmail.com&gt;
 */
public class CommonsLangTest {

    private static final Logger logger = getLogger(lookup().lookupClass());

    @Test
    public void a() {
        final Class<?>[] primitiveClasses = ClassUtils.wrappersToPrimitives(null);
        logger.debug("primitiveClasses: {}", Arrays.toString(primitiveClasses));
    }
}

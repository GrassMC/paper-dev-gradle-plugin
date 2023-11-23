/*
 * Copyright 2023 GrassMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.grassmc.paperdev.utils

import org.objectweb.asm.ClassReader

private const val OBJECT_CLASS = "java/lang/Object"
private const val RECORD_CLASS = "java/lang/Record"

/**
 * Reads and returns the list of base class names from [classBytes] bytecode.
 * The list contains the superclass and all interfaces implemented by the class.
 * The list does not contain [java.lang.Object] class and [java.lang.Record] class.
 */
internal fun readBaseClasses(classBytes: ByteArray) = ClassReader(classBytes).let {
    buildList {
        it.superName.takeUnless { it == OBJECT_CLASS || it == RECORD_CLASS }?.let { add(it) }
        addAll(it.interfaces)
    }
}

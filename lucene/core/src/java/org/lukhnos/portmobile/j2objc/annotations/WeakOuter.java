package org.lukhnos.portmobile.j2objc.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * The code is from j2objc to reduce the number of dependencies needed during compile time.
 * j2objc only requires this annotation to have the same name ("WeakOuter") for it to generate
 * code with weak references.
 *
 * Annotation that indicates an inner class has a weak relationship
 * to its owning class.
 *
 * @author Tom Ball
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface WeakOuter {
}

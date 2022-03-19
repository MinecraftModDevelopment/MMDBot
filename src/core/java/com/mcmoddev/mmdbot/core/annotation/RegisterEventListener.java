package com.mcmoddev.mmdbot.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a class or a field with this annotation in order to register it automatically to
 * an {@link io.github.matyrobbrt.eventdispatcher.EventBus} from {@link com.mcmoddev.mmdbot.core.event.Events}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface RegisterEventListener {

    /**
     * The type of the bus that the member should be registered to.
     *
     * @return the type of the bus that the member should be registered to
     */
    BusType bus();

    enum BusType {
        MODERATION,
        MISCELLANEOUS
    }
}

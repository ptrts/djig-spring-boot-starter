package org.taruts.djig.core.mainContext.proxy;

import org.taruts.djig.dynamicApi.DynamicComponent;

/**
 * This exception is supposed to be thrown by a proxy of a dynamic implementation of a dynamic interface such as {@link DynamicComponent}.
 * A proxy throws it if the dynamic delegate is not set and a method of the interface is called.
 * <p>
 * This should not happen in the process of a refresh, because during a refresh the old delegate stays in place until replaced by a new one.
 * In theory this might happen during a context refresh, if a dynamic interface method is called.
 */
public class DelegateNotSetException extends RuntimeException {
}

package com.github.tix320.jouska.core.infrastructure.concurrent;

/**
 * Interface is used for objects which are thread-safe and share your lock object via method {@link LockOwner#getLock()} for synchronization outside him.
 *
 * @author Tigran Sargsyan on 22-Apr-20.
 */
public interface LockOwner extends ThreadSafe {

	Object getLock();
}

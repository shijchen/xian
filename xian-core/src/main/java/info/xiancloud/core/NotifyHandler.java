package info.xiancloud.core;

import info.xiancloud.core.message.UnitResponse;
import info.xiancloud.core.util.LOG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Internal handler for the xian framework to handle callbacks from rpc etc.
 *
 * @author happyyangyuan
 */
public abstract class NotifyHandler {

    /**
     * Boolean object, 'null' means not yet, 'true' for timeout, 'false' if already called back.
     * Defaults to null.
     * This parameter is read by multiple threads must be volatile to be safe.
     */
    private volatile Boolean timeout = null;
    private List<Action> beforeActions = new ArrayList<>();
    private List<Action> afterActions = new ArrayList<>();

    public void callback(UnitResponse unitResponseObject) {
        for (Action beforeAction : beforeActions) {
            try {
                beforeAction.run(unitResponseObject);
            } catch (Throwable throwable) {
                LOG.error("beforeAction执行失败，但是不妨碍下一个action执行", throwable);
            }
        }
        if (timeout != null && timeout) {
            LOG.error("本次消息已经被判定为超时,但是超时后又收到了响应! unitResponseObject= " + unitResponseObject);
        } else {
            try {
                handle(unitResponseObject);
            } catch (Throwable e) {
                LOG.error("回调执行失败", e);
            } finally {
                /**
                 * this is the default value once callback is called.
                 * the frame has set the custom timeout value explicitly.
                 * @see info.xiancloud.core.message.sender.local.AbstractLocalAsyncSender#timeoutAfter(long, long)
                 */
                timeout = false;
            }
        }
        for (Action afterAction : afterActions) {
            try {
                afterAction.run(unitResponseObject);
            } catch (Throwable e) {
                LOG.error("afterAction执行失败，但是不妨碍下一个action执行", e);
            }
        }
    }

    public void addBefore(Action... oneOrMoreActions) {
        Collections.addAll(beforeActions, oneOrMoreActions);
    }

    public void addAfter(Action... oneOrMoreActions) {
        Collections.addAll(afterActions, oneOrMoreActions);
    }

    protected abstract void handle(UnitResponse unitResponse);

    public NotifyHandler setTimeout(boolean timeout) {
        this.timeout = timeout;
        return this;
    }

    public Boolean getTimeout() {
        return timeout;
    }

    public abstract static class Action {
        protected abstract void run(UnitResponse out);
    }
}

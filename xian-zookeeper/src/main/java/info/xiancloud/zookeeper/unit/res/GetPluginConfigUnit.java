package info.xiancloud.zookeeper.unit.res;

import info.xiancloud.core.*;
import info.xiancloud.core.distribution.res.IResAware;
import info.xiancloud.core.message.UnitRequest;
import info.xiancloud.core.message.UnitResponse;
import info.xiancloud.core.util.LOG;
import info.xiancloud.core.util.StringUtil;
import info.xiancloud.zookeeper.unit.ZookeeperGroup;

/**
 * @author happyyangyuan
 */
public class GetPluginConfigUnit implements Unit {
    @Override
    public String getName() {
        return "getPluginConfig";
    }

    @Override
    public UnitMeta getMeta() {
        return UnitMeta.createWithDescription("现在本unit只用来做调试使用，程序内如果想要获取注册中心配置值，" +
                "请直接使用IResAware.singleton.get()")
                .setDocApi(false);
    }

    @Override
    public Input getInput() {
        return new Input()
                .add("key", String.class, "配置项key，不允许为空", REQUIRED)
                .add("plugin", String.class, "插件名", REQUIRED);
    }

    @Override
    public void execute(UnitRequest msg, Handler<UnitResponse> handler) {
        String key = msg.get("key", String.class);
        String pluginName = msg.get("plugin", String.class);
        String value = IResAware.singleton.get(pluginName, key);
        if (StringUtil.isEmpty(value)) {
            LOG.debug("配置尚未注册到zk，因此返回null");
            handler.handle(UnitResponse.createUnknownError(null, "配置尚未注册至zk，请读取本地配置代替."));
            return;
        }
        handler.handle(UnitResponse.createSuccess(value));
    }

    @Override
    public Group getGroup() {
        return ZookeeperGroup.singleton;
    }
}

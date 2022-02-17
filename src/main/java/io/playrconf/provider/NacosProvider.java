/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 The Play Remote Configuration Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.playrconf.provider;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.playrconf.sdk.AbstractProvider;
import io.playrconf.sdk.FileCfgObject;
import io.playrconf.sdk.KeyValueCfgObject;
import io.playrconf.sdk.exception.RemoteConfException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Retrieves configuration from Nacos.
 *
 * @author Kristin Cowalcijk
 * @since 19.02
 */
public class NacosProvider extends AbstractProvider {

    /**
     * Contains the provider version.
     */
    private static String providerVersion;

    @Override
    public String getName() {
        return "Nacos";
    }

    @Override
    public String getVersion() {
        if (NacosProvider.providerVersion == null) {
            synchronized (NacosProvider.class) {
                Properties properties = new Properties();
                try (InputStream is = NacosProvider.class.getClassLoader()
                    .getResourceAsStream("playrconf-nacos.properties")) {
                    properties.load(is);
                    NacosProvider.providerVersion = properties.getProperty("playrconf.nacos.version", "unknown");
                } catch (final IOException ignore) {
                }
            }
        }
        return NacosProvider.providerVersion;
    }

    @Override
    public String getConfigurationObjectName() {
        return "nacos";
    }

    @Override
    public void loadData(final Config config,
                         final Consumer<KeyValueCfgObject> kvObjConsumer,
                         final Consumer<FileCfgObject> fileObjConsumer) throws ConfigException, RemoteConfException {
        String serverAddr = config.getString("server-addr");
        String namespace = config.getString("namespace");
        String group = config.getString("group");
        String dataId = config.getString("data-id");
        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
            ConfigService configService = NacosFactory.createConfigService(properties);
            String configurationContent = configService.getConfig(dataId, group, 3000);
            if (configurationContent == null) {
                String message = String.format("failed to fetch config with dataId=%s in group %s", dataId, group);
                throw new RemoteConfException(message);
            }
            Config remoteConfiguration = ConfigFactory.parseString(configurationContent);
            remoteConfiguration.entrySet().forEach(entry -> {
                String value = entry.getValue().render();
                if (isFile(value)) {
                    fileObjConsumer.accept(new FileCfgObject(entry.getKey(), value));
                } else {
                    kvObjConsumer.accept(new KeyValueCfgObject(entry.getKey(), value));
                }
            });
        } catch (NacosException e) {
            throw new RemoteConfException("cannot create nacos config service object", e);
        }
    }
}

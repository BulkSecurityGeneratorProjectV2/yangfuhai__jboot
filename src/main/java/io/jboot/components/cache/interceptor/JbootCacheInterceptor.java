/**
 * Copyright (c) 2015-2019, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.components.cache.interceptor;


import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.components.cache.AopCache;
import io.jboot.components.cache.annotation.Cacheable;
import io.jboot.db.model.JbootModel;
import io.jboot.exception.JbootException;
import io.jboot.utils.AnnotationUtil;
import io.jboot.utils.ClassUtil;
import io.jboot.utils.ModelCopier;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * 缓存操作的拦截器
 *
 * @author michael yang
 */
public class JbootCacheInterceptor implements Interceptor {

    private static final String NULL_VALUE = "NULL_VALUE";

    @Override
    public void intercept(Invocation inv) {

        Method method = inv.getMethod();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);
        if (cacheable == null) {
            inv.invoke();
            return;
        }

        String unlessString = AnnotationUtil.get(cacheable.unless());
        if (Utils.isUnless(unlessString, method, inv.getArgs())) {
            inv.invoke();
            return;
        }

        Class targetClass = inv.getTarget().getClass();
        String cacheName = AnnotationUtil.get(cacheable.name());
        Utils.ensureCachenameAvailable(method, targetClass, cacheName);
        String cacheKey = Utils.buildCacheKey(AnnotationUtil.get(cacheable.key()), targetClass, method, inv.getArgs());

        Object data = AopCache.get(cacheName, cacheKey);
        if (data != null) {
            if (NULL_VALUE.equals(data)) {
                inv.setReturnValue(null);
            } else if (cacheable.returnCopyEnable()) {
                setReturnValueByCopy(inv, data);
            } else {
                inv.setReturnValue(data);
            }
            return;
        }

        inv.invoke();

        data = inv.getReturnValue();

        if (data != null) {
            Utils.putDataToCache(cacheable.liveSeconds(), cacheName, cacheKey, data);
        } else if (cacheable.nullCacheEnable()) {
            Utils.putDataToCache(cacheable.liveSeconds(), cacheName, cacheKey, NULL_VALUE);
        }
    }


    private void setReturnValueByCopy(Invocation inv, Object data) {
        try {
            if (data instanceof List) {
                inv.setReturnValue(ModelCopier.copy((List<? extends JbootModel>) data));
            } else if (data instanceof Set) {
                inv.setReturnValue(ModelCopier.copy((Set<? extends JbootModel>) data));
            } else if (data instanceof Page) {
                inv.setReturnValue(ModelCopier.copy((Page<? extends JbootModel>) data));
            } else if (data instanceof JbootModel) {
                inv.setReturnValue(ModelCopier.copy((JbootModel) data));
            } else {
                throw newException(null, inv, data);
            }
        } catch (Exception ex) {
            throw newException(ex, inv, data);
        }

    }

    private JbootException newException(Exception ex, Invocation inv, Object data) {
        String msg = "can not copy data for type [" + data.getClass().getName() + "] in method :"
                + ClassUtil.buildMethodString(inv.getMethod()) + " , can not set @Cacheable(returnCopyEnable=true)";

        return ex == null ? new JbootException(msg) : new JbootException(msg, ex);
    }


}
